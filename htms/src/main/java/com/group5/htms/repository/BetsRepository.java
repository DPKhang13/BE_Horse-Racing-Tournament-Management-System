package com.group5.htms.repository;

import com.group5.htms.entity.Bets;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BetsRepository extends JpaRepository<Bets, Integer> {
}