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
import com.group5.htms.enums.JockeyAssignmentStatus;
import com.group5.htms.enums.JockeyStatus;
import com.group5.htms.enums.RaceRegistrationStatus;
import com.group5.htms.enums.RoleType;
import com.group5.htms.exception.ResourceNotFoundException;
import com.group5.htms.mapper.JockeyAssignmentMapper;
import com.group5.htms.repository.JockeyHorseAssignmentsRepository;
import com.group5.htms.repository.JockeyProfilesRepository;
import com.group5.htms.repository.RaceRegistrationsRepository;
import com.group5.htms.repository.RacesRepository;
import com.group5.htms.service.AuthService;
import com.group5.htms.service.JockeyAssignmentService;
import com.group5.htms.validation.JockeyAssignmentValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JockeyAssignmentServiceImpl implements JockeyAssignmentService {
    private static final Duration DEFAULT_RESPONSE_DURATION = Duration.ofHours(24);
    private static final Duration REGISTRATION_CLOSE_BUFFER = Duration.ofHours(2);
    private static final List<String> ACTIVE_ASSIGNMENT_STATUSES = List.of(
            JockeyAssignmentStatus.PENDING.getValue(),
            JockeyAssignmentStatus.ACCEPTED.getValue(),
            JockeyAssignmentStatus.CONFIRMED.getValue()
    );

    private final JockeyHorseAssignmentsRepository jockeyHorseAssignmentsRepository;
    private final RaceRegistrationsRepository raceRegistrationsRepository;
    private final RacesRepository racesRepository;
    private final JockeyProfilesRepository jockeyProfilesRepository;
    private final AuthService authService;
    private final JockeyAssignmentMapper jockeyAssignmentMapper;
    private final JockeyAssignmentValidator jockeyAssignmentValidator;

    @Override
    @Transactional(readOnly = true)
    public List<JockeyAssignmentListResponse> getAllAssignments() {
        return jockeyHorseAssignmentsRepository.findAll()
                .stream()
                .map(jockeyAssignmentMapper::toListResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<JockeyAssignmentListResponse> getMyInvitations(String status) {
        Integer jockeyId = authService.getCurrentUserId();

        return findAssignmentsByJockey(jockeyId, status)
                .stream()
                .map(jockeyAssignmentMapper::toListResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<JockeyAssignmentListResponse> getSentInvitations(String status) {
        Integer ownerId = authService.getCurrentUserId();

        return findAssignmentsByOwner(ownerId, status)
                .stream()
                .map(jockeyAssignmentMapper::toListResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public JockeyAssignmentResponse getAssignmentById(Integer id) {
        return jockeyAssignmentMapper.toResponse(findAssignment(id));
    }

    @Override
    @Transactional
    public JockeyAssignmentResponse createInvitation(JockeyInvitationCreateRequest request) {
        Instant now = Instant.now();
        RaceRegistrations registration = findRegistration(request.getRegistrationId());
        Races race = findRace(request.getRaceId());
        JockeyProfiles jockey = findJockey(request.getJockeyId());

        if (!authService.currentUserHasRole(RoleType.ADMIN.getValue())) {
            jockeyAssignmentValidator.ensureOwnerCanManageRegistration(registration, authService.getCurrentUserId());
        }
        jockeyAssignmentValidator.ensureRaceMatchesRegistration(race, registration);
        jockeyAssignmentValidator.ensureRegistrationCanInviteJockey(registration);
        jockeyAssignmentValidator.ensureRegistrationStillOpen(registration, race);
        jockeyAssignmentValidator.ensureJockeyCanBeInvited(jockey);
        List<JockeyHorseAssignments> registrationAssignments = jockeyHorseAssignmentsRepository.findByReg_IdAndStatusIn(
                registration.getId(),
                ACTIVE_ASSIGNMENT_STATUSES
        );
        expirePendingAssignmentsIfNeeded(registrationAssignments, now);
        jockeyAssignmentValidator.ensureNoActiveAssignment(
                registrationAssignments,
                now,
                "This registration already has an active jockey invitation"
        );

        List<JockeyHorseAssignments> raceJockeyAssignments = jockeyHorseAssignmentsRepository.findByRaces_IdAndJockey_IdAndStatusIn(
                race.getId(),
                jockey.getId(),
                ACTIVE_ASSIGNMENT_STATUSES
        );
        expirePendingAssignmentsIfNeeded(raceJockeyAssignments, now);
        jockeyAssignmentValidator.ensureNoActiveAssignment(
                raceJockeyAssignments,
                now,
                "Jockey is already assigned to this race"
        );

        if (request.getGateNumber() != null) {
            List<JockeyHorseAssignments> gateAssignments = jockeyHorseAssignmentsRepository.findByRaces_IdAndGateNumberAndStatusIn(
                    race.getId(),
                    request.getGateNumber(),
                    ACTIVE_ASSIGNMENT_STATUSES
            );
            expirePendingAssignmentsIfNeeded(gateAssignments, now);
            jockeyAssignmentValidator.ensureNoActiveAssignment(
                    gateAssignments,
                    now,
                    "Gate number is already used in this race"
            );
        }

        JockeyHorseAssignments assignment = jockeyAssignmentMapper.toEntity(request);
        assignment.setReg(registration);
        assignment.setRaces(race);
        assignment.setJockey(jockey);
        assignment.setStatus(JockeyAssignmentStatus.PENDING.getValue());
        assignment.setInvitedAt(now);
        assignment.setResponseDeadline(calculateResponseDeadline(registration, now));
        assignment.setRespondedAt(null);
        assignment.setCancelledAt(null);
        assignment.setExpiredAt(null);

        return jockeyAssignmentMapper.toResponse(jockeyHorseAssignmentsRepository.save(assignment));
    }

    @Override
    @Transactional
    public JockeyAssignmentResponse updateInvitation(Integer id, JockeyInvitationUpdateRequest request) {
        Instant now = Instant.now();
        JockeyHorseAssignments assignment = findAssignmentForCurrentOwner(id);

        jockeyAssignmentValidator.ensurePending(assignment, "Only pending invitations can be updated");
        jockeyAssignmentValidator.ensureNoWorkflowFields(request);

        RaceRegistrations registration = request.getRegistrationId() == null
                ? assignment.getReg()
                : findRegistration(request.getRegistrationId());
        Races race = request.getRaceId() == null
                ? assignment.getRaces()
                : findRace(request.getRaceId());
        JockeyProfiles jockey = request.getJockeyId() == null
                ? assignment.getJockey()
                : findJockey(request.getJockeyId());

        if (!authService.currentUserHasRole(RoleType.ADMIN.getValue())) {
            jockeyAssignmentValidator.ensureOwnerCanManageRegistration(registration, authService.getCurrentUserId());
        }
        jockeyAssignmentValidator.ensureRaceMatchesRegistration(race, registration);
        jockeyAssignmentValidator.ensureRegistrationCanInviteJockey(registration);
        jockeyAssignmentValidator.ensureRegistrationStillOpen(registration, race);
        jockeyAssignmentValidator.ensureJockeyCanBeInvited(jockey);

        List<JockeyHorseAssignments> raceJockeyAssignments = jockeyHorseAssignmentsRepository.findByRaces_IdAndJockey_IdAndStatusIn(
                race.getId(),
                jockey.getId(),
                ACTIVE_ASSIGNMENT_STATUSES
        );
        expirePendingAssignmentsIfNeeded(raceJockeyAssignments, now);
        jockeyAssignmentValidator.ensureNoActiveAssignmentExcept(
                raceJockeyAssignments,
                assignment.getId(),
                now,
                "Jockey is already assigned to this race"
        );

        if (request.getGateNumber() != null) {
            List<JockeyHorseAssignments> gateAssignments = jockeyHorseAssignmentsRepository.findByRaces_IdAndGateNumberAndStatusIn(
                    race.getId(),
                    request.getGateNumber(),
                    ACTIVE_ASSIGNMENT_STATUSES
            );
            expirePendingAssignmentsIfNeeded(gateAssignments, now);
            jockeyAssignmentValidator.ensureNoActiveAssignmentExcept(
                    gateAssignments,
                    assignment.getId(),
                    now,
                    "Gate number is already used in this race"
            );
        }

        assignment.setReg(registration);
        assignment.setRaces(race);
        assignment.setJockey(jockey);
        if (request.getGateNumber() != null) {
            assignment.setGateNumber(request.getGateNumber());
        }

        return jockeyAssignmentMapper.toResponse(jockeyHorseAssignmentsRepository.save(assignment));
    }

    @Override
    @Transactional
    public JockeyAssignmentResponse respondInvitation(Integer id, JockeyInvitationResponseRequest request) {
        JockeyHorseAssignments assignment = findAssignmentForCurrentJockey(id);
        Instant now = Instant.now();

        expirePendingAssignmentIfNeeded(assignment, now);
        jockeyAssignmentValidator.ensurePending(assignment, "Only pending invitations can be responded");

        String responseStatus = jockeyAssignmentValidator.normalizeResponseStatus(request.getStatus());
        assignment.setStatus(responseStatus);
        assignment.setRespondedAt(now);

        if (JockeyAssignmentStatus.ACCEPTED.equalsValue(responseStatus)) {
            assignment.getJockey().setStatus(JockeyStatus.UNAVAILABLE.getValue());
            RaceRegistrations registration = assignment.getReg();
            registration.setJockey(assignment.getJockey());
            registration.setOwnerConfirmationStatus(RaceRegistrationStatus.CONFIRMED.getValue());
            registration.setOwnerConfirmedAt(now);
        }

        return jockeyAssignmentMapper.toResponse(jockeyHorseAssignmentsRepository.save(assignment));
    }

    @Override
    @Transactional
    public JockeyAssignmentResponse cancelInvitation(Integer id) {
        JockeyHorseAssignments assignment = findAssignmentForCurrentOwner(id);
        jockeyAssignmentValidator.ensurePending(assignment, "Only pending invitations can be cancelled");

        assignment.setStatus(JockeyAssignmentStatus.CANCELLED.getValue());
        assignment.setCancelledAt(Instant.now());

        return jockeyAssignmentMapper.toResponse(jockeyHorseAssignmentsRepository.save(assignment));
    }

    @Override
    @Transactional
    public JockeyAssignmentResponse confirmAssignment(Integer id) {
        JockeyHorseAssignments assignment = findAssignmentForCurrentOwner(id);

        jockeyAssignmentValidator.ensureAccepted(assignment, "Only accepted assignments can be confirmed");

        assignment.setStatus(JockeyAssignmentStatus.CONFIRMED.getValue());
        assignment.getJockey().setStatus(JockeyStatus.UNAVAILABLE.getValue());
        assignment.getReg().setJockey(assignment.getJockey());
        assignment.getReg().setOwnerConfirmationStatus(RaceRegistrationStatus.CONFIRMED.getValue());
        assignment.getReg().setOwnerConfirmedAt(Instant.now());

        return jockeyAssignmentMapper.toResponse(jockeyHorseAssignmentsRepository.save(assignment));
    }


    private JockeyHorseAssignments findAssignment(Integer id) {
        return jockeyHorseAssignmentsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jockey assignment not found"));
    }

    private JockeyHorseAssignments findAssignmentForCurrentOwner(Integer id) {
        JockeyHorseAssignments assignment = findAssignment(id);
        if (!authService.currentUserHasRole(RoleType.ADMIN.getValue())) {
            jockeyAssignmentValidator.ensureOwnerCanManageRegistration(assignment.getReg(), authService.getCurrentUserId());
        }
        return assignment;
    }

    private JockeyHorseAssignments findAssignmentForCurrentJockey(Integer id) {
        JockeyHorseAssignments assignment = findAssignment(id);
        Integer jockeyId = authService.getCurrentUserId();

        jockeyAssignmentValidator.ensureJockeyCanRespond(assignment, jockeyId);

        return assignment;
    }

    private RaceRegistrations findRegistration(Integer id) {
        return raceRegistrationsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Race registration not found"));
    }

    private Races findRace(Integer id) {
        return racesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Race not found"));
    }

    private JockeyProfiles findJockey(Integer id) {
        return jockeyProfilesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jockey profile not found"));
    }


    private void expirePendingAssignmentsIfNeeded(List<JockeyHorseAssignments> assignments, Instant now) {
        assignments.stream()
                .filter(assignment -> JockeyAssignmentStatus.PENDING.equalsValue(assignment.getStatus()))
                .filter(assignment -> jockeyAssignmentValidator.isPendingExpired(assignment, now))
                .forEach(assignment -> {
                    assignment.setStatus(JockeyAssignmentStatus.EXPIRED.getValue());
                    assignment.setExpiredAt(now);
                    jockeyHorseAssignmentsRepository.save(assignment);
                });
    }

    private void expirePendingAssignmentIfNeeded(JockeyHorseAssignments assignment, Instant now) {
        if (JockeyAssignmentStatus.PENDING.equalsValue(assignment.getStatus()) && jockeyAssignmentValidator.isPendingExpired(assignment, now)) {
            assignment.setStatus(JockeyAssignmentStatus.EXPIRED.getValue());
            assignment.setExpiredAt(now);
            jockeyHorseAssignmentsRepository.save(assignment);
        }
    }

    private Instant calculateResponseDeadline(RaceRegistrations registration, Instant now) {
        Instant deadline = now.plus(DEFAULT_RESPONSE_DURATION);

        if (registration.getTournaments() != null && registration.getTournaments().getRegistrationCloseAt() != null) {
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