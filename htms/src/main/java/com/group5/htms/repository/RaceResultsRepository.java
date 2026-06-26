package com.group5.htms.repository;

import com.group5.htms.entity.RaceResults;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RaceResultsRepository extends JpaRepository<RaceResults, Integer> {
    long countByAssignment_Jockey_IdAndFinishPosition(Integer jockeyId, Integer finishPosition);

    Optional<RaceResults> findByRaces_IdAndAssignment_Id(Integer raceId, Integer assignmentId);
}
