package com.group5.htms.repository;

import com.group5.htms.entity.RaceRegistrations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RaceRegistrationsRepository extends JpaRepository<RaceRegistrations, Integer> {
    List<RaceRegistrations> findByHorses_Id(Integer horseId);

    List<RaceRegistrations> findByOwner_IdOrderByRegisteredAtDesc(Integer ownerId);

    List<RaceRegistrations> findByOwner_IdAndStatusIgnoreCaseOrderByRegisteredAtDesc(Integer ownerId, String status);

    long countByRaces_Id(Integer raceId);

    boolean existsByTournaments_IdAndHorses_Id(Integer tournamentId, Integer horseId);

    boolean existsByTournaments_IdAndHorses_IdAndIdNot(
            Integer tournamentId,
            Integer horseId,
            Integer registrationId
    );
}
