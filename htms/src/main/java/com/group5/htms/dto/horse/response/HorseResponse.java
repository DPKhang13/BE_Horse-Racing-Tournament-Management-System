package com.group5.htms.dto.horse.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@Getter
public class HorseResponse {
    private Integer id;
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
    private Instant registeredAt;
    private String ownerFullName;
    private String ownerEmail;
    private String ownerPhone;
    private String ownerStableName;
    private String ownerLicenseNumber;
}
