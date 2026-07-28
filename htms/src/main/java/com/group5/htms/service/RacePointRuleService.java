package com.group5.htms.service;

import com.group5.htms.dto.racepointrule.request.RacePointRuleItemRequest;
import com.group5.htms.dto.racepointrule.response.RacePointRuleResponse;

import java.util.List;

public interface RacePointRuleService {
    List<RacePointRuleResponse> getPointRules(Integer raceId);

    List<RacePointRuleResponse> createPointRules(Integer raceId, List<RacePointRuleItemRequest> request);

    List<RacePointRuleResponse> replacePointRules(Integer raceId, List<RacePointRuleItemRequest> request);

    void deletePointRule(Integer raceId, Integer ruleId);
}
