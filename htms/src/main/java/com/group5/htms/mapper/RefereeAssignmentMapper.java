package com.group5.htms.mapper;

import com.group5.htms.dto.refereeassignment.request.RefereeAssignmentCreateRequest;
import com.group5.htms.dto.refereeassignment.response.RefereeAssignmentResponse;
import com.group5.htms.entity.RaceRefereeAssignments;
import com.group5.htms.entity.Races;
import com.group5.htms.entity.RefereeProfiles;
import com.group5.htms.entity.Users;
import org.springframework.stereotype.Component;

@Component
public class RefereeAssignmentMapper {

    public RaceRefereeAssignments toEntity(RefereeAssignmentCreateRequest request) {
        if (request == null) {
            return null;
        }

        return RaceRefereeAssignments.builder()
                .races(toRaceShell(request.getRaceId()))
                .referee(toRefereeShell(request.getRefereeId()))
                .refereeRole(clean(request.getRefereeRole()))
                .build();
    }

    public RefereeAssignmentResponse toResponse(RaceRefereeAssignments assignment) {
        if (assignment == null) {
            return null;
        }

        Races race = assignment.getRaces();
        RefereeProfiles referee = assignment.getReferee();
        Users user = referee != null ? referee.getUsers() : null;

        return RefereeAssignmentResponse.builder()
                .id(assignment.getId())
                .refAssignId(assignment.getId())
                .raceId(race != null ? race.getId() : null)
                .raceName(race != null ? race.getName() : null)
                .refereeId(referee != null ? referee.getId() : null)
                .refereeUserId(user != null ? user.getId() : null)
                .refereeUsername(user != null ? user.getUsername() : null)
                .refereeFullName(user != null ? user.getFullName() : null)
                .refereeRole(assignment.getRefereeRole())
                .assignedAt(assignment.getAssignedAt())
                .build();
    }

    private Races toRaceShell(Integer raceId) {
        if (raceId == null) {
            return null;
        }

        Races race = new Races();
        race.setId(raceId);
        return race;
    }

    private RefereeProfiles toRefereeShell(Integer refereeId) {
        if (refereeId == null) {
            return null;
        }

        RefereeProfiles referee = new RefereeProfiles();
        referee.setId(refereeId);
        return referee;
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }

        String cleaned = value.trim();

        return cleaned.isBlank() ? null : cleaned;
    }
}
