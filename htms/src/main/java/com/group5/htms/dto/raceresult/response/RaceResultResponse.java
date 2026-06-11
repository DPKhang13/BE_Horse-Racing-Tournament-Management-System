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
    private Integer raceId;
    private Integer horseId;
    private Integer ownerId;
    private Integer reportId;
    private Integer finalRound;
    private Integer finishPosition;
    private BigDecimal finishTimeSec;
    private Integer pointsAwarded;
    private Boolean isDisqualified;
    private String disqualifyReason;
    private String status;
    private Instant recordedAt;
    private Instant publishedAt;
}
