package com.group5.htms.dto.auth.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class UserMeResponse {
    private Integer id;
    private Integer userId;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String roleType;
    private String avatarUrl;
    private String status;
    private Instant createdAt;
    private List<String> roles;
    private OwnerProfileResponse ownerProfile;
    private JockeyProfileResponse jockeyProfile;
    private RefereeProfileResponse refereeProfile;

    @Data
    @Builder
    public static class OwnerProfileResponse {
        private Integer ownerId;
        private String stableName;
        private String licenseNumber;
        private String address;
        private Integer favoriteJockeyId;
        private String status;
        private Instant createdAt;
    }

    @Data
    @Builder
    public static class JockeyProfileResponse {
        private Integer jockeyId;
        private String licenseNumber;
        private Integer rankingPoints;
        private Integer totalWins;
        private Integer totalRaces;
        private Integer experienceYears;
        private String status;
    }

    @Data
    @Builder
    public static class RefereeProfileResponse {
        private Integer refereeId;
        private String licenseNumber;
        private String address;
        private String status;
        private Instant createdAt;
    }
}
