package com.group5.htms.service.impl;

import com.group5.htms.common.exceptions.ResourceNotFoundException;
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
import com.group5.htms.repository.UsersRepository;
import com.group5.htms.service.BetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BetServiceImpl implements BetService {
    private final BetsRepository betsRepository;
    private final RolesRepository rolesRepository;
    private final JockeyHorseAssignmentsRepository jockeyHorseAssignmentsRepository;
    private final UsersRepository usersRepository;
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
        validateCreateReferences(request);
        Bets bet = betMapper.toEntity(request);

        return betMapper.toResponse(betsRepository.save(bet));
    }

    @Override
    @Transactional
    public BetResponse updateBet(Integer id, BetUpdateRequest request) {
        Bets bet = findBet(id);
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
        if (request.getSettledById() != null) {
            validateUserExists(request.getSettledById());
            Users settledBy = new Users();
            settledBy.setId(request.getSettledById());
            bet.setSettledBy(settledBy);
        }
        if (request.getSettledType() != null && !request.getSettledType().isBlank()) {
            bet.setSettledType(request.getSettledType().trim());
        }

        return betMapper.toResponse(betsRepository.save(bet));
    }

    @Override
    @Transactional
    public void deleteBet(Integer id) {
        Bets bet = findBet(id);
        betsRepository.delete(bet);
    }

    private Bets findBet(Integer id) {
        return betsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bet not found"));
    }

    private void validateCreateReferences(BetCreateRequest request) {
        validateRoleExists(request.getSpectatorRoleId(), "Spectator role not found");
        validateAssignmentExists(request.getAssignmentId());
        if (request.getSettledById() != null) {
            validateUserExists(request.getSettledById());
        }
    }

    private void validateUpdateReferences(BetUpdateRequest request) {
        if (request.getSpectatorRoleId() != null) {
            validateRoleExists(request.getSpectatorRoleId(), "Spectator role not found");
        }
        if (request.getAssignmentId() != null) {
            validateAssignmentExists(request.getAssignmentId());
        }
        if (request.getSettledById() != null) {
            validateUserExists(request.getSettledById());
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

    private void validateUserExists(Integer id) {
        if (!usersRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found");
        }
    }
}
