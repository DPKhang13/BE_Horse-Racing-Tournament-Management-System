package com.group5.htms.repository;

import com.group5.htms.entity.RefereeReports;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefereeReportsRepository extends JpaRepository<RefereeReports, Integer> {
    boolean existsByRaces_IdAndReferee_IdAndReportTypeIgnoreCase(Integer raceId, Integer refereeId, String reportType);

    List<RefereeReports> findByRaces_IdOrderBySubmittedAtDesc(Integer raceId);
}
