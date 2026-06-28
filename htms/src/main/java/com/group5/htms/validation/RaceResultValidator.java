package com.group5.htms.validation;

import com.group5.htms.dto.raceresult.request.RaceResultCreateRequest;
import com.group5.htms.dto.raceresult.request.RaceResultDraftItemRequest;
import com.group5.htms.dto.raceresult.request.RaceResultDraftRequest;
import com.group5.htms.dto.raceresult.request.RaceResultUpdateRequest;
import com.group5.htms.entity.JockeyHorseAssignments;
import com.group5.htms.entity.RaceRefereeAssignments;
import com.group5.htms.entity.RaceResults;
import com.group5.htms.entity.Races;
import com.group5.htms.entity.RefereeReports;
import com.group5.htms.entity.Users;
import com.group5.htms.enums.RaceResultStatus;
import com.group5.htms.enums.RaceStatus;
import com.group5.htms.enums.RoleType;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.exception.ResourceNotFoundException;
import com.group5.htms.repository.RaceResultsRepository;
import com.group5.htms.repository.RefereeReportsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RaceResultValidator {
    private static final String ROLE_CHIEF_REFEREE = "chief_referee";
    private static final String ROLE_MAIN_REFEREE = "main_referee";

    private final RaceResultsRepository raceResultsRepository;
    private final RefereeReportsRepository refereeReportsRepository;

    public void ensureNoManagedCreateFields(RaceResultCreateRequest request) {
        if (hasText(request.getStatus()) || request.getRecordedAt() != null || request.getPublishedAt() != null) {
            throw new BadRequestException("Use workflow transition APIs to update result status");
        }
    }

    public void ensureNoManagedUpdateFields(RaceResultUpdateRequest request) {
        if (hasText(request.getStatus()) || request.getRecordedAt() != null || request.getPublishedAt() != null) {
            throw new BadRequestException("Use workflow transition APIs to update result status");
        }
    }

    public void validateSingleResult(RaceResults result) {
        if (result.getAssignment() == null || result.getAssignment().getId() == null) {
            throw new BadRequestException("Assignment does not belong to this race");
        }
        if (Boolean.TRUE.equals(result.getIsDisqualified())) {
            result.setPointsAwarded(0);
            if (!hasText(result.getDisqualifyReason())) {
                throw new BadRequestException("Disqualification reason is required");
            }
            result.setFinishPosition(null);
            return;
        }
        if (result.getFinishPosition() == null) {
            throw new BadRequestException("Finish position is required for non-disqualified result");
        }
        result.setPointsAwarded(0);
        result.setStatus(RaceResultStatus.DRAFT.getValue());
    }

    public void validateDraftRequest(RaceResultDraftRequest request) {
        if (request == null || request.getResults() == null || request.getResults().isEmpty()) {
            throw new BadRequestException("Results are required");
        }
    }

    public void validateDraftItems(
            List<RaceResultDraftItemRequest> items,
            List<JockeyHorseAssignments> confirmedAssignments
    ) {
        Set<Integer> confirmedAssignmentIds = new HashSet<>();
        confirmedAssignments.forEach(assignment -> confirmedAssignmentIds.add(assignment.getId()));

        if (items.size() != confirmedAssignmentIds.size()) {
            throw new BadRequestException("Every confirmed assignment must have a result");
        }

        Set<Integer> assignmentIds = new HashSet<>();
        Set<Integer> finishPositions = new HashSet<>();
        for (RaceResultDraftItemRequest item : items) {
            if (item == null || item.getAssignmentId() == null) {
                throw new BadRequestException("Assignment does not belong to this race");
            }
            if (!assignmentIds.add(item.getAssignmentId())) {
                throw new BadRequestException("Duplicate result for assignment");
            }
            if (!confirmedAssignmentIds.contains(item.getAssignmentId())) {
                throw new BadRequestException("Assignment does not belong to this race");
            }

            boolean disqualified = Boolean.TRUE.equals(item.getIsDisqualified());
            if (disqualified) {
                if (!hasText(item.getDisqualifyReason())) {
                    throw new BadRequestException("Disqualification reason is required");
                }
                continue;
            }

            if (item.getFinishPosition() == null) {
                throw new BadRequestException("Finish position is required for non-disqualified result");
            }
            if (!finishPositions.add(item.getFinishPosition())) {
                throw new BadRequestException("Duplicate finish position");
            }
        }
    }

    public void validateResultCompleteness(List<JockeyHorseAssignments> confirmedAssignments, List<RaceResults> results) {
        if (confirmedAssignments.size() != results.size()) {
            throw new BadRequestException("Every confirmed assignment must have a result");
        }

        Set<Integer> assignmentIds = new HashSet<>();
        confirmedAssignments.forEach(assignment -> assignmentIds.add(assignment.getId()));
        for (RaceResults result : results) {
            if (result.getAssignment() == null
                    || result.getAssignment().getId() == null
                    || !assignmentIds.remove(result.getAssignment().getId())) {
                throw new BadRequestException("Every confirmed assignment must have a result");
            }
        }
        if (!assignmentIds.isEmpty()) {
            throw new BadRequestException("Every confirmed assignment must have a result");
        }
    }

    public void validateResultPositions(List<RaceResults> results) {
        Set<Integer> positions = new HashSet<>();
        for (RaceResults result : results) {
            boolean disqualified = Boolean.TRUE.equals(result.getIsDisqualified());
            if (disqualified) {
                result.setPointsAwarded(0);
                if (!hasText(result.getDisqualifyReason())) {
                    throw new BadRequestException("Disqualification reason is required");
                }
                continue;
            }

            if (result.getFinishPosition() == null) {
                throw new BadRequestException("Finish position is required for non-disqualified result");
            }
            if (!positions.add(result.getFinishPosition())) {
                throw new BadRequestException("Duplicate finish position");
            }
        }

        for (int position = 1; position <= positions.size(); position++) {
            if (!positions.contains(position)) {
                throw new BadRequestException("Every non-disqualified result must have a continuous finish position");
            }
        }
    }

    public RaceResults ensureExactlyOneValidWinner(List<RaceResults> results, String message) {
        List<RaceResults> winners = results.stream()
                .filter(result -> !Boolean.TRUE.equals(result.getIsDisqualified()))
                .filter(result -> Integer.valueOf(1).equals(result.getFinishPosition()))
                .toList();
        if (winners.size() != 1) {
            throw new BadRequestException(message);
        }
        return winners.get(0);
    }

    public void ensureCurrentUserIsRaceReferee(Users user) {
        if (user == null
                || user.getId() == null
                || user.getRoleType() == null
                || !RoleType.RACE_REFEREE.getValue().equalsIgnoreCase(user.getRoleType().trim())) {
            throw new BadRequestException("Current user must be race referee");
        }
    }

    public void ensureChiefOrMainReferee(RaceRefereeAssignments assignment) {
        String role = cleanLower(assignment.getRefereeRole());
        if (!ROLE_CHIEF_REFEREE.equals(role) && !ROLE_MAIN_REFEREE.equals(role)) {
            throw new BadRequestException("Only chief or main referee can submit race results");
        }
    }

    public void ensureRaceInProgressForResults(Races race) {
        if (!RaceStatus.IN_PROGRESS.equalsValue(race.getStatus())) {
            throw new BadRequestException("Race must be in progress to submit results");
        }
    }

    public void ensureNoPublishedResults(Integer raceId) {
        if (raceResultsRepository.existsByRaces_IdAndStatusIgnoreCase(raceId, RaceResultStatus.PUBLISHED.getValue())) {
            throw new BadRequestException("Race results are already published");
        }
    }

    public void ensureNotPublished(RaceResults result) {
        if (RaceResultStatus.PUBLISHED.equalsValue(result.getStatus())) {
            throw new BadRequestException("Race results are already published");
        }
    }

    public void ensureReportBelongsToRaceAndReferee(RefereeReports report, Integer raceId, Integer refereeId) {
        if (report.getRaces() == null || !Objects.equals(report.getRaces().getId(), raceId)) {
            throw new BadRequestException("Report does not belong to this race");
        }
        if (report.getReferee() == null || !Objects.equals(report.getReferee().getId(), refereeId)) {
            throw new BadRequestException("Report does not belong to this referee");
        }
    }

    public void ensureReportExists(Integer id) {
        if (!refereeReportsRepository.existsById(id)) {
            throw new ResourceNotFoundException("Referee report not found");
        }
    }

    private String cleanLower(String value) {
        String cleaned = clean(value);
        return cleaned == null ? null : cleaned.toLowerCase();
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }

        String cleaned = value.trim();
        return cleaned.isBlank() ? null : cleaned;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
