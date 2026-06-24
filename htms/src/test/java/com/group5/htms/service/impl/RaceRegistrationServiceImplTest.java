package com.group5.htms.service.impl;

import com.group5.htms.dto.raceregistration.request.RaceRegistrationCreateRequest;
import com.group5.htms.dto.raceregistration.request.RaceRegistrationApprovalRequest;
import com.group5.htms.dto.raceregistration.request.RaceRegistrationApproveRequest;
import com.group5.htms.dto.raceregistration.request.RaceRegistrationRejectRequest;
import com.group5.htms.dto.raceregistration.response.RaceRegistrationResponse;
import com.group5.htms.entity.HorseOwnerProfiles;
import com.group5.htms.entity.Horses;
import com.group5.htms.entity.RaceRegistrations;
import com.group5.htms.entity.Races;
import com.group5.htms.entity.TournamentSchedules;
import com.group5.htms.entity.Tournaments;
import com.group5.htms.entity.Users;
import com.group5.htms.enums.RaceRegistrationStatus;
import com.group5.htms.enums.RaceStatus;
import com.group5.htms.enums.RoleType;
import com.group5.htms.enums.TournamentStatus;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.mapper.RaceRegistrationMapper;
import com.group5.htms.repository.HorseOwnerProfilesRepository;
import com.group5.htms.repository.HorsesRepository;
import com.group5.htms.repository.JockeyProfilesRepository;
import com.group5.htms.repository.RaceRegistrationsRepository;
import com.group5.htms.repository.RacesRepository;
import com.group5.htms.repository.TournamentsRepository;
import com.group5.htms.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RaceRegistrationServiceImplTest {
    @Mock
    private RaceRegistrationsRepository raceRegistrationsRepository;

    @Mock
    private TournamentsRepository tournamentsRepository;

    @Mock
    private RacesRepository racesRepository;

    @Mock
    private HorsesRepository horsesRepository;

    @Mock
    private HorseOwnerProfilesRepository horseOwnerProfilesRepository;

    @Mock
    private JockeyProfilesRepository jockeyProfilesRepository;

    @Mock
    private AuthService authService;

    @Mock
    private RaceRegistrationMapper raceRegistrationMapper;

    private RaceRegistrationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RaceRegistrationServiceImpl(
                raceRegistrationsRepository,
                tournamentsRepository,
                racesRepository,
                horsesRepository,
                horseOwnerProfilesRepository,
                jockeyProfilesRepository,
                authService,
                raceRegistrationMapper
        );
    }

    @Test
    void createRegistrationSucceedsWhenRegistrationIsOpenAndHorseBelongsToOwner() {
        RaceRegistrationCreateRequest request = request();
        RaceRegistrations registration = RaceRegistrations.builder()
                .status(RaceRegistrationStatus.PENDING.getValue())
                .registeredAt(Instant.now())
                .build();
        RaceRegistrationResponse expectedResponse = RaceRegistrationResponse.builder()
                .regId(99)
                .status(RaceRegistrationStatus.PENDING.getValue())
                .build();
        mockValidCreateReferences();
        when(raceRegistrationsRepository.existsByTournaments_IdAndHorses_IdAndStatusNotIgnoreCase(
                1,
                5,
                RaceRegistrationStatus.DELETED.getValue()
        )).thenReturn(false);
        when(raceRegistrationsRepository.countByRaces_IdAndStatusIgnoreCase(
                2,
                RaceRegistrationStatus.APPROVED.getValue()
        )).thenReturn(0L);
        when(raceRegistrationMapper.toEntity(request)).thenReturn(registration);
        when(raceRegistrationsRepository.save(registration)).thenReturn(registration);
        when(raceRegistrationMapper.toResponse(registration)).thenReturn(expectedResponse);

        RaceRegistrationResponse response = service.createRegistration(request);

        ArgumentCaptor<RaceRegistrations> captor = ArgumentCaptor.forClass(RaceRegistrations.class);
        verify(raceRegistrationsRepository).save(captor.capture());
        assertThat(response).isSameAs(expectedResponse);
        assertThat(captor.getValue().getTournaments().getId()).isEqualTo(1);
        assertThat(captor.getValue().getRaces().getId()).isEqualTo(2);
        assertThat(captor.getValue().getHorses().getId()).isEqualTo(5);
        assertThat(captor.getValue().getOwner().getId()).isEqualTo(1);
        assertThat(captor.getValue().getJockey()).isNull();
    }

    @Test
    void createRegistrationFailsIfHorseAlreadyRegisteredInTournament() {
        RaceRegistrationCreateRequest request = request();
        mockValidCreateReferences();
        when(raceRegistrationsRepository.existsByTournaments_IdAndHorses_IdAndStatusNotIgnoreCase(
                1,
                5,
                RaceRegistrationStatus.DELETED.getValue()
        )).thenReturn(true);

        assertThatThrownBy(() -> service.createRegistration(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Horse can only register once in the same tournament");
    }

    @Test
    void createRegistrationFailsIfHorseRankGroupDoesNotMatchRaceRankGroup() {
        RaceRegistrationCreateRequest request = request();
        mockValidCreateReferences(horse("B"), race(TournamentStatus.REGISTRATION_OPEN.getValue(), RaceStatus.REGISTRATION_OPEN.getValue()));

        assertThatThrownBy(() -> service.createRegistration(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Horse rank group does not match race rank group");
    }

    @Test
    void createRegistrationFailsIfRaceDoesNotBelongToTournament() {
        RaceRegistrationCreateRequest request = request();
        mockValidCurrentOwner();
        Tournaments tournament = tournament(TournamentStatus.REGISTRATION_OPEN.getValue());
        Races race = race(TournamentStatus.REGISTRATION_OPEN.getValue(), RaceStatus.REGISTRATION_OPEN.getValue());
        race.getSchedule().getTournaments().setId(999);
        when(tournamentsRepository.findById(1)).thenReturn(Optional.of(tournament));
        when(racesRepository.findById(2)).thenReturn(Optional.of(race));
        when(horsesRepository.findById(5)).thenReturn(Optional.of(horse("A")));
        when(horseOwnerProfilesRepository.findById(1)).thenReturn(Optional.of(owner()));

        assertThatThrownBy(() -> service.createRegistration(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Race does not belong to this tournament");
    }

    @Test
    void createRegistrationFailsIfTournamentIsNotRegistrationOpen() {
        RaceRegistrationCreateRequest request = request();
        mockValidCreateReferences(tournament(TournamentStatus.UPCOMING.getValue()), race(TournamentStatus.UPCOMING.getValue(), RaceStatus.REGISTRATION_OPEN.getValue()));

        assertThatThrownBy(() -> service.createRegistration(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Registration is not open for this tournament");
    }

    @Test
    void createRegistrationFailsIfRaceIsNotRegistrationOpen() {
        RaceRegistrationCreateRequest request = request();
        mockValidCreateReferences(tournament(TournamentStatus.REGISTRATION_OPEN.getValue()), race(TournamentStatus.REGISTRATION_OPEN.getValue(), RaceStatus.SCHEDULED.getValue()));

        assertThatThrownBy(() -> service.createRegistration(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Registration is not open for this race");
    }

    @Test
    void approveRegistrationSucceedsIfRegistrationIsPending() {
        RaceRegistrations registration = registration(RaceRegistrationStatus.PENDING.getValue());
        RaceRegistrationResponse expectedResponse = RaceRegistrationResponse.builder()
                .regId(10)
                .status(RaceRegistrationStatus.APPROVED.getValue())
                .build();
        when(raceRegistrationsRepository.findById(10)).thenReturn(Optional.of(registration));
        when(raceRegistrationsRepository.countByRaces_IdAndStatusIgnoreCase(
                2,
                RaceRegistrationStatus.APPROVED.getValue()
        )).thenReturn(0L);
        when(authService.getCurrentUserId()).thenReturn(99);
        when(raceRegistrationsRepository.save(registration)).thenReturn(registration);
        when(raceRegistrationMapper.toResponse(registration)).thenReturn(expectedResponse);

        RaceRegistrationResponse response = service.approveRegistration(10, new RaceRegistrationApproveRequest());

        assertThat(response).isSameAs(expectedResponse);
        assertThat(registration.getStatus()).isEqualTo(RaceRegistrationStatus.APPROVED.getValue());
        assertThat(registration.getApprovedAt()).isNotNull();
        assertThat(registration.getApprovedBy().getId()).isEqualTo(99);
    }

    @Test
    void approveRegistrationFailsIfRegistrationIsNotPending() {
        RaceRegistrations registration = registration(RaceRegistrationStatus.APPROVED.getValue());
        when(raceRegistrationsRepository.findById(10)).thenReturn(Optional.of(registration));

        assertThatThrownBy(() -> service.approveRegistration(10, new RaceRegistrationApproveRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Only pending registrations can be approved");
    }

    @Test
    void approveRegistrationFailsIfRaceMaxHorsesReached() {
        RaceRegistrations registration = registration(RaceRegistrationStatus.PENDING.getValue());
        when(raceRegistrationsRepository.findById(10)).thenReturn(Optional.of(registration));
        when(raceRegistrationsRepository.countByRaces_IdAndStatusIgnoreCase(
                2,
                RaceRegistrationStatus.APPROVED.getValue()
        )).thenReturn(2L);

        assertThatThrownBy(() -> service.approveRegistration(10, new RaceRegistrationApproveRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Race maximum horses limit has been reached");
    }

    @Test
    void rejectRegistrationSucceedsIfRegistrationIsPending() {
        RaceRegistrations registration = registration(RaceRegistrationStatus.PENDING.getValue());
        RaceRegistrationResponse expectedResponse = RaceRegistrationResponse.builder()
                .regId(10)
                .status(RaceRegistrationStatus.REJECTED.getValue())
                .build();
        when(raceRegistrationsRepository.findById(10)).thenReturn(Optional.of(registration));
        when(raceRegistrationsRepository.save(registration)).thenReturn(registration);
        when(raceRegistrationMapper.toResponse(registration)).thenReturn(expectedResponse);

        RaceRegistrationResponse response = service.rejectRegistration(10, new RaceRegistrationRejectRequest());

        assertThat(response).isSameAs(expectedResponse);
        assertThat(registration.getStatus()).isEqualTo(RaceRegistrationStatus.REJECTED.getValue());
    }

    @Test
    void rejectRegistrationFailsIfRegistrationIsNotPending() {
        RaceRegistrations registration = registration(RaceRegistrationStatus.APPROVED.getValue());
        when(raceRegistrationsRepository.findById(10)).thenReturn(Optional.of(registration));

        assertThatThrownBy(() -> service.rejectRegistration(10, new RaceRegistrationRejectRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Only pending registrations can be rejected");
    }

    @Test
    void legacyApproveRegistrationRejectsArbitraryStatusUpdate() {
        RaceRegistrationApprovalRequest request = new RaceRegistrationApprovalRequest();
        request.setStatus(RaceRegistrationStatus.REJECTED.getValue());

        assertThatThrownBy(() -> service.approveRegistration(10, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Use workflow transition APIs to update status");
    }

    private void mockValidCreateReferences() {
        mockValidCreateReferences(horse("A"), race(TournamentStatus.REGISTRATION_OPEN.getValue(), RaceStatus.REGISTRATION_OPEN.getValue()));
    }

    private void mockValidCreateReferences(Horses horse, Races race) {
        mockValidCreateReferences(tournament(TournamentStatus.REGISTRATION_OPEN.getValue()), race, horse);
    }

    private void mockValidCreateReferences(Tournaments tournament, Races race) {
        mockValidCreateReferences(tournament, race, horse("A"));
    }

    private void mockValidCreateReferences(Tournaments tournament, Races race, Horses horse) {
        mockValidCurrentOwner();
        when(tournamentsRepository.findById(1)).thenReturn(Optional.of(tournament));
        when(racesRepository.findById(2)).thenReturn(Optional.of(race));
        when(horsesRepository.findById(5)).thenReturn(Optional.of(horse));
        when(horseOwnerProfilesRepository.findById(1)).thenReturn(Optional.of(owner()));
    }

    private void mockValidCurrentOwner() {
        Users user = new Users();
        user.setId(1);
        user.setRoleType(RoleType.HORSE_OWNER.getValue());
        when(authService.getCurrentUserId()).thenReturn(1);
        when(authService.getCurrentUser()).thenReturn(user);
    }

    private RaceRegistrationCreateRequest request() {
        RaceRegistrationCreateRequest request = new RaceRegistrationCreateRequest();
        request.setTournamentId(1);
        request.setRaceId(2);
        request.setHorseId(5);
        return request;
    }

    private Tournaments tournament(String status) {
        return Tournaments.builder()
                .id(1)
                .status(status)
                .build();
    }

    private Races race(String tournamentStatus, String raceStatus) {
        Tournaments tournament = Tournaments.builder()
                .id(1)
                .status(tournamentStatus)
                .build();
        TournamentSchedules schedule = TournamentSchedules.builder()
                .id(3)
                .tournaments(tournament)
                .build();

        return Races.builder()
                .id(2)
                .schedule(schedule)
                .rankGroup("A")
                .maxHorses(2)
                .status(raceStatus)
                .build();
    }

    private Horses horse(String rankGroup) {
        return Horses.builder()
                .id(5)
                .owner(owner())
                .rankGroup(rankGroup)
                .status("active")
                .build();
    }

    private RaceRegistrations registration(String status) {
        return RaceRegistrations.builder()
                .id(10)
                .tournaments(tournament(TournamentStatus.REGISTRATION_OPEN.getValue()))
                .races(race(TournamentStatus.REGISTRATION_OPEN.getValue(), RaceStatus.REGISTRATION_OPEN.getValue()))
                .horses(horse("A"))
                .owner(owner())
                .status(status)
                .registeredAt(Instant.now())
                .build();
    }

    private HorseOwnerProfiles owner() {
        return HorseOwnerProfiles.builder()
                .id(1)
                .build();
    }
}
