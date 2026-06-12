package com.group5.htms.repository;

import com.group5.htms.entity.RaceResults;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RaceResultsRepository extends JpaRepository<RaceResults, Integer> {
}
