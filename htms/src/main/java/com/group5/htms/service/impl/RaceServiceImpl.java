package com.group5.htms.service.impl;

import com.group5.htms.dto.jockeyassignment.response.JockeyAssignmentListResponse;
import com.group5.htms.dto.race.response.RaceGateAvailabilityResponse;
import com.group5.htms.dto.race.response.RaceBettingOpenResponse;
import com.group5.htms.dto.race.request.RaceCreateRequest;
import com.group5.htms.dto.race.request.RaceStartRequest;
import com.group5.htms.dto.race.request.RaceUpdateRequest;
import com.group5.htms.dto.race.response.RaceListResponse;
import com.group5.htms.dto.race.response.RaceResponse;
import com.group5.htms.dto.race.response.RaceStartResponse;
import com.group5.htms.dto.race.response.ScheduledRaceCountResponse;
import com.group5.htms.dto.schedule.request.TournamentScheduleCreateRequest;
import com.group5.htms.dto.schedule.request.TournamentScheduleUpdateRequest;
import com.group5.htms.dto.schedule.response.TournamentScheduleResponse;
import com.group5.htms.entity.RaceRegistrations;
import com.group5.htms.entity.Races;
import com.group5.htms.entity.TournamentSchedules;
import com.group5.htms.entity.Tournaments;
import com.group5.htms.enums.RaceRegistrationStatus;
import com.group5.htms.enums.RaceResultStatus;
import com.group5.htms.enums.RaceStatus;
import com.group5.htms.enums.JockeyAssignmentStatus;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.exception.ResourceNotFoundException;
import com.group5.htms.mapper.JockeyAssignmentMapper;
import com.group5.htms.mapper.RaceMapper;
import com.group5.htms.mapper.TournamentScheduleMapper;
import com.group5.htms.repository.JockeyHorseAssignmentsRepository;
import com.group5.htms.repository.RaceRefereeAssignmentsRepository;
import com.group5.htms.repository.RaceRegistrationsRepository;
import com.group5.htms.repository.RacePointRulesRepository;
import com.group5.htms.repository.RaceResultsRepository;
import com.group5.htms.repository.RacesRepository;
import com.group5.htms.repository.TournamentSchedulesRepository;
import com.group5.htms.repository.TournamentsRepository;
import com.group5.htms.service.BetOptionService;
import com.group5.htms.service.RaceService;
import com.group5.htms.validation.RaceValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class RaceServiceImpl implements RaceService {
    private static final int MIN_GATE_COUNT = 8;
    private static final List<String> RELEASED_REGISTRATION_STATUSES = List.of(
            RaceRegistrationStatus.REJECTED.getValue(),
            RaceRegistrationStatus.CANCELLED.getValue()
    );


    private final RacesRepository racesRepository;
    private final TournamentsRepository tournamentsRepository;
    private final TournamentSchedulesRepository tournamentSchedulesRepository;
    private final RaceRegistrationsRepository raceRegistrationsRepository;
    private final JockeyHorseAssignmentsRepository jockeyHorseAssignmentsRepository;
    private final RaceRefereeAssignmentsRepository raceRefereeAssignmentsRepository;
    private final RacePointRulesRepository racePointRulesRepository;
    private final RaceResultsRepository raceResultsRepository;
    private final JockeyAssignmentMapper jockeyAssignmentMapper;
    private final RaceMapper raceMapper;
    private final TournamentScheduleMapper tournamentScheduleMapper;
    private final BetOptionService betOptionService;
    private final RaceValidator raceValidator;
    private final RaceParticipationCancellationService raceParticipationCancellationService;

    @Override
    @Transactional(readOnly = true)
    public ScheduledRaceCountResponse getScheduledRaceCount() {
        return ScheduledRaceCountResponse.builder()
                .scheduledRaceCount(racesRepository.countByStatusIgnoreCase(RaceStatus.SCHEDULED.getValue()))
                .build();
    }

    @Override
    @Transactional
    public TournamentScheduleResponse createSchedule(
            Integer tournamentId,
            TournamentScheduleCreateRequest request
    ) {
        if (tournamentId == null) {
            throw new BadRequestException("Tournament id is required");
        }

        if (request == null) {
            throw new BadRequestException("Schedule request is required");
        }

        Tournaments tournament = tournamentsRepository.findById(tournamentId)
                .orElseThrow(() -> new BadRequestException("Tournament not found"));

        raceValidator.ensureTournamentCanArrangeRace(tournament);
        raceValidator.ensureScheduleDateWithinTournament(tournament, request.getRaceDate());
        validateDuplicateSchedule(tournamentId, request);

        TournamentSchedules schedule = tournamentScheduleMapper.toEntity(request, tournament);
        TournamentSchedules savedSchedule = tournamentSchedulesRepository.save(schedule);

        return tournamentScheduleMapper.toResponse(savedSchedule);
    }

    @Override
    @Transactional
    public RaceResponse createRace(Integer scheduleId, RaceCreateRequest request) {
        if (scheduleId == null) {
            throw new BadRequestException("Schedule id is required");
        }

        if (request == null) {
            throw new BadRequestException("Race request is required");
        }

        TournamentSchedules schedule = tournamentSchedulesRepository.findById(scheduleId)
                .orElseThrow(() -> new BadRequestException("Schedule not found"));

        raceValidator.ensureTournamentCanArrangeRace(schedule.getTournaments());
        validateRaceRequest(schedule, request);

        Races race = raceMapper.toEntity(request, schedule);
        Races savedRace = racesRepository.save(race);

        return raceMapper.toResponse(
                savedRace,
                0L,
                0L,
                0L,
                racePointRulesRepository.findByRace_IdOrderByFinishPositionAsc(savedRace.getId())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TournamentScheduleResponse> getSchedulesByTournament(Integer tournamentId) {
        if (tournamentId == null) {
            throw new BadRequestException("Tournament id is required");
        }

        if (!tournamentsRepository.existsById(tournamentId)) {
            throw new BadRequestException("Tournament not found");
        }

        return tournamentSchedulesRepository
                .findByTournamentsIdOrderByRaceDateAscDayNumberAsc(tournamentId)
                .stream()
                .map(tournamentScheduleMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TournamentScheduleResponse getScheduleById(Integer scheduleId) {
        return tournamentScheduleMapper.toResponse(getScheduleEntity(scheduleId));
    }

    @Override
    @Transactional
    public TournamentScheduleResponse updateSchedule(
            Integer scheduleId,
            TournamentScheduleUpdateRequest request
    ) {
        if (request == null) {
            throw new BadRequestException("Schedule update request is required");
        }

        TournamentSchedules schedule = getScheduleEntity(scheduleId);
        Tournaments tournament = schedule.getTournaments();

        raceValidator.ensureTournamentCanArrangeRace(tournament);
        validateScheduleUpdate(schedule, request);

        tournamentScheduleMapper.updateEntity(schedule, request);

        return tournamentScheduleMapper.toResponse(
                tournamentSchedulesRepository.save(schedule)
        );
    }


    @Override
    @Transactional(readOnly = true)
    public RaceResponse getRaceById(Integer raceId) {
        return toDetailResponse(getRaceEntity(raceId));
    }

    @Override
    @Transactional
    public RaceResponse updateRace(Integer raceId, RaceUpdateRequest request) {
        if (request == null) {
            throw new BadRequestException("Race update request is required");
        }

        Races race = getRaceEntity(raceId);
        raceValidator.ensureTournamentCanArrangeRace(race.getSchedule().getTournaments());
        validateRaceUpdateRequest(race, request);

        raceMapper.updateEntity(race, request);
        Races savedRace = racesRepository.save(race);

        return toDetailResponse(savedRace);
    }

    @Override
    @Transactional
    public RaceStartResponse startRace(Integer raceId, RaceStartRequest request) {
        Races race = getRaceEntity(raceId);
        String previousStatus = race.getStatus();

        raceValidator.ensureRaceCanStart(race, request);
        validateRaceHasRequiredAssignments(race.getId());

        race.setStatus(RaceStatus.IN_PROGRESS.getValue());
        Races savedRace = racesRepository.save(race);

        return RaceStartResponse.builder()
                .raceId(savedRace.getId())
                .raceName(savedRace.getName())
                .previousStatus(previousStatus)
                .status(savedRace.getStatus())
                .scheduledAt(savedRace.getScheduledAt())
                .predictionClosesAt(savedRace.getPredictionClosesAt())
                .bettingClosed(true)
                .message("Race started successfully")
                .build();
    }

    @Override
    @Transactional
    public RaceBettingOpenResponse openBetting(Integer raceId) {
        Races race = getRaceEntity(raceId);
        String previousStatus = race.getStatus();

        raceValidator.ensureRaceCanOpenBetting(race);
        race.setStatus(RaceStatus.OPEN_FOR_BETTING.getValue());
        Races savedRace = racesRepository.save(race);

        return RaceBettingOpenResponse.builder()
                .raceId(savedRace.getId())
                .raceName(savedRace.getName())
                .previousStatus(previousStatus)
                .status(savedRace.getStatus())
                .scheduledAt(savedRace.getScheduledAt())
                .predictionClosesAt(savedRace.getPredictionClosesAt())
                .betOptions(betOptionService.generateBetOptionsForRace(savedRace.getId()))
                .message("Betting opened successfully")
                .build();
    }

    @Override
    @Transactional
    public RaceResponse completeRace(Integer raceId) {
        throw new BadRequestException("Use race result publish workflow to complete race");
    }

    @Override
    @Transactional
    public void cancelRace(Integer raceId) {
        Races race = getRaceEntity(raceId);

        raceValidator.ensureTournamentCanArrangeRace(race.getSchedule().getTournaments());

        if (RaceStatus.COMPLETED.getValue().equalsIgnoreCase(race.getStatus())) {
            throw new BadRequestException("Completed race cannot be cancelled");
        }

        race.setStatus(RaceStatus.CANCELLED.getValue());
        raceParticipationCancellationService.cancelRaceParticipants(race.getId());
        racesRepository.save(race);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RaceListResponse> getRacesByTournament(Integer tournamentId, String status) {
        if (!tournamentsRepository.existsById(tournamentId)) {
            throw new ResourceNotFoundException("Tournament not found");
        }

        if (status != null && !status.isBlank()) {
            if (!RaceStatus.isValid(status)) {
                throw new BadRequestException("Invalid race status");
            }

            return racesRepository
                    .findBySchedule_Tournaments_IdAndStatusIgnoreCaseOrderByScheduledAtAsc(tournamentId, status.trim())
                    .stream()
                    .map(this::toResponseWithCounts)
                    .toList();
        }

        return racesRepository.findBySchedule_Tournaments_IdOrderByScheduledAtAsc(tournamentId)
                .stream()
                .map(this::toResponseWithCounts)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<JockeyAssignmentListResponse> getApprovedParticipantsByRace(Integer raceId) {
        Races race = getRaceEntity(raceId);
        return jockeyHorseAssignmentsRepository
                .findByRaces_IdAndStatusIgnoreCaseAndReg_StatusIgnoreCaseOrderByReg_GateNumberAsc(
                        race.getId(),
                        JockeyAssignmentStatus.CONFIRMED.getValue(),
                        RaceRegistrationStatus.APPROVED.getValue()
                )
                .stream()
                .map(jockeyAssignmentMapper::toListResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RaceGateAvailabilityResponse getAvailableGates(Integer raceId) {
        Races race = getRaceEntity(raceId);
        int gateCount = gateCount(race);
        Set<Integer> occupiedGates = raceRegistrationsRepository
                .findByRaces_IdAndStatusNotInOrderByGateNumberAsc(race.getId(), RELEASED_REGISTRATION_STATUSES)
                .stream()
                .map(RaceRegistrations::getGateNumber)
                .filter(gate -> gate != null && gate >= 1 && gate <= gateCount)
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));

        List<Integer> availableGates = IntStream.rangeClosed(1, gateCount)
                .filter(gate -> !occupiedGates.contains(gate))
                .boxed()
                .toList();

        return RaceGateAvailabilityResponse.builder()
                .raceId(race.getId())
                .raceName(race.getName())
                .maxHorses(race.getMaxHorses())
                .gateCount(gateCount)
                .availableGates(availableGates)
                .occupiedGates(List.copyOf(occupiedGates))
                .build();
    }

    private RaceListResponse toResponseWithCounts(Races race) {
        Integer raceId = race.getId();

        return raceMapper.toListResponse(
                race,
                raceRegistrationsRepository.countByRaces_IdAndStatusIgnoreCase(
                        raceId,
                        RaceRegistrationStatus.APPROVED.getValue()
                ),
                jockeyHorseAssignmentsRepository.countByRaces_IdAndStatusIgnoreCase(raceId, JockeyAssignmentStatus.ACCEPTED.getValue()),
                raceRefereeAssignmentsRepository.countByRaces_Id(raceId)
        );
    }

    private RaceResponse toDetailResponse(Races race) {
        Integer raceId = race.getId();

        return raceMapper.toResponse(
                race,
                raceRegistrationsRepository.countByRaces_IdAndStatusIgnoreCase(
                        raceId,
                        RaceRegistrationStatus.APPROVED.getValue()
                ),
                jockeyHorseAssignmentsRepository.countByRaces_IdAndStatusIgnoreCase(raceId, JockeyAssignmentStatus.ACCEPTED.getValue()),
                raceRefereeAssignmentsRepository.countByRaces_Id(raceId),
                racePointRulesRepository.findByRace_IdOrderByFinishPositionAsc(raceId)
        );
    }

    private int gateCount(Races race) {
        Integer maxHorses = race == null ? null : race.getMaxHorses();
        return Math.max(MIN_GATE_COUNT, maxHorses == null ? MIN_GATE_COUNT : maxHorses);
    }

    private TournamentSchedules getScheduleEntity(Integer scheduleId) {
        if (scheduleId == null) {
            throw new BadRequestException("Schedule id is required");
        }

        return tournamentSchedulesRepository.findById(scheduleId)
                .orElseThrow(() -> new BadRequestException("Schedule not found"));
    }

    private Races getRaceEntity(Integer raceId) {
        if (raceId == null) {
            throw new BadRequestException("Race id is required");
        }

        return racesRepository.findById(raceId)
                .orElseThrow(() -> new BadRequestException("Race not found"));
    }

    private void validateDuplicateSchedule(
            Integer tournamentId,
            TournamentScheduleCreateRequest request
    ) {
        if (tournamentSchedulesRepository.existsByTournamentsIdAndDayNumber(
                tournamentId,
                request.getDayNumber()
        )) {
            throw new BadRequestException("Schedule day number already exists in this tournament");
        }

        if (tournamentSchedulesRepository.existsByTournamentsIdAndRaceDate(
                tournamentId,
                request.getRaceDate()
        )) {
            throw new BadRequestException("Schedule race date already exists in this tournament");
        }
    }

    private void validateScheduleUpdate(
            TournamentSchedules schedule,
            TournamentScheduleUpdateRequest request
    ) {
        Tournaments tournament = schedule.getTournaments();
        Integer tournamentId = tournament.getId();

        if (request.getRaceDate() != null) {
            raceValidator.ensureScheduleDateWithinTournament(tournament, request.getRaceDate());

            if (!request.getRaceDate().equals(schedule.getRaceDate())
                    && racesRepository.countByScheduleId(schedule.getId()) > 0) {
                throw new BadRequestException("Cannot change race date after races have been created");
            }

            if (tournamentSchedulesRepository.existsByTournamentsIdAndRaceDateAndIdNot(
                    tournamentId,
                    request.getRaceDate(),
                    schedule.getId()
            )) {
                throw new BadRequestException("Schedule race date already exists in this tournament");
            }
        }

        if (request.getDayNumber() != null
                && tournamentSchedulesRepository.existsByTournamentsIdAndDayNumberAndIdNot(
                tournamentId,
                request.getDayNumber(),
                schedule.getId()
        )) {
            throw new BadRequestException("Schedule day number already exists in this tournament");
        }
    }

    private void validateRaceRequest(TournamentSchedules schedule, RaceCreateRequest request) {
        if (racesRepository.existsByScheduleIdAndRaceNumber(
                schedule.getId(),
                request.getRaceNumber()
        )) {
            throw new BadRequestException("Race number already exists in this schedule");
        }

        raceValidator.ensureScheduledAtMatchesSchedule(schedule, request.getScheduledAt());

        raceValidator.ensurePredictionClosesBeforeRace(request.getPredictionClosesAt(), request.getScheduledAt());

        String status = request.getStatus();

        if (status != null
                && !status.isBlank()
                && !RaceStatus.SCHEDULED.getValue().equalsIgnoreCase(status.trim())
                && !RaceStatus.UPCOMING.getValue().equalsIgnoreCase(status.trim())) {
            throw new BadRequestException("Invalid race status");
        }
    }

    private void validateRaceUpdateRequest(Races race, RaceUpdateRequest request) {
        if (request.getRaceNumber() != null
                && racesRepository.existsByScheduleIdAndRaceNumberAndIdNot(
                race.getSchedule().getId(),
                request.getRaceNumber(),
                race.getId()
        )) {
            throw new BadRequestException("Race number already exists in this schedule");
        }

        var scheduledAt = request.getScheduledAt() == null
                ? race.getScheduledAt()
                : request.getScheduledAt();

        var predictionClosesAt = request.getPredictionClosesAt() == null
                ? race.getPredictionClosesAt()
                : request.getPredictionClosesAt();

        raceValidator.ensureScheduledAtMatchesSchedule(race.getSchedule(), scheduledAt);

        raceValidator.ensurePredictionClosesBeforeRace(predictionClosesAt, scheduledAt);

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            throw new BadRequestException("Use workflow transition APIs to update status");
        }
    }

    private void validateRaceHasRequiredAssignments(Integer raceId) {
        if (jockeyHorseAssignmentsRepository.countByRaces_IdAndStatusIgnoreCase(
                raceId,
                JockeyAssignmentStatus.CONFIRMED.getValue()
        ) < 1) {
            throw new BadRequestException("Race must have at least one confirmed jockey assignment before starting");
        }

        if (raceRefereeAssignmentsRepository.countByRaces_Id(raceId) < 1) {
            throw new BadRequestException("Race must have at least one assigned referee before starting");
        }
    }
}

