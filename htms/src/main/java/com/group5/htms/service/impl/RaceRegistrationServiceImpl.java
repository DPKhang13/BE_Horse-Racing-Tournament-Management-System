package com.group5.htms.service.impl;

import com.group5.htms.dto.raceregistration.request.RaceRegistrationApprovalRequest;
import com.group5.htms.dto.raceregistration.request.RaceRegistrationApproveRequest;
import com.group5.htms.dto.raceregistration.request.RaceRegistrationCancelRequest;
import com.group5.htms.dto.raceregistration.request.ChiefInspectionRequest;
import com.group5.htms.dto.raceregistration.request.RaceRegistrationCreateRequest;
import com.group5.htms.dto.raceregistration.request.RaceRegistrationRejectRequest;
import com.group5.htms.dto.raceregistration.request.RaceRegistrationUpdateRequest;
import com.group5.htms.dto.raceregistration.response.RaceRegistrationListResponse;
import com.group5.htms.dto.raceregistration.response.RaceRegistrationResponse;
import com.group5.htms.entity.HorseOwnerProfiles;
import com.group5.htms.entity.Horses;
import com.group5.htms.entity.JockeyHorseAssignments;
import com.group5.htms.entity.RaceRegistrations;
import com.group5.htms.entity.RaceRefereeAssignments;
import com.group5.htms.entity.Races;
import com.group5.htms.entity.RefereeProfiles;
import com.group5.htms.entity.Tournaments;
import com.group5.htms.entity.Users;
import com.group5.htms.enums.RaceRegistrationStatus;
import com.group5.htms.enums.ChiefInspectionStatus;
import com.group5.htms.enums.JockeyAssignmentStatus;
import com.group5.htms.enums.RaceStatus;
import com.group5.htms.enums.RoleType;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.exception.ResourceNotFoundException;
import com.group5.htms.mapper.RaceRegistrationMapper;
import com.group5.htms.repository.HorseOwnerProfilesRepository;
import com.group5.htms.repository.HorsesRepository;
import com.group5.htms.repository.JockeyHorseAssignmentsRepository;
import com.group5.htms.repository.RaceRegistrationsRepository;
import com.group5.htms.repository.RacesRepository;
import com.group5.htms.repository.TournamentsRepository;
import com.group5.htms.service.AuthService;
import com.group5.htms.service.RaceRegistrationService;
import com.group5.htms.validation.RaceRegistrationValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RaceRegistrationServiceImpl implements RaceRegistrationService {
    private static final int MIN_GATE_COUNT = 8;
    private static final List<String> RELEASED_REGISTRATION_STATUSES = List.of(
            RaceRegistrationStatus.REJECTED.getValue(),
            RaceRegistrationStatus.CANCELLED.getValue()
    );
    private static final List<String> ACTIVE_JOCKEY_ASSIGNMENT_STATUSES = List.of(
            JockeyAssignmentStatus.PENDING.getValue(),
            JockeyAssignmentStatus.ACCEPTED.getValue(),
            JockeyAssignmentStatus.CONFIRMED.getValue()
    );


    private final RaceRegistrationsRepository raceRegistrationsRepository;
    private final TournamentsRepository tournamentsRepository;
    private final RacesRepository racesRepository;
    private final HorsesRepository horsesRepository;
    private final HorseOwnerProfilesRepository horseOwnerProfilesRepository;
    private final JockeyHorseAssignmentsRepository jockeyHorseAssignmentsRepository;
    private final AuthService authService;
    private final RaceRegistrationMapper raceRegistrationMapper;
    private final RaceRegistrationValidator raceRegistrationValidator;
    private final RefereeRaceAuthorizationService refereeRaceAuthorizationService;

    @Override
    @Transactional(readOnly = true)
    public List<RaceRegistrationListResponse> getAllRegistrations() {
        return raceRegistrationsRepository.findByStatusIgnoreCaseOrderByRegisteredAtDesc(
                        RaceRegistrationStatus.APPROVED.getValue()
                )
                .stream()
                .filter(registration -> ChiefInspectionStatus.APPROVED.equalsValue(registration.getChiefInspectionStatus()))
                .map(raceRegistrationMapper::toListResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RaceRegistrationListResponse> getMyRegistrations() {
        Integer ownerId = authService.getCurrentUserId();

        return raceRegistrationsRepository.findByOwner_IdOrderByRegisteredAtDesc(ownerId)
                .stream()
                .map(raceRegistrationMapper::toListResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RaceRegistrationListResponse> getAdminApprovalRegistrations() {
        return raceRegistrationsRepository
                .findByStatusIgnoreCaseAndOwnerConfirmationStatusIgnoreCaseAndJockeyIsNotNullOrderByRegisteredAtDesc(
                        RaceRegistrationStatus.PENDING.getValue(),
                        RaceRegistrationStatus.CONFIRMED.getValue()
                )
                .stream()
                .filter(registration -> RaceStatus.REGISTRATION_OPEN.equalsValue(registration.getRaces().getStatus()))
                .map(raceRegistrationMapper::toListResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RaceRegistrationListResponse> getChiefInspectionRegistrations(Integer raceId) {
        Races race = findRace(raceId);
        refereeRaceAuthorizationService.requireChiefReferee(race.getId());
        if (!RaceStatus.REGISTRATION_CLOSED.equalsValue(race.getStatus())) {
            throw new BadRequestException("Horse inspection is available only when race registration is closed");
        }

        return raceRegistrationsRepository.findByRaces_Id(race.getId())
                .stream()
                .filter(registration -> RaceRegistrationStatus.APPROVED.equalsValue(registration.getStatus()))
                .filter(registration -> RaceRegistrationStatus.CONFIRMED.equalsValue(registration.getOwnerConfirmationStatus()))
                .filter(this::hasConfirmedJockeyAssignment)
                .map(raceRegistrationMapper::toListResponse)
                .toList();
    }
    @Override
    @Transactional(readOnly = true)
    public RaceRegistrationResponse getRegistrationById(Integer id) {
        return raceRegistrationMapper.toResponse(findRegistration(id));
    }

    @Override
    @Transactional(readOnly = true)
    public RaceRegistrationResponse getMyRegistrationById(Integer id) {
        Integer ownerId = authService.getCurrentUserId();
        return raceRegistrationMapper.toResponse(findRegistrationForCurrentOwner(id, ownerId));
    }

    @Override
    @Transactional
    public RaceRegistrationResponse createRegistration(RaceRegistrationCreateRequest request) {
        Integer ownerId = authService.getCurrentUserId();
        request.setOwnerId(ownerId);

        Tournaments tournament = findTournament(request.getTournamentId());
        Races race = findRace(request.getRaceId());
        Horses horse = findHorse(request.getHorseId());
        HorseOwnerProfiles owner = findOwner(ownerId);

        raceRegistrationValidator.ensureRaceBelongsToTournament(race, tournament.getId());
        if (!authService.currentUserHasRole(RoleType.ADMIN.getValue())) {
            raceRegistrationValidator.ensureHorseBelongsToOwner(horse, ownerId);
        }
        raceRegistrationValidator.ensureRegistrationOpen(tournament, race);
        raceRegistrationValidator.ensureHorseActive(horse);
        raceRegistrationValidator.ensureHorseRankGroupMatchesRace(horse, race);
        raceRegistrationValidator.ensureHorseNotRegisteredInRace(
                raceRegistrationsRepository.existsByRaces_IdAndHorses_Id(race.getId(), horse.getId())
        );
        raceRegistrationValidator.ensureHorseHasNoScheduleConflict(
                raceRegistrationsRepository.existsHorseScheduleConflictInTournament(
                        tournament.getId(),
                        horse.getId(),
                        race.getScheduledAt(),
                        race.getId(),
                        RELEASED_REGISTRATION_STATUSES
                )
        );
        validateGateForCreate(race, request.getGateNumber());

        RaceRegistrations registration = raceRegistrationMapper.toEntity(request);
        registration.setTournaments(tournament);
        registration.setRaces(race);
        registration.setHorses(horse);
        registration.setGateNumber(request.getGateNumber());
        registration.setOwner(owner);
        registration.setJockey(null);
        registration.setStatus(RaceRegistrationStatus.PENDING.getValue());
        registration.setOwnerConfirmationStatus(RaceRegistrationStatus.PENDING.getValue());
        registration.setChiefInspectionStatus(ChiefInspectionStatus.PENDING.getValue());
        registration.setRegisteredAt(Instant.now());

        return raceRegistrationMapper.toResponse(raceRegistrationsRepository.save(registration));
    }

    @Override
    @Transactional
    public RaceRegistrationResponse updateRegistration(Integer id, RaceRegistrationUpdateRequest request) {
        Integer ownerId = authService.getCurrentUserId();
        RaceRegistrations registration = authService.currentUserHasRole(RoleType.ADMIN.getValue())
                ? findRegistration(id)
                : findRegistrationForCurrentOwner(id, ownerId);

        raceRegistrationValidator.ensureNoWorkflowFields(request);

        Tournaments tournament = request.getTournamentId() == null
                ? registration.getTournaments()
                : findTournament(request.getTournamentId());
        Races race = request.getRaceId() == null
                ? registration.getRaces()
                : findRace(request.getRaceId());
        Horses horse = request.getHorseId() == null
                ? registration.getHorses()
                : findHorse(request.getHorseId());
        Integer gateNumber = request.getGateNumber() == null ? registration.getGateNumber() : request.getGateNumber();

        raceRegistrationValidator.ensureRaceBelongsToTournament(race, tournament.getId());
        if (!authService.currentUserHasRole(RoleType.ADMIN.getValue())) {
            raceRegistrationValidator.ensureHorseBelongsToOwner(horse, ownerId);
        }
        raceRegistrationValidator.ensureHorseActive(horse);
        raceRegistrationValidator.ensureHorseRankGroupMatchesRace(horse, race);
        raceRegistrationValidator.ensureHorseNotRegisteredInRace(
                raceRegistrationsRepository.existsByRaces_IdAndHorses_IdAndIdNot(
                        race.getId(),
                        horse.getId(),
                        registration.getId()
                )
        );
        raceRegistrationValidator.ensureHorseHasNoScheduleConflict(
                raceRegistrationsRepository.existsHorseScheduleConflictInTournamentForUpdate(
                        tournament.getId(),
                        horse.getId(),
                        race.getScheduledAt(),
                        race.getId(),
                        registration.getId(),
                        RELEASED_REGISTRATION_STATUSES
                )
        );
        validateGateForUpdate(registration, race, gateNumber);

        registration.setTournaments(tournament);
        registration.setRaces(race);
        registration.setHorses(horse);
        registration.setGateNumber(gateNumber);

        return raceRegistrationMapper.toResponse(raceRegistrationsRepository.save(registration));
    }

    @Override
    @Transactional
    public RaceRegistrationResponse approveRegistration(Integer id, RaceRegistrationApprovalRequest request) {
        String status = request == null ? null : request.getStatus();
        raceRegistrationValidator.ensureApproveStatusRequested(status);

        return approveRegistration(id, new RaceRegistrationApproveRequest());
    }

    @Override
    @Transactional
    public RaceRegistrationResponse approveRegistration(Integer id, RaceRegistrationApproveRequest request) {
        RaceRegistrations registration = findRegistration(id);
        raceRegistrationValidator.ensureCanApprove(registration);
        ensureRaceCapacityAvailable(registration.getRaces());

        registration.setStatus(RaceRegistrationStatus.APPROVED.getValue());
        registration.setApprovedAt(Instant.now());
        registration.setApprovedBy(currentUserReference());
        registration.setAdminReviewedAt(Instant.now());
        registration.setAdminReviewedBy(currentUserReference());
        registration.setAdminReviewNote(clean(request == null ? null : request.getNote()));

        RaceRegistrationResponse response = raceRegistrationMapper.toResponse(raceRegistrationsRepository.save(registration));
        refreshRaceReadiness(registration.getRaces());
        return response;
    }

    @Override
    @Transactional
    public RaceRegistrationResponse inspectRegistration(
            Integer raceId,
            Integer registrationId,
            ChiefInspectionRequest request
    ) {
        Races race = findRace(raceId);
        RaceRefereeAssignments chiefAssignment = refereeRaceAuthorizationService.requireChiefReferee(race.getId());
        RaceRegistrations registration = findRegistration(registrationId);

        if (!race.getId().equals(registration.getRaces().getId())) {
            throw new BadRequestException("Registration does not belong to this race");
        }
        raceRegistrationValidator.ensureCanInspect(registration);
        String inspectionStatus = cleanLower(request.getStatus());
        raceRegistrationValidator.ensureValidChiefInspectionStatus(inspectionStatus);

        registration.setChiefInspectionStatus(inspectionStatus);
        registration.setChiefInspectedBy(chiefAssignment.getReferee());
        registration.setChiefInspectedAt(Instant.now());
        registration.setChiefInspectionNote(clean(request.getNote()));

        if (ChiefInspectionStatus.REJECTED.equalsValue(inspectionStatus)) {
            registration.setStatus(RaceRegistrationStatus.REJECTED.getValue());
            cancelActiveJockeyAssignments(registration);
        }

        RaceRegistrationResponse response = raceRegistrationMapper.toResponse(
                raceRegistrationsRepository.save(registration)
        );
        refreshRaceReadiness(race);
        return response;
    }

    @Override
    @Transactional
    public RaceRegistrationResponse rejectRegistration(Integer id, RaceRegistrationRejectRequest request) {
        RaceRegistrations registration = findRegistration(id);

        raceRegistrationValidator.ensureCanReject(registration);

        registration.setStatus(RaceRegistrationStatus.REJECTED.getValue());
        registration.setAdminReviewedAt(Instant.now());
        registration.setAdminReviewedBy(currentUserReference());
        registration.setAdminReviewNote(clean(request == null ? null : request.getReason()));
        cancelActiveJockeyAssignments(registration);

        RaceRegistrationResponse response = raceRegistrationMapper.toResponse(raceRegistrationsRepository.save(registration));
        refreshRaceReadiness(registration.getRaces());
        return response;
    }

    @Override
    @Transactional
    public RaceRegistrationResponse cancelRegistration(Integer id, RaceRegistrationCancelRequest request) {
        Integer ownerId = authService.getCurrentUserId();
        RaceRegistrations registration = authService.currentUserHasRole(RoleType.ADMIN.getValue())
                ? findRegistration(id)
                : findRegistrationForCurrentOwner(id, ownerId);
        raceRegistrationValidator.ensureCanCancel(registration);

        registration.setStatus(RaceRegistrationStatus.CANCELLED.getValue());

        return raceRegistrationMapper.toResponse(raceRegistrationsRepository.save(registration));
    }


    private void validateGateForCreate(Races race, Integer gateNumber) {
        ensureGateInRange(race, gateNumber);
        if (raceRegistrationsRepository.existsByRaces_IdAndGateNumberAndStatusNotIn(
                race.getId(),
                gateNumber,
                RELEASED_REGISTRATION_STATUSES
        )) {
            throw new BadRequestException("Gate number is already registered for this race");
        }
    }

    private void validateGateForUpdate(RaceRegistrations registration, Races race, Integer gateNumber) {
        ensureGateInRange(race, gateNumber);
        if (raceRegistrationsRepository.existsByRaces_IdAndGateNumberAndStatusNotInAndIdNot(
                race.getId(),
                gateNumber,
                RELEASED_REGISTRATION_STATUSES,
                registration.getId()
        )) {
            throw new BadRequestException("Gate number is already registered for this race");
        }
    }

    private void ensureGateInRange(Races race, Integer gateNumber) {
        if (gateNumber == null) {
            throw new BadRequestException("Gate number is required");
        }
        int gateCount = gateCount(race);
        if (gateNumber < 1 || gateNumber > gateCount) {
            throw new BadRequestException("Gate number must be between 1 and " + gateCount);
        }
    }

    private int gateCount(Races race) {
        Integer maxHorses = race == null ? null : race.getMaxHorses();
        return Math.max(MIN_GATE_COUNT, maxHorses == null ? MIN_GATE_COUNT : maxHorses);
    }

    private Integer resolveOwnerId(Integer requestedOwnerId) {
        if (authService.currentUserHasRole(RoleType.ADMIN.getValue()) && requestedOwnerId != null) {
            return requestedOwnerId;
        }

        return authService.getCurrentUserId();
    }

    private RaceRegistrations findRegistration(Integer id) {
        return raceRegistrationsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Race registration not found"));
    }

    private RaceRegistrations findRegistrationForCurrentOwner(Integer id, Integer ownerId) {
        RaceRegistrations registration = findRegistration(id);

        raceRegistrationValidator.ensureOwnerCanManageRegistration(registration, ownerId);

        return registration;
    }

    private Tournaments findTournament(Integer id) {
        return tournamentsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found"));
    }

    private Races findRace(Integer id) {
        return racesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Race not found"));
    }

    private Horses findHorse(Integer id) {
        return horsesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Horse not found"));
    }

    private HorseOwnerProfiles findOwner(Integer id) {
        return horseOwnerProfilesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Horse owner profile not found"));
    }

    private Users currentUserReference() {
        Users user = new Users();
        user.setId(authService.getCurrentUserId());
        return user;
    }

    private void ensureRaceCapacityAvailable(Races race) {
        Integer maxHorses = race.getMaxHorses();
        if (maxHorses == null) {
            return;
        }

        long approvedCount = raceRegistrationsRepository.countByRaces_IdAndStatusIgnoreCase(
                race.getId(),
                RaceRegistrationStatus.APPROVED.getValue()
        );
        if (approvedCount >= maxHorses) {
            throw new com.group5.htms.exception.BadRequestException("Race maximum horses limit has been reached");
        }
    }

    private boolean hasConfirmedJockeyAssignment(RaceRegistrations registration) {
        return jockeyHorseAssignmentsRepository.findByReg_Id(registration.getId())
                .stream()
                .anyMatch(assignment -> JockeyAssignmentStatus.CONFIRMED.equalsValue(assignment.getStatus()));
    }

    private void cancelActiveJockeyAssignments(RaceRegistrations registration) {
        Instant now = Instant.now();
        List<JockeyHorseAssignments> activeAssignments = jockeyHorseAssignmentsRepository
                .findByReg_IdAndStatusIn(registration.getId(), ACTIVE_JOCKEY_ASSIGNMENT_STATUSES);
        activeAssignments.forEach(assignment -> {
            assignment.setStatus(JockeyAssignmentStatus.CANCELLED.getValue());
            assignment.setCancelledAt(now);
            assignment.setResponseDeadline(null);
        });
        if (!activeAssignments.isEmpty()) {
            jockeyHorseAssignmentsRepository.saveAll(activeAssignments);
        }
    }

    private void refreshRaceReadiness(Races race) {
        if (!RaceStatus.REGISTRATION_CLOSED.equalsValue(race.getStatus())) {
            return;
        }

        List<RaceRegistrations> registrations = raceRegistrationsRepository.findByRaces_Id(race.getId());
        boolean hasPendingFinalReview = registrations.stream()
                .filter(registration -> RaceRegistrationStatus.APPROVED.equalsValue(registration.getStatus()))
                .filter(this::hasConfirmedJockeyAssignment)
                .anyMatch(registration -> ChiefInspectionStatus.PENDING.equalsValue(registration.getChiefInspectionStatus()));
        if (hasPendingFinalReview) {
            return;
        }

        boolean hasFinalApprovedParticipant = registrations.stream()
                .filter(registration -> RaceRegistrationStatus.APPROVED.equalsValue(registration.getStatus()))
                .filter(registration -> ChiefInspectionStatus.APPROVED.equalsValue(registration.getChiefInspectionStatus()))
                .anyMatch(this::hasConfirmedJockeyAssignment);
        if (hasFinalApprovedParticipant) {
            race.setStatus(RaceStatus.READY.getValue());
            racesRepository.save(race);
        }
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.trim();
        return cleaned.isBlank() ? null : cleaned;
    }

    private String cleanLower(String value) {
        String cleaned = clean(value);
        return cleaned == null ? null : cleaned.toLowerCase(java.util.Locale.ROOT);
    }

}
