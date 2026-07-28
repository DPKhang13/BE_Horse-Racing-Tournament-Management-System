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
import com.group5.htms.repository.JockeyHorseAssignmentsRepository;
import com.group5.htms.repository.RaceRegistrationsRepository;
import com.group5.htms.repository.RacesRepository;
import com.group5.htms.repository.TournamentsRepository;
import com.group5.htms.service.TournamentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TournamentRegistrationCloseScheduler {
    private static final Logger log = LoggerFactory.getLogger(TournamentRegistrationCloseScheduler.class);

    private final TournamentsRepository tournamentsRepository;
    private final RacesRepository racesRepository;
    private final RaceRegistrationsRepository raceRegistrationsRepository;
    private final JockeyHorseAssignmentsRepository jockeyHorseAssignmentsRepository;
    private final TournamentService tournamentService;

    @Scheduled(fixedDelay = 60_000)
    public void closeAutoClosableRegistrationWindows() {
        Instant now = Instant.now();
        List<Tournaments> openTournaments = tournamentsRepository.findByStatusIgnoreCaseOrderByStartDateAsc(
                TournamentStatus.REGISTRATION_OPEN.getValue()
        );

        openTournaments.stream()
                .filter(tournament -> shouldAutoClose(tournament, now))
                .forEach(tournament -> closeRegistration(tournament, now));
    }

    private boolean shouldAutoClose(Tournaments tournament, Instant now) {
        return isRegistrationCloseTimeReached(tournament, now) || areAllRegistrationOpenRacesFull(tournament);
    }

    private boolean isRegistrationCloseTimeReached(Tournaments tournament, Instant now) {
        return tournament.getRegistrationCloseAt() != null && !tournament.getRegistrationCloseAt().isAfter(now);
    }

    private boolean areAllRegistrationOpenRacesFull(Tournaments tournament) {
        List<Races> openRaces = racesRepository
                .findBySchedule_Tournaments_IdAndStatusIgnoreCaseOrderByScheduledAtAsc(
                        tournament.getId(),
                        RaceStatus.REGISTRATION_OPEN.getValue()
                );

        if (openRaces.isEmpty()) {
            return false;
        }

        List<RaceRegistrations> registrations = raceRegistrationsRepository.findByTournaments_Id(tournament.getId());
        if (registrations.isEmpty()) {
            return false;
        }

        Set<Integer> confirmedRegistrationIds = jockeyHorseAssignmentsRepository
                .findByReg_IdIn(registrations.stream().map(RaceRegistrations::getId).toList())
                .stream()
                .filter(assignment -> JockeyAssignmentStatus.CONFIRMED.equalsValue(assignment.getStatus()))
                .map(JockeyHorseAssignments::getReg)
                .map(RaceRegistrations::getId)
                .collect(Collectors.toSet());

        Map<Integer, Long> eligibleCountByRaceId = registrations.stream()
                .filter(registration -> RaceRegistrationStatus.APPROVED.equalsValue(registration.getStatus()))
                .filter(registration -> confirmedRegistrationIds.contains(registration.getId()))
                .collect(Collectors.groupingBy(registration -> registration.getRaces().getId(), Collectors.counting()));

        return openRaces.stream().allMatch(race -> {
            Integer maxHorses = race.getMaxHorses();
            if (maxHorses == null || maxHorses <= 0) {
                return false;
            }
            return eligibleCountByRaceId.getOrDefault(race.getId(), 0L) >= maxHorses;
        });
    }

    private void closeRegistration(Tournaments tournament, Instant now) {
        boolean registrationCloseTimeReached = isRegistrationCloseTimeReached(tournament, now);
        String reason = registrationCloseTimeReached
                ? "registration close time reached"
                : "all registration-open races are full";

        try {
            tournamentService.closeRegistration(tournament.getId(), autoCloseRequest(registrationCloseTimeReached));
        } catch (RuntimeException ex) {
            log.warn(
                    "Failed to auto close registration for tournament {} because {}",
                    tournament.getId(),
                    reason,
                    ex
            );
        }
    }

    private CloseRegistrationRequest autoCloseRequest(boolean allowCloseWithoutEligibleRaces) {
        CloseRegistrationRequest request = new CloseRegistrationRequest();
        request.setAutoRejectPending(true);
        request.setAutoCancelUnconfirmed(true);
        request.setAllowCloseWithoutEligibleRaces(allowCloseWithoutEligibleRaces);
        return request;
    }
}
