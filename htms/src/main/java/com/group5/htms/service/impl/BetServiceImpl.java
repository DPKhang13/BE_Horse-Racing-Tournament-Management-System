package com.group5.htms.service.impl;

import com.group5.htms.exception.ResourceNotFoundException;
import com.group5.htms.dto.bet.request.BetCheckRequest;
import com.group5.htms.dto.bet.request.BetCreateRequest;
import com.group5.htms.dto.bet.request.BetUpdateRequest;
import com.group5.htms.dto.bet.response.BetListResponse;
import com.group5.htms.dto.bet.response.BetResponse;
import com.group5.htms.entity.Bets;
import com.group5.htms.mapper.BetMapper;
import com.group5.htms.repository.BetOptionsRepository;
import com.group5.htms.repository.BetsRepository;
import com.group5.htms.service.AuthService;
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

    private final BetsRepository betsRepository;
    private final BetOptionsRepository betOptionsRepository;
    private final AuthService authService;
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
        request.setUserId(authService.getCurrentUserId());
        validateCreateReferences(request);
        Bets bet = betMapper.toEntity(request);

        return betMapper.toResponse(betsRepository.save(bet));
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

    private boolean isDeleted(String status) {
        return STATUS_DELETED.equalsIgnoreCase(status);
    }
}
