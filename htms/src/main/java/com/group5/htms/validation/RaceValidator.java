package com.group5.htms.validation;

import com.group5.htms.dto.race.request.RaceStartRequest;
import com.group5.htms.entity.Races;
import com.group5.htms.entity.TournamentSchedules;
import com.group5.htms.entity.Tournaments;
import com.group5.htms.enums.RaceStatus;
import com.group5.htms.enums.TournamentStatus;
import com.group5.htms.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Component
public class RaceValidator {
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    public void ensureTournamentCanArrangeRace(Tournaments tournament) {
        if (tournament == null) {
            throw new BadRequestException("Tournament not found");
        }

        if (TournamentStatus.COMPLETED.equalsValue(tournament.getStatus())
                || TournamentStatus.CANCELLED.equalsValue(tournament.getStatus())) {
            throw new BadRequestException("Cannot arrange races for completed or cancelled tournament");
        }
    }

    public void ensureScheduleDateWithinTournament(Tournaments tournament, LocalDate raceDate) {
        if (raceDate == null) {
            throw new BadRequestException("Race date is required");
        }

        if (raceDate.isBefore(tournament.getStartDate()) || raceDate.isAfter(tournament.getEndDate())) {
            throw new BadRequestException("Race date must be within tournament date range");
        }
    }

    public void ensureScheduledAtMatchesSchedule(TournamentSchedules schedule, Instant scheduledAt) {
        LocalDate scheduledDate = scheduledAt.atZone(VIETNAM_ZONE).toLocalDate();
        if (!scheduledDate.equals(schedule.getRaceDate())) {
            throw new BadRequestException("Scheduled time must be on the schedule race date");
        }
    }

    public void ensurePredictionClosesBeforeRace(Instant predictionClosesAt, Instant scheduledAt) {
        if (predictionClosesAt != null && !predictionClosesAt.isBefore(scheduledAt)) {
            throw new BadRequestException("Prediction close time must be before scheduled time");
        }
    }

    public void ensureRaceCanStart(Races race, RaceStartRequest request) {
        String status = race.getStatus();

        if (RaceStatus.CANCELLED.equalsValue(status)) {
            throw new BadRequestException("Cancelled race cannot be started");
        }
        if (RaceStatus.COMPLETED.equalsValue(status)) {
            throw new BadRequestException("Completed race cannot be started");
        }
        if (RaceStatus.IN_PROGRESS.equalsValue(status)) {
            throw new BadRequestException("Race is already in progress");
        }
        if (!RaceStatus.canStart(status)) {
            throw new BadRequestException("Only ready or open for betting races can be started");
        }
        if (RaceStatus.isBettingOpen(status)) {
            ensureBettingCanClose(race, request);
        }
    }

    private void ensureBettingCanClose(Races race, RaceStartRequest request) {
        Instant predictionClosesAt = race.getPredictionClosesAt();
        if (predictionClosesAt == null) {
            throw new BadRequestException("Prediction close time is required before starting race from betting status");
        }

        boolean forceCloseBetting = request != null && request.isForceCloseBetting();
        if (Instant.now().isBefore(predictionClosesAt) && !forceCloseBetting) {
            throw new BadRequestException("Prediction betting is still open. Use forceCloseBetting to start the race early");
        }
    }
}
