package com.group5.htms.service.impl;

import com.group5.htms.dto.refereeassignment.request.RefereeAssignmentCreateRequest;
import com.group5.htms.dto.refereeassignment.response.RefereeAssignmentResponse;
import com.group5.htms.entity.RaceRefereeAssignments;
import com.group5.htms.entity.Races;
import com.group5.htms.entity.RefereeProfiles;
import com.group5.htms.entity.Users;
import com.group5.htms.enums.RoleType;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.mapper.RefereeAssignmentMapper;
import com.group5.htms.repository.RaceRefereeAssignmentsRepository;
import com.group5.htms.repository.RacesRepository;
import com.group5.htms.repository.RefereeProfilesRepository;
import com.group5.htms.service.RefereeAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RefereeAssignmentServiceImpl implements RefereeAssignmentService {

    private static final int DEFAULT_MAX_REFEREES = 3;
    private static final String STATUS_ACTIVE = "active";
    private static final String STATUS_SCHEDULED = "scheduled";
    private static final String STATUS_UPCOMING = "upcoming";
    private static final String ROLE_CHIEF_REFEREE = "chief_referee";
    private static final String ROLE_MAIN_REFEREE = "main_referee";

    private static final Set<String> ALLOWED_REFEREE_ROLES = Set.of(
            ROLE_CHIEF_REFEREE,
            ROLE_MAIN_REFEREE,
            "finish_judge",
            "track_judge",
            "weight_judge"
    );

    private final RacesRepository racesRepository;
    private final RefereeProfilesRepository refereeProfilesRepository;
    private final RaceRefereeAssignmentsRepository raceRefereeAssignmentsRepository;
    private final RefereeAssignmentMapper refereeAssignmentMapper;

    @Override
    @Transactional
    public RefereeAssignmentResponse assignRefereeToRace(
            Integer raceId,
            RefereeAssignmentCreateRequest request
    ) {
        if (raceId == null) {
            throw new BadRequestException("Race id is required");
        }

        if (request == null) {
            throw new BadRequestException("Request body is required");
        }

        Races race = racesRepository.findById(raceId)
                .orElseThrow(() -> new BadRequestException("Race not found"));

        RefereeProfiles referee = refereeProfilesRepository
                .findById(request.getRefereeId())
                .orElseThrow(() -> new BadRequestException("Referee not found"));

        validateRaceCanAssignReferee(race);
        validateReferee(referee);
        validateDuplicateReferee(raceId, referee.getId());
        validateMaxReferees(race);

        String refereeRole = normalizeRefereeRole(request.getRefereeRole());
        validateChiefReferee(raceId, refereeRole);

        request.setRaceId(raceId);

        RaceRefereeAssignments assignment = refereeAssignmentMapper.toEntity(request);
        assignment.setRaces(race);
        assignment.setReferee(referee);
        assignment.setRefereeRole(refereeRole);
        assignment.setAssignedAt(Instant.now());

        RaceRefereeAssignments savedAssignment =
                raceRefereeAssignmentsRepository.save(assignment);

        return refereeAssignmentMapper.toResponse(savedAssignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefereeAssignmentResponse> getRefereesByRace(Integer raceId) {
        if (raceId == null) {
            throw new BadRequestException("Race id is required");
        }

        if (!racesRepository.existsById(raceId)) {
            throw new BadRequestException("Race not found");
        }

        return raceRefereeAssignmentsRepository
                .findByRaces_IdOrderByIdAsc(raceId)
                .stream()
                .map(refereeAssignmentMapper::toResponse)
                .toList();
    }

    private void validateRaceCanAssignReferee(Races race) {
        String status = race.getStatus();

        if (status == null) {
            throw new BadRequestException("Only scheduled or upcoming races can have referees assigned");
        }

        String normalizedStatus = status.trim().toLowerCase();

        if (!STATUS_SCHEDULED.equals(normalizedStatus)
                && !STATUS_UPCOMING.equals(normalizedStatus)) {
            throw new BadRequestException("Only scheduled or upcoming races can have referees assigned");
        }
    }

    private void validateReferee(RefereeProfiles referee) {
        if (referee.getStatus() == null
                || !STATUS_ACTIVE.equalsIgnoreCase(referee.getStatus().trim())) {
            throw new BadRequestException("Referee profile is not active");
        }

        Users user = referee.getUsers();

        if (user == null) {
            throw new BadRequestException("Referee not found");
        }

        if (user.getStatus() == null
                || !STATUS_ACTIVE.equalsIgnoreCase(user.getStatus().trim())) {
            throw new BadRequestException("Referee user account is not active");
        }

        if (user.getRoleType() == null
                || !RoleType.RACE_REFEREE.getValue().equalsIgnoreCase(user.getRoleType().trim())) {
            throw new BadRequestException("Referee not found");
        }
    }

    private void validateMaxReferees(Races race) {
        Integer maxRefereesValue = race.getMaxReferees();
        int maxReferees = maxRefereesValue == null
                ? DEFAULT_MAX_REFEREES
                : maxRefereesValue;

        if (maxReferees <= 0) {
            throw new BadRequestException("Maximum number of referees reached for this race");
        }

        long currentCount = raceRefereeAssignmentsRepository.countByRaces_Id(race.getId());

        if (currentCount >= maxReferees) {
            throw new BadRequestException("Maximum number of referees reached for this race");
        }
    }

    private void validateDuplicateReferee(Integer raceId, Integer refereeId) {
        boolean exists = raceRefereeAssignmentsRepository
                .existsByRaces_IdAndReferee_Id(raceId, refereeId);

        if (exists) {
            throw new BadRequestException("Referee is already assigned to this race");
        }
    }

    private String normalizeRefereeRole(String refereeRole) {
        if (refereeRole == null || refereeRole.isBlank()) {
            throw new BadRequestException("Referee role is required");
        }

        String normalizedRole = refereeRole.trim().toLowerCase();

        if (!ALLOWED_REFEREE_ROLES.contains(normalizedRole)) {
            throw new BadRequestException("Invalid referee role");
        }

        return normalizedRole;
    }

    private void validateChiefReferee(Integer raceId, String refereeRole) {
        if (!isChiefRefereeRole(refereeRole)) {
            return;
        }

        boolean chiefExists = raceRefereeAssignmentsRepository
                .existsByRaces_IdAndRefereeRoleIgnoreCase(raceId, ROLE_CHIEF_REFEREE)
                || raceRefereeAssignmentsRepository
                .existsByRaces_IdAndRefereeRoleIgnoreCase(raceId, ROLE_MAIN_REFEREE);

        if (chiefExists) {
            throw new BadRequestException("Chief referee already exists for this race");
        }
    }

    private boolean isChiefRefereeRole(String refereeRole) {
        return ROLE_CHIEF_REFEREE.equalsIgnoreCase(refereeRole)
                || ROLE_MAIN_REFEREE.equalsIgnoreCase(refereeRole);
    }
}
