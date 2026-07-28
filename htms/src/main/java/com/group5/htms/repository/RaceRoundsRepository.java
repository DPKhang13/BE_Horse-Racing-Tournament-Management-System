package com.group5.htms.repository;

import com.group5.htms.entity.RaceRounds;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RaceRoundsRepository extends JpaRepository<RaceRounds, Integer> {
    List<RaceRounds> findByRaces_IdOrderByRoundNumberAscPositionAsc(Integer raceId);

    List<RaceRounds> findByRaces_IdOrderByAssignment_IdAscRoundNumberAsc(Integer raceId);

    List<RaceRounds> findByAssignment_IdOrderByRoundNumberAsc(Integer assignmentId);

    boolean existsByRaces_IdAndAssignment_IdAndRoundNumber(Integer raceId, Integer assignmentId, Integer roundNumber);

    boolean existsByRaces_IdAndAssignment_IdAndRoundNumberAndIdNot(
            Integer raceId,
            Integer assignmentId,
            Integer roundNumber,
            Integer id
    );

    boolean existsByRaces_IdAndRoundNumberAndPosition(Integer raceId, Integer roundNumber, Integer position);

    boolean existsByRaces_IdAndRoundNumberAndPositionAndIdNot(
            Integer raceId,
            Integer roundNumber,
            Integer position,
            Integer id
    );
}
