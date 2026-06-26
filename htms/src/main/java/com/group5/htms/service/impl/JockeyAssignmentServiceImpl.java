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
import com.group5.htms.enums.JockeyStatus;
import com.group5.htms.enums.JockeyAssignmentStatus;
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
        validateRaceMatchesRegistration(raceId, registrationId);
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
        if (JockeyAssignmentStatus.ACCEPTED.getValue().equalsIgnoreCase(responseStatus)) {
            assignment.getJockey().setStatus(JockeyStatus.UNAVAILABLE.getValue());
        }

        return jockeyAssignmentMapper.toResponse(jockeyHorseAssignmentsRepository.save(assignment));
    }

    @Override
    @Transactional
    public void deleteAssignment(Integer id) {
        JockeyHorseAssignments assignment = findAssignmentForCurrentOwner(id);
        assignment.setStatus(JockeyAssignmentStatus.DELETED.getValue());
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

    private void validateNoDirectStatusUpdate(JockeyInvitationUpdateRequest request) {
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            throw new BadRequestException("Use workflow transition APIs to update status");
        }
    }

    private void validateResponseStatus(String status) {
        if (!JockeyAssignmentStatus.ACCEPTED.equalsValue(status)
                && !JockeyAssignmentStatus.REJECTED.equalsValue(status)) {
            throw new BadRequestException("Invalid response status");
        }
    }

    private void attachCreateReferences(
            JockeyHorseAssignments assignment,
            RaceRegistrations registration,
            Races race,
            JockeyProfiles jockey
    ) {
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

    private void validateRegistrationBelongsToCurrentOwner(RaceRegistrations registration) {
        Integer ownerId = authService.getCurrentUserId();

        if (registration.getOwner() == null
                || !Objects.equals(registration.getOwner().getId(), ownerId)) {
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

    private void validateRaceMatchesRegistration(Races race, RaceRegistrations registration) {
        if (!Objects.equals(registration.getRaces().getId(), race.getId())) {
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

    private void validateRaceJockeyAvailable(Integer raceId, Integer jockeyId, Instant now) {
        List<JockeyHorseAssignments> assignments =
                jockeyHorseAssignmentsRepository.findByRaces_IdAndJockey_IdAndStatusIn(
                        raceId,
                        jockeyId,
                        ACTIVE_ASSIGNMENT_STATUSES
                );

        expirePendingAssignmentsIfNeeded(assignments, now);

        if (assignments.stream().anyMatch(assignment -> isStillActive(assignment, now))) {
            throw new BadRequestException("Jockey is already assigned to this race");
        }
    }

    private void validateRaceGateAvailable(Integer raceId, Integer gateNumber) {
        if (gateNumber != null
                && jockeyHorseAssignmentsRepository.existsByRaces_IdAndGateNumber(raceId, gateNumber)) {
            throw new BadRequestException("Gate number is already used in this race");
        }
    }

    private void validateRaceGateAvailable(Integer raceId, Integer gateNumber, Instant now) {
        if (gateNumber == null) {
            return;
        }

        List<JockeyHorseAssignments> assignments =
                jockeyHorseAssignmentsRepository.findByRaces_IdAndGateNumberAndStatusIn(
                        raceId,
                        gateNumber,
                        ACTIVE_ASSIGNMENT_STATUSES
                );

        expirePendingAssignmentsIfNeeded(assignments, now);

        if (assignments.stream().anyMatch(assignment -> isStillActive(assignment, now))) {
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
        return JockeyAssignmentStatus.DELETED.getValue().equalsIgnoreCase(status);
    }

    private RaceRegistrations findRegistrationEntity(Integer id) {
        return raceRegistrationsRepository.findById(id)
                .filter(registration -> !isDeleted(registration.getStatus()))
                .orElseThrow(() -> new ResourceNotFoundException("Race registration not found"));
    }

    private Races findRaceEntity(Integer id) {
        return racesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Race not found"));
    }

    private JockeyProfiles findJockeyEntity(Integer id) {
        return jockeyProfilesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jockey profile not found"));
    }

    private void validateRegistrationCanInviteJockey(RaceRegistrations registration) {
        if (!RaceRegistrationStatus.APPROVED.equalsValue(registration.getStatus())) {
            throw new BadRequestException("Only approved registrations can invite jockeys");
        }
    }

    private void validateRegistrationStillOpen(RaceRegistrations registration, Races race) {
        validateRegistrationStillOpen(registration.getTournaments(), race);
    }

    private void validateRegistrationStillOpen(RaceRegistrations registration) {
        validateRegistrationStillOpen(registration, registration.getRaces());
    }

    private void validateRegistrationStillOpen(com.group5.htms.entity.Tournaments tournament, Races race) {
        if (!com.group5.htms.enums.TournamentStatus.REGISTRATION_OPEN.equalsValue(tournament.getStatus())) {
            throw new BadRequestException("Registration is not open for this tournament");
        }

        if (!RaceStatus.REGISTRATION_OPEN.equalsValue(race.getStatus())) {
            throw new BadRequestException("Registration is not open for this race");
        }
    }

    private void validateJockeyCanBeInvited(JockeyProfiles jockey) {
        if (jockey.getUsers() == null
                || jockey.getUsers().getRoleType() == null
                || !com.group5.htms.enums.RoleType.JOCKEY.getValue()
                .equalsIgnoreCase(jockey.getUsers().getRoleType().trim())) {
            throw new BadRequestException("Jockey profile not found");
        }

        if (jockey.getUsers().getStatus() == null
                || !STATUS_ACTIVE.equalsIgnoreCase(jockey.getUsers().getStatus().trim())) {
            throw new BadRequestException("Jockey user account is not active");
        }

        if (jockey.getStatus() == null
                || (!STATUS_ACTIVE.equalsIgnoreCase(jockey.getStatus().trim())
                && !STATUS_AVAILABLE.equalsIgnoreCase(jockey.getStatus().trim()))) {
            throw new BadRequestException("Jockey profile is not active");
        }
    }

    private void validateRegistrationHasNoActiveAssignment(Integer registrationId, Instant now) {
        List<JockeyHorseAssignments> assignments =
                jockeyHorseAssignmentsRepository.findByReg_IdAndStatusIn(
                        registrationId,
                        ACTIVE_ASSIGNMENT_STATUSES
                );

        expirePendingAssignmentsIfNeeded(assignments, now);

        boolean hasPending = assignments.stream()
                .anyMatch(assignment -> JockeyAssignmentStatus.PENDING.equalsValue(assignment.getStatus()));

        if (hasPending) {
            throw new BadRequestException("This registration already has a pending jockey invitation");
        }

        boolean hasAcceptedOrConfirmed = assignments.stream()
                .anyMatch(assignment -> JockeyAssignmentStatus.ACCEPTED.equalsValue(assignment.getStatus())
                        || JockeyAssignmentStatus.CONFIRMED.equalsValue(assignment.getStatus()));

        if (hasAcceptedOrConfirmed) {
            throw new BadRequestException("This registration already has an active jockey assignment");
        }
    }

    private void expirePendingAssignmentsIfNeeded(List<JockeyHorseAssignments> assignments, Instant now) {
        assignments.stream()
                .filter(assignment -> JockeyAssignmentStatus.PENDING.equalsValue(assignment.getStatus()))
                .filter(assignment -> isPendingExpired(assignment, now))
                .forEach(assignment -> {
                    assignment.setStatus(JockeyAssignmentStatus.EXPIRED.getValue());
                    assignment.setExpiredAt(now);
                    jockeyHorseAssignmentsRepository.save(assignment);
                });
    }

    private boolean isStillActive(JockeyHorseAssignments assignment, Instant now) {
        if (JockeyAssignmentStatus.PENDING.equalsValue(assignment.getStatus())) {
            return !isPendingExpired(assignment, now);
        }

        return JockeyAssignmentStatus.ACCEPTED.equalsValue(assignment.getStatus())
                || JockeyAssignmentStatus.CONFIRMED.equalsValue(assignment.getStatus());
    }

    private boolean isPendingExpired(JockeyHorseAssignments assignment, Instant now) {
        Instant responseDeadline = assignment.getResponseDeadline();

        return responseDeadline != null && !now.isBefore(responseDeadline);
    }

    private Instant calculateResponseDeadline(RaceRegistrations registration, Instant now) {
        Instant deadline = now.plus(DEFAULT_RESPONSE_DURATION);

        if (registration.getTournaments() != null
                && registration.getTournaments().getRegistrationCloseAt() != null) {
            Instant closeBufferDeadline = registration.getTournaments()
                    .getRegistrationCloseAt()
                    .minus(REGISTRATION_CLOSE_BUFFER);

            if (closeBufferDeadline.isBefore(deadline)) {
                deadline = closeBufferDeadline;
            }
        }

        return deadline;
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


