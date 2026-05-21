package com.group5.htms.repository;

import com.group5.htms.entity.RaceRegistrations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RaceRegistrationsRepository extends JpaRepository<RaceRegistrations, Integer> {
}