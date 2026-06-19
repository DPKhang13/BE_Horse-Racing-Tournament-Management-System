package com.group5.htms.repository;

import com.group5.htms.entity.BetOptions;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {
            "races",
            "assignment",
            "assignment.jockey",
            "assignment.jockey.users",
            "horses"
    })
    Optional<BetOptions> findFirstById(Integer id);

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

    @EntityGraph(attributePaths = {
            "races",
            "assignment",
            "assignment.jockey",
            "assignment.jockey.users",
            "horses"
    })
    Optional<BetOptions> findByRaces_IdAndHorses_Id(Integer raceId, Integer horseId);

    boolean existsByRaces_IdAndHorses_Id(Integer raceId, Integer horseId);
}
