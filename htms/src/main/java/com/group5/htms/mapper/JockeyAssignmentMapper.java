package com.group5.htms.mapper;

import com.group5.htms.dto.jockeyassignment.request.JockeyInvitationCreateRequest;
import com.group5.htms.dto.jockeyassignment.request.JockeyInvitationUpdateRequest;
import com.group5.htms.dto.jockeyassignment.response.JockeyAssignmentResponse;
import com.group5.htms.entity.JockeyHorseAssignments;
import com.group5.htms.entity.RaceRegistrations;
import com.group5.htms.entity.Races;
import com.group5.htms.entity.Roles;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class JockeyAssignmentMapper {
    public JockeyHorseAssignments toEntity(JockeyInvitationCreateRequest request) {
        return JockeyHorseAssignments.builder()
                .reg(toRegistration(request.getRegistrationId()))
                .races(toRace(request.getRaceId()))
                .jockeyRoles(toRole(request.getJockeyRoleId()))
                .gateNumber(request.getGateNumber())
                .status(defaultText(request.getStatus(), "pending"))
                .invitedAt(defaultInstant(request.getInvitedAt()))
                .respondedAt(request.getRespondedAt())
                .build();
    }

    public void updateAssignment(JockeyHorseAssignments assignment, JockeyInvitationUpdateRequest request) {
        if (request.getRegistrationId() != null) {
            assignment.setReg(toRegistration(request.getRegistrationId()));
        }
        if (request.getRaceId() != null) {
            assignment.setRaces(toRace(request.getRaceId()));
        }
        if (request.getJockeyRoleId() != null) {
            assignment.setJockeyRoles(toRole(request.getJockeyRoleId()));
        }
        if (request.getGateNumber() != null) {
            assignment.setGateNumber(request.getGateNumber());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            assignment.setStatus(request.getStatus().trim());
        }
        if (request.getInvitedAt() != null) {
            assignment.setInvitedAt(request.getInvitedAt());
        }
        if (request.getRespondedAt() != null) {
            assignment.setRespondedAt(request.getRespondedAt());
        }
    }

    public JockeyAssignmentResponse toResponse(JockeyHorseAssignments assignment) {
        return JockeyAssignmentResponse.builder()
                .id(assignment.getId())
                .registrationId(assignment.getReg().getId())
                .raceId(assignment.getRaces().getId())
                .jockeyRoleId(assignment.getJockeyRoles().getId())
                .gateNumber(assignment.getGateNumber())
                .status(assignment.getStatus())
                .invitedAt(assignment.getInvitedAt())
                .respondedAt(assignment.getRespondedAt())
                .build();
    }

    private RaceRegistrations toRegistration(Integer id) {
        RaceRegistrations registration = new RaceRegistrations();
        registration.setId(id);
        return registration;
    }

    private Races toRace(Integer id) {
        Races race = new Races();
        race.setId(id);
        return race;
    }

    private Roles toRole(Integer id) {
        Roles role = new Roles();
        role.setId(id);
        return role;
    }

    private String defaultText(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private Instant defaultInstant(Instant value) {
        return value == null ? Instant.now() : value;
    }
}
