package com.group5.htms.repository;

import com.group5.htms.entity.PrizeAwards;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrizeAwardsRepository extends JpaRepository<PrizeAwards, Integer> {

    List<PrizeAwards> findByTournaments_IdOrderByFinishPositionAsc(Integer tournamentId);

    boolean existsByTournaments_Id(Integer tournamentId);

    Optional<PrizeAwards> findByTournaments_IdAndFinishPosition(Integer tournamentId, Integer finishPosition);
}
