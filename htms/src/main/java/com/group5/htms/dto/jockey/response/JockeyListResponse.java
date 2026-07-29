package com.group5.htms.dto.jockey.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JockeyListResponse {
    private Integer jockeyId;
    private String username;
    private String fullName;
    private String avatarUrl;
    private String licenseNumber;
    private Integer rankingPoints;
    private Integer totalWins;
    private Integer totalRaces;
    private Integer experienceYears;
    private String status;
}
