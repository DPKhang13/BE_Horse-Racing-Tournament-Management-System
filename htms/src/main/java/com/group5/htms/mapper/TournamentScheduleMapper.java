package com.group5.htms.mapper;

import com.group5.htms.dto.schedule.request.TournamentScheduleCreateRequest;
import com.group5.htms.dto.schedule.request.TournamentScheduleUpdateRequest;
import com.group5.htms.dto.schedule.response.TournamentScheduleResponse;
import com.group5.htms.entity.TournamentSchedules;
import com.group5.htms.entity.Tournaments;
import org.springframework.stereotype.Component;

@Component
public class TournamentScheduleMapper {

    public TournamentSchedules toEntity(
            TournamentScheduleCreateRequest request,
            Tournaments tournament
    ) {
        if (request == null) {
            return null;
        }

        return TournamentSchedules.builder()
                .tournaments(tournament)
                .raceDate(request.getRaceDate())
                .dayNumber(request.getDayNumber())
                .title(clean(request.getTitle()))
                .note(clean(request.getNote()))
                .build();
    }

    public TournamentScheduleResponse toResponse(TournamentSchedules schedule) {
        if (schedule == null) {
            return null;
        }

        Tournaments tournament = schedule.getTournaments();

        return TournamentScheduleResponse.builder()
                .scheduleId(schedule.getId())
                .tournamentId(tournament != null ? tournament.getId() : null)
                .tournamentName(tournament != null ? tournament.getName() : null)
                .raceDate(schedule.getRaceDate())
                .dayNumber(schedule.getDayNumber())
                .title(schedule.getTitle())
                .note(schedule.getNote())
                .build();
    }

    public void updateEntity(
            TournamentSchedules schedule,
            TournamentScheduleUpdateRequest request
    ) {
        if (schedule == null || request == null) {
            return;
        }

        if (request.getRaceDate() != null) {
            schedule.setRaceDate(request.getRaceDate());
        }

        if (request.getDayNumber() != null) {
            schedule.setDayNumber(request.getDayNumber());
        }

        if (request.getTitle() != null) {
            schedule.setTitle(clean(request.getTitle()));
        }

        if (request.getNote() != null) {
            schedule.setNote(clean(request.getNote()));
        }
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }

        String cleaned = value.trim();

        return cleaned.isBlank() ? null : cleaned;
    }
}
