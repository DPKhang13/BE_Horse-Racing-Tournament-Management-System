package com.group5.htms.service.impl;

import com.group5.htms.dto.tournament.request.OpenRegistrationRequest;
import com.group5.htms.dto.tournament.request.TournamentCreateRequest;
import com.group5.htms.dto.tournament.request.TournamentUpdateRequest;
import com.group5.htms.dto.tournament.response.GlobalTournamentCountResponse;
import com.group5.htms.dto.tournament.response.OpenRegistrationResponse;
import com.group5.htms.dto.tournament.response.TournamentDetailResponse;
import com.group5.htms.dto.tournament.response.TournamentResponse;
import com.group5.htms.dto.tournament.response.TournamentSummaryResponse;
import com.group5.htms.entity.PrizeDistributions;
import com.group5.htms.entity.Races;
import com.group5.htms.entity.Tournaments;
import com.group5.htms.entity.Users;
import com.group5.htms.enums.RaceStatus;
import com.group5.htms.enums.TournamentStatus;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.exception.UnauthorizedException;
import com.group5.htms.mapper.TournamentMapper;
import com.group5.htms.repository.PrizeRepository;
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
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TournamentServiceImpl implements TournamentService {

    private final TournamentsRepository tournamentsRepository;
    private final UsersRepository usersRepository;
    private final TournamentSchedulesRepository tournamentSchedulesRepository;
    private final RacesRepository racesRepository;
    private final PrizeRepository prizeRepository;
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

//    @Override
//    @Transactional
//    public void deleteTournament(Integer tournamentId) {
//        Tournaments tournament = getTournamentEntity(tournamentId);
//
//        tournamentsRepository.delete(tournament);
//    }

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

        if (request.getStatus() != null
                && !request.getStatus().isBlank()
                && !TournamentStatus.isValid(request.getStatus())) {
            throw new BadRequestException("Invalid tournament status");
        }
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
