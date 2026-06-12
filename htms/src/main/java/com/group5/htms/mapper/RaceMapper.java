package com.group5.htms.mapper;

import com.group5.htms.dto.race.response.RaceResponse;
import com.group5.htms.entity.Races;
import org.springframework.stereotype.Component;

@Component
public class RaceMapper {
    public RaceResponse toResponse(Races race) {
        return RaceResponse.builder()
                .id(race.getId())
                .tournamentId(race.getSchedule().getTournaments().getId())
                .scheduleId(race.getSchedule().getId())
                .name(race.getName())
                .raceNumber(race.getRaceNumber())
                .rankGroup(race.getRankGroup())
                .lapCount(race.getLapCount())
                .scheduledAt(race.getScheduledAt())
                .predictionClosesAt(race.getPredictionClosesAt())
                .distanceM(race.getDistanceM())
                .trackType(race.getTrackType())
                .maxHorses(race.getMaxHorses())
                .maxReferees(race.getMaxReferees())
                .pointRuleNote(race.getPointRuleNote())
                .status(race.getStatus())
                .build();
    }
}
