package com.group5.htms.repository;

import com.group5.htms.entity.Races;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface RacesRepository extends JpaRepository<Races, Integer> {
    List<Races> findBySchedule_Tournaments_IdOrderByScheduledAtAsc(Integer tournamentId);

    List<Races> findBySchedule_Tournaments_IdAndStatusIgnoreCaseOrderByScheduledAtAsc(Integer tournamentId, String status);

    List<Races> findByScheduledAtAfterOrderByScheduledAtAsc(Instant scheduledAt, Pageable pageable);

    long countByScheduledAtAfter(Instant scheduledAt);

    boolean existsByScheduleIdAndRaceNumber(Integer scheduleId, Integer raceNumber);

    boolean existsByScheduleIdAndRaceNumberAndIdNot(
            Integer scheduleId,
            Integer raceNumber,
            Integer raceId
    );

    long countByScheduleId(Integer scheduleId);

    long countByStatusIgnoreCase(String status);
}
