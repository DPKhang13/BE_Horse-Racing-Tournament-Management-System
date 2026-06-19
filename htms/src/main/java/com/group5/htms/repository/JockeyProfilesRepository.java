package com.group5.htms.repository;

import com.group5.htms.entity.JockeyProfiles;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JockeyProfilesRepository extends JpaRepository<JockeyProfiles, Integer> {
    @Override
    @EntityGraph(attributePaths = "users")
    Optional<JockeyProfiles> findById(Integer id);

    List<JockeyProfiles> findByStatusIgnoreCaseOrderByRankingPointsDesc(String status);

    List<JockeyProfiles> findAllByOrderByRankingPointsDesc();

    List<JockeyProfiles> findAllByOrderByRankingPointsDescTotalWinsDescExperienceYearsDesc();

    List<JockeyProfiles> findByStatusIgnoreCaseOrderByRankingPointsDescTotalWinsDescExperienceYearsDesc(String status);
}
