package com.group5.htms.repository;

import com.group5.htms.entity.RefereeProfiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefereeProfilesRepository extends JpaRepository<RefereeProfiles, Integer> {
}
