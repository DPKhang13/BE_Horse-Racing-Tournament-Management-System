package com.group5.htms.dto.prize.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrizeAwardResponse {

    private Integer awardId;

    private Integer prizeId;

    private Integer tournamentId;

    private Integer raceId;

    private Integer resultId;

    private Integer horseId;

    private Integer ownerId;

    private Integer finishPosition;

    private BigDecimal amount;

    private String status;

    private Instant awardedAt;

    private String horseName;

    private String ownerFullName;

    private String tournamentName;

    private String prizeName;
}
