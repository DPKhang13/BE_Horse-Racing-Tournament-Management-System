package com.group5.htms.repository;

import com.group5.htms.entity.RaceRefereeAssignments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RaceRefereeAssignmentsRepository extends JpaRepository<RaceRefereeAssignments, Integer> {

    long countByRaces_Id(Integer raceId);

    boolean existsByRaces_IdAndReferee_Id(Integer raceId, Integer refereeId);

    boolean existsByRaces_IdAndRefereeRoleIgnoreCase(Integer raceId, String refereeRole);

    List<RaceRefereeAssignments> findByRaces_IdOrderByIdAsc(Integer raceId);
}
