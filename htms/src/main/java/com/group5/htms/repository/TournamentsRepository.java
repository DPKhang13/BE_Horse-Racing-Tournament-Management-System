package com.group5.htms.repository;

import com.group5.htms.entity.Tournaments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TournamentsRepository extends JpaRepository<Tournaments, Integer> {
}