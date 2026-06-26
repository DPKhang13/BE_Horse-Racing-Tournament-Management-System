package com.group5.htms.repository;

import com.group5.htms.entity.RacePointRules;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RacePointRulesRepository extends JpaRepository<RacePointRules, Integer> {
    List<RacePointRules> findByRace_IdOrderByFinishPositionAsc(Integer raceId);

    boolean existsByRace_IdAndFinishPosition(Integer raceId, Integer finishPosition);

    boolean existsByRace_IdAndFinishPositionAndIdNot(Integer raceId, Integer finishPosition, Integer id);

    void deleteByRace_Id(Integer raceId);
}
