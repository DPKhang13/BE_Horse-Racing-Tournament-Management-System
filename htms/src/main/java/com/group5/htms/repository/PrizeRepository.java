package com.group5.htms.repository;

import com.group5.htms.entity.PrizeDistributions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrizeRepository extends JpaRepository<PrizeDistributions, Integer> {

    List<PrizeDistributions> findByTournamentsIdOrderByFinishPositionAsc(Integer tournamentId);

    boolean existsByTournamentsIdAndFinishPosition(Integer tournamentId, Integer finishPosition);
}