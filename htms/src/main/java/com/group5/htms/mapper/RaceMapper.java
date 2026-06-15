package com.group5.htms.mapper;

import com.group5.htms.dto.race.response.RaceListResponse;
import com.group5.htms.dto.race.response.RaceResponse;
import com.group5.htms.entity.Races;
import com.group5.htms.entity.TournamentSchedules;
import com.group5.htms.entity.Tournaments;
import org.springframework.stereotype.Component;

@Component
public class RaceMapper {
    public RaceResponse toResponse(Races race) {
        return toResponse(race, null, null, null);
    }

    public RaceResponse toResponse(
            Races race,
            Long registeredHorseCount,
            Long acceptedJockeyCount,
            Long assignedRefereeCount
    ) {
        TournamentSchedules schedule = race.getSchedule();
        Tournaments tournament = schedule.getTournaments();

        return RaceResponse.builder()
                .id(race.getId())
                .raceId(race.getId())
                .tournamentId(tournament.getId())
                .scheduleId(schedule.getId())
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
                .tournamentName(tournament.getName())
                .raceDate(schedule.getRaceDate())
                .dayNumber(schedule.getDayNumber())
                .scheduleTitle(schedule.getTitle())
                .scheduleNote(schedule.getNote())
                .location(tournament.getLocation())
                .registeredHorseCount(registeredHorseCount)
                .acceptedJockeyCount(acceptedJockeyCount)
                .assignedRefereeCount(assignedRefereeCount)
                .build();
    }

    public RaceListResponse toListResponse(
            Races race,
            Long registeredHorseCount,
            Long acceptedJockeyCount,
            Long assignedRefereeCount
    ) {
        TournamentSchedules schedule = race.getSchedule();
        Tournaments tournament = schedule.getTournaments();

        return RaceListResponse.builder()
                .raceId(race.getId())
                .tournamentId(tournament.getId())
                .scheduleId(schedule.getId())
                .name(race.getName())
                .raceNumber(race.getRaceNumber())
                .rankGroup(race.getRankGroup())
                .scheduledAt(race.getScheduledAt())
                .predictionClosesAt(race.getPredictionClosesAt())
                .distanceM(race.getDistanceM())
                .trackType(race.getTrackType())
                .maxHorses(race.getMaxHorses())
                .status(race.getStatus())
                .tournamentName(tournament.getName())
                .raceDate(schedule.getRaceDate())
                .dayNumber(schedule.getDayNumber())
                .scheduleTitle(schedule.getTitle())
                .location(tournament.getLocation())
                .registeredHorseCount(registeredHorseCount)
                .acceptedJockeyCount(acceptedJockeyCount)
                .assignedRefereeCount(assignedRefereeCount)
                .build();
    }
}
