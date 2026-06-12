package com.group5.htms.repository;

import com.group5.htms.entity.Horses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HorsesRepository extends JpaRepository<Horses, Integer> {
    List<Horses> findByStatusIgnoreCaseOrderByRankingPointsDescTotalWinsDescNameAsc(String status);
}
