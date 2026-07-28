package com.group5.htms.repository;

import com.group5.htms.entity.Horses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HorsesRepository extends JpaRepository<Horses, Integer> {
    @Override
    List<Horses> findAll();

    @Override
    Optional<Horses> findById(Integer id);

    List<Horses> findByStatusIgnoreCaseOrderByRankingPointsDescTotalWinsDescNameAsc(String status);

    long countByStatusNotIgnoreCase(String status);
}
