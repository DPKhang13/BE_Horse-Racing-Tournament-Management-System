package com.group5.htms.dto.jockey.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JockeyRankingResponse {
    private Integer rank;
    private Integer id;
    private String fullName;
    private String avatarUrl;
    private String licenseNumber;
    private Integer rankingPoints;
    private Integer totalWins;
    private Integer experienceYears;
    private String status;
}
