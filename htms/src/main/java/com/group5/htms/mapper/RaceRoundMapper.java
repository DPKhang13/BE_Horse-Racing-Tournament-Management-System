package com.group5.htms.mapper;

import com.group5.htms.dto.raceround.request.RaceRoundCreateRequest;
import com.group5.htms.dto.raceround.request.RaceRoundUpdateRequest;
import com.group5.htms.dto.raceround.response.RaceRoundResponse;
import com.group5.htms.entity.Horses;
import com.group5.htms.entity.JockeyHorseAssignments;
import com.group5.htms.entity.RaceRounds;
import com.group5.htms.entity.Races;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class RaceRoundMapper {
    public RaceRounds toEntity(RaceRoundCreateRequest request, JockeyHorseAssignments assignment) {
        return RaceRounds.builder()
                .races(assignment.getRaces())
                .assignment(assignment)
                .horses(assignment.getReg().getHorses())
                .roundNumber(request.getRoundNumber())
                .position(request.getPosition())
                .lapTimeSec(request.getLapTimeSec())
                .recordedAt(defaultInstant(request.getRecordedAt()))
                .build();
    }

    public void updateRound(RaceRounds round, RaceRoundUpdateRequest request, JockeyHorseAssignments assignment) {
        if (assignment != null) {
            round.setRaces(assignment.getRaces());
            round.setAssignment(assignment);
            round.setHorses(assignment.getReg().getHorses());
        }
        if (request.getRoundNumber() != null) {
            round.setRoundNumber(request.getRoundNumber());
        }
        if (request.getPosition() != null) {
            round.setPosition(request.getPosition());
        }
        if (request.getLapTimeSec() != null) {
            round.setLapTimeSec(request.getLapTimeSec());
        }
        if (request.getRecordedAt() != null) {
            round.setRecordedAt(request.getRecordedAt());
        }
    }

    public RaceRoundResponse toResponse(RaceRounds round) {
        JockeyHorseAssignments assignment = round.getAssignment();
        Races race = round.getRaces();

        return RaceRoundResponse.builder()
                .id(round.getId())
                .roundId(round.getId())
                .raceId(race.getId())
                .assignmentId(assignment.getId())
                .horseId(round.getHorses().getId())
                .jockeyId(assignment.getJockey().getId())
                .ownerId(assignment.getReg().getOwner().getId())
                .roundNumber(round.getRoundNumber())
                .position(round.getPosition())
                .lapTimeSec(round.getLapTimeSec())
                .recordedAt(round.getRecordedAt())
                .raceName(race.getName())
                .raceNumber(race.getRaceNumber())
                .lapCount(race.getLapCount())
                .scheduledAt(race.getScheduledAt())
                .tournamentId(race.getSchedule().getTournaments().getId())
                .tournamentName(race.getSchedule().getTournaments().getName())
                .horseName(round.getHorses().getName())
                .horseAvatarUrl(round.getHorses().getAvatarUrl())
                .ownerFullName(assignment.getReg().getOwner().getUsers().getFullName())
                .jockeyFullName(assignment.getJockey().getUsers().getFullName())
                .gateNumber(assignment.getGateNumber())
                .build();
    }

    private Instant defaultInstant(Instant value) {
        return value == null ? Instant.now() : value;
    }
}
