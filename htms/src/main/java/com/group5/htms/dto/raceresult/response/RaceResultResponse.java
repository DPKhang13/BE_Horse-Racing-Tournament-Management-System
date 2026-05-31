package com.group5.htms.dto.raceresult.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@Getter
public class RaceResultResponse {
    private Integer id;
    private Integer assignmentId;
    private Integer reportId;
    private Integer prizeId;
    private Integer finishPosition;
    private BigDecimal finishTimeSec;
    private Integer pointsAwarded;
    private Boolean isDisqualified;
    private String disqualifyReason;
    private String status;
    private Instant recordedAt;
    private Instant publishedAt;
}
