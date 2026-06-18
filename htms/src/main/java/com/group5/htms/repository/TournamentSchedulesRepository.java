package com.group5.htms.repository;

import com.group5.htms.entity.TournamentSchedules;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TournamentSchedulesRepository extends JpaRepository<TournamentSchedules, Integer> {

    List<TournamentSchedules> findByTournamentsIdOrderByRaceDateAscDayNumberAsc(Integer tournamentId);

    boolean existsByTournamentsIdAndDayNumber(Integer tournamentId, Integer dayNumber);

    boolean existsByTournamentsIdAndRaceDate(Integer tournamentId, java.time.LocalDate raceDate);

    boolean existsByTournamentsIdAndDayNumberAndIdNot(
            Integer tournamentId,
            Integer dayNumber,
            Integer scheduleId
    );

    boolean existsByTournamentsIdAndRaceDateAndIdNot(
            Integer tournamentId,
            java.time.LocalDate raceDate,
            Integer scheduleId
    );
}
