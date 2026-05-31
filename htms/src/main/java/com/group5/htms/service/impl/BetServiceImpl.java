package com.group5.htms.service.impl;

import com.group5.htms.exception.ResourceNotFoundException;
import com.group5.htms.dto.bet.request.BetCheckRequest;
import com.group5.htms.dto.bet.request.BetCreateRequest;
import com.group5.htms.dto.bet.request.BetUpdateRequest;
import com.group5.htms.dto.bet.response.BetResponse;
import com.group5.htms.entity.Bets;
import com.group5.htms.entity.Users;
import com.group5.htms.mapper.BetMapper;
import com.group5.htms.repository.BetsRepository;
import com.group5.htms.repository.JockeyHorseAssignmentsRepository;
import com.group5.htms.repository.RolesRepository;
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
    private static final String ROLE_SPECTATOR = "spectator";

    private final BetsRepository betsRepository;
    private final RolesRepository rolesRepository;
    private final JockeyHorseAssignmentsRepository jockeyHorseAssignmentsRepository;
    private final AuthService authService;
    private final BetMapper betMapper;

    @Override
    public List<BetResponse> getAllBets() {
        return betsRepository.findAll()
                .stream()
                .map(betMapper::toResponse)
                .toList();
    }

    @Override
    public BetResponse getBetById(Integer id) {
        return betMapper.toResponse(findBet(id));
    }

    @Override
    @Transactional
    public BetResponse createBet(BetCreateRequest request) {
        request.setSpectatorRoleId(authService.getCurrentUserRoleId(ROLE_SPECTATOR));
        request.setPayoutPoints(null);
        request.setStatus(null);
        request.setSettledAt(null);
        request.setSettledById(null);
        request.setSettledType(null);
        validateCreateReferences(request);
        Bets bet = betMapper.toEntity(request);

        return betMapper.toResponse(betsRepository.save(bet));
    }

    @Override
    @Transactional
    public BetResponse updateBet(Integer id, BetUpdateRequest request) {
        Bets bet = findBetForCurrentSpectator(id);
        request.setSpectatorRoleId(null);
        request.setPayoutPoints(null);
        request.setStatus(null);
        request.setSettledAt(null);
        request.setSettledById(null);
        request.setSettledType(null);
        validateUpdateReferences(request);
        betMapper.updateBet(bet, request);

        return betMapper.toResponse(betsRepository.save(bet));
    }

    @Override
    @Transactional
    public BetResponse checkBet(Integer id, BetCheckRequest request) {
        Bets bet = findBet(id);

        bet.setStatus(request.getStatus().trim());
        bet.setPayoutPoints(request.getPayoutPoints() == null ? BigDecimal.ZERO : request.getPayoutPoints());
        bet.setSettledAt(request.getSettledAt() == null ? Instant.now() : request.getSettledAt());
        Users settledBy = new Users();
        settledBy.setId(authService.getCurrentUserId());
        bet.setSettledBy(settledBy);
        if (request.getSettledType() != null && !request.getSettledType().isBlank()) {
            bet.setSettledType(request.getSettledType().trim());
        }

        return betMapper.toResponse(betsRepository.save(bet));
    }

    @Override
    @Transactional
    public void deleteBet(Integer id) {
        Bets bet = findBetForCurrentSpectator(id);
        betsRepository.delete(bet);
    }

    private Bets findBet(Integer id) {
        return betsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bet not found"));
    }

    private Bets findBetForCurrentSpectator(Integer id) {
        Bets bet = findBet(id);
        Integer spectatorRoleId = authService.getCurrentUserRoleId(ROLE_SPECTATOR);

        if (!Objects.equals(bet.getSpectatorRoles().getId(), spectatorRoleId)) {
            throw new AccessDeniedException("You do not own this bet");
        }

        return bet;
    }

    private void validateCreateReferences(BetCreateRequest request) {
        validateRoleExists(request.getSpectatorRoleId(), "Spectator role not found");
        validateAssignmentExists(request.getAssignmentId());
    }

    private void validateUpdateReferences(BetUpdateRequest request) {
        if (request.getSpectatorRoleId() != null) {
            validateRoleExists(request.getSpectatorRoleId(), "Spectator role not found");
        }
        if (request.getAssignmentId() != null) {
            validateAssignmentExists(request.getAssignmentId());
        }
    }

    private void validateRoleExists(Integer id, String message) {
        if (!rolesRepository.existsById(id)) {
            throw new ResourceNotFoundException(message);
        }
    }

    private void validateAssignmentExists(Integer id) {
        if (!jockeyHorseAssignmentsRepository.existsById(id)) {
            throw new ResourceNotFoundException("Jockey assignment not found");
        }
    }

}
