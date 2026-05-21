package com.group5.htms.repository;

import com.group5.htms.entity.JockeyHorseAssignments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JockeyHorseAssignmentsRepository extends JpaRepository<JockeyHorseAssignments, Integer> {
}