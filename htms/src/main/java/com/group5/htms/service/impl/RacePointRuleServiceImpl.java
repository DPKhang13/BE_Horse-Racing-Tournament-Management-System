package com.group5.htms.service.impl;

import com.group5.htms.dto.racepointrule.request.RacePointRuleItemRequest;
import com.group5.htms.dto.racepointrule.response.RacePointRuleResponse;
import com.group5.htms.entity.RacePointRules;
import com.group5.htms.entity.Races;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.exception.ResourceNotFoundException;
import com.group5.htms.repository.RacePointRulesRepository;
import com.group5.htms.repository.RacesRepository;
import com.group5.htms.service.RacePointRuleService;
import com.group5.htms.validation.RacePointRuleValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RacePointRuleServiceImpl implements RacePointRuleService {
    private final RacesRepository racesRepository;
    private final RacePointRulesRepository racePointRulesRepository;
    private final RacePointRuleValidator racePointRuleValidator;

    @Override
    @Transactional(readOnly = true)
    public List<RacePointRuleResponse> getPointRules(Integer raceId) {
        Races race = getRaceEntity(raceId);
        return toResponseList(race.getId());
    }

    @Override
    @Transactional
    public List<RacePointRuleResponse> createPointRules(Integer raceId, List<RacePointRuleItemRequest> request) {
        Races race = getRaceEntity(raceId);
        racePointRuleValidator.ensurePointRulesEditable(race);
        racePointRuleValidator.validateRequest(request);
        racePointRuleValidator.ensureFinishPositionsDoNotExist(race.getId(), request);

        savePointRules(race, request);
        return toResponseList(race.getId());
    }

    @Override
    @Transactional
    public List<RacePointRuleResponse> replacePointRules(Integer raceId, List<RacePointRuleItemRequest> request) {
        Races race = getRaceEntity(raceId);
        racePointRuleValidator.ensurePointRulesEditable(race);
        racePointRuleValidator.validateRequest(request);

        racePointRulesRepository.deleteByRace_Id(race.getId());
        savePointRules(race, request);
        return toResponseList(race.getId());
    }

    @Override
    @Transactional
    public void deletePointRule(Integer raceId, Integer ruleId) {
        Races race = getRaceEntity(raceId);
        racePointRuleValidator.ensurePointRulesEditable(race);

        RacePointRules rule = racePointRulesRepository.findById(ruleId)
                .filter(existingRule -> existingRule.getRace() != null
                        && race.getId().equals(existingRule.getRace().getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Race point rule not found"));

        racePointRulesRepository.delete(rule);
    }

    private void savePointRules(Races race, List<RacePointRuleItemRequest> pointRules) {
        for (RacePointRuleItemRequest item : pointRules) {
            RacePointRules rule = RacePointRules.builder()
                    .race(race)
                    .finishPosition(item.getFinishPosition())
                    .points(item.getPoints())
                    .note(clean(item.getNote()))
                    .build();
            racePointRulesRepository.save(rule);
        }
    }

    private Races getRaceEntity(Integer raceId) {
        if (raceId == null) {
            throw new BadRequestException("Race id is required");
        }

        return racesRepository.findById(raceId)
                .orElseThrow(() -> new ResourceNotFoundException("Race not found"));
    }

    private List<RacePointRuleResponse> toResponseList(Integer raceId) {
        return racePointRulesRepository.findByRace_IdOrderByFinishPositionAsc(raceId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private RacePointRuleResponse toResponse(RacePointRules rule) {
        return RacePointRuleResponse.builder()
                .id(rule.getId())
                .raceId(rule.getRace() == null ? null : rule.getRace().getId())
                .finishPosition(rule.getFinishPosition())
                .points(rule.getPoints())
                .note(rule.getNote())
                .build();
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }

        String cleaned = value.trim().toLowerCase();
        return cleaned.isBlank() ? null : cleaned;
    }
}
