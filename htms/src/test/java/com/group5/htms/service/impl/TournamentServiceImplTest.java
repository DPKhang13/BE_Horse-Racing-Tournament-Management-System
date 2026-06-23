package com.group5.htms.service.impl;

import com.group5.htms.dto.tournament.request.OpenRegistrationRequest;
import com.group5.htms.dto.tournament.response.OpenRegistrationResponse;
import com.group5.htms.entity.PrizeDistributions;
import com.group5.htms.entity.Races;
import com.group5.htms.entity.Tournaments;
import com.group5.htms.enums.RaceStatus;
import com.group5.htms.enums.TournamentStatus;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.mapper.TournamentMapper;
import com.group5.htms.repository.PrizeRepository;
import com.group5.htms.repository.RacesRepository;
import com.group5.htms.repository.TournamentSchedulesRepository;
import com.group5.htms.repository.TournamentsRepository;
import com.group5.htms.repository.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TournamentServiceImplTest {
    private static final Integer TOURNAMENT_ID = 1;
    private static final Instant FIRST_RACE_START = Instant.parse("2026-07-01T08:00:00Z");

    @Mock
    private TournamentsRepository tournamentsRepository;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private TournamentSchedulesRepository tournamentSchedulesRepository;

    @Mock
    private RacesRepository racesRepository;

    @Mock
    private PrizeRepository prizeRepository;

    @Mock
    private TournamentMapper tournamentMapper;

    private TournamentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TournamentServiceImpl(
                tournamentsRepository,
                usersRepository,
                tournamentSchedulesRepository,
                racesRepository,
                prizeRepository,
                tournamentMapper
        );
    }

    @Test
    void openRegistrationFailsIfTournamentNotFound() {
        when(tournamentsRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.openRegistration(TOURNAMENT_ID, validRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Tournament not found");
    }

    @Test
    void openRegistrationFailsIfTournamentStatusIsNotUpcoming() {
        when(tournamentsRepository.findById(TOURNAMENT_ID))
                .thenReturn(Optional.of(tournament(TournamentStatus.REGISTRATION_OPEN.getValue())));

        assertThatThrownBy(() -> service.openRegistration(TOURNAMENT_ID, validRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Only upcoming tournaments can be opened for registration");
    }

    @Test
    void openRegistrationFailsIfTournamentHasNoSchedule() {
        when(tournamentsRepository.findById(TOURNAMENT_ID))
                .thenReturn(Optional.of(tournament(TournamentStatus.UPCOMING.getValue())));
        when(tournamentSchedulesRepository.countByTournamentsId(TOURNAMENT_ID)).thenReturn(0L);

        assertThatThrownBy(() -> service.openRegistration(TOURNAMENT_ID, validRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Tournament must have at least one schedule before opening registration");
    }

    @Test
    void openRegistrationFailsIfTournamentHasNoRace() {
        when(tournamentsRepository.findById(TOURNAMENT_ID))
                .thenReturn(Optional.of(tournament(TournamentStatus.UPCOMING.getValue())));
        when(tournamentSchedulesRepository.countByTournamentsId(TOURNAMENT_ID)).thenReturn(1L);
        when(racesRepository.findBySchedule_Tournaments_IdOrderByScheduledAtAsc(TOURNAMENT_ID))
                .thenReturn(List.of());

        assertThatThrownBy(() -> service.openRegistration(TOURNAMENT_ID, validRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Tournament must have at least one race before opening registration");
    }

    @Test
    void openRegistrationFailsIfPrizeOnlyHasPositionOne() {
        arrangeReadyTournament(List.of(prize(1, "300.00")), List.of(race(RaceStatus.SCHEDULED.getValue())));

        assertThatThrownBy(() -> service.openRegistration(TOURNAMENT_ID, validRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Tournament must have exactly 3 prize distributions for positions 1, 2 and 3");
    }

    @Test
    void openRegistrationFailsIfPrizeHasPositionFour() {
        arrangeReadyTournament(
                List.of(prize(1, "100.00"), prize(2, "100.00"), prize(4, "100.00")),
                List.of(race(RaceStatus.SCHEDULED.getValue()))
        );

        assertThatThrownBy(() -> service.openRegistration(TOURNAMENT_ID, validRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Only finish positions 1, 2 and 3 can receive prizes");
    }

    @Test
    void openRegistrationFailsIfTotalPrizeAmountIsLessThanPrizePool() {
        arrangeReadyTournament(
                List.of(prize(1, "100.00"), prize(2, "100.00"), prize(3, "50.00")),
                List.of(race(RaceStatus.SCHEDULED.getValue()))
        );

        assertThatThrownBy(() -> service.openRegistration(TOURNAMENT_ID, validRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Total prize amount must be equal to tournament prize pool");
    }

    @Test
    void openRegistrationFailsIfTotalPrizeAmountIsGreaterThanPrizePool() {
        arrangeReadyTournament(
                List.of(prize(1, "150.00"), prize(2, "100.00"), prize(3, "100.00")),
                List.of(race(RaceStatus.SCHEDULED.getValue()))
        );

        assertThatThrownBy(() -> service.openRegistration(TOURNAMENT_ID, validRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Total prize amount must be equal to tournament prize pool");
    }

    @Test
    void openRegistrationFailsIfRegistrationCloseAtIsAfterOrEqualFirstRaceStartTime() {
        arrangeReadyTournament(validPrizes(), List.of(race(RaceStatus.SCHEDULED.getValue())));
        OpenRegistrationRequest request = OpenRegistrationRequest.builder()
                .registrationCloseAt(FIRST_RACE_START)
                .build();

        assertThatThrownBy(() -> service.openRegistration(TOURNAMENT_ID, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Registration close time must be before the first race starts");
    }

    @Test
    void openRegistrationSucceedsAndOpensScheduledRaces() {
        Tournaments tournament = tournament(TournamentStatus.UPCOMING.getValue());
        Races scheduledRace = race(RaceStatus.SCHEDULED.getValue());
        Races cancelledRace = race(RaceStatus.CANCELLED.getValue());

        arrangeReadyTournament(tournament, validPrizes(), List.of(scheduledRace, cancelledRace));
        when(tournamentsRepository.save(tournament)).thenReturn(tournament);

        OpenRegistrationResponse response = service.openRegistration(TOURNAMENT_ID, validRequest());

        assertThat(tournament.getStatus()).isEqualTo(TournamentStatus.REGISTRATION_OPEN.getValue());
        assertThat(tournament.getRegistrationOpenAt()).isEqualTo(validRequest().getRegistrationOpenAt());
        assertThat(tournament.getRegistrationCloseAt()).isEqualTo(validRequest().getRegistrationCloseAt());
        assertThat(scheduledRace.getStatus()).isEqualTo(RaceStatus.REGISTRATION_OPEN.getValue());
        assertThat(cancelledRace.getStatus()).isEqualTo(RaceStatus.CANCELLED.getValue());
        assertThat(response.getTournamentId()).isEqualTo(TOURNAMENT_ID);
        assertThat(response.getStatus()).isEqualTo(TournamentStatus.REGISTRATION_OPEN.getValue());
        assertThat(response.getTotalSchedules()).isEqualTo(1);
        assertThat(response.getTotalRaces()).isEqualTo(2);
        verify(racesRepository).saveAll(List.of(scheduledRace, cancelledRace));
    }

    @Test
    void openRegistrationDoesNotSaveWhenValidationFails() {
        arrangeReadyTournament(
                List.of(prize(1, "100.00"), prize(2, "100.00"), prize(3, "50.00")),
                List.of(race(RaceStatus.SCHEDULED.getValue()))
        );

        assertThatThrownBy(() -> service.openRegistration(TOURNAMENT_ID, validRequest()))
                .isInstanceOf(BadRequestException.class);

        verify(tournamentsRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(racesRepository, never()).saveAll(anyList());
    }

    private void arrangeReadyTournament(List<PrizeDistributions> prizes, List<Races> races) {
        arrangeReadyTournament(tournament(TournamentStatus.UPCOMING.getValue()), prizes, races);
    }

    private void arrangeReadyTournament(Tournaments tournament, List<PrizeDistributions> prizes, List<Races> races) {
        when(tournamentsRepository.findById(TOURNAMENT_ID)).thenReturn(Optional.of(tournament));
        when(tournamentSchedulesRepository.countByTournamentsId(TOURNAMENT_ID)).thenReturn(1L);
        when(racesRepository.findBySchedule_Tournaments_IdOrderByScheduledAtAsc(TOURNAMENT_ID)).thenReturn(races);
        when(prizeRepository.findByTournamentsIdOrderByFinishPositionAsc(TOURNAMENT_ID)).thenReturn(prizes);
    }

    private OpenRegistrationRequest validRequest() {
        return OpenRegistrationRequest.builder()
                .registrationOpenAt(Instant.parse("2026-06-01T00:00:00Z"))
                .registrationCloseAt(FIRST_RACE_START.minusSeconds(3600))
                .build();
    }

    private Tournaments tournament(String status) {
        return Tournaments.builder()
                .id(TOURNAMENT_ID)
                .name("Summer Cup")
                .location("HCMC")
                .startDate(LocalDate.of(2026, 7, 1))
                .endDate(LocalDate.of(2026, 7, 3))
                .prizePool(new BigDecimal("300.00"))
                .status(status)
                .createdAt(Instant.parse("2026-05-01T00:00:00Z"))
                .build();
    }

    private Races race(String status) {
        return Races.builder()
                .id(10)
                .name("Race 1")
                .scheduledAt(FIRST_RACE_START)
                .status(status)
                .build();
    }

    private List<PrizeDistributions> validPrizes() {
        return List.of(prize(1, "100.00"), prize(2, "100.00"), prize(3, "100.00"));
    }

    private PrizeDistributions prize(Integer finishPosition, String amount) {
        return PrizeDistributions.builder()
                .finishPosition(finishPosition)
                .amount(new BigDecimal(amount))
                .build();
    }
}
