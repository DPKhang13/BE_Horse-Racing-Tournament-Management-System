package com.group5.htms.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group5.htms.entity.JockeyHorseAssignments;
import com.group5.htms.entity.RacePointRules;
import com.group5.htms.entity.RaceResults;
import com.group5.htms.entity.Races;
import com.group5.htms.enums.JockeyAssignmentStatus;
import com.group5.htms.enums.RaceResultStatus;
import com.group5.htms.enums.RaceStatus;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.mapper.RaceResultMapper;
import com.group5.htms.repository.BetsRepository;
import com.group5.htms.repository.JockeyHorseAssignmentsRepository;
import com.group5.htms.repository.NotificationsRepository;
import com.group5.htms.repository.RacePointRulesRepository;
import com.group5.htms.repository.RaceResultAdminEditAuditRepository;
import com.group5.htms.repository.RaceRefereeAssignmentsRepository;
import com.group5.htms.repository.RaceResultsRepository;
import com.group5.htms.repository.RaceRoundsRepository;
import com.group5.htms.repository.RacesRepository;
import com.group5.htms.repository.RefereeProfilesRepository;
import com.group5.htms.repository.RefereeReportsRepository;
import com.group5.htms.repository.TournamentsRepository;
import com.group5.htms.repository.UsersRepository;
import com.group5.htms.repository.WalletTransactionsRepository;
import com.group5.htms.repository.WalletsRepository;
import com.group5.htms.service.AuthService;
import com.group5.htms.validation.RaceResultValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RaceResultServiceImplTest {
    @Mock
    private RaceResultsRepository raceResultsRepository;
    @Mock
    private RaceRoundsRepository raceRoundsRepository;
    @Mock
    private JockeyHorseAssignmentsRepository jockeyHorseAssignmentsRepository;
    @Mock
    private RacePointRulesRepository racePointRulesRepository;
    @Mock
    private RaceResultAdminEditAuditRepository raceResultAdminEditAuditRepository;
    @Mock
    private RefereeReportsRepository refereeReportsRepository;
    @Mock
    private TournamentsRepository tournamentsRepository;
    @Mock
    private RacesRepository racesRepository;
    @Mock
    private RaceRefereeAssignmentsRepository raceRefereeAssignmentsRepository;
    @Mock
    private RefereeProfilesRepository refereeProfilesRepository;
    @Mock
    private BetsRepository betsRepository;
    @Mock
    private WalletsRepository walletsRepository;
    @Mock
    private WalletTransactionsRepository walletTransactionsRepository;
    @Mock
    private NotificationsRepository notificationsRepository;
    @Mock
    private UsersRepository usersRepository;
    @Mock
    private AuthService authService;
    @Mock
    private RaceResultMapper raceResultMapper;
    @Mock
    private RefereeRaceAuthorizationService refereeRaceAuthorizationService;

    private RaceResultServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RaceResultServiceImpl(
                raceResultsRepository,
                raceRoundsRepository,
                jockeyHorseAssignmentsRepository,
                racePointRulesRepository,
                raceResultAdminEditAuditRepository,
                refereeReportsRepository,
                tournamentsRepository,
                racesRepository,
                betsRepository,
                walletsRepository,
                walletTransactionsRepository,
                notificationsRepository,
                usersRepository,
                authService,
                raceResultMapper,
                new RaceResultValidator(raceResultsRepository, refereeReportsRepository),
                refereeRaceAuthorizationService,
                new ObjectMapper()
        );
    }

    @Test
    void confirmResultsRanksAndAwardsPointsByFinishTimeInsteadOfRequestedPosition() {
        Races race = Races.builder()
                .id(10)
                .status(RaceStatus.IN_PROGRESS.getValue())
                .build();
        JockeyHorseAssignments slowAssignment = assignment(101);
        JockeyHorseAssignments middleAssignment = assignment(102);
        JockeyHorseAssignments fastAssignment = assignment(103);
        RaceResults slowResult = result(1, race, slowAssignment, "15.280", 1);
        RaceResults middleResult = result(2, race, middleAssignment, "16.070", 2);
        RaceResults fastResult = result(3, race, fastAssignment, "15.000", 3);
        List<RaceResults> results = List.of(slowResult, middleResult, fastResult);

        when(racesRepository.findById(10)).thenReturn(Optional.of(race));
        when(raceResultsRepository.existsByRaces_IdAndStatusIgnoreCase(
                10,
                RaceResultStatus.PUBLISHED.getValue()
        )).thenReturn(false);
        when(raceResultsRepository.findByRaces_IdOrderByFinishPositionAsc(10)).thenReturn(results);
        when(jockeyHorseAssignmentsRepository.findByRaces_IdAndStatusIgnoreCase(
                10,
                JockeyAssignmentStatus.CONFIRMED.getValue()
        )).thenReturn(List.of(slowAssignment, middleAssignment, fastAssignment));
        when(racePointRulesRepository.findByRace_IdAndFinishPosition(10, 1))
                .thenReturn(Optional.of(pointRule(1, 10)));
        when(racePointRulesRepository.findByRace_IdAndFinishPosition(10, 2))
                .thenReturn(Optional.of(pointRule(2, 8)));
        when(racePointRulesRepository.findByRace_IdAndFinishPosition(10, 3))
                .thenReturn(Optional.of(pointRule(3, 6)));
        when(raceResultsRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.confirmResults(10);

        verify(refereeRaceAuthorizationService).requireChiefReferee(10);

        assertThat(fastResult.getFinishPosition()).isEqualTo(1);
        assertThat(middleResult.getFinishPosition()).isEqualTo(2);
        assertThat(slowResult.getFinishPosition()).isEqualTo(3);
        assertThat(fastResult.getPointsAwarded()).isEqualTo(10);
        assertThat(middleResult.getPointsAwarded()).isEqualTo(8);
        assertThat(slowResult.getPointsAwarded()).isEqualTo(6);
    }

    @Test
    void confirmResultsRequiresChiefRefereeAssignment() {
        Races race = Races.builder()
                .id(10)
                .status(RaceStatus.IN_PROGRESS.getValue())
                .build();

        when(racesRepository.findById(10)).thenReturn(Optional.of(race));
        when(refereeRaceAuthorizationService.requireChiefReferee(10))
                .thenThrow(new BadRequestException("Only the chief referee assigned to this race can perform this action"));

        assertThatThrownBy(() -> service.confirmResults(10))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Only the chief referee assigned to this race can perform this action");
    }

    private JockeyHorseAssignments assignment(Integer id) {
        return JockeyHorseAssignments.builder()
                .id(id)
                .status(JockeyAssignmentStatus.CONFIRMED.getValue())
                .build();
    }

    private RaceResults result(
            Integer id,
            Races race,
            JockeyHorseAssignments assignment,
            String finishTimeSec,
            Integer requestedPosition
    ) {
        return RaceResults.builder()
                .id(id)
                .races(race)
                .assignment(assignment)
                .finishTimeSec(new BigDecimal(finishTimeSec))
                .finishPosition(requestedPosition)
                .pointsAwarded(0)
                .isDisqualified(false)
                .status(RaceResultStatus.DRAFT.getValue())
                .build();
    }

    private RacePointRules pointRule(Integer position, Integer points) {
        return RacePointRules.builder()
                .finishPosition(position)
                .points(points)
                .build();
    }
}
