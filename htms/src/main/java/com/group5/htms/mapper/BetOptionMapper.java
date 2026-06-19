package com.group5.htms.mapper;

import com.group5.htms.dto.betoption.response.BetOptionResponse;
import com.group5.htms.entity.BetOptions;
import com.group5.htms.entity.JockeyHorseAssignments;
import com.group5.htms.entity.JockeyProfiles;
import com.group5.htms.entity.Users;
import org.springframework.stereotype.Component;

@Component
public class BetOptionMapper {
    public BetOptionResponse toResponse(BetOptions option) {
        JockeyHorseAssignments assignment = option.getAssignment();
        JockeyProfiles jockey = assignment.getJockey();
        Users jockeyUser = jockey.getUsers();

        return BetOptionResponse.builder()
                .optionId(option.getId())
                .raceId(option.getRaces().getId())
                .raceName(option.getRaces().getName())
                .raceNumber(option.getRaces().getRaceNumber())
                .scheduledAt(option.getRaces().getScheduledAt())
                .predictionClosesAt(option.getRaces().getPredictionClosesAt())
                .assignmentId(assignment.getId())
                .horseId(option.getHorses().getId())
                .horseName(option.getHorses().getName())
                .horseAvatarUrl(option.getHorses().getAvatarUrl())
                .gateNumber(assignment.getGateNumber())
                .jockeyId(jockey.getId())
                .jockeyName(jockeyUser == null ? null : jockeyUser.getFullName())
                .jockeyFullName(jockeyUser == null ? null : jockeyUser.getFullName())
                .jockeyAvatarUrl(jockeyUser == null ? null : jockeyUser.getAvatarUrl())
                .currentRate(option.getCurrentRate())
                .totalBetPoints(option.getTotalBetPoints())
                .totalBetCount(option.getTotalBetCount())
                .updatedAt(option.getUpdatedAt())
                .build();
    }
}
