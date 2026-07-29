package com.group5.htms.repository;

import com.group5.htms.entity.RaceRegistrations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface RaceRegistrationsRepository extends JpaRepository<RaceRegistrations, Integer> {
    List<RaceRegistrations> findByHorses_Id(Integer horseId);

    List<RaceRegistrations> findByTournaments_Id(Integer tournamentId);

    List<RaceRegistrations> findByRaces_Id(Integer raceId);

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

    @Query("""
            select count(registration) > 0
            from RaceRegistrations registration
            where registration.tournaments.id = :tournamentId
              and registration.horses.id = :horseId
              and registration.races.scheduledAt = :scheduledAt
              and registration.races.id <> :raceId
              and lower(registration.status) not in :releasedStatuses
              and lower(registration.races.status) not in :terminalRaceStatuses
            """)
    boolean existsHorseScheduleConflictInTournament(
            @Param("tournamentId") Integer tournamentId,
            @Param("horseId") Integer horseId,
            @Param("scheduledAt") Instant scheduledAt,
            @Param("raceId") Integer raceId,
            @Param("releasedStatuses") List<String> releasedStatuses,
            @Param("terminalRaceStatuses") List<String> terminalRaceStatuses
    );

    @Query("""
            select count(registration) > 0
            from RaceRegistrations registration
            where registration.tournaments.id = :tournamentId
              and registration.horses.id = :horseId
              and registration.races.scheduledAt = :scheduledAt
              and registration.races.id <> :raceId
              and registration.id <> :registrationId
              and lower(registration.status) not in :releasedStatuses
              and lower(registration.races.status) not in :terminalRaceStatuses
            """)
    boolean existsHorseScheduleConflictInTournamentForUpdate(
            @Param("tournamentId") Integer tournamentId,
            @Param("horseId") Integer horseId,
            @Param("scheduledAt") Instant scheduledAt,
            @Param("raceId") Integer raceId,
            @Param("registrationId") Integer registrationId,
            @Param("releasedStatuses") List<String> releasedStatuses,
            @Param("terminalRaceStatuses") List<String> terminalRaceStatuses
    );
}

