package com.group5.htms.service.impl;

import com.group5.htms.dto.jockeyassignment.request.JockeyInvitationCreateRequest;
import com.group5.htms.dto.jockeyassignment.request.JockeyInvitationResponseRequest;
import com.group5.htms.dto.jockeyassignment.request.JockeyInvitationUpdateRequest;
import com.group5.htms.dto.jockeyassignment.response.JockeyAssignmentListResponse;
import com.group5.htms.dto.jockeyassignment.response.JockeyAssignmentResponse;
import com.group5.htms.entity.JockeyHorseAssignments;
import com.group5.htms.entity.JockeyProfiles;
import com.group5.htms.entity.RaceRegistrations;
import com.group5.htms.entity.Races;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.exception.ResourceNotFoundException;
import com.group5.htms.mapper.JockeyAssignmentMapper;
import com.group5.htms.repository.JockeyProfilesRepository;
import com.group5.htms.repository.JockeyHorseAssignmentsRepository;
import com.group5.htms.repository.RaceRegistrationsRepository;
import com.group5.htms.repository.RacesRepository;
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
    private static final String STATUS_DELETED = "deleted";
    private static final String STATUS_ACCEPTED = "accepted";
    private static final String JOCKEY_STATUS_UNAVAILABLE = "unavailable";

    private final JockeyHorseAssignmentsRepository jockeyHorseAssignmentsRepository;
    private final RaceRegistrationsRepository raceRegistrationsRepository;
    private final RacesRepository racesRepository;
    private final JockeyProfilesRepository jockeyProfilesRepository;
    private final AuthService authService;
    private final JockeyAssignmentMapper jockeyAssignmentMapper;

    @Override
    public List<JockeyAssignmentListResponse> getAllAssignments() {
        return jockeyHorseAssignmentsRepository.findAll()
                .stream()
                .filter(assignment -> !isDeleted(assignment.getStatus()))
                .map(jockeyAssignmentMapper::toListResponse)
                .toList();
    }

    @Override
    public List<JockeyAssignmentListResponse> getMyInvitations(String status) {
        Integer jockeyId = authService.getCurrentUserId();

        return findAssignmentsByJockey(jockeyId, status)
                .stream()
                .filter(assignment -> !isDeleted(assignment.getStatus()))
                .map(jockeyAssignmentMapper::toListResponse)
                .toList();
    }

    @Override
    public List<JockeyAssignmentListResponse> getSentInvitations(String status) {
        Integer ownerId = authService.getCurrentUserId();

        return findAssignmentsByOwner(ownerId, status)
                .stream()
                .filter(assignment -> !isDeleted(assignment.getStatus()))
                .map(jockeyAssignmentMapper::toListResponse)
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
        validateRaceMatchesRegistration(request.getRaceId(), request.getRegistrationId());
        validateRaceJockeyAvailable(request.getRaceId(), request.getJockeyId());
        validateRaceGateAvailable(request.getRaceId(), request.getGateNumber());
        JockeyHorseAssignments assignment = jockeyAssignmentMapper.toEntity(request);
        attachCreateReferences(assignment, request);

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
        Integer registrationId = request.getRegistrationId() == null
                ? assignment.getReg().getId()
                : request.getRegistrationId();
        Integer raceId = request.getRaceId() == null
                ? assignment.getRaces().getId()
                : request.getRaceId();
        Integer jockeyId = request.getJockeyId() == null
                ? assignment.getJockey().getId()
                : request.getJockeyId();
        Integer gateNumber = request.getGateNumber() == null
                ? assignment.getGateNumber()
                : request.getGateNumber();
        validateRaceMatchesRegistration(raceId, registrationId);
        validateRaceJockeyAvailableForUpdate(raceId, jockeyId, id);
        validateRaceGateAvailableForUpdate(raceId, gateNumber, id);
        jockeyAssignmentMapper.updateAssignment(assignment, request);

        return jockeyAssignmentMapper.toResponse(jockeyHorseAssignmentsRepository.save(assignment));
    }

    @Override
    @Transactional
    public JockeyAssignmentResponse respondInvitation(Integer id, JockeyInvitationResponseRequest request) {
        JockeyHorseAssignments assignment = findAssignmentForCurrentJockey(id);

        String responseStatus = request.getStatus().trim();
        assignment.setStatus(responseStatus);
        assignment.setRespondedAt(request.getRespondedAt() == null ? Instant.now() : request.getRespondedAt());
        if (STATUS_ACCEPTED.equalsIgnoreCase(responseStatus)) {
            assignment.getJockey().setStatus(JOCKEY_STATUS_UNAVAILABLE);
        }

        return jockeyAssignmentMapper.toResponse(jockeyHorseAssignmentsRepository.save(assignment));
    }

    @Override
    @Transactional
    public void deleteAssignment(Integer id) {
        JockeyHorseAssignments assignment = findAssignmentForCurrentOwner(id);
        assignment.setStatus(STATUS_DELETED);
        jockeyHorseAssignmentsRepository.save(assignment);
    }

    private JockeyHorseAssignments findAssignment(Integer id) {
        return jockeyHorseAssignmentsRepository.findById(id)
                .filter(assignment -> !isDeleted(assignment.getStatus()))
                .orElseThrow(() -> new ResourceNotFoundException("Jockey assignment not found"));
    }

    private JockeyHorseAssignments findAssignmentForCurrentOwner(Integer id) {
        JockeyHorseAssignments assignment = findAssignment(id);
        Integer ownerId = authService.getCurrentUserId();

        if (!Objects.equals(assignment.getReg().getOwner().getId(), ownerId)) {
            throw new AccessDeniedException("You do not own this jockey assignment");
        }

        return assignment;
    }

    private JockeyHorseAssignments findAssignmentForCurrentJockey(Integer id) {
        JockeyHorseAssignments assignment = findAssignment(id);
        Integer jockeyId = authService.getCurrentUserId();

        if (!Objects.equals(assignment.getJockey().getId(), jockeyId)) {
            throw new AccessDeniedException("This invitation is not assigned to you");
        }

        return assignment;
    }

    private void validateCreateReferences(JockeyInvitationCreateRequest request) {
        validateRegistrationExists(request.getRegistrationId());
        validateRaceExists(request.getRaceId());
        validateJockeyExists(request.getJockeyId());
    }

    private void validateUpdateReferences(JockeyInvitationUpdateRequest request) {
        if (request.getRegistrationId() != null) {
            validateRegistrationExists(request.getRegistrationId());
        }
        if (request.getRaceId() != null) {
            validateRaceExists(request.getRaceId());
        }
        if (request.getJockeyId() != null) {
            validateJockeyExists(request.getJockeyId());
        }
    }

    private void attachCreateReferences(JockeyHorseAssignments assignment, JockeyInvitationCreateRequest request) {
        RaceRegistrations registration = raceRegistrationsRepository.findById(request.getRegistrationId())
                .orElseThrow(() -> new ResourceNotFoundException("Race registration not found"));
        Races race = racesRepository.findById(request.getRaceId())
                .orElseThrow(() -> new ResourceNotFoundException("Race not found"));
        JockeyProfiles jockey = jockeyProfilesRepository.findById(request.getJockeyId())
                .orElseThrow(() -> new ResourceNotFoundException("Jockey profile not found"));

        assignment.setReg(registration);
        assignment.setRaces(race);
        assignment.setJockey(jockey);
    }

    private void validateRegistrationExists(Integer id) {
        if (!raceRegistrationsRepository.existsById(id)) {
            throw new ResourceNotFoundException("Race registration not found");
        }
    }

    private void validateRegistrationBelongsToCurrentOwner(Integer id) {
        Integer ownerId = authService.getCurrentUserId();
        boolean belongsToOwner = raceRegistrationsRepository.findById(id)
                .map(registration -> !isDeleted(registration.getStatus())
                        && Objects.equals(registration.getOwner().getId(), ownerId))
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

    private void validateRaceMatchesRegistration(Integer raceId, Integer registrationId) {
        boolean matchesRegistration = raceRegistrationsRepository.findById(registrationId)
                .map(registration -> Objects.equals(registration.getRaces().getId(), raceId))
                .orElse(false);

        if (!matchesRegistration) {
            throw new BadRequestException("Race does not match this registration");
        }
    }

    private void validateJockeyExists(Integer id) {
        if (!jockeyProfilesRepository.existsById(id)) {
            throw new ResourceNotFoundException("Jockey profile not found");
        }
    }

    private void validateRaceJockeyAvailable(Integer raceId, Integer jockeyId) {
        if (jockeyHorseAssignmentsRepository.existsByRaces_IdAndJockey_Id(raceId, jockeyId)) {
            throw new BadRequestException("Jockey is already assigned to this race");
        }
    }

    private void validateRaceGateAvailable(Integer raceId, Integer gateNumber) {
        if (gateNumber != null
                && jockeyHorseAssignmentsRepository.existsByRaces_IdAndGateNumber(raceId, gateNumber)) {
            throw new BadRequestException("Gate number is already used in this race");
        }
    }

    private void validateRaceJockeyAvailableForUpdate(
            Integer raceId,
            Integer jockeyId,
            Integer assignmentId
    ) {
        if (jockeyHorseAssignmentsRepository.existsByRaces_IdAndJockey_IdAndIdNot(
                raceId,
                jockeyId,
                assignmentId
        )) {
            throw new BadRequestException("Jockey is already assigned to this race");
        }
    }

    private void validateRaceGateAvailableForUpdate(
            Integer raceId,
            Integer gateNumber,
            Integer assignmentId
    ) {
        if (gateNumber != null
                && jockeyHorseAssignmentsRepository.existsByRaces_IdAndGateNumberAndIdNot(
                raceId,
                gateNumber,
                assignmentId
        )) {
            throw new BadRequestException("Gate number is already used in this race");
        }
    }

    private boolean isDeleted(String status) {
        return STATUS_DELETED.equalsIgnoreCase(status);
    }

    private List<JockeyHorseAssignments> findAssignmentsByJockey(Integer jockeyId, String status) {
        if (status != null && !status.isBlank()) {
            return jockeyHorseAssignmentsRepository
                    .findByJockey_IdAndStatusIgnoreCaseOrderByInvitedAtDesc(jockeyId, status.trim());
        }

        return jockeyHorseAssignmentsRepository.findByJockey_IdOrderByInvitedAtDesc(jockeyId);
    }

    private List<JockeyHorseAssignments> findAssignmentsByOwner(Integer ownerId, String status) {
        if (status != null && !status.isBlank()) {
            return jockeyHorseAssignmentsRepository
                    .findByReg_Owner_IdAndStatusIgnoreCaseOrderByInvitedAtDesc(ownerId, status.trim());
        }

        return jockeyHorseAssignmentsRepository.findByReg_Owner_IdOrderByInvitedAtDesc(ownerId);
    }
}
