package com.group5.htms.service.impl;

import com.group5.htms.entity.JockeyHorseAssignments;
import com.group5.htms.entity.JockeyProfiles;
import com.group5.htms.entity.RaceRegistrations;
import com.group5.htms.entity.Races;
import com.group5.htms.enums.JockeyAssignmentStatus;
import com.group5.htms.enums.RaceRegistrationStatus;
import com.group5.htms.enums.RaceStatus;
import com.group5.htms.repository.JockeyHorseAssignmentsRepository;
import com.group5.htms.repository.RaceRegistrationsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RaceParticipationCancellationServiceTest {

    @Mock
    private RaceRegistrationsRepository raceRegistrationsRepository;

    @Mock
    private JockeyHorseAssignmentsRepository jockeyHorseAssignmentsRepository;

    private RaceParticipationCancellationService service;

    @BeforeEach
    void setUp() {
        service = new RaceParticipationCancellationService(
                raceRegistrationsRepository,
                jockeyHorseAssignmentsRepository
        );
    }

    @Test
    void cancelRaceParticipantsReleasesActiveRegistrationsAndAssignments() {
        JockeyProfiles jockey = JockeyProfiles.builder().id(3).build();
        RaceRegistrations pendingRegistration = registration(
                1,
                RaceRegistrationStatus.PENDING.getValue(),
                jockey
        );
        RaceRegistrations approvedRegistration = registration(
                2,
                RaceRegistrationStatus.APPROVED.getValue(),
                jockey
        );
        RaceRegistrations rejectedRegistration = registration(
                3,
                RaceRegistrationStatus.REJECTED.getValue(),
                jockey
        );
        JockeyHorseAssignments pendingAssignment = assignment(
                11,
                pendingRegistration,
                JockeyAssignmentStatus.PENDING.getValue()
        );
        JockeyHorseAssignments confirmedAssignment = assignment(
                12,
                approvedRegistration,
                JockeyAssignmentStatus.CONFIRMED.getValue()
        );
        JockeyHorseAssignments rejectedAssignment = assignment(
                13,
                rejectedRegistration,
                JockeyAssignmentStatus.REJECTED.getValue()
        );

        when(raceRegistrationsRepository.findByRaces_Id(10)).thenReturn(List.of(
                pendingRegistration,
                approvedRegistration,
                rejectedRegistration
        ));
        when(jockeyHorseAssignmentsRepository.findByReg_IdIn(List.of(1, 2, 3))).thenReturn(List.of(
                pendingAssignment,
                confirmedAssignment,
                rejectedAssignment
        ));

        service.cancelRaceParticipants(10);

        assertReleasedRegistration(pendingRegistration);
        assertReleasedRegistration(approvedRegistration);
        assertThat(rejectedRegistration.getStatus()).isEqualTo(RaceRegistrationStatus.REJECTED.getValue());
        assertThat(rejectedRegistration.getJockey()).isSameAs(jockey);

        assertCancelledAssignment(pendingAssignment);
        assertCancelledAssignment(confirmedAssignment);
        assertThat(rejectedAssignment.getStatus()).isEqualTo(JockeyAssignmentStatus.REJECTED.getValue());
        assertThat(rejectedAssignment.getCancelledAt()).isNull();

        verify(raceRegistrationsRepository).saveAll(List.of(pendingRegistration, approvedRegistration));
        verify(jockeyHorseAssignmentsRepository).saveAll(List.of(pendingAssignment, confirmedAssignment));
    }

    @Test
    void cancelTournamentParticipantsReleasesAllTournamentRegistrations() {
        RaceRegistrations registration = registration(
                1,
                RaceRegistrationStatus.CONFIRMED.getValue(),
                JockeyProfiles.builder().id(3).build()
        );
        registration.setRaces(Races.builder().status(RaceStatus.READY.getValue()).build());
        RaceRegistrations completedRegistration = registration(
                2,
                RaceRegistrationStatus.APPROVED.getValue(),
                JockeyProfiles.builder().id(4).build()
        );
        completedRegistration.setRaces(Races.builder().status(RaceStatus.COMPLETED.getValue()).build());
        JockeyHorseAssignments assignment = assignment(
                11,
                registration,
                JockeyAssignmentStatus.ACCEPTED.getValue()
        );
        JockeyHorseAssignments completedAssignment = assignment(
                12,
                completedRegistration,
                JockeyAssignmentStatus.CONFIRMED.getValue()
        );

        when(raceRegistrationsRepository.findByTournaments_Id(5)).thenReturn(List.of(
                registration,
                completedRegistration
        ));
        when(jockeyHorseAssignmentsRepository.findByReg_IdIn(List.of(1))).thenReturn(List.of(assignment));

        service.cancelTournamentParticipants(5);

        assertReleasedRegistration(registration);
        assertCancelledAssignment(assignment);
        assertThat(completedRegistration.getStatus()).isEqualTo(RaceRegistrationStatus.APPROVED.getValue());
        assertThat(completedRegistration.getJockey()).isNotNull();
        assertThat(completedAssignment.getStatus()).isEqualTo(JockeyAssignmentStatus.CONFIRMED.getValue());
        verify(raceRegistrationsRepository).saveAll(List.of(registration));
        verify(jockeyHorseAssignmentsRepository).saveAll(List.of(assignment));
    }

    private RaceRegistrations registration(Integer id, String status, JockeyProfiles jockey) {
        return RaceRegistrations.builder()
                .id(id)
                .status(status)
                .ownerConfirmationStatus(RaceRegistrationStatus.CONFIRMED.getValue())
                .ownerConfirmedAt(Instant.parse("2026-07-01T08:00:00Z"))
                .jockey(jockey)
                .build();
    }

    private JockeyHorseAssignments assignment(
            Integer id,
            RaceRegistrations registration,
            String status
    ) {
        return JockeyHorseAssignments.builder()
                .id(id)
                .reg(registration)
                .status(status)
                .responseDeadline(Instant.parse("2026-07-02T08:00:00Z"))
                .build();
    }

    private void assertReleasedRegistration(RaceRegistrations registration) {
        assertThat(registration.getStatus()).isEqualTo(RaceRegistrationStatus.CANCELLED.getValue());
        assertThat(registration.getOwnerConfirmationStatus())
                .isEqualTo(RaceRegistrationStatus.CANCELLED.getValue());
        assertThat(registration.getJockey()).isNull();
        assertThat(registration.getOwnerConfirmedAt()).isNull();
    }

    private void assertCancelledAssignment(JockeyHorseAssignments assignment) {
        assertThat(assignment.getStatus()).isEqualTo(JockeyAssignmentStatus.CANCELLED.getValue());
        assertThat(assignment.getCancelledAt()).isNotNull();
        assertThat(assignment.getResponseDeadline()).isNull();
    }
}
