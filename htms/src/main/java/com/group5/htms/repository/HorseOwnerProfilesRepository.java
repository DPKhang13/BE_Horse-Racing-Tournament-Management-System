package com.group5.htms.repository;

import com.group5.htms.entity.HorseOwnerProfiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HorseOwnerProfilesRepository extends JpaRepository<HorseOwnerProfiles, Integer> {
}
