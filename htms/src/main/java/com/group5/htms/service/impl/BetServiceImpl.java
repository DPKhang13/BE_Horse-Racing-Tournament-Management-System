package com.group5.htms.service.impl;

import com.group5.htms.dto.bet.request.BetCheckRequest;
import com.group5.htms.dto.bet.request.BetCreateRequest;
import com.group5.htms.dto.bet.request.BetUpdateRequest;
import com.group5.htms.dto.bet.response.BetListResponse;
import com.group5.htms.dto.bet.response.BetResponse;
import com.group5.htms.dto.betoption.response.BetOptionResponse;
import com.group5.htms.dto.dashboard.response.PredictionRaceResponse;
import com.group5.htms.dto.dashboard.response.SpectatorDashboardResponse;
import com.group5.htms.dto.notification.response.NotificationListResponse;
import com.group5.htms.dto.raceresult.response.RaceResultListResponse;
import com.group5.htms.entity.BetOptions;
import com.group5.htms.entity.Bets;
import com.group5.htms.entity.Races;
import com.group5.htms.entity.Users;
import com.group5.htms.entity.WalletTransactions;
import com.group5.htms.entity.Wallets;
import com.group5.htms.enums.BetStatus;
import com.group5.htms.enums.RaceResultStatus;
import com.group5.htms.enums.RaceStatus;
import com.group5.htms.enums.RoleType;
import com.group5.htms.enums.WalletTransactionStatus;
import com.group5.htms.enums.WalletTransactionType;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.exception.ResourceNotFoundException;
import com.group5.htms.mapper.BetMapper;
import com.group5.htms.mapper.NotificationMapper;
import com.group5.htms.mapper.RaceResultMapper;
import com.group5.htms.repository.BetOptionsRepository;
import com.group5.htms.repository.BetsRepository;
import com.group5.htms.repository.NotificationsRepository;
import com.group5.htms.repository.RaceResultsRepository;
import com.group5.htms.repository.RacesRepository;
import com.group5.htms.repository.UsersRepository;
import com.group5.htms.repository.WalletTransactionsRepository;
import com.group5.htms.repository.WalletsRepository;
import com.group5.htms.service.AuthService;
import com.group5.htms.service.BetOptionService;
import com.group5.htms.service.BetService;
import com.group5.htms.validation.BetValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BetServiceImpl implements BetService {
    private static final String REF_TYPE_BET = "bet";
    private static final int DASHBOARD_LIMIT = 6;
    private static final int OPEN_PREDICTION_OPTION_LIMIT = 60;

    private final BetsRepository betsRepository;
    private final BetOptionsRepository betOptionsRepository;
    private final WalletsRepository walletsRepository;
    private final WalletTransactionsRepository walletTransactionsRepository;
    private final UsersRepository usersRepository;
    private final RacesRepository racesRepository;
    private final RaceResultsRepository raceResultsRepository;
    private final NotificationsRepository notificationsRepository;
    private final AuthService authService;
    private final BetOptionService betOptionService;
    private final BetMapper betMapper;
    private final RaceResultMapper raceResultMapper;
    private final NotificationMapper notificationMapper;
    private final BetValidator betValidator;

    @Override
    public List<BetListResponse> getAllBets() {
        return betsRepository.findAll()
                .stream()
                .map(betMapper::toListResponse)
                .toList();
    }

    @Override
    public BetResponse getBetById(Integer id) {
        return betMapper.toResponse(findBet(id));
    }

    @Override
    @Transactional(readOnly = true)
    public SpectatorDashboardResponse getSpectatorDashboard() {
        Integer userId = authService.getCurrentUserId();
        Instant now = Instant.now();

        List<BetListResponse> activeBets = betsRepository
                .findByUsers_IdAndStatusIgnoreCaseOrderByPlacedAtDesc(
                        userId,
                        BetStatus.PENDING.getValue(),
                        PageRequest.of(0, DASHBOARD_LIMIT)
                )
                .stream()
                .map(betMapper::toListResponse)
                .toList();

        List<SpectatorDashboardResponse.UpcomingRaceItem> upcomingRaces = racesRepository
                .findByScheduledAtAfterOrderByScheduledAtAsc(now, PageRequest.of(0, DASHBOARD_LIMIT))
                .stream()
                .map(this::toUpcomingRaceItem)
                .toList();

        List<RaceResultListResponse> latestResults = raceResultsRepository
                .findByStatusIgnoreCaseOrderByPublishedAtDesc(
                        RaceResultStatus.PUBLISHED.getValue(),
                        PageRequest.of(0, DASHBOARD_LIMIT)
                )
                .stream()
                .map(raceResultMapper::toListResponse)
                .toList();

        List<NotificationListResponse> notifications = notificationsRepository
                .findByUsers_IdOrderByCreatedAtDesc(userId, PageRequest.of(0, DASHBOARD_LIMIT))
                .stream()
                .map(notificationMapper::toListResponse)
                .toList();

        List<PredictionRaceResponse> openPredictionRaces = getOpenPredictionRaces();

        return SpectatorDashboardResponse.builder()
                .wallet(toWalletSummary(userId))
                .summaryCount(SpectatorDashboardResponse.SummaryCount.builder()
                        .activeBetCount(betsRepository.countByUsers_IdAndStatusIgnoreCase(userId, BetStatus.PENDING.getValue()))
                        .settledBetCount(
                                betsRepository.countByUsers_IdAndStatusIgnoreCase(userId, BetStatus.WON.getValue())
                                        + betsRepository.countByUsers_IdAndStatusIgnoreCase(userId, BetStatus.LOST.getValue())
                        )
                        .unreadNotificationCount(notificationsRepository.countByUsers_IdAndIsReadFalse(userId))
                        .upcomingRaceCount(racesRepository.countByScheduledAtAfter(now))
                        .openPredictionRaceCount(openPredictionRaces.size())
                        .build())
                .upcomingRaces(upcomingRaces)
                .activeBets(activeBets)
                .latestResults(latestResults)
                .notifications(notifications)
                .openPredictionRaces(openPredictionRaces)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PredictionRaceResponse> getOpenPredictionRaces() {
        Instant now = Instant.now();
        List<BetOptions> options = betOptionsRepository
                .findByRaces_StatusIgnoreCaseAndRaces_PredictionClosesAtAfterOrderByRaces_ScheduledAtAscCurrentRateAsc(
                        RaceStatus.OPEN_FOR_BETTING.getValue(),
                        now,
                        PageRequest.of(0, OPEN_PREDICTION_OPTION_LIMIT)
                );

        Map<Integer, PredictionRaceAccumulator> raceMap = new LinkedHashMap<>();
        for (BetOptions option : options) {
            Races race = option.getRaces();
            PredictionRaceAccumulator accumulator = raceMap.computeIfAbsent(
                    race.getId(),
                    ignored -> new PredictionRaceAccumulator(race)
            );
            accumulator.options.add(toPredictionOption(option));
        }

        return raceMap.values()
                .stream()
                .limit(DASHBOARD_LIMIT)
                .map(PredictionRaceAccumulator::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public BetResponse createBet(BetCreateRequest request) {
        Integer userId = authService.getCurrentUserId();
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        BetOptions option = findBetOptionForBetting(request.getOptionId());
        betValidator.ensureRaceOpenForBetting(option);
        betValidator.ensurePredictionStillOpen(option);

        Wallets wallet = getLockedWallet(userId);
        BigDecimal betPoints = request.getBetPoints();
        BigDecimal pointsBefore = safeMoney(wallet.getPointBalance());

        betValidator.ensureEnoughPoints(pointsBefore, betPoints);

        BigDecimal pointsAfter = pointsBefore.subtract(betPoints);
        wallet.setPointBalance(pointsAfter);

        request.setUserId(userId);
        request.setBetRate(option.getCurrentRate());

        Bets bet = betMapper.toEntity(request);
        bet.setUsers(user);
        bet.setOption(option);
        bet.setStatus(BetStatus.PENDING.getValue());

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
        betValidator.ensureNoBackendManagedUpdateFields(request);
        validateUpdateReferences(request);
        betMapper.updateBet(bet, request);

        return betMapper.toResponse(betsRepository.save(bet));
    }

    @Override
    @Transactional
    public BetResponse checkBet(Integer id, BetCheckRequest request) {
        Bets bet = findBet(id);

        bet.setStatus(betValidator.normalizeSettlementStatus(request.getStatus()));
        bet.setRewardPoints(request.getRewardPoints() == null ? BigDecimal.ZERO : request.getRewardPoints());
        bet.setSettledAt(request.getSettledAt() == null ? Instant.now() : request.getSettledAt());

        return betMapper.toResponse(betsRepository.save(bet));
    }

    private SpectatorDashboardResponse.WalletSummary toWalletSummary(Integer userId) {
        return walletsRepository.findByUsersId(userId)
                .map(wallet -> SpectatorDashboardResponse.WalletSummary.builder()
                        .walletId(wallet.getId())
                        .pointBalance(wallet.getPointBalance())
                        .status(wallet.getStatus())
                        .createdAt(wallet.getCreatedAt())
                        .build())
                .orElseGet(() -> SpectatorDashboardResponse.WalletSummary.builder()
                        .pointBalance(BigDecimal.ZERO)
                        .status("not_created")
                        .build());
    }

    private SpectatorDashboardResponse.UpcomingRaceItem toUpcomingRaceItem(Races race) {
        return SpectatorDashboardResponse.UpcomingRaceItem.builder()
                .raceId(race.getId())
                .raceName(race.getName())
                .raceNumber(race.getRaceNumber())
                .scheduledAt(race.getScheduledAt())
                .predictionClosesAt(race.getPredictionClosesAt())
                .distanceM(race.getDistanceM())
                .trackType(race.getTrackType())
                .status(race.getStatus())
                .tournamentId(race.getSchedule().getTournaments().getId())
                .tournamentName(race.getSchedule().getTournaments().getName())
                .location(race.getSchedule().getTournaments().getLocation())
                .build();
    }

    private PredictionRaceResponse.OptionItem toPredictionOption(BetOptions option) {
        return PredictionRaceResponse.OptionItem.builder()
                .optionId(option.getId())
                .assignmentId(option.getAssignment().getId())
                .horseId(option.getHorses().getId())
                .horseName(option.getHorses().getName())
                .horseAvatarUrl(option.getHorses().getAvatarUrl())
                .jockeyId(option.getAssignment().getJockey().getId())
                .jockeyFullName(option.getAssignment().getJockey().getUsers().getFullName())
                .gateNumber(option.getAssignment().getGateNumber())
                .currentRate(option.getCurrentRate())
                .totalBetPoints(option.getTotalBetPoints())
                .totalBetCount(option.getTotalBetCount())
                .updatedAt(option.getUpdatedAt())
                .build();
    }

    private Bets findBet(Integer id) {
        return betsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bet not found"));
    }

    private Bets findBetForCurrentSpectator(Integer id) {
        Bets bet = findBet(id);
        Integer userId = authService.getCurrentUserId();

        if (!authService.currentUserHasRole(RoleType.ADMIN.getValue()) && !Objects.equals(bet.getUsers().getId(), userId)) {
            throw new AccessDeniedException("You do not own this bet");
        }

        return bet;
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

    private Wallets getLockedWallet(Integer userId) {
        Wallets wallet = walletsRepository.findByUsersId(userId)
                .orElseThrow(() -> new BadRequestException("Wallet not found"));

        Wallets lockedWallet = walletsRepository.findFirstById(wallet.getId())
                .orElseThrow(() -> new BadRequestException("Wallet not found"));

        betValidator.ensureWalletActive(lockedWallet);

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

    private class PredictionRaceAccumulator {
        private final Races race;
        private final List<PredictionRaceResponse.OptionItem> options = new ArrayList<>();

        private PredictionRaceAccumulator(Races race) {
            this.race = race;
        }

        private PredictionRaceResponse toResponse() {
            return PredictionRaceResponse.builder()
                    .raceId(race.getId())
                    .raceName(race.getName())
                    .raceNumber(race.getRaceNumber())
                    .scheduledAt(race.getScheduledAt())
                    .predictionClosesAt(race.getPredictionClosesAt())
                    .distanceM(race.getDistanceM())
                    .trackType(race.getTrackType())
                    .status(race.getStatus())
                    .tournamentId(race.getSchedule().getTournaments().getId())
                    .tournamentName(race.getSchedule().getTournaments().getName())
                    .location(race.getSchedule().getTournaments().getLocation())
                    .options(options)
                    .build();
        }
    }
}
