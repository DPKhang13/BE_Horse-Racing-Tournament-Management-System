package com.group5.htms.service.impl;

import com.group5.htms.dto.jockeyassignment.request.JockeyInvitationCreateRequest;
import com.group5.htms.dto.jockeyassignment.request.JockeyInvitationResponseRequest;
import com.group5.htms.dto.jockeyassignment.response.JockeyAssignmentResponse;
import com.group5.htms.entity.HorseOwnerProfiles;
import com.group5.htms.entity.Horses;
import com.group5.htms.entity.JockeyHorseAssignments;
import com.group5.htms.entity.JockeyProfiles;
import com.group5.htms.entity.RaceRegistrations;
import com.group5.htms.entity.Races;
import com.group5.htms.entity.TournamentSchedules;
import com.group5.htms.entity.Tournaments;
import com.group5.htms.entity.Users;
import com.group5.htms.enums.JockeyAssignmentStatus;
import com.group5.htms.enums.JockeyStatus;
import com.group5.htms.enums.RaceRegistrationStatus;
import com.group5.htms.enums.RaceStatus;
import com.group5.htms.enums.RoleType;
import com.group5.htms.enums.TournamentStatus;
import com.group5.htms.enums.UserStatus;
import com.group5.htms.mapper.JockeyAssignmentMapper;
import com.group5.htms.repository.JockeyHorseAssignmentsRepository;
import com.group5.htms.repository.JockeyProfilesRepository;
import com.group5.htms.repository.RaceRegistrationsRepository;
import com.group5.htms.repository.RacesRepository;
import com.group5.htms.service.AuthService;
import com.group5.htms.validation.JockeyAssignmentValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JockeyAssignmentServiceImplTest {
    @Mock
    private JockeyHorseAssignmentsRepository jockeyHorseAssignmentsRepository;

    @Mock
    private RaceRegistrationsRepository raceRegistrationsRepository;

    @Mock
    private RacesRepository racesRepository;

    @Mock
    private JockeyProfilesRepository jockeyProfilesRepository;

    @Mock
    private AuthService authService;

    @Mock
    private JockeyAssignmentMapper jockeyAssignmentMapper;

    private JockeyAssignmentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new JockeyAssignmentServiceImpl(
                jockeyHorseAssignmentsRepository,
                raceRegistrationsRepository,
                racesRepository,
                jockeyProfilesRepository,
                authService,
                jockeyAssignmentMapper,
                new JockeyAssignmentValidator()
        );
    }

    @Test
    void createInvitationAllowsPendingRegistrationAndUsesFortyEightHourDeadline() {
        RaceRegistrations registration = pendingRegistration();
        Races race = registration.getRaces();
        JockeyProfiles jockey = jockey();
        JockeyInvitationCreateRequest request = createRequest();
        JockeyHorseAssignments assignment = new JockeyHorseAssignments();
        JockeyAssignmentResponse expectedResponse = JockeyAssignmentResponse.builder()
                .assignmentId(50)
                .status(JockeyAssignmentStatus.PENDING.getValue())
                .build();
        Instant before = Instant.now();

        when(raceRegistrationsRepository.findById(10)).thenReturn(Optional.of(registration));
        when(racesRepository.findById(2)).thenReturn(Optional.of(race));
        when(jockeyProfilesRepository.findById(7)).thenReturn(Optional.of(jockey));
        when(authService.currentUserHasRole(RoleType.ADMIN.getValue())).thenReturn(false);
        when(authService.getCurrentUserId()).thenReturn(1);
        when(jockeyHorseAssignmentsRepository.findByReg_IdAndStatusIn(10, activeStatuses())).thenReturn(List.of());
        when(jockeyHorseAssignmentsRepository.findByRaces_IdAndJockey_IdAndStatusIn(2, 7, activeStatuses())).thenReturn(List.of());
        when(jockeyHorseAssignmentsRepository.findByReg_IdAndRaces_IdAndJockey_IdAndStatusIn(10, 2, 7, terminalStatuses())).thenReturn(List.of());
        when(jockeyAssignmentMapper.toEntity(request)).thenReturn(assignment);
        when(jockeyHorseAssignmentsRepository.save(assignment)).thenReturn(assignment);
        when(jockeyAssignmentMapper.toResponse(assignment)).thenReturn(expectedResponse);

        JockeyAssignmentResponse response = service.createInvitation(request);

        Instant after = Instant.now();
        ArgumentCaptor<JockeyHorseAssignments> captor = ArgumentCaptor.forClass(JockeyHorseAssignments.class);
        verify(jockeyHorseAssignmentsRepository).save(captor.capture());
        assertThat(response).isSameAs(expectedResponse);
        assertThat(captor.getValue().getReg()).isSameAs(registration);
        assertThat(captor.getValue().getStatus()).isEqualTo(JockeyAssignmentStatus.PENDING.getValue());
        assertThat(captor.getValue().getResponseDeadline())
                .isBetween(before.plus(Duration.ofHours(48)), after.plus(Duration.ofHours(48)));
    }

    @Test
    void createInvitationClearsRejectedOldDeadlineAndCreatesNewPendingInvitationForSameJockey() {
        RaceRegistrations registration = pendingRegistration();
        Races race = registration.getRaces();
        JockeyProfiles jockey = jockey();
        JockeyInvitationCreateRequest request = createRequest();
        JockeyHorseAssignments oldRejected = assignment(registration, jockey, JockeyAssignmentStatus.REJECTED.getValue());
        oldRejected.setId(40);
        oldRejected.setResponseDeadline(Instant.now().plus(Duration.ofHours(12)));
        JockeyHorseAssignments newAssignment = new JockeyHorseAssignments();
        JockeyAssignmentResponse expectedResponse = JockeyAssignmentResponse.builder()
                .assignmentId(51)
                .status(JockeyAssignmentStatus.PENDING.getValue())
                .build();

        when(raceRegistrationsRepository.findById(10)).thenReturn(Optional.of(registration));
        when(racesRepository.findById(2)).thenReturn(Optional.of(race));
        when(jockeyProfilesRepository.findById(7)).thenReturn(Optional.of(jockey));
        when(authService.currentUserHasRole(RoleType.ADMIN.getValue())).thenReturn(false);
        when(authService.getCurrentUserId()).thenReturn(1);
        when(jockeyHorseAssignmentsRepository.findByReg_IdAndStatusIn(10, activeStatuses())).thenReturn(List.of());
        when(jockeyHorseAssignmentsRepository.findByRaces_IdAndJockey_IdAndStatusIn(2, 7, activeStatuses())).thenReturn(List.of());
        when(jockeyHorseAssignmentsRepository.findByReg_IdAndRaces_IdAndJockey_IdAndStatusIn(10, 2, 7, terminalStatuses()))
                .thenReturn(List.of(oldRejected));
        when(jockeyAssignmentMapper.toEntity(request)).thenReturn(newAssignment);
        when(jockeyHorseAssignmentsRepository.save(newAssignment)).thenReturn(newAssignment);
        when(jockeyAssignmentMapper.toResponse(newAssignment)).thenReturn(expectedResponse);

        JockeyAssignmentResponse response = service.createInvitation(request);

        ArgumentCaptor<JockeyHorseAssignments> captor = ArgumentCaptor.forClass(JockeyHorseAssignments.class);
        verify(jockeyHorseAssignmentsRepository).saveAll(List.of(oldRejected));
        verify(jockeyHorseAssignmentsRepository).save(captor.capture());
        assertThat(response).isSameAs(expectedResponse);
        assertThat(oldRejected.getResponseDeadline()).isNull();
        assertThat(captor.getValue().getStatus()).isEqualTo(JockeyAssignmentStatus.PENDING.getValue());
        assertThat(captor.getValue().getResponseDeadline()).isNotNull();
    }

    @Test
    void respondInvitationAcceptsWithoutConfirmingRegistration() {
        RaceRegistrations registration = pendingRegistration();
        JockeyProfiles jockey = jockey();
        JockeyHorseAssignments assignment = assignment(registration, jockey, JockeyAssignmentStatus.PENDING.getValue());
        JockeyInvitationResponseRequest request = responseRequest(JockeyAssignmentStatus.ACCEPTED.getValue());
        JockeyAssignmentResponse expectedResponse = JockeyAssignmentResponse.builder()
                .assignmentId(50)
                .status(JockeyAssignmentStatus.ACCEPTED.getValue())
                .build();

        when(jockeyHorseAssignmentsRepository.findById(50)).thenReturn(Optional.of(assignment));
        when(authService.getCurrentUserId()).thenReturn(7);
        when(jockeyHorseAssignmentsRepository.save(assignment)).thenReturn(assignment);
        when(jockeyAssignmentMapper.toResponse(assignment)).thenReturn(expectedResponse);

        JockeyAssignmentResponse response = service.respondInvitation(50, request);

        assertThat(response).isSameAs(expectedResponse);
        assertThat(assignment.getStatus()).isEqualTo(JockeyAssignmentStatus.ACCEPTED.getValue());
        assertThat(assignment.getRespondedAt()).isNotNull();
        assertThat(registration.getJockey()).isNull();
        assertThat(registration.getOwnerConfirmationStatus()).isEqualTo(RaceRegistrationStatus.PENDING.getValue());
        assertThat(registration.getOwnerConfirmedAt()).isNull();
        assertThat(jockey.getStatus()).isEqualTo(JockeyStatus.AVAILABLE.getValue());
    }

    @Test
    void respondInvitationRejectClearsResponseDeadline() {
        RaceRegistrations registration = pendingRegistration();
        JockeyProfiles jockey = jockey();
        JockeyHorseAssignments assignment = assignment(registration, jockey, JockeyAssignmentStatus.PENDING.getValue());
        JockeyInvitationResponseRequest request = responseRequest(JockeyAssignmentStatus.REJECTED.getValue());
        JockeyAssignmentResponse expectedResponse = JockeyAssignmentResponse.builder()
                .assignmentId(50)
                .status(JockeyAssignmentStatus.REJECTED.getValue())
                .build();

        when(jockeyHorseAssignmentsRepository.findById(50)).thenReturn(Optional.of(assignment));
        when(authService.getCurrentUserId()).thenReturn(7);
        when(jockeyHorseAssignmentsRepository.save(assignment)).thenReturn(assignment);
        when(jockeyAssignmentMapper.toResponse(assignment)).thenReturn(expectedResponse);

        JockeyAssignmentResponse response = service.respondInvitation(50, request);

        assertThat(response).isSameAs(expectedResponse);
        assertThat(assignment.getStatus()).isEqualTo(JockeyAssignmentStatus.REJECTED.getValue());
        assertThat(assignment.getRespondedAt()).isNotNull();
        assertThat(assignment.getResponseDeadline()).isNull();
    }

    @Test
    void confirmAssignmentConfirmsRegistrationForAdminApproval() {
        RaceRegistrations registration = pendingRegistration();
        JockeyProfiles jockey = jockey();
        JockeyHorseAssignments assignment = assignment(registration, jockey, JockeyAssignmentStatus.ACCEPTED.getValue());
        JockeyAssignmentResponse expectedResponse = JockeyAssignmentResponse.builder()
                .assignmentId(50)
                .status(JockeyAssignmentStatus.CONFIRMED.getValue())
                .build();

        when(jockeyHorseAssignmentsRepository.findById(50)).thenReturn(Optional.of(assignment));
        when(authService.currentUserHasRole(RoleType.ADMIN.getValue())).thenReturn(false);
        when(authService.getCurrentUserId()).thenReturn(1);
        when(jockeyHorseAssignmentsRepository.save(assignment)).thenReturn(assignment);
        when(jockeyAssignmentMapper.toResponse(assignment)).thenReturn(expectedResponse);

        JockeyAssignmentResponse response = service.confirmAssignment(50);

        assertThat(response).isSameAs(expectedResponse);
        assertThat(assignment.getStatus()).isEqualTo(JockeyAssignmentStatus.CONFIRMED.getValue());
        assertThat(registration.getJockey()).isSameAs(jockey);
        assertThat(registration.getOwnerConfirmationStatus()).isEqualTo(RaceRegistrationStatus.CONFIRMED.getValue());
        assertThat(registration.getOwnerConfirmedAt()).isNotNull();
        assertThat(jockey.getStatus()).isEqualTo(JockeyStatus.UNAVAILABLE.getValue());
    }

    private JockeyInvitationCreateRequest createRequest() {
        JockeyInvitationCreateRequest request = new JockeyInvitationCreateRequest();
        request.setRegistrationId(10);
        request.setRaceId(2);
        request.setJockeyId(7);
        return request;
    }

    private JockeyInvitationResponseRequest responseRequest(String status) {
        JockeyInvitationResponseRequest request = new JockeyInvitationResponseRequest();
        request.setStatus(status);
        return request;
    }

    private RaceRegistrations pendingRegistration() {
        Tournaments tournament = Tournaments.builder()
                .id(1)
                .status(TournamentStatus.REGISTRATION_OPEN.getValue())
                .build();
        Races race = Races.builder()
                .id(2)
                .status(RaceStatus.REGISTRATION_OPEN.getValue())
                .schedule(TournamentSchedules.builder().id(3).tournaments(tournament).build())
                .build();
        HorseOwnerProfiles owner = HorseOwnerProfiles.builder().id(1).build();

        return RaceRegistrations.builder()
                .id(10)
                .tournaments(tournament)
                .races(race)
                .horses(Horses.builder().id(5).owner(owner).build())
                .owner(owner)
                .status(RaceRegistrationStatus.PENDING.getValue())
                .ownerConfirmationStatus(RaceRegistrationStatus.PENDING.getValue())
                .registeredAt(Instant.now())
                .build();
    }

    private JockeyProfiles jockey() {
        return JockeyProfiles.builder()
                .id(7)
                .users(Users.builder()
                        .id(7)
                        .roleType(RoleType.JOCKEY.getValue())
                        .status(UserStatus.ACTIVE.getValue())
                        .build())
                .status(JockeyStatus.AVAILABLE.getValue())
                .build();
    }

    private JockeyHorseAssignments assignment(RaceRegistrations registration, JockeyProfiles jockey, String status) {
        return JockeyHorseAssignments.builder()
                .id(50)
                .reg(registration)
                .races(registration.getRaces())
                .jockey(jockey)
                .status(status)
                .invitedAt(Instant.now())
                .responseDeadline(Instant.now().plus(Duration.ofHours(48)))
                .build();
    }

    private List<String> activeStatuses() {
        return List.of(
                JockeyAssignmentStatus.PENDING.getValue(),
                JockeyAssignmentStatus.ACCEPTED.getValue(),
                JockeyAssignmentStatus.CONFIRMED.getValue()
        );
    }

    private List<String> terminalStatuses() {
        return List.of(
                JockeyAssignmentStatus.REJECTED.getValue(),
                JockeyAssignmentStatus.CANCELLED.getValue(),
                JockeyAssignmentStatus.EXPIRED.getValue()
        );
    }
}

