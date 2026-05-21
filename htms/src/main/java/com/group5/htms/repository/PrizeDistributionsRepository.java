package com.group5.htms.repository;

import com.group5.htms.entity.PrizeDistributions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrizeDistributionsRepository extends JpaRepository<PrizeDistributions, Integer> {
}