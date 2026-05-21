package com.group5.htms.repository;

import com.group5.htms.entity.RefereeReports;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefereeReportsRepository extends JpaRepository<RefereeReports, Integer> {
}