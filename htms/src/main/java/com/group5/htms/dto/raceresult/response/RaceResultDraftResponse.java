package com.group5.htms.dto.raceresult.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class RaceResultDraftResponse {
    private Integer raceId;
    private String raceName;
    private String status;
    private Integer reportId;
    private Integer submittedByRefereeId;
    private List<RaceResultResponse> results;
}
