package com.group5.htms.validation;

import com.group5.htms.dto.jockeyassignment.request.JockeyInvitationUpdateRequest;
import com.group5.htms.entity.JockeyHorseAssignments;
import com.group5.htms.entity.JockeyProfiles;
import com.group5.htms.entity.RaceRegistrations;
import com.group5.htms.entity.Races;
import com.group5.htms.enums.JockeyAssignmentStatus;
import com.group5.htms.enums.JockeyStatus;
import com.group5.htms.enums.RaceRegistrationStatus;
import com.group5.htms.enums.RaceStatus;
import com.group5.htms.enums.RoleStatus;
import com.group5.htms.enums.RoleType;
import com.group5.htms.enums.TournamentStatus;
import com.group5.htms.enums.UserStatus;
import com.group5.htms.exception.BadRequestException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Component
public class JockeyAssignmentValidator {
    public void ensureOwnerCanManageRegistration(RaceRegistrations registration, Integer ownerId) {
        if (registration == null || registration.getOwner() == null
                || !Objects.equals(registration.getOwner().getId(), ownerId)) {
            throw new AccessDeniedException("You do not own this race registration");
        }
    }

    public void ensureJockeyCanRespond(JockeyHorseAssignments assignment, Integer jockeyId) {
        if (assignment == null || assignment.getJockey() == null
                || !Objects.equals(assignment.getJockey().getId(), jockeyId)) {
            throw new AccessDeniedException("This invitation is not assigned to you");
        }
    }

    public void ensureRaceMatchesRegistration(Races race, RaceRegistrations registration) {
        if (race == null || registration == null || !Objects.equals(registration.getRaces().getId(), race.getId())) {
            throw new BadRequestException("Race does not match this registration");
        }
    }

    public void ensureRegistrationCanInviteJockey(RaceRegistrations registration) {
        if (!RaceRegistrationStatus.APPROVED.equalsValue(registration.getStatus())) {
            throw new BadRequestException("Only approved registrations can invite jockeys");
        }
    }

    public void ensureRegistrationStillOpen(RaceRegistrations registration, Races race) {
        if (!TournamentStatus.REGISTRATION_OPEN.equalsValue(registration.getTournaments().getStatus())) {
            throw new BadRequestException("Registration is not open for this tournament");
        }
        if (!RaceStatus.REGISTRATION_OPEN.equalsValue(race.getStatus())) {
            throw new BadRequestException("Registration is not open for this race");
        }
    }

    public void ensureJockeyCanBeInvited(JockeyProfiles jockey) {
        if (jockey == null || jockey.getUsers() == null
                || !RoleType.JOCKEY.getValue().equalsIgnoreCase(jockey.getUsers().getRoleType())) {
            throw new BadRequestException("Jockey profile not found");
        }
        if (!UserStatus.ACTIVE.getValue().equalsIgnoreCase(jockey.getUsers().getStatus())) {
            throw new BadRequestException("Jockey user account is not active");
        }
        if (!RoleStatus.ACTIVE.getValue().equalsIgnoreCase(jockey.getStatus())
                && !JockeyStatus.AVAILABLE.getValue().equalsIgnoreCase(jockey.getStatus())) {
            throw new BadRequestException("Jockey profile is not active");
        }
    }

    public void ensureNoWorkflowFields(JockeyInvitationUpdateRequest request) {
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            throw new BadRequestException("Use workflow transition APIs to update status");
        }
        if (request.getInvitedAt() != null || request.getRespondedAt() != null) {
            throw new BadRequestException("Workflow timestamps are managed by backend");
        }
    }

    public void ensurePending(JockeyHorseAssignments assignment, String message) {
        if (!JockeyAssignmentStatus.PENDING.equalsValue(assignment.getStatus())) {
            throw new BadRequestException(message);
        }
    }

    public void ensureAccepted(JockeyHorseAssignments assignment, String message) {
        if (!JockeyAssignmentStatus.ACCEPTED.equalsValue(assignment.getStatus())) {
            throw new BadRequestException(message);
        }
    }

    public String normalizeResponseStatus(String status) {
        if (!JockeyAssignmentStatus.ACCEPTED.equalsValue(status)
                && !JockeyAssignmentStatus.REJECTED.equalsValue(status)) {
            throw new BadRequestException("Invalid response status");
        }
        return status.trim().toLowerCase();
    }

    public void ensureNoActiveAssignment(List<JockeyHorseAssignments> assignments, Instant now, String message) {
        boolean hasActiveAssignment = assignments.stream().anyMatch(assignment -> isStillActive(assignment, now));
        if (hasActiveAssignment) {
            throw new BadRequestException(message);
        }
    }

    public void ensureNoActiveAssignmentExcept(
            List<JockeyHorseAssignments> assignments,
            Integer assignmentId,
            Instant now,
            String message
    ) {
        boolean hasActiveAssignment = assignments.stream()
                .filter(assignment -> !Objects.equals(assignment.getId(), assignmentId))
                .anyMatch(assignment -> isStillActive(assignment, now));
        if (hasActiveAssignment) {
            throw new BadRequestException(message);
        }
    }

    public boolean isStillActive(JockeyHorseAssignments assignment, Instant now) {
        if (JockeyAssignmentStatus.PENDING.equalsValue(assignment.getStatus())) {
            return !isPendingExpired(assignment, now);
        }

        return JockeyAssignmentStatus.ACCEPTED.equalsValue(assignment.getStatus())
                || JockeyAssignmentStatus.CONFIRMED.equalsValue(assignment.getStatus());
    }

    public boolean isPendingExpired(JockeyHorseAssignments assignment, Instant now) {
        Instant responseDeadline = assignment.getResponseDeadline();
        return responseDeadline != null && !now.isBefore(responseDeadline);
    }
}