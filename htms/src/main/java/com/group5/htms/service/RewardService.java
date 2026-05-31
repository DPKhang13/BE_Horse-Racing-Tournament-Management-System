package com.group5.htms.service;

import com.group5.htms.dto.bet.response.BetResponse;
import com.group5.htms.dto.reward.request.RewardCalculateRequest;

public interface RewardService {
    BetResponse calculateReward(Integer betId, RewardCalculateRequest request);
}
