package com.group5.htms.dto.jockey.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JockeyResponse {
    private Integer id;
    private Integer jockeyId;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String avatarUrl;
    private String licenseNumber;
    private Integer rankingPoints;
    private Integer totalWins;
    private Integer experienceYears;
    private String status;
    private String userStatus;
    private java.time.Instant createdAt;
}
