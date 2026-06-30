package com.group5.htms.repository;

import com.group5.htms.entity.BetOptions;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface BetOptionsRepository extends JpaRepository<BetOptions, Integer> {
    @Override
    Optional<BetOptions> findById(Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<BetOptions> findFirstById(Integer id);

    @Override
    List<BetOptions> findAll();

    List<BetOptions> findByRaces_IdOrderByCurrentRateAsc(Integer raceId);

    List<BetOptions> findByRaces_StatusIgnoreCaseAndRaces_PredictionClosesAtAfterOrderByRaces_ScheduledAtAscCurrentRateAsc(
            String status,
            Instant predictionClosesAt,
            Pageable pageable
    );

    Optional<BetOptions> findByRaces_IdAndHorses_Id(Integer raceId, Integer horseId);

    boolean existsByRaces_IdAndHorses_Id(Integer raceId, Integer horseId);
}
