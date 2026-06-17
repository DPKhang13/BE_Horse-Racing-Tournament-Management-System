package com.group5.htms.repository;

import com.group5.htms.entity.Horses;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HorsesRepository extends JpaRepository<Horses, Integer> {
    @Override
    @EntityGraph(attributePaths = {"owner", "owner.users"})
    List<Horses> findAll();

    @Override
    @EntityGraph(attributePaths = {"owner", "owner.users"})
    Optional<Horses> findById(Integer id);

    @EntityGraph(attributePaths = {"owner", "owner.users"})
    List<Horses> findByStatusIgnoreCaseOrderByRankingPointsDescTotalWinsDescNameAsc(String status);
}
