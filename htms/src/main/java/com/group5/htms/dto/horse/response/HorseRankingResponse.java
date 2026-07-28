package com.group5.htms.dto.horse.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HorseRankingResponse {
    private Integer rank;
    private Integer id;
    private Integer horseId;
    private Integer ownerId;
    private String name;
    private String breed;
    private String rankGroup;
    private Integer rankingPoints;
    private Integer totalWins;
    private Integer totalRaces;
    private String avatarUrl;
    private String status;
    private String ownerFullName;
    private String ownerEmail;
    private String ownerPhone;
    private String ownerStableName;
    private String ownerLicenseNumber;
}
