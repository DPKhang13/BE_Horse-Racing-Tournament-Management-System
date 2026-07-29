package com.group5.htms.repository;

import com.group5.htms.entity.HorseOwnerProfiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HorseOwnerProfilesRepository extends JpaRepository<HorseOwnerProfiles, Integer> {
    @Override
    Optional<HorseOwnerProfiles> findById(Integer id);
}
