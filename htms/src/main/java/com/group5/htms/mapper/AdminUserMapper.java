package com.group5.htms.mapper;

import com.group5.htms.dto.admin.response.AdminUserDetailResponse;
import com.group5.htms.dto.admin.response.AdminUserListResponse;
import com.group5.htms.entity.HorseOwnerProfiles;
import com.group5.htms.entity.JockeyProfiles;
import com.group5.htms.entity.RefereeProfiles;
import com.group5.htms.entity.Users;
import com.group5.htms.entity.Wallets;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminUserMapper {

    public AdminUserListResponse toListResponse(
            Users user,
            HorseOwnerProfiles ownerProfile,
            JockeyProfiles jockeyProfile,
            RefereeProfiles refereeProfile
    ) {
        return AdminUserListResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .roleType(user.getRoleType())
                .status(user.getStatus())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .profileStatus(resolveProfileStatus(ownerProfile, jockeyProfile, refereeProfile))
                .build();
    }

    public AdminUserDetailResponse toDetailResponse(
            Users user,
            HorseOwnerProfiles ownerProfile,
            JockeyProfiles jockeyProfile,
            RefereeProfiles refereeProfile,
            Wallets wallet
    ) {
        return AdminUserDetailResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .roleType(user.getRoleType())
                .status(user.getStatus())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .horseOwnerProfile(toOwnerProfile(ownerProfile))
                .jockeyProfile(toJockeyProfile(jockeyProfile))
                .refereeProfile(toRefereeProfile(refereeProfile))
                .walletSummary(toWalletSummary(wallet))
                .build();
    }

    private AdminUserDetailResponse.HorseOwnerProfileResponse toOwnerProfile(HorseOwnerProfiles profile) {
        if (profile == null) {
            return null;
        }

        JockeyProfiles favoriteJockey = profile.getFavoriteJockey();
        return AdminUserDetailResponse.HorseOwnerProfileResponse.builder()
                .ownerId(profile.getId())
                .stableName(profile.getStableName())
                .licenseNumber(profile.getLicenseNumber())
                .address(profile.getAddress())
                .favoriteJockeyId(favoriteJockey == null ? null : favoriteJockey.getId())
                .favoriteJockeyName(favoriteJockey == null || favoriteJockey.getUsers() == null
                        ? null
                        : favoriteJockey.getUsers().getFullName())
                .status(profile.getStatus())
                .createdAt(profile.getCreatedAt())
                .build();
    }

    private AdminUserDetailResponse.JockeyProfileResponse toJockeyProfile(JockeyProfiles profile) {
        if (profile == null) {
            return null;
        }

        return AdminUserDetailResponse.JockeyProfileResponse.builder()
                .jockeyId(profile.getId())
                .licenseNumber(profile.getLicenseNumber())
                .rankingPoints(profile.getRankingPoints())
                .totalWins(profile.getTotalWins())
                .experienceYears(profile.getExperienceYears())
                .status(profile.getStatus())
                .build();
    }

    private AdminUserDetailResponse.RefereeProfileResponse toRefereeProfile(RefereeProfiles profile) {
        if (profile == null) {
            return null;
        }

        return AdminUserDetailResponse.RefereeProfileResponse.builder()
                .refereeId(profile.getId())
                .licenseNumber(profile.getLicenseNumber())
                .address(profile.getAddress())
                .status(profile.getStatus())
                .createdAt(profile.getCreatedAt())
                .build();
    }

    private AdminUserDetailResponse.WalletSummaryResponse toWalletSummary(Wallets wallet) {
        if (wallet == null) {
            return null;
        }

        return AdminUserDetailResponse.WalletSummaryResponse.builder()
                .walletId(wallet.getId())
                .pointBalance(wallet.getPointBalance())
                .status(wallet.getStatus())
                .createdAt(wallet.getCreatedAt())
                .build();
    }

    private String resolveProfileStatus(
            HorseOwnerProfiles ownerProfile,
            JockeyProfiles jockeyProfile,
            RefereeProfiles refereeProfile
    ) {
        if (ownerProfile != null) {
            return ownerProfile.getStatus();
        }
        if (jockeyProfile != null) {
            return jockeyProfile.getStatus();
        }
        if (refereeProfile != null) {
            return refereeProfile.getStatus();
        }
        return null;
    }
}
