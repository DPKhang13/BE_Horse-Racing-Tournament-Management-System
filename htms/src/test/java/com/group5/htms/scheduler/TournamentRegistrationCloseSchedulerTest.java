package com.group5.htms.scheduler;

import com.group5.htms.dto.tournament.request.CloseRegistrationRequest;
import com.group5.htms.entity.JockeyHorseAssignments;
import com.group5.htms.entity.RaceRegistrations;
import com.group5.htms.entity.Races;
import com.group5.htms.entity.Tournaments;
import com.group5.htms.enums.JockeyAssignmentStatus;
import com.group5.htms.enums.RaceRegistrationStatus;
import com.group5.htms.enums.RaceStatus;
import com.group5.htms.enums.TournamentStatus;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.repository.JockeyHorseAssignmentsRepository;
import com.group5.htms.repository.RaceRegistrationsRepository;
import com.group5.htms.repository.RacesRepository;
import com.group5.htms.repository.TournamentsRepository;
import com.group5.htms.service.TournamentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TournamentRegistrationCloseSchedulerTest {
    @Mock
    private TournamentsRepository tournamentsRepository;

    @Mock
    private RacesRepository racesRepository;

    @Mock
    private RaceRegistrationsRepository raceRegistrationsRepository;

    @Mock
    private JockeyHorseAssignmentsRepository jockeyHorseAssignmentsRepository;

    @Mock
    private TournamentService tournamentService;

    @Test
    void closeOverdueRegistrationWindowsClosesWithAutoCleanupFlags() {
        Tournaments tournament = tournament(1);
        when(tournamentsRepository.findByStatusIgnoreCaseOrderByStartDateAsc(TournamentStatus.REGISTRATION_OPEN.getValue()))
                .thenReturn(List.of(tournament));

        TournamentRegistrationCloseScheduler scheduler =
                scheduler();

        scheduler.closeAutoClosableRegistrationWindows();

        ArgumentCaptor<CloseRegistrationRequest> captor = ArgumentCaptor.forClass(CloseRegistrationRequest.class);
        verify(tournamentService).closeRegistration(eq(1), captor.capture());
        assertThat(captor.getValue().isAutoRejectPending()).isTrue();
        assertThat(captor.getValue().isAutoCancelUnconfirmed()).isTrue();
    }

    @Test
    void closeAutoClosableRegistrationWindowsClosesBeforeDeadlineWhenAllOpenRacesAreFull() {
        Tournaments tournament = tournament(1);
        tournament.setRegistrationCloseAt(Instant.now().plusSeconds(3600));
        Races race = Races.builder()
                .id(10)
                .status(RaceStatus.REGISTRATION_OPEN.getValue())
                .maxHorses(1)
                .build();
        RaceRegistrations registration = RaceRegistrations.builder()
                .id(20)
                .races(race)
                .status(RaceRegistrationStatus.APPROVED.getValue())
                .build();
        JockeyHorseAssignments assignment = JockeyHorseAssignments.builder()
                .id(30)
                .reg(registration)
                .status(JockeyAssignmentStatus.CONFIRMED.getValue())
                .build();
        when(tournamentsRepository.findByStatusIgnoreCaseOrderByStartDateAsc(TournamentStatus.REGISTRATION_OPEN.getValue()))
                .thenReturn(List.of(tournament));
        when(racesRepository.findBySchedule_Tournaments_IdAndStatusIgnoreCaseOrderByScheduledAtAsc(
                1,
                RaceStatus.REGISTRATION_OPEN.getValue()
        )).thenReturn(List.of(race));
        when(raceRegistrationsRepository.findByTournaments_Id(1)).thenReturn(List.of(registration));
        when(jockeyHorseAssignmentsRepository.findByReg_IdIn(List.of(20))).thenReturn(List.of(assignment));

        scheduler().closeAutoClosableRegistrationWindows();

        verify(tournamentService).closeRegistration(eq(1), any(CloseRegistrationRequest.class));
    }

    @Test
    void closeOverdueRegistrationWindowsContinuesWhenOneTournamentCannotClose() {
        Tournaments blockedTournament = tournament(1);
        Tournaments nextTournament = tournament(2);
        when(tournamentsRepository.findByStatusIgnoreCaseOrderByStartDateAsc(TournamentStatus.REGISTRATION_OPEN.getValue()))
                .thenReturn(List.of(blockedTournament, nextTournament));
        doThrow(new BadRequestException("Race has no horse registrations"))
                .when(tournamentService)
                .closeRegistration(eq(1), any(CloseRegistrationRequest.class));

        scheduler().closeAutoClosableRegistrationWindows();

        verify(tournamentService).closeRegistration(eq(1), any(CloseRegistrationRequest.class));
        verify(tournamentService).closeRegistration(eq(2), any(CloseRegistrationRequest.class));
    }

    private TournamentRegistrationCloseScheduler scheduler() {
        return new TournamentRegistrationCloseScheduler(
                tournamentsRepository,
                racesRepository,
                raceRegistrationsRepository,
                jockeyHorseAssignmentsRepository,
                tournamentService
        );
    }

    private Tournaments tournament(Integer id) {
        return Tournaments.builder()
                .id(id)
                .status(TournamentStatus.REGISTRATION_OPEN.getValue())
                .registrationCloseAt(Instant.now().minusSeconds(1))
                .build();
    }
}
