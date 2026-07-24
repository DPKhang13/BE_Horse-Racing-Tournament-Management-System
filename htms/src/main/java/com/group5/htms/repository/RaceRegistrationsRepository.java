package com.group5.htms.repository;

import com.group5.htms.entity.RaceRegistrations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface RaceRegistrationsRepository extends JpaRepository<RaceRegistrations, Integer> {
    List<RaceRegistrations> findByHorses_Id(Integer horseId);

    List<RaceRegistrations> findByTournaments_Id(Integer tournamentId);

    List<RaceRegistrations> findByOwner_IdOrderByRegisteredAtDesc(Integer ownerId);

    List<RaceRegistrations> findByStatusIgnoreCaseOrderByRegisteredAtDesc(String status);

    List<RaceRegistrations> findByStatusIgnoreCaseAndOwnerConfirmationStatusIgnoreCaseAndJockeyIsNotNullOrderByRegisteredAtDesc(
            String status,
            String ownerConfirmationStatus
    );

    long countByRaces_Id(Integer raceId);

    long countByRaces_IdAndStatusIgnoreCase(Integer raceId, String status);

    List<RaceRegistrations> findByRaces_IdAndStatusNotInOrderByGateNumberAsc(Integer raceId, List<String> statuses);

    boolean existsByRaces_IdAndGateNumberAndStatusNotIn(Integer raceId, Integer gateNumber, List<String> statuses);

    boolean existsByRaces_IdAndGateNumberAndStatusNotInAndIdNot(
            Integer raceId,
            Integer gateNumber,
            List<String> statuses,
            Integer registrationId
    );

    boolean existsByRaces_IdAndHorses_Id(Integer raceId, Integer horseId);

    boolean existsByRaces_IdAndHorses_IdAndIdNot(
            Integer raceId,
            Integer horseId,
            Integer registrationId
    );

    boolean existsByHorses_IdAndRaces_ScheduledAtAndStatusNotIn(
            Integer horseId,
            Instant scheduledAt,
            List<String> statuses
    );

    boolean existsByHorses_IdAndRaces_ScheduledAtAndStatusNotInAndIdNot(
            Integer horseId,
            Instant scheduledAt,
            List<String> statuses,
            Integer registrationId
    );
}

