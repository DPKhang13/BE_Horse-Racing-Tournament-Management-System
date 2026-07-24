package com.group5.htms.service.impl;

import com.group5.htms.entity.JockeyHorseAssignments;
import com.group5.htms.entity.RaceRegistrations;
import com.group5.htms.enums.JockeyAssignmentStatus;
import com.group5.htms.enums.RaceRegistrationStatus;
import com.group5.htms.enums.RaceStatus;
import com.group5.htms.repository.JockeyHorseAssignmentsRepository;
import com.group5.htms.repository.RaceRegistrationsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RaceParticipationCancellationService {

    private static final Set<String> ACTIVE_REGISTRATION_STATUSES = Set.of(
            RaceRegistrationStatus.PENDING.getValue(),
            RaceRegistrationStatus.APPROVED.getValue(),
            RaceRegistrationStatus.CONFIRMED.getValue()
    );

    private static final Set<String> ACTIVE_ASSIGNMENT_STATUSES = Set.of(
            JockeyAssignmentStatus.PENDING.getValue(),
            JockeyAssignmentStatus.ACCEPTED.getValue(),
            JockeyAssignmentStatus.CONFIRMED.getValue()
    );

    private final RaceRegistrationsRepository raceRegistrationsRepository;
    private final JockeyHorseAssignmentsRepository jockeyHorseAssignmentsRepository;

    @Transactional
    public void cancelRaceParticipants(Integer raceId) {
        cancelParticipants(raceRegistrationsRepository.findByRaces_Id(raceId), Instant.now());
    }

    @Transactional
    public void cancelTournamentParticipants(Integer tournamentId) {
        List<RaceRegistrations> registrations = raceRegistrationsRepository.findByTournaments_Id(tournamentId)
                .stream()
                .filter(registration -> registration.getRaces() == null
                        || !RaceStatus.COMPLETED.equalsValue(registration.getRaces().getStatus()))
                .toList();
        cancelParticipants(registrations, Instant.now());
    }

    private void cancelParticipants(List<RaceRegistrations> registrations, Instant cancelledAt) {
        if (registrations.isEmpty()) {
            return;
        }

        List<Integer> registrationIds = registrations.stream()
                .map(RaceRegistrations::getId)
                .toList();
        List<JockeyHorseAssignments> assignments = jockeyHorseAssignmentsRepository.findByReg_IdIn(registrationIds);

        List<RaceRegistrations> activeRegistrations = registrations.stream()
                .filter(registration -> hasStatus(ACTIVE_REGISTRATION_STATUSES, registration.getStatus()))
                .toList();
        activeRegistrations.forEach(registration -> {
            registration.setStatus(RaceRegistrationStatus.CANCELLED.getValue());
            registration.setOwnerConfirmationStatus(RaceRegistrationStatus.CANCELLED.getValue());
            registration.setJockey(null);
            registration.setOwnerConfirmedAt(null);
        });

        List<JockeyHorseAssignments> activeAssignments = assignments.stream()
                .filter(assignment -> hasStatus(ACTIVE_ASSIGNMENT_STATUSES, assignment.getStatus()))
                .toList();
        activeAssignments.forEach(assignment -> {
            assignment.setStatus(JockeyAssignmentStatus.CANCELLED.getValue());
            assignment.setCancelledAt(cancelledAt);
            assignment.setResponseDeadline(null);
        });

        if (!activeRegistrations.isEmpty()) {
            raceRegistrationsRepository.saveAll(activeRegistrations);
        }
        if (!activeAssignments.isEmpty()) {
            jockeyHorseAssignmentsRepository.saveAll(activeAssignments);
        }
    }

    private boolean hasStatus(Set<String> statuses, String status) {
        return status != null && statuses.stream().anyMatch(value -> value.equalsIgnoreCase(status.trim()));
    }
}
