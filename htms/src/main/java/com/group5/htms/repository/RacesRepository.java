package com.group5.htms.repository;

import com.group5.htms.entity.Races;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RacesRepository extends JpaRepository<Races, Integer> {
}