package com.group5.htms.mapper;

import com.group5.htms.dto.raceresult.request.RaceResultCreateRequest;
import com.group5.htms.dto.raceresult.request.RaceResultUpdateRequest;
import com.group5.htms.dto.raceresult.response.RaceResultResponse;
import com.group5.htms.entity.HorseOwnerProfiles;
import com.group5.htms.entity.Horses;
import com.group5.htms.entity.JockeyHorseAssignments;
import com.group5.htms.entity.RaceResults;
import com.group5.htms.entity.Races;
import com.group5.htms.entity.RefereeReports;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class RaceResultMapper {
    public RaceResults toEntity(RaceResultCreateRequest request, JockeyHorseAssignments assignment) {
        return RaceResults.builder()
                .assignment(toAssignment(request.getAssignmentId()))
                .races(toRace(assignment.getRaces().getId()))
                .horses(toHorse(assignment.getReg().getHorses().getId()))
                .owner(toOwner(assignment.getReg().getOwner().getId()))
                .report(toNullableReport(request.getReportId()))
                .finalRound(request.getFinalRound())
                .finishPosition(request.getFinishPosition())
                .finishTimeSec(request.getFinishTimeSec())
                .pointsAwarded(defaultZero(request.getPointsAwarded()))
                .isDisqualified(request.getIsDisqualified() != null && request.getIsDisqualified())
                .disqualifyReason(trim(request.getDisqualifyReason()))
                .status(defaultText(request.getStatus(), "draft"))
                .recordedAt(defaultInstant(request.getRecordedAt()))
                .publishedAt(request.getPublishedAt())
                .build();
    }

    public void updateResult(RaceResults result, RaceResultUpdateRequest request, JockeyHorseAssignments assignment) {
        if (request.getAssignmentId() != null) {
            result.setAssignment(toAssignment(request.getAssignmentId()));
            result.setRaces(toRace(assignment.getRaces().getId()));
            result.setHorses(toHorse(assignment.getReg().getHorses().getId()));
            result.setOwner(toOwner(assignment.getReg().getOwner().getId()));
        }
        if (request.getReportId() != null) {
            result.setReport(toReport(request.getReportId()));
        }
        if (request.getFinalRound() != null) {
            result.setFinalRound(request.getFinalRound());
        }
        if (request.getFinishPosition() != null) {
            result.setFinishPosition(request.getFinishPosition());
        }
        if (request.getFinishTimeSec() != null) {
            result.setFinishTimeSec(request.getFinishTimeSec());
        }
        if (request.getPointsAwarded() != null) {
            result.setPointsAwarded(request.getPointsAwarded());
        }
        if (request.getIsDisqualified() != null) {
            result.setIsDisqualified(request.getIsDisqualified());
        }
        if (request.getDisqualifyReason() != null) {
            result.setDisqualifyReason(request.getDisqualifyReason().trim());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            result.setStatus(request.getStatus().trim());
        }
        if (request.getRecordedAt() != null) {
            result.setRecordedAt(request.getRecordedAt());
        }
        if (request.getPublishedAt() != null) {
            result.setPublishedAt(request.getPublishedAt());
        }
    }

    public RaceResultResponse toResponse(RaceResults result) {
        return RaceResultResponse.builder()
                .id(result.getId())
                .assignmentId(result.getAssignment().getId())
                .raceId(result.getRaces().getId())
                .horseId(result.getHorses().getId())
                .ownerId(result.getOwner().getId())
                .reportId(result.getReport() == null ? null : result.getReport().getId())
                .finalRound(result.getFinalRound())
                .finishPosition(result.getFinishPosition())
                .finishTimeSec(result.getFinishTimeSec())
                .pointsAwarded(result.getPointsAwarded())
                .isDisqualified(result.getIsDisqualified())
                .disqualifyReason(result.getDisqualifyReason())
                .status(result.getStatus())
                .recordedAt(result.getRecordedAt())
                .publishedAt(result.getPublishedAt())
                .build();
    }

    private JockeyHorseAssignments toAssignment(Integer id) {
        JockeyHorseAssignments assignment = new JockeyHorseAssignments();
        assignment.setId(id);
        return assignment;
    }

    private Races toRace(Integer id) {
        Races race = new Races();
        race.setId(id);
        return race;
    }

    private Horses toHorse(Integer id) {
        Horses horse = new Horses();
        horse.setId(id);
        return horse;
    }

    private HorseOwnerProfiles toOwner(Integer id) {
        HorseOwnerProfiles owner = new HorseOwnerProfiles();
        owner.setId(id);
        return owner;
    }

    private RefereeReports toReport(Integer id) {
        RefereeReports report = new RefereeReports();
        report.setId(id);
        return report;
    }

    private RefereeReports toNullableReport(Integer id) {
        return id == null ? null : toReport(id);
    }

    private Integer defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    private String defaultText(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private Instant defaultInstant(Instant value) {
        return value == null ? Instant.now() : value;
    }
}
