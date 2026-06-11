package com.group5.htms.repository;

import com.group5.htms.entity.JockeyHorseAssignments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JockeyHorseAssignmentsRepository extends JpaRepository<JockeyHorseAssignments, Integer> {
    List<JockeyHorseAssignments> findByReg_Id(Integer registrationId);

    List<JockeyHorseAssignments> findByReg_IdIn(Iterable<Integer> registrationIds);
}
