package com.group5.htms.repository;

import com.group5.htms.entity.JockeyProfiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JockeyProfilesRepository extends JpaRepository<JockeyProfiles, Integer> {
    List<JockeyProfiles> findByStatusIgnoreCaseOrderByRankingPointsDesc(String status);

    List<JockeyProfiles> findAllByOrderByRankingPointsDesc();

    List<JockeyProfiles> findByStatusIgnoreCaseOrderByRankingPointsDescTotalWinsDescExperienceYearsDesc(String status);
}
