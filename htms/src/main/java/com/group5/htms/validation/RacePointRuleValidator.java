package com.group5.htms.validation;

import com.group5.htms.dto.racepointrule.request.RacePointRuleItemRequest;
import com.group5.htms.entity.Races;
import com.group5.htms.enums.RaceStatus;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.repository.RacePointRulesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RacePointRuleValidator {
    private final RacePointRulesRepository racePointRulesRepository;

    public void validateRequest(List<RacePointRuleItemRequest> request) {
        if (request == null || request.isEmpty()) {
            throw new BadRequestException("Point rules are required");
        }

        Set<Integer> positions = new HashSet<>();
        for (RacePointRuleItemRequest item : request) {
            if (item == null) {
                throw new BadRequestException("Point rule item is required");
            }

            if (!positions.add(item.getFinishPosition())) {
                throw new BadRequestException("Duplicate finish position in point rules");
            }
        }
    }

    public void ensurePointRulesEditable(Races race) {
        String status = clean(race.getStatus());
        if (RaceStatus.IN_PROGRESS.getValue().equals(status)
                || RaceStatus.COMPLETED.getValue().equals(status)
                || RaceStatus.CANCELLED.getValue().equals(status)) {
            throw new BadRequestException("Cannot update point rules after race has started");
        }
    }

    public void ensureFinishPositionsDoNotExist(Integer raceId, List<RacePointRuleItemRequest> request) {
        for (RacePointRuleItemRequest item : request) {
            if (racePointRulesRepository.existsByRace_IdAndFinishPosition(raceId, item.getFinishPosition())) {
                throw new BadRequestException("Finish position already exists in race point rules");
            }
        }
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }

        String cleaned = value.trim().toLowerCase();
        return cleaned.isBlank() ? null : cleaned;
    }
}
