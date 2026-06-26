package com.group5.htms.dto.racepointrule.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RacePointRuleResponse {
    private Integer id;
    private Integer raceId;
    private Integer finishPosition;
    private Integer points;
    private String note;
}
