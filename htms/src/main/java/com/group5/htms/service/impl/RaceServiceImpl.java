package com.group5.htms.service.impl;

import com.group5.htms.dto.race.request.RaceCreateRequest;
import com.group5.htms.dto.race.request.RaceUpdateRequest;
import com.group5.htms.dto.race.response.RaceListResponse;
import com.group5.htms.dto.race.response.RaceResponse;
import com.group5.htms.dto.schedule.request.TournamentScheduleCreateRequest;
import com.group5.htms.dto.schedule.request.TournamentScheduleUpdateRequest;
import com.group5.htms.dto.schedule.response.TournamentScheduleResponse;
import com.group5.htms.dto.racepointrule.request.RacePointRuleItemRequest;
import com.group5.htms.entity.RacePointRules;
import com.group5.htms.entity.Races;
import com.group5.htms.entity.TournamentSchedules;
import com.group5.htms.entity.Tournaments;
import com.group5.htms.enums.TournamentStatus;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.exception.ResourceNotFoundException;
import com.group5.htms.mapper.RaceMapper;
import com.group5.htms.mapper.TournamentScheduleMapper;
import com.group5.htms.repository.JockeyHorseAssignmentsRepository;
import com.group5.htms.repository.RaceRefereeAssignmentsRepository;
import com.group5.htms.repository.RaceRegistrationsRepository;
import com.group5.htms.repository.RacePointRulesRepository;
import com.group5.htms.repository.RacesRepository;
import com.group5.htms.repository.TournamentSchedulesRepository;
import com.group5.htms.repository.TournamentsRepository;
import com.group5.htms.service.RaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RaceServiceImpl implements RaceService {
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final String RACE_STATUS_SCHEDULED = "scheduled";
    private static final String RACE_STATUS_UPCOMING = "upcoming";
    private static final String RACE_STATUS_CANCELLED = "cancelled";
    private static final String RACE_STATUS_COMPLETED = "completed";

    private final RacesRepository racesRepository;
    private final TournamentsRepository tournamentsRepository;
    private final TournamentSchedulesRepository tournamentSchedulesRepository;
    private final RaceRegistrationsRepository raceRegistrationsRepository;
    private final JockeyHorseAssignmentsRepository jockeyHorseAssignmentsRepository;
    private final RaceRefereeAssignmentsRepository raceRefereeAssignmentsRepository;
    private final RacePointRulesRepository racePointRulesRepository;
    private final RaceMapper raceMapper;
    private final TournamentScheduleMapper tournamentScheduleMapper;

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

        validateTournamentCanArrangeRace(tournament);
        validateScheduleDate(tournament, request.getRaceDate());
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

        validateTournamentCanArrangeRace(schedule.getTournaments());
        validateRaceRequest(schedule, request);

        Races race = raceMapper.toEntity(request, schedule);
        Races savedRace = racesRepository.save(race);
        savePointRules(savedRace, request.getPointRules());

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

        validateTournamentCanArrangeRace(tournament);
        validateScheduleUpdate(schedule, request);

        tournamentScheduleMapper.updateEntity(schedule, request);

        return tournamentScheduleMapper.toResponse(
                tournamentSchedulesRepository.save(schedule)
        );
    }

    @Override
    @Transactional
    public void deleteSchedule(Integer scheduleId) {
        TournamentSchedules schedule = getScheduleEntity(scheduleId);

        validateTournamentCanArrangeRace(schedule.getTournaments());

        if (racesRepository.countByScheduleId(schedule.getId()) > 0) {
            throw new BadRequestException("Cannot delete schedule that already has races");
        }

        tournamentSchedulesRepository.delete(schedule);
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
        validateTournamentCanArrangeRace(race.getSchedule().getTournaments());
        validateRaceUpdateRequest(race, request);

        raceMapper.updateEntity(race, request);
        Races savedRace = racesRepository.save(race);
        if (request.getPointRules() != null) {
            racePointRulesRepository.deleteByRace_Id(savedRace.getId());
            savePointRules(savedRace, request.getPointRules());
        }

        return toDetailResponse(savedRace);
    }

    @Override
    @Transactional
    public void cancelRace(Integer raceId) {
        Races race = getRaceEntity(raceId);

        validateTournamentCanArrangeRace(race.getSchedule().getTournaments());

        if (RACE_STATUS_COMPLETED.equalsIgnoreCase(race.getStatus())) {
            throw new BadRequestException("Completed race cannot be cancelled");
        }

        race.setStatus(RACE_STATUS_CANCELLED);
        racesRepository.save(race);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RaceListResponse> getRacesByTournament(Integer tournamentId, String status) {
        if (!tournamentsRepository.existsById(tournamentId)) {
            throw new ResourceNotFoundException("Tournament not found");
        }

        if (status != null && !status.isBlank()) {
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
                jockeyHorseAssignmentsRepository.countByRaces_IdAndStatusIgnoreCase(raceId, "accepted"),
                raceRefereeAssignmentsRepository.countByRaces_Id(raceId)
        );
    }

    private RaceResponse toDetailResponse(Races race) {
        Integer raceId = race.getId();

        return raceMapper.toResponse(
                race,
                raceRegistrationsRepository.countByRaces_Id(raceId),
                jockeyHorseAssignmentsRepository.countByRaces_IdAndStatusIgnoreCase(raceId, "accepted"),
                raceRefereeAssignmentsRepository.countByRaces_Id(raceId),
                racePointRulesRepository.findByRace_IdOrderByFinishPositionAsc(raceId)
        );
    }

    private void savePointRules(Races race, List<RacePointRuleItemRequest> pointRules) {
        if (pointRules == null) {
            return;
        }

        for (RacePointRuleItemRequest item : pointRules) {
            if (item == null) {
                continue;
            }

            if (racePointRulesRepository.existsByRace_IdAndFinishPosition(race.getId(), item.getFinishPosition())) {
                throw new BadRequestException("Finish position already exists in race point rules");
            }

            RacePointRules rule = RacePointRules.builder()
                    .race(race)
                    .finishPosition(item.getFinishPosition())
                    .points(item.getPoints())
                    .note(item.getNote() == null ? null : item.getNote().trim())
                    .build();
            racePointRulesRepository.save(rule);
        }
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

    private void validateTournamentCanArrangeRace(Tournaments tournament) {
        if (tournament == null) {
            throw new BadRequestException("Tournament not found");
        }

        String status = tournament.getStatus();

        if (TournamentStatus.COMPLETED.getValue().equalsIgnoreCase(status)
                || TournamentStatus.CANCELLED.getValue().equalsIgnoreCase(status)) {
            throw new BadRequestException("Cannot arrange races for completed or cancelled tournament");
        }
    }

    private void validateScheduleDate(Tournaments tournament, LocalDate raceDate) {
        if (raceDate == null) {
            throw new BadRequestException("Race date is required");
        }

        if (raceDate.isBefore(tournament.getStartDate())
                || raceDate.isAfter(tournament.getEndDate())) {
            throw new BadRequestException("Race date must be within tournament date range");
        }
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
            validateScheduleDate(tournament, request.getRaceDate());

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

        LocalDate scheduledDate = request.getScheduledAt()
                .atZone(VIETNAM_ZONE)
                .toLocalDate();

        if (!scheduledDate.equals(schedule.getRaceDate())) {
            throw new BadRequestException("Scheduled time must be on the schedule race date");
        }

        if (request.getPredictionClosesAt() != null
                && !request.getPredictionClosesAt().isBefore(request.getScheduledAt())) {
            throw new BadRequestException("Prediction close time must be before scheduled time");
        }

        String status = request.getStatus();

        if (status != null
                && !status.isBlank()
                && !RACE_STATUS_SCHEDULED.equalsIgnoreCase(status.trim())
                && !RACE_STATUS_UPCOMING.equalsIgnoreCase(status.trim())) {
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

        java.time.Instant scheduledAt = request.getScheduledAt() == null
                ? race.getScheduledAt()
                : request.getScheduledAt();

        java.time.Instant predictionClosesAt = request.getPredictionClosesAt() == null
                ? race.getPredictionClosesAt()
                : request.getPredictionClosesAt();

        LocalDate scheduledDate = scheduledAt.atZone(VIETNAM_ZONE).toLocalDate();

        if (!scheduledDate.equals(race.getSchedule().getRaceDate())) {
            throw new BadRequestException("Scheduled time must be on the schedule race date");
        }

        if (predictionClosesAt != null && !predictionClosesAt.isBefore(scheduledAt)) {
            throw new BadRequestException("Prediction close time must be before scheduled time");
        }

        if (request.getStatus() != null
                && !request.getStatus().isBlank()
                && !isValidRaceStatus(request.getStatus())) {
            throw new BadRequestException("Invalid race status");
        }
    }

    private boolean isValidRaceStatus(String status) {
        String normalizedStatus = status.trim().toLowerCase();

        return RACE_STATUS_SCHEDULED.equals(normalizedStatus)
                || RACE_STATUS_UPCOMING.equals(normalizedStatus)
                || RACE_STATUS_CANCELLED.equals(normalizedStatus)
                || RACE_STATUS_COMPLETED.equals(normalizedStatus);
    }
}
