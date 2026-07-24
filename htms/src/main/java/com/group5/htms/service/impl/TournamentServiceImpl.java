package com.group5.htms.service.impl;

import com.group5.htms.dto.tournament.request.CloseRegistrationRequest;
import com.group5.htms.dto.tournament.request.OpenRegistrationRequest;
import com.group5.htms.dto.tournament.request.TournamentCreateRequest;
import com.group5.htms.dto.tournament.request.TournamentUpdateRequest;
import com.group5.htms.dto.tournament.response.CloseRegistrationResponse;
import com.group5.htms.dto.tournament.response.GlobalTournamentCountResponse;
import com.group5.htms.dto.tournament.response.OpenRegistrationResponse;
import com.group5.htms.dto.tournament.response.TournamentDetailResponse;
import com.group5.htms.dto.tournament.response.TournamentResponse;
import com.group5.htms.dto.tournament.response.TournamentSummaryResponse;
import com.group5.htms.entity.JockeyHorseAssignments;
import com.group5.htms.entity.PrizeDistributions;
import com.group5.htms.entity.RaceRegistrations;
import com.group5.htms.entity.Races;
import com.group5.htms.entity.Tournaments;
import com.group5.htms.entity.Users;
import com.group5.htms.enums.JockeyAssignmentStatus;
import com.group5.htms.enums.RaceRegistrationStatus;
import com.group5.htms.enums.RaceStatus;
import com.group5.htms.enums.TournamentStatus;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.exception.UnauthorizedException;
import com.group5.htms.mapper.TournamentMapper;
import com.group5.htms.repository.JockeyHorseAssignmentsRepository;
import com.group5.htms.repository.PrizeRepository;
import com.group5.htms.repository.RaceRegistrationsRepository;
import com.group5.htms.repository.RacesRepository;
import com.group5.htms.repository.TournamentSchedulesRepository;
import com.group5.htms.repository.TournamentsRepository;
import com.group5.htms.repository.UsersRepository;
import com.group5.htms.service.TournamentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TournamentServiceImpl implements TournamentService {
    private static final List<String> ACTIVE_ASSIGNMENT_STATUSES = List.of(
            JockeyAssignmentStatus.PENDING.getValue(),
            JockeyAssignmentStatus.ACCEPTED.getValue(),
            JockeyAssignmentStatus.CONFIRMED.getValue()
    );

    private final TournamentsRepository tournamentsRepository;
    private final UsersRepository usersRepository;
    private final TournamentSchedulesRepository tournamentSchedulesRepository;
    private final RacesRepository racesRepository;
    private final PrizeRepository prizeRepository;
    private final RaceRegistrationsRepository raceRegistrationsRepository;
    private final JockeyHorseAssignmentsRepository jockeyHorseAssignmentsRepository;
    private final TournamentMapper tournamentMapper;

    @Override
    @Transactional(readOnly = true)
    public GlobalTournamentCountResponse getGlobalTournamentCount() {
        return GlobalTournamentCountResponse.builder()
                .globalTournamentCount(tournamentsRepository.count())
                .build();
    }

    @Override
    @Transactional
    public TournamentResponse createTournament(TournamentCreateRequest request) {
        validateCreateRequest(request);

        if (tournamentsRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new BadRequestException("Tournament name already exists");
        }

        Users currentUser = getCurrentUser();

        Tournaments tournament = tournamentMapper.toEntity(request);
        tournament.setCreatedBy(currentUser);

        Tournaments savedTournament = tournamentsRepository.save(tournament);

        return tournamentMapper.toResponse(savedTournament);
    }

    @Override
    @Transactional
    public TournamentResponse updateTournament(Integer tournamentId, TournamentUpdateRequest request) {
        Tournaments tournament = getTournamentEntity(tournamentId);

        validateUpdateRequest(tournament, request);

        tournamentMapper.updateEntity(tournament, request);

        Tournaments savedTournament = tournamentsRepository.save(tournament);

        return tournamentMapper.toResponse(savedTournament);
    }

    @Override
    @Transactional(readOnly = true)
    public TournamentDetailResponse getTournamentById(Integer tournamentId) {
        Tournaments tournament = getTournamentEntity(tournamentId);

        return tournamentMapper.toDetailResponse(
                tournament,
                tournamentSchedulesRepository
                        .findByTournamentsIdOrderByRaceDateAscDayNumberAsc(tournamentId),
                prizeRepository.findByTournamentsIdOrderByFinishPositionAsc(tournamentId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TournamentSummaryResponse> getAllTournaments(String status) {
        List<Tournaments> tournaments;

        if (status != null && !status.isBlank()) {
            String normalizedStatus = status.trim().toLowerCase();

            if (!TournamentStatus.isValid(normalizedStatus)) {
                throw new BadRequestException("Invalid tournament status");
            }

            tournaments = tournamentsRepository
                    .findByStatusIgnoreCaseOrderByStartDateAsc(normalizedStatus);
        } else {
            tournaments = tournamentsRepository.findAllByOrderByStartDateAsc();
        }

        return tournaments.stream()
                .map(tournamentMapper::toSummaryResponse)
                .toList();
    }


    @Override
    @Transactional
    public TournamentResponse cancelTournament(Integer tournamentId) {
        Tournaments tournament = getTournamentEntity(tournamentId);

        if (TournamentStatus.CANCELLED.getValue().equalsIgnoreCase(tournament.getStatus())) {
            throw new BadRequestException("Tournament is already cancelled");
        }

        if (TournamentStatus.COMPLETED.getValue().equalsIgnoreCase(tournament.getStatus())) {
            throw new BadRequestException("Completed tournament cannot be cancelled");
        }

        tournament.setStatus(TournamentStatus.CANCELLED.getValue());

        Tournaments savedTournament = tournamentsRepository.save(tournament);

        return tournamentMapper.toResponse(savedTournament);
    }

    @Override
    @Transactional
    public OpenRegistrationResponse openRegistration(Integer tournamentId, OpenRegistrationRequest request) {
        if (tournamentId == null) {
            throw new BadRequestException("Tournament id is required");
        }

        if (request == null) {
            throw new BadRequestException("Open registration request is required");
        }

        if (request.getRegistrationCloseAt() == null) {
            throw new BadRequestException("Registration close time is required");
        }

        Tournaments tournament = tournamentsRepository.findById(tournamentId)
                .orElseThrow(() -> new BadRequestException("Tournament not found"));

        validateTournamentCanOpenRegistration(tournament);

        long totalSchedules = validateTournamentHasSchedules(tournamentId);
        List<Races> races = validateTournamentHasRaces(tournamentId);

        validatePrizeDistributionReadyForRegistration(tournament);
        validateRegistrationCloseBeforeFirstRace(request.getRegistrationCloseAt(), races);

        Instant registrationOpenAt = request.getRegistrationOpenAt() == null
                ? Instant.now()
                : request.getRegistrationOpenAt();

        tournament.setStatus(TournamentStatus.REGISTRATION_OPEN.getValue());
        tournament.setRegistrationOpenAt(registrationOpenAt);
        tournament.setRegistrationCloseAt(request.getRegistrationCloseAt());
        openScheduledRacesForRegistration(races);

        Tournaments savedTournament = tournamentsRepository.save(tournament);
        racesRepository.saveAll(races);

        return OpenRegistrationResponse.builder()
                .tournamentId(savedTournament.getId())
                .tournamentName(savedTournament.getName())
                .status(savedTournament.getStatus())
                .registrationOpenAt(registrationOpenAt)
                .registrationCloseAt(request.getRegistrationCloseAt())
                .totalSchedules((int) totalSchedules)
                .totalRaces(races.size())
                .build();
    }

    @Override
    @Transactional
    public CloseRegistrationResponse closeRegistration(Integer tournamentId, CloseRegistrationRequest request) {
        if (tournamentId == null) {
            throw new BadRequestException("Tournament id is required");
        }

        CloseRegistrationRequest closeRequest = request == null
                ? new CloseRegistrationRequest()
                : request;

        Tournaments tournament = tournamentsRepository.findById(tournamentId)
                .orElseThrow(() -> new BadRequestException("Tournament not found"));

        if (!TournamentStatus.REGISTRATION_OPEN.equalsValue(tournament.getStatus())) {
            throw new BadRequestException("Only registration open tournaments can be closed");
        }

        List<Races> registrationOpenRaces = racesRepository
                .findBySchedule_Tournaments_IdAndStatusIgnoreCaseOrderByScheduledAtAsc(
                        tournamentId,
                        RaceStatus.REGISTRATION_OPEN.getValue()
                );
        List<RaceRegistrations> registrations = raceRegistrationsRepository.findByTournaments_Id(tournamentId)
                .stream()
                .toList();
        List<JockeyHorseAssignments> assignments = jockeyHorseAssignmentsRepository.findByReg_IdIn(
                registrations.stream().map(RaceRegistrations::getId).toList()
        );
        Set<Integer> confirmedRegistrationIds = assignments.stream()
                .filter(assignment -> JockeyAssignmentStatus.CONFIRMED.equalsValue(assignment.getStatus()))
                .map(assignment -> assignment.getReg().getId())
                .collect(Collectors.toSet());
        Map<Integer, List<JockeyHorseAssignments>> assignmentsByRegistrationId =
                groupAssignmentsByRegistrationId(assignments);

        List<RaceRegistrations> pendingRegistrations = registrations.stream()
                .filter(registration -> RaceRegistrationStatus.PENDING.equalsValue(registration.getStatus()))
                .toList();
        List<RaceRegistrations> pendingRegistrationsWithActiveInvitation = pendingRegistrations.stream()
                .filter(registration -> hasActiveJockeyAssignment(registration, assignmentsByRegistrationId))
                .toList();
        List<RaceRegistrations> pendingRegistrationsWithoutActiveInvitation = pendingRegistrations.stream()
                .filter(registration -> !hasActiveJockeyAssignment(registration, assignmentsByRegistrationId))
                .toList();
        List<RaceRegistrations> pendingRegistrationsToReject = closeRequest.isAutoRejectPending()
                ? pendingRegistrations
                : pendingRegistrationsWithoutActiveInvitation;
        List<RaceRegistrations> approvedUnconfirmedRegistrations = registrations.stream()
                .filter(registration -> RaceRegistrationStatus.APPROVED.equalsValue(registration.getStatus()))
                .filter(registration -> !confirmedRegistrationIds.contains(registration.getId()))
                .toList();

        validateOpenRacesHaveEligibleRegistrations(
                registrationOpenRaces,
                registrations,
                confirmedRegistrationIds,
                assignmentsByRegistrationId,
                closeRequest.isAllowCloseWithoutEligibleRaces()
        );

        if (!pendingRegistrationsWithActiveInvitation.isEmpty() && !closeRequest.isAutoRejectPending()) {
            throw new BadRequestException(
                    "There are pending registrations that still have active jockey invitations. Enable auto reject pending registrations before closing, or resolve these registrations first: "
                            + formatRegistrationDetails(pendingRegistrationsWithActiveInvitation, assignmentsByRegistrationId)
            );
        }

        if (!approvedUnconfirmedRegistrations.isEmpty() && !closeRequest.isAutoCancelUnconfirmed()) {
            throw new BadRequestException(
                    "There are approved registrations without confirmed jockey assignments: "
                            + formatRegistrationDetails(approvedUnconfirmedRegistrations, assignmentsByRegistrationId)
            );
        }

        Instant now = Instant.now();
        pendingRegistrationsToReject.forEach(registration -> {
            registration.setStatus(RaceRegistrationStatus.REJECTED.getValue());
            registration.setOwnerConfirmationStatus(RaceRegistrationStatus.REJECTED.getValue());
            registration.setJockey(null);
            registration.setOwnerConfirmedAt(null);
        });
        cancelActiveAssignmentsForRegistrations(assignments, pendingRegistrationsToReject, now);

        if (closeRequest.isAutoCancelUnconfirmed()) {
            approvedUnconfirmedRegistrations.forEach(registration -> {
                registration.setStatus(RaceRegistrationStatus.CANCELLED.getValue());
                registration.setOwnerConfirmationStatus(RaceRegistrationStatus.CANCELLED.getValue());
                registration.setJockey(null);
                registration.setOwnerConfirmedAt(null);
            });
            cancelActiveAssignmentsForRegistrations(assignments, approvedUnconfirmedRegistrations, now);
        }

        Set<Integer> readyRaceIds = registrations.stream()
                .filter(registration -> RaceRegistrationStatus.APPROVED.equalsValue(registration.getStatus()))
                .filter(registration -> confirmedRegistrationIds.contains(registration.getId()))
                .map(registration -> registration.getRaces().getId())
                .collect(Collectors.toSet());

        int readyRaceCount = 0;
        int closedRaceCount = 0;

        for (Races race : registrationOpenRaces) {
            if (readyRaceIds.contains(race.getId())) {
                race.setStatus(RaceStatus.READY.getValue());
                readyRaceCount++;
            } else {
                race.setStatus(RaceStatus.REGISTRATION_CLOSED.getValue());
                closedRaceCount++;
            }
        }

        tournament.setStatus(TournamentStatus.REGISTRATION_CLOSED.getValue());

        raceRegistrationsRepository.saveAll(registrations);
        jockeyHorseAssignmentsRepository.saveAll(assignments);
        racesRepository.saveAll(registrationOpenRaces);
        Tournaments savedTournament = tournamentsRepository.save(tournament);

        return CloseRegistrationResponse.builder()
                .tournamentId(savedTournament.getId())
                .tournamentName(savedTournament.getName())
                .status(savedTournament.getStatus())
                .rejectedPendingRegistrations(pendingRegistrationsToReject.size())
                .cancelledUnconfirmedRegistrations(
                        closeRequest.isAutoCancelUnconfirmed()
                                ? approvedUnconfirmedRegistrations.size()
                                : 0
                )
                .closedRaceCount(closedRaceCount)
                .readyRaceCount(readyRaceCount)
                .message(closeRequest.isAllowCloseWithoutEligibleRaces()
                        ? "Registration closed successfully. Races without eligible horses were closed without being marked ready"
                        : "Registration closed successfully. Every registration-open race has at least one approved horse with a confirmed jockey assignment")
                .build();
    }

    private void validateOpenRacesHaveEligibleRegistrations(
            List<Races> registrationOpenRaces,
            List<RaceRegistrations> registrations,
            Set<Integer> confirmedRegistrationIds,
            Map<Integer, List<JockeyHorseAssignments>> assignmentsByRegistrationId,
            boolean allowCloseWithoutEligibleRaces
    ) {
        if (registrationOpenRaces.isEmpty() && !allowCloseWithoutEligibleRaces) {
            throw new BadRequestException("No registration-open races found for this tournament");
        }

        Map<Integer, List<RaceRegistrations>> registrationsByRaceId = registrations.stream()
                .filter(this::isActiveRegistration)
                .collect(Collectors.groupingBy(registration -> registration.getRaces().getId()));

        for (Races race : registrationOpenRaces) {
            List<RaceRegistrations> raceRegistrations = registrationsByRaceId.getOrDefault(race.getId(), List.of());

            if (raceRegistrations.isEmpty()) {
                if (allowCloseWithoutEligibleRaces) {
                    continue;
                }
                throw new BadRequestException(
                        "Race " + race.getId() + " - " + race.getName()
                                + " has no horse registrations; cannot close tournament registration"
                );
            }

            long eligibleHorseCount = raceRegistrations.stream()
                    .filter(registration -> RaceRegistrationStatus.APPROVED.equalsValue(registration.getStatus()))
                    .filter(registration -> confirmedRegistrationIds.contains(registration.getId()))
                    .count();

            if (eligibleHorseCount <= 0) {
                if (allowCloseWithoutEligibleRaces) {
                    continue;
                }
                List<RaceRegistrations> notEligibleRegistrations = raceRegistrations.stream()
                        .filter(registration -> !isEligibleRegistration(registration, confirmedRegistrationIds))
                        .toList();

                throw new BadRequestException(
                        "Race " + race.getId() + " - " + race.getName()
                                + " has no approved horses with confirmed jockey assignments; cannot close tournament registration"
                                + formatNotEligibleRegistrationSuffix(
                                notEligibleRegistrations,
                                assignmentsByRegistrationId
                        )
                );
            }
        }
    }

    private boolean isEligibleRegistration(
            RaceRegistrations registration,
            Set<Integer> confirmedRegistrationIds
    ) {
        return RaceRegistrationStatus.APPROVED.equalsValue(registration.getStatus())
                && confirmedRegistrationIds.contains(registration.getId());
    }

    private boolean isActiveRegistration(RaceRegistrations registration) {
        return registration != null
                && !RaceRegistrationStatus.REJECTED.equalsValue(registration.getStatus())
                && !RaceRegistrationStatus.CANCELLED.equalsValue(registration.getStatus());
    }

    private boolean hasActiveJockeyAssignment(
            RaceRegistrations registration,
            Map<Integer, List<JockeyHorseAssignments>> assignmentsByRegistrationId
    ) {
        return assignmentsByRegistrationId.getOrDefault(registration.getId(), List.of())
                .stream()
                .anyMatch(assignment -> JockeyAssignmentStatus.PENDING.equalsValue(assignment.getStatus())
                        || JockeyAssignmentStatus.ACCEPTED.equalsValue(assignment.getStatus())
                        || JockeyAssignmentStatus.CONFIRMED.equalsValue(assignment.getStatus()));
    }

    private void cancelActiveAssignmentsForRegistrations(
            List<JockeyHorseAssignments> assignments,
            List<RaceRegistrations> registrations,
            Instant now
    ) {
        Set<Integer> registrationIds = registrations.stream()
                .map(RaceRegistrations::getId)
                .collect(Collectors.toSet());

        if (registrationIds.isEmpty()) {
            return;
        }

        assignments.stream()
                .filter(assignment -> assignment.getReg() != null)
                .filter(assignment -> registrationIds.contains(assignment.getReg().getId()))
                .filter(assignment -> ACTIVE_ASSIGNMENT_STATUSES.stream()
                        .anyMatch(status -> status.equalsIgnoreCase(assignment.getStatus())))
                .forEach(assignment -> {
                    assignment.setStatus(JockeyAssignmentStatus.CANCELLED.getValue());
                    assignment.setCancelledAt(now);
                    assignment.setResponseDeadline(null);
                });
    }

    private Map<Integer, List<JockeyHorseAssignments>> groupAssignmentsByRegistrationId(
            List<JockeyHorseAssignments> assignments
    ) {
        return assignments.stream()
                .collect(Collectors.groupingBy(assignment -> assignment.getReg().getId()));
    }

    private String formatNotEligibleRegistrationSuffix(
            List<RaceRegistrations> registrations,
            Map<Integer, List<JockeyHorseAssignments>> assignmentsByRegistrationId
    ) {
        if (registrations.isEmpty()) {
            return "";
        }

        return ". Not eligible registration(s): "
                + formatRegistrationDetails(registrations, assignmentsByRegistrationId);
    }

    private String formatRegistrationDetails(
            List<RaceRegistrations> registrations,
            Map<Integer, List<JockeyHorseAssignments>> assignmentsByRegistrationId
    ) {
        return registrations.stream()
                .map(registration -> formatRegistrationDetail(registration, assignmentsByRegistrationId))
                .collect(Collectors.joining("; "));
    }

    private String formatRegistrationDetail(
            RaceRegistrations registration,
            Map<Integer, List<JockeyHorseAssignments>> assignmentsByRegistrationId
    ) {
        List<JockeyHorseAssignments> assignments = assignmentsByRegistrationId.getOrDefault(
                registration.getId(),
                List.of()
        );

        return "Registration #" + registration.getId()
                + " for horse " + safeHorseName(registration)
                + " by " + safeOwnerName(registration)
                + " in " + safeRaceName(registration)
                + " is not eligible because the registration is " + registration.getStatus()
                + " and owner confirmation is " + registration.getOwnerConfirmationStatus()
                + ". " + formatAssignmentDetails(assignments);
    }

    private String formatAssignmentDetails(List<JockeyHorseAssignments> assignments) {
        if (assignments.isEmpty()) {
            return "No jockey invitation has been confirmed";
        }

        return assignments.stream()
                .map(assignment -> "Invitation #" + assignment.getId()
                        + " to " + safeJockeyName(assignment)
                        + " is " + assignment.getStatus())
                .collect(Collectors.joining("; "));
    }

    private String safeHorseName(RaceRegistrations registration) {
        return registration.getHorses() == null ? "unknown" : registration.getHorses().getName();
    }

    private String safeOwnerName(RaceRegistrations registration) {
        if (registration.getOwner() == null) {
            return "unknown";
        }
        if (registration.getOwner().getStableName() != null
                && !registration.getOwner().getStableName().isBlank()) {
            return registration.getOwner().getStableName();
        }
        return registration.getOwner().getUsers() == null
                ? "ownerId=" + registration.getOwner().getId()
                : registration.getOwner().getUsers().getFullName();
    }

    private String safeRaceName(RaceRegistrations registration) {
        return registration.getRaces() == null ? "unknown" : registration.getRaces().getName();
    }

    private String safeJockeyName(JockeyHorseAssignments assignment) {
        if (assignment.getJockey() == null) {
            return "unknown";
        }
        return assignment.getJockey().getUsers() == null
                ? "jockeyId=" + assignment.getJockey().getId()
                : assignment.getJockey().getUsers().getFullName();
    }

    @Override
    @Transactional
    public TournamentResponse startTournament(Integer tournamentId) {
        Tournaments tournament = getTournamentEntity(tournamentId);

        if (TournamentStatus.CANCELLED.equalsValue(tournament.getStatus())) {
            throw new BadRequestException("Cancelled tournament cannot be started");
        }

        if (TournamentStatus.COMPLETED.equalsValue(tournament.getStatus())) {
            throw new BadRequestException("Completed tournament cannot be started");
        }

        if (TournamentStatus.IN_PROGRESS.equalsValue(tournament.getStatus())) {
            throw new BadRequestException("Tournament is already in progress");
        }

        if (!TournamentStatus.REGISTRATION_CLOSED.equalsValue(tournament.getStatus())) {
            throw new BadRequestException("Only registration closed tournaments can be started");
        }

        List<Races> races = racesRepository.findBySchedule_Tournaments_IdOrderByScheduledAtAsc(tournamentId);
        if (races.isEmpty()) {
            throw new BadRequestException("Tournament must have at least one race before starting");
        }

        boolean hasRunnableRace = races.stream().anyMatch(this::isRunnableRace);
        if (!hasRunnableRace) {
            throw new BadRequestException("Tournament must have at least one ready race before starting");
        }

        tournament.setStatus(TournamentStatus.IN_PROGRESS.getValue());

        return tournamentMapper.toResponse(tournamentsRepository.save(tournament));
    }

    @Override
    @Transactional
    public TournamentResponse completeTournament(Integer tournamentId) {
        Tournaments tournament = getTournamentEntity(tournamentId);

        if (TournamentStatus.CANCELLED.equalsValue(tournament.getStatus())) {
            throw new BadRequestException("Cancelled tournament cannot be completed");
        }

        if (TournamentStatus.COMPLETED.equalsValue(tournament.getStatus())) {
            throw new BadRequestException("Tournament is already completed");
        }

        if (!TournamentStatus.IN_PROGRESS.equalsValue(tournament.getStatus())) {
            throw new BadRequestException("Only in progress tournaments can be completed");
        }

        List<Races> races = racesRepository.findBySchedule_Tournaments_IdOrderByScheduledAtAsc(tournamentId);
        if (races.isEmpty()) {
            throw new BadRequestException("Tournament must have at least one race before completing");
        }

        boolean hasCompletedRace = races.stream()
                .anyMatch(race -> RaceStatus.COMPLETED.equalsValue(race.getStatus()));
        if (!hasCompletedRace) {
            throw new BadRequestException("Tournament must have at least one completed race before completing");
        }

        boolean hasUnfinishedRace = races.stream()
                .anyMatch(race -> !isTerminalRace(race));
        if (hasUnfinishedRace) {
            throw new BadRequestException("All races must be completed or cancelled before completing tournament");
        }

        tournament.setStatus(TournamentStatus.COMPLETED.getValue());

        return tournamentMapper.toResponse(tournamentsRepository.save(tournament));
    }

    private Tournaments getTournamentEntity(Integer tournamentId) {
        if (tournamentId == null) {
            throw new BadRequestException("Tournament id is required");
        }

        return tournamentsRepository.findById(tournamentId)
                .orElseThrow(() -> new BadRequestException("Tournament not found"));
    }

    private void validateTournamentCanOpenRegistration(Tournaments tournament) {
        if (!TournamentStatus.UPCOMING.getValue().equalsIgnoreCase(tournament.getStatus())) {
            throw new BadRequestException("Only upcoming tournaments can be opened for registration");
        }
    }

    private long validateTournamentHasSchedules(Integer tournamentId) {
        long totalSchedules = tournamentSchedulesRepository.countByTournamentsId(tournamentId);

        if (totalSchedules <= 0) {
            throw new BadRequestException("Tournament must have at least one schedule before opening registration");
        }

        return totalSchedules;
    }

    private List<Races> validateTournamentHasRaces(Integer tournamentId) {
        List<Races> races = racesRepository.findBySchedule_Tournaments_IdOrderByScheduledAtAsc(tournamentId);

        if (races.isEmpty()) {
            throw new BadRequestException("Tournament must have at least one race before opening registration");
        }

        return races;
    }

    private void validatePrizeDistributionReadyForRegistration(Tournaments tournament) {
        List<PrizeDistributions> prizes = prizeRepository
                .findByTournamentsIdOrderByFinishPositionAsc(tournament.getId());

        if (prizes.size() != 3) {
            throw new BadRequestException("Tournament must have exactly 3 prize distributions for positions 1, 2 and 3");
        }

        Set<Integer> positions = new HashSet<>();
        BigDecimal totalPrizeAmount = BigDecimal.ZERO;

        for (PrizeDistributions prize : prizes) {
            Integer finishPosition = prize.getFinishPosition();

            if (finishPosition == null || finishPosition < 1 || finishPosition > 3) {
                throw new BadRequestException("Only finish positions 1, 2 and 3 can receive prizes");
            }

            positions.add(finishPosition);
            totalPrizeAmount = totalPrizeAmount.add(prize.getAmount() == null ? BigDecimal.ZERO : prize.getAmount());
        }

        if (!positions.equals(Set.of(1, 2, 3))) {
            throw new BadRequestException("Tournament prize distributions must include finish positions 1, 2 and 3");
        }

        BigDecimal prizePool = tournament.getPrizePool() == null ? BigDecimal.ZERO : tournament.getPrizePool();

        if (totalPrizeAmount.compareTo(prizePool) != 0) {
            throw new BadRequestException("Total prize amount must be equal to tournament prize pool");
        }
    }

    private void validateRegistrationCloseBeforeFirstRace(Instant registrationCloseAt, List<Races> races) {
        Instant firstRaceStart = races.stream()
                .map(Races::getScheduledAt)
                .filter(java.util.Objects::nonNull)
                .min(Instant::compareTo)
                .orElseThrow(() -> new BadRequestException("Tournament must have at least one race before opening registration"));

        if (!registrationCloseAt.isBefore(firstRaceStart)) {
            throw new BadRequestException("Registration close time must be before the first race starts");
        }
    }

    private void openScheduledRacesForRegistration(List<Races> races) {
        races.stream()
                .filter(race -> RaceStatus.canOpenRegistration(race.getStatus()))
                .forEach(race -> race.setStatus(RaceStatus.REGISTRATION_OPEN.getValue()));
    }

    private boolean isRunnableRace(Races race) {
        String status = race.getStatus();

        return RaceStatus.READY.equalsValue(status)
                || RaceStatus.OPEN_FOR_BETTING.equalsValue(status)
                || RaceStatus.IN_PROGRESS.equalsValue(status);
    }

    private boolean isTerminalRace(Races race) {
        String status = race.getStatus();

        return RaceStatus.COMPLETED.equalsValue(status)
                || RaceStatus.CANCELLED.equalsValue(status);
    }

    private Users getCurrentUser() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return usersRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    private void validateCreateRequest(TournamentCreateRequest request) {
        if (request == null) {
            throw new BadRequestException("Tournament request is required");
        }

        validateDateRange(request.getStartDate(), request.getEndDate());

        if (request.getStatus() != null
                && !request.getStatus().isBlank()
                && !TournamentStatus.isValid(request.getStatus())) {
            throw new BadRequestException("Invalid tournament status");
        }
    }

    private void validateUpdateRequest(
            Tournaments existingTournament,
            TournamentUpdateRequest request
    ) {
        if (request == null) {
            throw new BadRequestException("Tournament update request is required");
        }

        LocalDate startDate = request.getStartDate() != null
                ? request.getStartDate()
                : existingTournament.getStartDate();

        LocalDate endDate = request.getEndDate() != null
                ? request.getEndDate()
                : existingTournament.getEndDate();

        validateDateRange(startDate, endDate);

    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            throw new BadRequestException("Start date is required");
        }

        if (endDate == null) {
            throw new BadRequestException("End date is required");
        }

        if (endDate.isBefore(startDate)) {
            throw new BadRequestException("End date must be after or equal to start date");
        }
    }
}
