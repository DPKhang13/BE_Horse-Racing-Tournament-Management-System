package com.group5.htms.repository;

import com.group5.htms.entity.BetOptions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BetOptionsRepository extends JpaRepository<BetOptions, Integer> {
}
