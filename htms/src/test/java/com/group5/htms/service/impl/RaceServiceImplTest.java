package com.group5.htms.service.impl;

import com.group5.htms.dto.race.request.RaceStartRequest;
import com.group5.htms.dto.race.request.RaceUpdateRequest;
import com.group5.htms.dto.race.response.RaceStartResponse;
import com.group5.htms.entity.Races;
import com.group5.htms.entity.TournamentSchedules;
import com.group5.htms.entity.Tournaments;
import com.group5.htms.enums.RaceStatus;
import com.group5.htms.enums.TournamentStatus;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.mapper.RaceMapper;
import com.group5.htms.mapper.TournamentScheduleMapper;
import com.group5.htms.repository.JockeyHorseAssignmentsRepository;
import com.group5.htms.repository.RaceRefereeAssignmentsRepository;
import com.group5.htms.repository.RaceRegistrationsRepository;
import com.group5.htms.repository.RacesRepository;
import com.group5.htms.repository.TournamentSchedulesRepository;
import com.group5.htms.repository.TournamentsRepository;
import com.group5.htms.service.BetOptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RaceServiceImplTest {
    @Mock
    private RacesRepository racesRepository;

    @Mock
    private TournamentsRepository tournamentsRepository;

    @Mock
    private TournamentSchedulesRepository tournamentSchedulesRepository;

    @Mock
    private RaceRegistrationsRepository raceRegistrationsRepository;

    @Mock
    private JockeyHorseAssignmentsRepository jockeyHorseAssignmentsRepository;

    @Mock
    private RaceRefereeAssignmentsRepository raceRefereeAssignmentsRepository;

    @Mock
    private RaceMapper raceMapper;

    @Mock
    private TournamentScheduleMapper tournamentScheduleMapper;

    @Mock
    private BetOptionService betOptionService;

    private RaceServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RaceServiceImpl(
                racesRepository,
                tournamentsRepository,
                tournamentSchedulesRepository,
                raceRegistrationsRepository,
                jockeyHorseAssignmentsRepository,
                raceRefereeAssignmentsRepository,
                raceMapper,
                tournamentScheduleMapper,
                betOptionService
        );
    }

    @Test
    void updateRaceRejectsDirectStatusUpdate() {
        RaceUpdateRequest request = RaceUpdateRequest.builder()
                .status(RaceStatus.OPEN_FOR_BETTING.getValue())
                .build();

        when(racesRepository.findById(10)).thenReturn(Optional.of(race()));

        assertThatThrownBy(() -> service.updateRace(10, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Use workflow transition APIs to update status");
    }

    @Test
    void startRaceFailsWhenRaceNotFound() {
        when(racesRepository.findById(10)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.startRace(10, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Race not found");
    }

    @Test
    void startRaceFailsWhenStatusIsScheduled() {
        assertStartFailsWithStatus(
                RaceStatus.SCHEDULED.getValue(),
                "Only ready or open for betting races can be started"
        );
    }

    @Test
    void startRaceFailsWhenStatusIsRegistrationOpen() {
        assertStartFailsWithStatus(
                RaceStatus.REGISTRATION_OPEN.getValue(),
                "Only ready or open for betting races can be started"
        );
    }

    @Test
    void startRaceFailsWhenStatusIsRegistrationClosed() {
        assertStartFailsWithStatus(
                RaceStatus.REGISTRATION_CLOSED.getValue(),
                "Only ready or open for betting races can be started"
        );
    }

    @Test
    void startRaceSucceedsWhenStatusIsReady() {
        Races race = race(RaceStatus.READY.getValue());
        mockRaceReadyToStart(race);
        when(racesRepository.save(race)).thenReturn(race);

        RaceStartResponse response = service.startRace(10, RaceStartRequest.builder().build());

        assertThat(response.getRaceId()).isEqualTo(10);
        assertThat(response.getRaceName()).isEqualTo("Race 1");
        assertThat(response.getPreviousStatus()).isEqualTo(RaceStatus.READY.getValue());
        assertThat(response.getStatus()).isEqualTo(RaceStatus.IN_PROGRESS.getValue());
        assertThat(response.getBettingClosed()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Race started successfully");
        assertThat(race.getStatus()).isEqualTo(RaceStatus.IN_PROGRESS.getValue());
        verify(racesRepository).save(race);
    }

    @Test
    void startRaceSucceedsWhenBettingOpenAndPredictionClosed() {
        Races race = race(RaceStatus.OPEN_FOR_BETTING.getValue());
        race.setPredictionClosesAt(Instant.now().minusSeconds(60));
        mockRaceReadyToStart(race);
        when(racesRepository.save(race)).thenReturn(race);

        RaceStartResponse response = service.startRace(10, RaceStartRequest.builder().build());

        assertThat(response.getPreviousStatus()).isEqualTo(RaceStatus.OPEN_FOR_BETTING.getValue());
        assertThat(response.getStatus()).isEqualTo(RaceStatus.IN_PROGRESS.getValue());
        verify(racesRepository).save(race);
    }

    @Test
    void startRaceFailsWhenBettingOpenAndPredictionStillOpenWithoutForce() {
        Races race = race(RaceStatus.OPEN_FOR_BETTING.getValue());
        race.setPredictionClosesAt(Instant.now().plusSeconds(600));
        when(racesRepository.findById(10)).thenReturn(Optional.of(race));

        assertThatThrownBy(() -> service.startRace(10, RaceStartRequest.builder().build()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Prediction betting is still open. Use forceCloseBetting to start the race early");

        verify(racesRepository, never()).save(any());
    }

    @Test
    void startRaceSucceedsWhenBettingOpenAndForceCloseBettingIsTrue() {
        Races race = race(RaceStatus.OPEN_FOR_BETTING.getValue());
        race.setPredictionClosesAt(Instant.now().plusSeconds(600));
        mockRaceReadyToStart(race);
        when(racesRepository.save(race)).thenReturn(race);

        RaceStartResponse response = service.startRace(
                10,
                RaceStartRequest.builder().forceCloseBetting(true).build()
        );

        assertThat(response.getStatus()).isEqualTo(RaceStatus.IN_PROGRESS.getValue());
        assertThat(response.getBettingClosed()).isTrue();
        verify(racesRepository).save(race);
    }

    @Test
    void startRaceFailsWhenAlreadyInProgress() {
        assertStartFailsWithStatus(
                RaceStatus.IN_PROGRESS.getValue(),
                "Race is already in progress"
        );
    }

    @Test
    void startRaceFailsWhenCompleted() {
        assertStartFailsWithStatus(
                RaceStatus.COMPLETED.getValue(),
                "Completed race cannot be started"
        );
    }

    @Test
    void startRaceFailsWhenCancelled() {
        assertStartFailsWithStatus(
                RaceStatus.CANCELLED.getValue(),
                "Cancelled race cannot be started"
        );
    }

    @Test
    void startRaceFailsWithoutConfirmedJockeyAssignment() {
        Races race = race(RaceStatus.READY.getValue());
        when(racesRepository.findById(10)).thenReturn(Optional.of(race));
        when(jockeyHorseAssignmentsRepository.countByRaces_IdAndStatusIgnoreCase(10, "confirmed"))
                .thenReturn(0L);

        assertThatThrownBy(() -> service.startRace(10, RaceStartRequest.builder().build()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Race must have at least one confirmed jockey assignment before starting");

        verify(racesRepository, never()).save(any());
    }

    @Test
    void startRaceFailsWithoutAssignedReferee() {
        Races race = race(RaceStatus.READY.getValue());
        when(racesRepository.findById(10)).thenReturn(Optional.of(race));
        when(jockeyHorseAssignmentsRepository.countByRaces_IdAndStatusIgnoreCase(10, "confirmed"))
                .thenReturn(1L);
        when(raceRefereeAssignmentsRepository.countByRaces_Id(10)).thenReturn(0L);

        assertThatThrownBy(() -> service.startRace(10, RaceStartRequest.builder().build()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Race must have at least one assigned referee before starting");

        verify(racesRepository, never()).save(any());
    }

    @Test
    void startRaceFailsWhenBettingOpenWithoutPredictionCloseTime() {
        Races race = race(RaceStatus.OPEN_FOR_BETTING.getValue());
        race.setPredictionClosesAt(null);
        when(racesRepository.findById(10)).thenReturn(Optional.of(race));

        assertThatThrownBy(() -> service.startRace(10, RaceStartRequest.builder().build()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Prediction close time is required before starting race from betting status");

        verify(racesRepository, never()).save(any());
    }

    private void assertStartFailsWithStatus(String status, String expectedMessage) {
        Races race = race(status);
        when(racesRepository.findById(10)).thenReturn(Optional.of(race));

        assertThatThrownBy(() -> service.startRace(10, RaceStartRequest.builder().build()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage(expectedMessage);

        verify(racesRepository, never()).save(any());
    }

    private void mockRaceReadyToStart(Races race) {
        when(racesRepository.findById(10)).thenReturn(Optional.of(race));
        when(jockeyHorseAssignmentsRepository.countByRaces_IdAndStatusIgnoreCase(10, "confirmed"))
                .thenReturn(1L);
        when(raceRefereeAssignmentsRepository.countByRaces_Id(10)).thenReturn(1L);
    }

    private Races race(String status) {
        Races race = race();
        race.setStatus(status);

        return race;
    }

    private Races race() {
        Tournaments tournament = Tournaments.builder()
                .id(1)
                .status(TournamentStatus.UPCOMING.getValue())
                .build();
        TournamentSchedules schedule = TournamentSchedules.builder()
                .id(2)
                .tournaments(tournament)
                .raceDate(LocalDate.of(2026, 7, 1))
                .build();

        return Races.builder()
                .id(10)
                .name("Race 1")
                .schedule(schedule)
                .raceNumber(1)
                .scheduledAt(Instant.parse("2026-07-01T08:00:00Z"))
                .predictionClosesAt(Instant.parse("2026-07-01T07:00:00Z"))
                .status(RaceStatus.SCHEDULED.getValue())
                .build();
    }
}
