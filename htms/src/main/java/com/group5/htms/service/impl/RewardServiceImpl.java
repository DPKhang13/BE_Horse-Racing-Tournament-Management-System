package com.group5.htms.service.impl;

import com.group5.htms.exception.ResourceNotFoundException;
import com.group5.htms.dto.bet.response.BetResponse;
import com.group5.htms.dto.reward.request.RewardCalculateRequest;
import com.group5.htms.entity.Bets;
import com.group5.htms.mapper.BetMapper;
import com.group5.htms.repository.BetsRepository;
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
    private final BetMapper betMapper;

    @Override
    @Transactional
    public BetResponse calculateReward(Integer betId, RewardCalculateRequest request) {
        Bets bet = betsRepository.findById(betId)
                .orElseThrow(() -> new ResourceNotFoundException("Bet not found"));

        BigDecimal reward = request.getRewardPoints() == null ? BigDecimal.ZERO : request.getRewardPoints();

        bet.setRewardPoints(reward);
        bet.setStatus(request.getStatus().trim());
        bet.setSettledAt(request.getSettledAt() == null ? Instant.now() : request.getSettledAt());

        return betMapper.toResponse(betsRepository.save(bet));
    }
}
