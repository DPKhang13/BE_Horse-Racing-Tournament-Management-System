package com.group5.htms.mapper;

import com.group5.htms.dto.race.request.RaceCreateRequest;
import com.group5.htms.dto.race.request.RaceUpdateRequest;
import com.group5.htms.dto.race.response.RaceListResponse;
import com.group5.htms.dto.race.response.RaceResponse;
import com.group5.htms.entity.Races;
import com.group5.htms.entity.TournamentSchedules;
import com.group5.htms.entity.Tournaments;
import com.group5.htms.enums.RaceStatus;
import org.springframework.stereotype.Component;

@Component
public class RaceMapper {
    private static final Integer DEFAULT_LAP_COUNT = 1;
    private static final Integer DEFAULT_MAX_HORSES = 8;
    private static final Integer DEFAULT_MAX_REFEREES = 3;

    public Races toEntity(RaceCreateRequest request, TournamentSchedules schedule) {
        if (request == null) {
            return null;
        }

        return Races.builder()
                .schedule(schedule)
                .name(clean(request.getName()))
                .raceNumber(request.getRaceNumber())
                .rankGroup(clean(request.getRankGroup()))
                .lapCount(request.getLapCount() == null ? DEFAULT_LAP_COUNT : request.getLapCount())
                .scheduledAt(request.getScheduledAt())
                .predictionClosesAt(request.getPredictionClosesAt())
                .distanceM(request.getDistanceM())
                .trackType(clean(request.getTrackType()))
                .maxHorses(request.getMaxHorses() == null ? DEFAULT_MAX_HORSES : request.getMaxHorses())
                .maxReferees(request.getMaxReferees() == null ? DEFAULT_MAX_REFEREES : request.getMaxReferees())
                .pointRuleNote(clean(request.getPointRuleNote()))
                .status(defaultStatus(request.getStatus()))
                .build();
    }

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

    public void updateEntity(Races race, RaceUpdateRequest request) {
        if (race == null || request == null) {
            return;
        }

        if (request.getName() != null) {
            race.setName(clean(request.getName()));
        }

        if (request.getRaceNumber() != null) {
            race.setRaceNumber(request.getRaceNumber());
        }

        if (request.getRankGroup() != null) {
            race.setRankGroup(clean(request.getRankGroup()));
        }

        if (request.getLapCount() != null) {
            race.setLapCount(request.getLapCount());
        }

        if (request.getScheduledAt() != null) {
            race.setScheduledAt(request.getScheduledAt());
        }

        if (request.getPredictionClosesAt() != null) {
            race.setPredictionClosesAt(request.getPredictionClosesAt());
        }

        if (request.getDistanceM() != null) {
            race.setDistanceM(request.getDistanceM());
        }

        if (request.getTrackType() != null) {
            race.setTrackType(clean(request.getTrackType()));
        }

        if (request.getMaxHorses() != null) {
            race.setMaxHorses(request.getMaxHorses());
        }

        if (request.getMaxReferees() != null) {
            race.setMaxReferees(request.getMaxReferees());
        }

        if (request.getPointRuleNote() != null) {
            race.setPointRuleNote(clean(request.getPointRuleNote()));
        }

    }

    private String defaultStatus(String value) {
        if (value == null || value.isBlank()) {
            return RaceStatus.SCHEDULED.getValue();
        }

        return value.trim().toLowerCase();
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }

        String cleaned = value.trim();

        return cleaned.isBlank() ? null : cleaned;
    }
}
