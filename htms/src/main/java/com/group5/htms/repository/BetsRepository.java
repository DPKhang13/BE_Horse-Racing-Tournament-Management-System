package com.group5.htms.repository;

import com.group5.htms.entity.Bets;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BetsRepository extends JpaRepository<Bets, Integer> {
    List<Bets> findByOption_Races_IdAndStatusIgnoreCase(Integer raceId, String status);
    List<Bets> findByUsers_IdAndStatusIgnoreCaseOrderByPlacedAtDesc(Integer userId, String status, Pageable pageable);

    long countByUsers_IdAndStatusIgnoreCase(Integer userId, String status);
}
