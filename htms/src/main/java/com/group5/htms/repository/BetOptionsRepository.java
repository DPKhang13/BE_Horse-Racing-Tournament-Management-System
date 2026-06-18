package com.group5.htms.repository;

import com.group5.htms.entity.BetOptions;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BetOptionsRepository extends JpaRepository<BetOptions, Integer> {
    @Override
    @EntityGraph(attributePaths = {
            "races",
            "assignment",
            "assignment.jockey",
            "assignment.jockey.users",
            "horses"
    })
    Optional<BetOptions> findById(Integer id);

    @Override
    @EntityGraph(attributePaths = {
            "races",
            "assignment",
            "assignment.jockey",
            "assignment.jockey.users",
            "horses"
    })
    List<BetOptions> findAll();

    @EntityGraph(attributePaths = {
            "races",
            "assignment",
            "assignment.jockey",
            "assignment.jockey.users",
            "horses"
    })
    List<BetOptions> findByRaces_IdOrderByCurrentRateAsc(Integer raceId);
}
