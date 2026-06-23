package com.group5.htms.service.impl;

import com.group5.htms.dto.race.request.RaceUpdateRequest;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
                .hasMessage("Use workflow transition APIs to update race status");
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
                .schedule(schedule)
                .raceNumber(1)
                .scheduledAt(Instant.parse("2026-07-01T08:00:00Z"))
                .predictionClosesAt(Instant.parse("2026-07-01T07:00:00Z"))
                .status(RaceStatus.SCHEDULED.getValue())
                .build();
    }
}
