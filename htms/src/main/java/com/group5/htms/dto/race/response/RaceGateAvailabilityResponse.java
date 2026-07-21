package com.group5.htms.dto.race.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RaceGateAvailabilityResponse {
    private Integer raceId;
    private String raceName;
    private Integer maxHorses;
    private Integer gateCount;
    private List<Integer> availableGates;
    private List<Integer> occupiedGates;
}