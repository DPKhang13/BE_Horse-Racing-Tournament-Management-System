package com.group5.htms.service.impl;

import com.group5.htms.common.exceptions.ResourceNotFoundException;
import com.group5.htms.dto.jockeyassignment.request.JockeyInvitationCreateRequest;
import com.group5.htms.dto.jockeyassignment.request.JockeyInvitationResponseRequest;
import com.group5.htms.dto.jockeyassignment.request.JockeyInvitationUpdateRequest;
import com.group5.htms.dto.jockeyassignment.response.JockeyAssignmentResponse;
import com.group5.htms.entity.JockeyHorseAssignments;
import com.group5.htms.mapper.JockeyAssignmentMapper;
import com.group5.htms.repository.JockeyHorseAssignmentsRepository;
import com.group5.htms.repository.RaceRegistrationsRepository;
import com.group5.htms.repository.RacesRepository;
import com.group5.htms.repository.RolesRepository;
import com.group5.htms.service.JockeyAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JockeyAssignmentServiceImpl implements JockeyAssignmentService {
    private final JockeyHorseAssignmentsRepository jockeyHorseAssignmentsRepository;
    private final RaceRegistrationsRepository raceRegistrationsRepository;
    private final RacesRepository racesRepository;
    private final RolesRepository rolesRepository;
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
        JockeyHorseAssignments assignment = jockeyAssignmentMapper.toEntity(request);

        return jockeyAssignmentMapper.toResponse(jockeyHorseAssignmentsRepository.save(assignment));
    }

    @Override
    @Transactional
    public JockeyAssignmentResponse updateInvitation(Integer id, JockeyInvitationUpdateRequest request) {
        JockeyHorseAssignments assignment = findAssignment(id);
        validateUpdateReferences(request);
        jockeyAssignmentMapper.updateAssignment(assignment, request);

        return jockeyAssignmentMapper.toResponse(jockeyHorseAssignmentsRepository.save(assignment));
    }

    @Override
    @Transactional
    public JockeyAssignmentResponse respondInvitation(Integer id, JockeyInvitationResponseRequest request) {
        JockeyHorseAssignments assignment = findAssignment(id);

        assignment.setStatus(request.getStatus().trim());
        assignment.setRespondedAt(request.getRespondedAt() == null ? Instant.now() : request.getRespondedAt());

        return jockeyAssignmentMapper.toResponse(jockeyHorseAssignmentsRepository.save(assignment));
    }

    @Override
    @Transactional
    public void deleteAssignment(Integer id) {
        JockeyHorseAssignments assignment = findAssignment(id);
        jockeyHorseAssignmentsRepository.delete(assignment);
    }

    private JockeyHorseAssignments findAssignment(Integer id) {
        return jockeyHorseAssignmentsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jockey assignment not found"));
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
