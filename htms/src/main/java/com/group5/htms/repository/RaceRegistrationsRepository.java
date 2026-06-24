package com.group5.htms.repository;

import com.group5.htms.entity.RaceRegistrations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RaceRegistrationsRepository extends JpaRepository<RaceRegistrations, Integer> {
    List<RaceRegistrations> findByHorses_Id(Integer horseId);

    List<RaceRegistrations> findByTournaments_Id(Integer tournamentId);

    List<RaceRegistrations> findByOwner_IdOrderByRegisteredAtDesc(Integer ownerId);

    long countByRaces_Id(Integer raceId);

    long countByRaces_IdAndStatusIgnoreCase(Integer raceId, String status);

    boolean existsByTournaments_IdAndHorses_Id(Integer tournamentId, Integer horseId);

    boolean existsByTournaments_IdAndHorses_IdAndStatusNotIgnoreCase(
            Integer tournamentId,
            Integer horseId,
            String status
    );

    boolean existsByTournaments_IdAndHorses_IdAndIdNot(
            Integer tournamentId,
            Integer horseId,
            Integer registrationId
    );
}
