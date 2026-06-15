package com.group5.htms.mapper;

import com.group5.htms.dto.jockey.response.JockeyListResponse;
import com.group5.htms.dto.jockey.response.JockeyResponse;
import com.group5.htms.dto.jockey.response.JockeyRankingResponse;
import com.group5.htms.entity.JockeyProfiles;
import com.group5.htms.entity.Users;
import org.springframework.stereotype.Component;

@Component
public class JockeyMapper {
    public JockeyResponse toResponse(JockeyProfiles jockey) {
        Users user = jockey.getUsers();

        return JockeyResponse.builder()
                .id(jockey.getId())
                .jockeyId(jockey.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .licenseNumber(jockey.getLicenseNumber())
                .rankingPoints(jockey.getRankingPoints())
                .totalWins(jockey.getTotalWins())
                .experienceYears(jockey.getExperienceYears())
                .status(jockey.getStatus())
                .userStatus(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public JockeyListResponse toListResponse(JockeyProfiles jockey) {
        Users user = jockey.getUsers();

        return JockeyListResponse.builder()
                .jockeyId(jockey.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .licenseNumber(jockey.getLicenseNumber())
                .rankingPoints(jockey.getRankingPoints())
                .totalWins(jockey.getTotalWins())
                .experienceYears(jockey.getExperienceYears())
                .status(jockey.getStatus())
                .build();
    }

    public JockeyRankingResponse toRankingResponse(JockeyProfiles jockey, Integer rank) {
        return toRankingResponse(jockey, rank, null, null);
    }

    public JockeyRankingResponse toRankingResponse(
            JockeyProfiles jockey,
            Integer rank,
            Long totalRaces,
            Double winRate
    ) {
        Users user = jockey.getUsers();

        return JockeyRankingResponse.builder()
                .rank(rank)
                .id(jockey.getId())
                .jockeyId(jockey.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .licenseNumber(jockey.getLicenseNumber())
                .rankingPoints(jockey.getRankingPoints())
                .totalWins(jockey.getTotalWins())
                .experienceYears(jockey.getExperienceYears())
                .status(jockey.getStatus())
                .userStatus(user.getStatus())
                .createdAt(user.getCreatedAt())
                .totalRaces(totalRaces)
                .winRate(winRate)
                .build();
    }
}
