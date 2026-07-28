package com.group5.htms.service.impl;

import com.group5.htms.entity.RaceRefereeAssignments;
import com.group5.htms.entity.RefereeProfiles;
import com.group5.htms.entity.Users;
import com.group5.htms.enums.RoleType;
import com.group5.htms.enums.RoleStatus;
import com.group5.htms.enums.UserStatus;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.exception.ResourceNotFoundException;
import com.group5.htms.repository.RaceRefereeAssignmentsRepository;
import com.group5.htms.repository.RefereeProfilesRepository;
import com.group5.htms.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class RefereeRaceAuthorizationService {
    public static final String ROLE_CHIEF_REFEREE = "chief_referee";
    public static final String ROLE_MAIN_REFEREE = "main_referee";

    private final AuthService authService;
    private final RefereeProfilesRepository refereeProfilesRepository;
    private final RaceRefereeAssignmentsRepository raceRefereeAssignmentsRepository;

    public RefereeProfiles getCurrentReferee() {
        Users user = authService.getCurrentUser();
        if (user == null
                || user.getId() == null
                || user.getRoleType() == null
                || !RoleType.RACE_REFEREE.getValue().equalsIgnoreCase(user.getRoleType().trim())) {
            throw new BadRequestException("Current user must be race referee");
        }

        if (user.getStatus() == null || !UserStatus.ACTIVE.getValue().equalsIgnoreCase(user.getStatus().trim())) {
            throw new BadRequestException("Current referee user account is not active");
        }

        RefereeProfiles referee = refereeProfilesRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Referee profile not found"));
        if (referee.getStatus() == null || !RoleStatus.ACTIVE.getValue().equalsIgnoreCase(referee.getStatus().trim())) {
            throw new BadRequestException("Current referee profile is not active");
        }
        return referee;
    }

    public RaceRefereeAssignments requireAssignedReferee(Integer raceId) {
        RefereeProfiles referee = getCurrentReferee();
        return findAssignment(raceId, referee.getId(), "Only assigned referees can access this race");
    }

    public RaceRefereeAssignments requireChiefReferee(Integer raceId) {
        return requireRole(raceId, ROLE_CHIEF_REFEREE);
    }

    public RaceRefereeAssignments requireMainReferee(Integer raceId) {
        return requireRole(raceId, ROLE_MAIN_REFEREE);
    }

    private RaceRefereeAssignments requireRole(Integer raceId, String requiredRole) {
        RefereeProfiles referee = getCurrentReferee();
        RaceRefereeAssignments assignment = findAssignment(
                raceId,
                referee.getId(),
                "Only the " + requiredRole.replace('_', ' ') + " assigned to this race can perform this action"
        );

        String actualRole = assignment.getRefereeRole() == null
                ? null
                : assignment.getRefereeRole().trim().toLowerCase(Locale.ROOT);
        if (!requiredRole.equals(actualRole)) {
            throw new BadRequestException(
                    "Only the " + requiredRole.replace('_', ' ') + " assigned to this race can perform this action"
            );
        }
        return assignment;
    }

    private RaceRefereeAssignments findAssignment(Integer raceId, Integer refereeId, String message) {
        if (raceId == null) {
            throw new BadRequestException("Race id is required");
        }
        return raceRefereeAssignmentsRepository.findByRaces_IdAndReferee_Id(raceId, refereeId)
                .orElseThrow(() -> new BadRequestException(message));
    }
}
