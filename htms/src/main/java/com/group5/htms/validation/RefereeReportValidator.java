package com.group5.htms.validation;

import com.group5.htms.dto.refereereport.request.RefereeReportCreateRequest;
import com.group5.htms.entity.Races;
import com.group5.htms.entity.Users;
import com.group5.htms.enums.RaceStatus;
import com.group5.htms.enums.RoleType;
import com.group5.htms.exception.BadRequestException;
import org.springframework.stereotype.Component;

@Component
public class RefereeReportValidator {
    private static final String VERDICT_CLEAN = "clean";
    private static final String VERDICT_VIOLATION = "violation";

    public void ensureReportRequestExists(RefereeReportCreateRequest request) {
        if (request == null) {
            throw new BadRequestException("Report request is required");
        }
    }

    public void ensureCurrentUserIsRaceReferee(Users user) {
        if (user == null
                || user.getId() == null
                || user.getRoleType() == null
                || !RoleType.RACE_REFEREE.getValue().equalsIgnoreCase(user.getRoleType().trim())) {
            throw new BadRequestException("Current user must be race referee");
        }
    }

    public void ensureRaceInProgressForReport(Races race) {
        if (!RaceStatus.IN_PROGRESS.equalsValue(race.getStatus())) {
            throw new BadRequestException("Race must be in progress to submit report");
        }
    }

    public void validateVerdict(String verdict, String violationNotes) {
        if (!VERDICT_CLEAN.equals(verdict) && !VERDICT_VIOLATION.equals(verdict)) {
            throw new BadRequestException("Invalid report verdict");
        }

        if (VERDICT_VIOLATION.equals(verdict) && !hasText(violationNotes)) {
            throw new BadRequestException("Violation notes are required when verdict is violation");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
