package com.group5.htms.repository;

import com.group5.htms.entity.JockeyProfiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JockeyProfilesRepository extends JpaRepository<JockeyProfiles, Integer> {
}