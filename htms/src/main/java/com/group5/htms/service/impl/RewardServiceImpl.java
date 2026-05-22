package com.group5.htms.service.impl;

import com.group5.htms.common.exceptions.ResourceNotFoundException;
import com.group5.htms.dto.bet.response.BetResponse;
import com.group5.htms.dto.reward.request.RewardCalculateRequest;
import com.group5.htms.entity.Bets;
import com.group5.htms.entity.Users;
import com.group5.htms.mapper.BetMapper;
import com.group5.htms.repository.BetsRepository;
import com.group5.htms.repository.UsersRepository;
import com.group5.htms.service.RewardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RewardServiceImpl implements RewardService {
    private final BetsRepository betsRepository;
    private final UsersRepository usersRepository;
    private final BetMapper betMapper;

    @Override
    @Transactional
    public BetResponse calculateReward(Integer betId, RewardCalculateRequest request) {
        Bets bet = betsRepository.findById(betId)
                .orElseThrow(() -> new ResourceNotFoundException("Bet not found"));

        BigDecimal payout = request.getPayoutPoints();
        if (payout == null) {
            payout = "win".equalsIgnoreCase(request.getSettledType())
                    ? bet.getPotentialPayoutPoints()
                    : BigDecimal.ZERO;
        }

        bet.setPayoutPoints(payout);
        bet.setStatus(request.getStatus().trim());
        bet.setSettledType(request.getSettledType().trim());
        bet.setSettledAt(request.getSettledAt() == null ? Instant.now() : request.getSettledAt());
        if (request.getSettledById() != null) {
            validateUserExists(request.getSettledById());
            Users settledBy = new Users();
            settledBy.setId(request.getSettledById());
            bet.setSettledBy(settledBy);
        }

        return betMapper.toResponse(betsRepository.save(bet));
    }

    private void validateUserExists(Integer id) {
        if (!usersRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found");
        }
    }
}
