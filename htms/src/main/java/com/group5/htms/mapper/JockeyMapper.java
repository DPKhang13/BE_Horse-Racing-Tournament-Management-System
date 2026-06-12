package com.group5.htms.mapper;

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
                .build();
    }

    public JockeyRankingResponse toRankingResponse(JockeyProfiles jockey, Integer rank) {
        Users user = jockey.getUsers();

        return JockeyRankingResponse.builder()
                .rank(rank)
                .id(jockey.getId())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .licenseNumber(jockey.getLicenseNumber())
                .rankingPoints(jockey.getRankingPoints())
                .totalWins(jockey.getTotalWins())
                .experienceYears(jockey.getExperienceYears())
                .status(jockey.getStatus())
                .build();
    }
}
