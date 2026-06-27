package com.group5.htms.service.impl;

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
import com.group5.htms.entity.Races;
import com.group5.htms.entity.TournamentSchedules;
import com.group5.htms.entity.Tournaments;
import com.group5.htms.enums.RaceResultStatus;
import com.group5.htms.enums.RaceStatus;
import com.group5.htms.enums.JockeyAssignmentStatus;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.exception.ResourceNotFoundException;
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

@Service
@RequiredArgsConstructor
public class RaceServiceImpl implements RaceService {

    private final RacesRepository racesRepository;
    private final TournamentsRepository tournamentsRepository;
    private final TournamentSchedulesRepository tournamentSchedulesRepository;
    private final RaceRegistrationsRepository raceRegistrationsRepository;
    private final JockeyHorseAssignmentsRepository jockeyHorseAssignmentsRepository;
    private final RaceRefereeAssignmentsRepository raceRefereeAssignmentsRepository;
    private final RacePointRulesRepository racePointRulesRepository;
    private final RaceResultsRepository raceResultsRepository;
    private final RaceMapper raceMapper;
    private final TournamentScheduleMapper tournamentScheduleMapper;
    private final BetOptionService betOptionService;
    private final RaceValidator raceValidator;

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
    public RaceResponse completeRace(Integer raceId) {
        Races race = getRaceEntity(raceId);

        if (RaceStatus.CANCELLED.equalsValue(race.getStatus())) {
            throw new BadRequestException("Cancelled race cannot be completed");
        }

        if (RaceStatus.COMPLETED.equalsValue(race.getStatus())) {
            throw new BadRequestException("Race is already completed");
        }

        if (!RaceStatus.canCompleteRace(race.getStatus())) {
            throw new BadRequestException("Only in progress races can be completed");
        }

        if (raceResultsRepository.countByRaces_IdAndStatusIgnoreCase(
                race.getId(),
                RaceResultStatus.PUBLISHED.getValue()
        ) < 1) {
            throw new BadRequestException("Race must have at least one published result before completing");
        }

        race.setStatus(RaceStatus.COMPLETED.getValue());

        return toDetailResponse(racesRepository.save(race));
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

    private RaceListResponse toResponseWithCounts(Races race) {
        Integer raceId = race.getId();

        return raceMapper.toListResponse(
                race,
                raceRegistrationsRepository.countByRaces_Id(raceId),
                jockeyHorseAssignmentsRepository.countByRaces_IdAndStatusIgnoreCase(raceId, JockeyAssignmentStatus.ACCEPTED.getValue()),
                raceRefereeAssignmentsRepository.countByRaces_Id(raceId)
        );
    }

    private RaceResponse toDetailResponse(Races race) {
        Integer raceId = race.getId();

        return raceMapper.toResponse(
                race,
                raceRegistrationsRepository.countByRaces_Id(raceId),
                jockeyHorseAssignmentsRepository.countByRaces_IdAndStatusIgnoreCase(raceId, JockeyAssignmentStatus.ACCEPTED.getValue()),
                raceRefereeAssignmentsRepository.countByRaces_Id(raceId),
                racePointRulesRepository.findByRace_IdOrderByFinishPositionAsc(raceId)
        );
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



