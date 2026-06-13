package com.group5.htms.mapper;

import com.group5.htms.dto.auth.response.UserMeResponse;
import com.group5.htms.entity.HorseOwnerProfiles;
import com.group5.htms.entity.JockeyProfiles;
import com.group5.htms.entity.RefereeProfiles;
import com.group5.htms.entity.Users;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuthMapper {

    public UserMeResponse toUserMeResponse(Users user) {
        return toUserMeResponse(user, null, null, null);
    }

    public UserMeResponse toUserMeResponse(
            Users user,
            HorseOwnerProfiles ownerProfile,
            JockeyProfiles jockeyProfile,
            RefereeProfiles refereeProfile
    ) {
        if (user == null) {
            return null;
        }

        List<String> roles = user.getRoleType() == null || user.getRoleType().isBlank()
                ? List.of()
                : List.of(user.getRoleType().toLowerCase());

        return UserMeResponse.builder()
                .id(user.getId())
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .roleType(user.getRoleType())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .roles(roles)
                .ownerProfile(toOwnerProfileResponse(ownerProfile))
                .jockeyProfile(toJockeyProfileResponse(jockeyProfile))
                .refereeProfile(toRefereeProfileResponse(refereeProfile))
                .build();
    }

    private UserMeResponse.OwnerProfileResponse toOwnerProfileResponse(
            HorseOwnerProfiles profile
    ) {
        if (profile == null) {
            return null;
        }

        return UserMeResponse.OwnerProfileResponse.builder()
                .ownerId(profile.getId())
                .stableName(profile.getStableName())
                .licenseNumber(profile.getLicenseNumber())
                .address(profile.getAddress())
                .favoriteJockeyId(profile.getFavoriteJockey() != null
                        ? profile.getFavoriteJockey().getId()
                        : null)
                .status(profile.getStatus())
                .createdAt(profile.getCreatedAt())
                .build();
    }

    private UserMeResponse.JockeyProfileResponse toJockeyProfileResponse(
            JockeyProfiles profile
    ) {
        if (profile == null) {
            return null;
        }

        return UserMeResponse.JockeyProfileResponse.builder()
                .jockeyId(profile.getId())
                .licenseNumber(profile.getLicenseNumber())
                .rankingPoints(profile.getRankingPoints())
                .totalWins(profile.getTotalWins())
                .experienceYears(profile.getExperienceYears())
                .status(profile.getStatus())
                .build();
    }

    private UserMeResponse.RefereeProfileResponse toRefereeProfileResponse(
            RefereeProfiles profile
    ) {
        if (profile == null) {
            return null;
        }

        return UserMeResponse.RefereeProfileResponse.builder()
                .refereeId(profile.getId())
                .licenseNumber(profile.getLicenseNumber())
                .address(profile.getAddress())
                .status(profile.getStatus())
                .createdAt(profile.getCreatedAt())
                .build();
    }
}
