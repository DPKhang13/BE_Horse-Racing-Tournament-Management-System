package com.group5.htms.service.impl;

import com.group5.htms.exception.ResourceNotFoundException;
import com.group5.htms.dto.bet.request.BetCheckRequest;
import com.group5.htms.dto.bet.request.BetCreateRequest;
import com.group5.htms.dto.bet.request.BetUpdateRequest;
import com.group5.htms.dto.bet.response.BetListResponse;
import com.group5.htms.dto.bet.response.BetResponse;
import com.group5.htms.dto.betoption.response.BetOptionResponse;
import com.group5.htms.entity.BetOptions;
import com.group5.htms.entity.Bets;
import com.group5.htms.entity.Users;
import com.group5.htms.entity.WalletTransactions;
import com.group5.htms.entity.Wallets;
import com.group5.htms.enums.RaceStatus;
import com.group5.htms.enums.WalletStatus;
import com.group5.htms.enums.WalletTransactionStatus;
import com.group5.htms.enums.WalletTransactionType;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.mapper.BetMapper;
import com.group5.htms.repository.BetOptionsRepository;
import com.group5.htms.repository.BetsRepository;
import com.group5.htms.repository.UsersRepository;
import com.group5.htms.repository.WalletTransactionsRepository;
import com.group5.htms.repository.WalletsRepository;
import com.group5.htms.service.AuthService;
import com.group5.htms.service.BetOptionService;
import com.group5.htms.service.BetService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BetServiceImpl implements BetService {
    private static final String STATUS_DELETED = "deleted";
    private static final String BET_STATUS_PENDING = "pending";
    private static final String REF_TYPE_BET = "bet";

    private final BetsRepository betsRepository;
    private final BetOptionsRepository betOptionsRepository;
    private final WalletsRepository walletsRepository;
    private final WalletTransactionsRepository walletTransactionsRepository;
    private final UsersRepository usersRepository;
    private final AuthService authService;
    private final BetOptionService betOptionService;
    private final BetMapper betMapper;

    @Override
    public List<BetListResponse> getAllBets() {
        return betsRepository.findAll()
                .stream()
                .filter(bet -> !isDeleted(bet.getStatus()))
                .map(betMapper::toListResponse)
                .toList();
    }

    @Override
    public BetResponse getBetById(Integer id) {
        return betMapper.toResponse(findBet(id));
    }

    @Override
    @Transactional
    public BetResponse createBet(BetCreateRequest request) {
        Integer userId = authService.getCurrentUserId();
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        BetOptions option = findBetOptionForBetting(request.getOptionId());
        validateRaceOpenForBetting(option);
        validatePredictionStillOpen(option);

        Wallets wallet = getLockedWallet(userId);
        BigDecimal betPoints = request.getBetPoints();
        BigDecimal pointsBefore = safeMoney(wallet.getPointBalance());

        if (pointsBefore.compareTo(betPoints) < 0) {
            throw new BadRequestException("Wallet balance is not enough to place this bet");
        }

        BigDecimal pointsAfter = pointsBefore.subtract(betPoints);
        wallet.setPointBalance(pointsAfter);

        request.setUserId(userId);
        request.setBetRate(option.getCurrentRate());

        Bets bet = betMapper.toEntity(request);
        bet.setUsers(user);
        bet.setOption(option);
        bet.setStatus(BET_STATUS_PENDING);

        option.setTotalBetPoints(safeMoney(option.getTotalBetPoints()).add(betPoints));
        option.setTotalBetCount((option.getTotalBetCount() == null ? 0 : option.getTotalBetCount()) + 1);
        option.setUpdatedAt(Instant.now());

        Bets savedBet = betsRepository.save(bet);
        betOptionsRepository.save(option);
        walletsRepository.save(wallet);
        createBetWalletTransaction(user, wallet, savedBet, betPoints, pointsBefore, pointsAfter);
        refreshCurrentRateAfterMarketChange(option);

        return betMapper.toResponse(savedBet);
    }

    @Override
    @Transactional
    public BetResponse updateBet(Integer id, BetUpdateRequest request) {
        Bets bet = findBetForCurrentSpectator(id);
        request.setUserId(null);
        request.setRewardPoints(null);
        request.setStatus(null);
        request.setSettledAt(null);
        validateUpdateReferences(request);
        betMapper.updateBet(bet, request);

        return betMapper.toResponse(betsRepository.save(bet));
    }

    @Override
    @Transactional
    public BetResponse checkBet(Integer id, BetCheckRequest request) {
        Bets bet = findBet(id);

        bet.setStatus(request.getStatus().trim());
        bet.setRewardPoints(request.getRewardPoints() == null ? BigDecimal.ZERO : request.getRewardPoints());
        bet.setSettledAt(request.getSettledAt() == null ? Instant.now() : request.getSettledAt());

        return betMapper.toResponse(betsRepository.save(bet));
    }

    @Override
    @Transactional
    public void deleteBet(Integer id) {
        Bets bet = findBetForCurrentSpectator(id);
        bet.setStatus(STATUS_DELETED);
        betsRepository.save(bet);
    }

    private Bets findBet(Integer id) {
        return betsRepository.findById(id)
                .filter(bet -> !isDeleted(bet.getStatus()))
                .orElseThrow(() -> new ResourceNotFoundException("Bet not found"));
    }

    private Bets findBetForCurrentSpectator(Integer id) {
        Bets bet = findBet(id);
        Integer userId = authService.getCurrentUserId();

        if (!Objects.equals(bet.getUsers().getId(), userId)) {
            throw new AccessDeniedException("You do not own this bet");
        }

        return bet;
    }

    private void validateCreateReferences(BetCreateRequest request) {
        validateOptionExists(request.getOptionId());
    }

    private void validateUpdateReferences(BetUpdateRequest request) {
        if (request.getOptionId() != null) {
            validateOptionExists(request.getOptionId());
        }
    }

    private void validateOptionExists(Integer id) {
        if (!betOptionsRepository.existsById(id)) {
            throw new ResourceNotFoundException("Bet option not found");
        }
    }

    private BetOptions findBetOptionForBetting(Integer optionId) {
        if (optionId == null) {
            throw new BadRequestException("Option id is required");
        }

        return betOptionsRepository.findFirstById(optionId)
                .orElseThrow(() -> new ResourceNotFoundException("Bet option not found"));
    }

    private void validateRaceOpenForBetting(BetOptions option) {
        String status = option.getRaces().getStatus();

        if (status == null || !RaceStatus.OPEN_FOR_BETTING.getValue().equalsIgnoreCase(status.trim())) {
            throw new BadRequestException("Race is not open for betting");
        }
    }

    private void validatePredictionStillOpen(BetOptions option) {
        Instant predictionClosesAt = option.getRaces().getPredictionClosesAt();

        if (predictionClosesAt != null && !Instant.now().isBefore(predictionClosesAt)) {
            throw new BadRequestException("Prediction is already closed for this race");
        }
    }

    private Wallets getLockedWallet(Integer userId) {
        Wallets wallet = walletsRepository.findByUsersId(userId)
                .orElseThrow(() -> new BadRequestException("Wallet not found"));

        Wallets lockedWallet = walletsRepository.findFirstById(wallet.getId())
                .orElseThrow(() -> new BadRequestException("Wallet not found"));

        if (!WalletStatus.ACTIVE.getValue().equalsIgnoreCase(lockedWallet.getStatus())) {
            throw new BadRequestException("Wallet is not active");
        }

        return lockedWallet;
    }

    private void createBetWalletTransaction(
            Users user,
            Wallets wallet,
            Bets bet,
            BigDecimal betPoints,
            BigDecimal pointsBefore,
            BigDecimal pointsAfter
    ) {
        WalletTransactions transaction = WalletTransactions.builder()
                .wallets(wallet)
                .users(user)
                .txType(WalletTransactionType.BET.getValue())
                .cashAmount(BigDecimal.ZERO)
                .pointsAmount(betPoints)
                .exchangeRate(BigDecimal.ONE)
                .pointsBefore(pointsBefore)
                .pointsAfter(pointsAfter)
                .status(WalletTransactionStatus.COMPLETED.getValue())
                .refType(REF_TYPE_BET)
                .refId(bet.getId())
                .createdBy(user)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        walletTransactionsRepository.save(transaction);
    }

    private BigDecimal safeMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private void refreshCurrentRateAfterMarketChange(BetOptions option) {
        List<BetOptionResponse> recalculatedOptions =
                betOptionService.recalculateRatesForRace(option.getRaces().getId());

        recalculatedOptions.stream()
                .filter(response -> Objects.equals(response.getOptionId(), option.getId()))
                .findFirst()
                .ifPresent(response -> option.setCurrentRate(response.getCurrentRate()));
    }

    private boolean isDeleted(String status) {
        return STATUS_DELETED.equalsIgnoreCase(status);
    }
}
