package com.group5.htms.repository;

import com.group5.htms.entity.RaceRefereeAssignments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RaceRefereeAssignmentsRepository extends JpaRepository<RaceRefereeAssignments, Integer> {
}