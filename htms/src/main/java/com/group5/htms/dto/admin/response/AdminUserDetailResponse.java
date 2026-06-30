package com.group5.htms.dto.admin.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
public class AdminUserDetailResponse {
    private Integer userId;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String roleType;
    private String status;
    private String avatarUrl;
    private Instant createdAt;
    private HorseOwnerProfileResponse horseOwnerProfile;
    private JockeyProfileResponse jockeyProfile;
    private RefereeProfileResponse refereeProfile;
    private WalletSummaryResponse walletSummary;

    @Getter
    @Builder
    public static class HorseOwnerProfileResponse {
        private Integer ownerId;
        private String stableName;
        private String licenseNumber;
        private String address;
        private Integer favoriteJockeyId;
        private String favoriteJockeyName;
        private String status;
        private Instant createdAt;
    }

    @Getter
    @Builder
    public static class JockeyProfileResponse {
        private Integer jockeyId;
        private String licenseNumber;
        private Integer rankingPoints;
        private Integer totalWins;
        private Integer experienceYears;
        private String status;
    }

    @Getter
    @Builder
    public static class RefereeProfileResponse {
        private Integer refereeId;
        private String licenseNumber;
        private String address;
        private String status;
        private Instant createdAt;
    }

    @Getter
    @Builder
    public static class WalletSummaryResponse {
        private Integer walletId;
        private BigDecimal pointBalance;
        private String status;
        private Instant createdAt;
    }
}
