package com.group5.htms.repository;

import com.group5.htms.entity.JockeyHorseAssignments;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface JockeyHorseAssignmentsRepository extends JpaRepository<JockeyHorseAssignments, Integer> {
    List<JockeyHorseAssignments> findByReg_Id(Integer registrationId);

    List<JockeyHorseAssignments> findByReg_IdAndStatusIn(Integer registrationId, Collection<String> statuses);

    List<JockeyHorseAssignments> findByReg_IdIn(Iterable<Integer> registrationIds);

    List<JockeyHorseAssignments> findByJockey_IdOrderByInvitedAtDesc(Integer jockeyId);

    List<JockeyHorseAssignments> findByJockey_IdAndStatusIgnoreCaseOrderByInvitedAtDesc(Integer jockeyId, String status);

    List<JockeyHorseAssignments> findByReg_Owner_IdOrderByInvitedAtDesc(Integer ownerId);

    List<JockeyHorseAssignments> findByReg_Owner_IdAndStatusIgnoreCaseOrderByInvitedAtDesc(Integer ownerId, String status);

    @EntityGraph(attributePaths = {
            "races",
            "reg",
            "reg.horses",
            "jockey",
            "jockey.users"
    })
    List<JockeyHorseAssignments> findByRaces_IdAndStatusIgnoreCase(Integer raceId, String status);

    List<JockeyHorseAssignments> findByRaces_IdAndJockey_IdAndStatusIn(
            Integer raceId,
            Integer jockeyId,
            Collection<String> statuses
    );

    List<JockeyHorseAssignments> findByRaces_IdAndGateNumberAndStatusIn(
            Integer raceId,
            Integer gateNumber,
            Collection<String> statuses
    );

    long countByRaces_IdAndStatusIgnoreCase(Integer raceId, String status);

    long countByJockey_Id(Integer jockeyId);

    boolean existsByRaces_IdAndJockey_Id(Integer raceId, Integer jockeyId);

    boolean existsByRaces_IdAndJockey_IdAndStatusIn(
            Integer raceId,
            Integer jockeyId,
            Collection<String> statuses
    );

    boolean existsByRaces_IdAndGateNumber(Integer raceId, Integer gateNumber);

    boolean existsByRaces_IdAndGateNumberAndStatusIn(
            Integer raceId,
            Integer gateNumber,
            Collection<String> statuses
    );

    boolean existsByRaces_IdAndJockey_IdAndIdNot(
            Integer raceId,
            Integer jockeyId,
            Integer assignmentId
    );

    boolean existsByRaces_IdAndGateNumberAndIdNot(
            Integer raceId,
            Integer gateNumber,
            Integer assignmentId
    );
}
