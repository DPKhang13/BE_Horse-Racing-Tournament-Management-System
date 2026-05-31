package com.group5.htms.service.impl;

import com.group5.htms.exception.ResourceNotFoundException;
import com.group5.htms.dto.jockeyassignment.request.JockeyInvitationCreateRequest;
import com.group5.htms.dto.jockeyassignment.request.JockeyInvitationResponseRequest;
import com.group5.htms.dto.jockeyassignment.request.JockeyInvitationUpdateRequest;
import com.group5.htms.dto.jockeyassignment.response.JockeyAssignmentResponse;
import com.group5.htms.entity.JockeyHorseAssignments;
import com.group5.htms.mapper.JockeyAssignmentMapper;
import com.group5.htms.repository.BetsRepository;
import com.group5.htms.repository.JockeyHorseAssignmentsRepository;
import com.group5.htms.repository.RaceResultsRepository;
import com.group5.htms.repository.RaceRegistrationsRepository;
import com.group5.htms.repository.RacesRepository;
import com.group5.htms.repository.RolesRepository;
import com.group5.htms.service.AuthService;
import com.group5.htms.service.JockeyAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class JockeyAssignmentServiceImpl implements JockeyAssignmentService {
    private static final String ROLE_HORSE_OWNER = "horse_owner";
    private static final String ROLE_JOCKEY = "jockey";

    private final JockeyHorseAssignmentsRepository jockeyHorseAssignmentsRepository;
    private final RaceResultsRepository raceResultsRepository;
    private final BetsRepository betsRepository;
    private final RaceRegistrationsRepository raceRegistrationsRepository;
    private final RacesRepository racesRepository;
    private final RolesRepository rolesRepository;
    private final AuthService authService;
    private final JockeyAssignmentMapper jockeyAssignmentMapper;

    @Override
    public List<JockeyAssignmentResponse> getAllAssignments() {
        return jockeyHorseAssignmentsRepository.findAll()
                .stream()
                .map(jockeyAssignmentMapper::toResponse)
                .toList();
    }

    @Override
    public JockeyAssignmentResponse getAssignmentById(Integer id) {
        return jockeyAssignmentMapper.toResponse(findAssignment(id));
    }

    @Override
    @Transactional
    public JockeyAssignmentResponse createInvitation(JockeyInvitationCreateRequest request) {
        validateCreateReferences(request);
        validateRegistrationBelongsToCurrentOwner(request.getRegistrationId());
        JockeyHorseAssignments assignment = jockeyAssignmentMapper.toEntity(request);

        return jockeyAssignmentMapper.toResponse(jockeyHorseAssignmentsRepository.save(assignment));
    }

    @Override
    @Transactional
    public JockeyAssignmentResponse updateInvitation(Integer id, JockeyInvitationUpdateRequest request) {
        JockeyHorseAssignments assignment = findAssignmentForCurrentOwner(id);
        validateUpdateReferences(request);
        if (request.getRegistrationId() != null) {
            validateRegistrationBelongsToCurrentOwner(request.getRegistrationId());
        }
        jockeyAssignmentMapper.updateAssignment(assignment, request);

        return jockeyAssignmentMapper.toResponse(jockeyHorseAssignmentsRepository.save(assignment));
    }

    @Override
    @Transactional
    public JockeyAssignmentResponse respondInvitation(Integer id, JockeyInvitationResponseRequest request) {
        JockeyHorseAssignments assignment = findAssignmentForCurrentJockey(id);

        assignment.setStatus(request.getStatus().trim());
        assignment.setRespondedAt(request.getRespondedAt() == null ? Instant.now() : request.getRespondedAt());

        return jockeyAssignmentMapper.toResponse(jockeyHorseAssignmentsRepository.save(assignment));
    }

    @Override
    @Transactional
    public void deleteAssignment(Integer id) {
        JockeyHorseAssignments assignment = findAssignmentForCurrentOwner(id);
        betsRepository.deleteByAssignment_Id(id);
        raceResultsRepository.deleteByAssignment_Id(id);
        jockeyHorseAssignmentsRepository.delete(assignment);
    }

    private JockeyHorseAssignments findAssignment(Integer id) {
        return jockeyHorseAssignmentsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jockey assignment not found"));
    }

    private JockeyHorseAssignments findAssignmentForCurrentOwner(Integer id) {
        JockeyHorseAssignments assignment = findAssignment(id);
        Integer ownerRoleId = authService.getCurrentUserRoleId(ROLE_HORSE_OWNER);

        if (!Objects.equals(assignment.getReg().getOwnerRoles().getId(), ownerRoleId)) {
            throw new AccessDeniedException("You do not own this jockey assignment");
        }

        return assignment;
    }

    private JockeyHorseAssignments findAssignmentForCurrentJockey(Integer id) {
        JockeyHorseAssignments assignment = findAssignment(id);
        Integer jockeyRoleId = authService.getCurrentUserRoleId(ROLE_JOCKEY);

        if (!Objects.equals(assignment.getJockeyRoles().getId(), jockeyRoleId)) {
            throw new AccessDeniedException("This invitation is not assigned to you");
        }

        return assignment;
    }

    private void validateCreateReferences(JockeyInvitationCreateRequest request) {
        validateRegistrationExists(request.getRegistrationId());
        validateRaceExists(request.getRaceId());
        validateRoleExists(request.getJockeyRoleId());
    }

    private void validateUpdateReferences(JockeyInvitationUpdateRequest request) {
        if (request.getRegistrationId() != null) {
            validateRegistrationExists(request.getRegistrationId());
        }
        if (request.getRaceId() != null) {
            validateRaceExists(request.getRaceId());
        }
        if (request.getJockeyRoleId() != null) {
            validateRoleExists(request.getJockeyRoleId());
        }
    }

    private void validateRegistrationExists(Integer id) {
        if (!raceRegistrationsRepository.existsById(id)) {
            throw new ResourceNotFoundException("Race registration not found");
        }
    }

    private void validateRegistrationBelongsToCurrentOwner(Integer id) {
        Integer ownerRoleId = authService.getCurrentUserRoleId(ROLE_HORSE_OWNER);
        boolean belongsToOwner = raceRegistrationsRepository.findById(id)
                .map(registration -> Objects.equals(registration.getOwnerRoles().getId(), ownerRoleId))
                .orElse(false);

        if (!belongsToOwner) {
            throw new AccessDeniedException("You do not own this race registration");
        }
    }

    private void validateRaceExists(Integer id) {
        if (!racesRepository.existsById(id)) {
            throw new ResourceNotFoundException("Race not found");
        }
    }

    private void validateRoleExists(Integer id) {
        if (!rolesRepository.existsById(id)) {
            throw new ResourceNotFoundException("Jockey role not found");
        }
    }
}
