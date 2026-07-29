package com.group5.htms.dto.horse.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Builder
@Getter
public class HorseListResponse {
    private Integer horseId;
    private Integer ownerId;
    private String name;
    private String breed;
    private Integer age;
    private BigDecimal weightKg;
    private String rankGroup;
    private Integer rankingPoints;
    private String avatarUrl;
    private Integer totalWins;
    private Integer totalRaces;
    private String status;
    private String ownerFullName;
    private String ownerStableName;
}
