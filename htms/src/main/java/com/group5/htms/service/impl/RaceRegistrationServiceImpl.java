package com.group5.htms.service.impl;

import com.group5.htms.exceptions.ResourceNotFoundException;
import com.group5.htms.dto.raceregistration.request.RaceRegistrationApprovalRequest;
import com.group5.htms.dto.raceregistration.request.RaceRegistrationCreateRequest;
import com.group5.htms.dto.raceregistration.request.RaceRegistrationUpdateRequest;
import com.group5.htms.dto.raceregistration.response.RaceRegistrationResponse;
import com.group5.htms.entity.RaceRegistrations;
import com.group5.htms.entity.Users;
import com.group5.htms.mapper.RaceRegistrationMapper;
import com.group5.htms.repository.HorsesRepository;
import com.group5.htms.repository.RaceRegistrationsRepository;
import com.group5.htms.repository.RacesRepository;
import com.group5.htms.repository.RolesRepository;
import com.group5.htms.repository.TournamentsRepository;
import com.group5.htms.service.AuthService;
import com.group5.htms.service.RaceRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RaceRegistrationServiceImpl implements RaceRegistrationService {
    private static final String ROLE_HORSE_OWNER = "horse_owner";

    private final RaceRegistrationsRepository raceRegistrationsRepository;
    private final TournamentsRepository tournamentsRepository;
    private final RacesRepository racesRepository;
    private final HorsesRepository horsesRepository;
    private final RolesRepository rolesRepository;
    private final AuthService authService;
    private final RaceRegistrationMapper raceRegistrationMapper;

    @Override
    public List<RaceRegistrationResponse> getAllRegistrations() {
        return raceRegistrationsRepository.findAll()
                .stream()
                .map(raceRegistrationMapper::toResponse)
                .toList();
    }

    @Override
    public RaceRegistrationResponse getRegistrationById(Integer id) {
        return raceRegistrationMapper.toResponse(findRegistration(id));
    }

    @Override
    @Transactional
    public RaceRegistrationResponse createRegistration(RaceRegistrationCreateRequest request) {
        Integer ownerRoleId = authService.getCurrentUserRoleId(ROLE_HORSE_OWNER);
        request.setOwnerRoleId(ownerRoleId);
        request.setStatus(null);
        request.setApprovedAt(null);
        request.setApprovedById(null);
        validateCreateReferences(request);
        validateHorseBelongsToOwner(request.getHorseId(), ownerRoleId);
        RaceRegistrations registration = raceRegistrationMapper.toEntity(request);

        return raceRegistrationMapper.toResponse(raceRegistrationsRepository.save(registration));
    }

    @Override
    @Transactional
    public RaceRegistrationResponse updateRegistration(Integer id, RaceRegistrationUpdateRequest request) {
        Integer ownerRoleId = authService.getCurrentUserRoleId(ROLE_HORSE_OWNER);
        RaceRegistrations registration = findRegistrationForCurrentOwner(id, ownerRoleId);
        request.setOwnerRoleId(null);
        request.setStatus(null);
        request.setApprovedAt(null);
        request.setApprovedById(null);
        validateUpdateReferences(request);
        if (request.getHorseId() != null) {
            validateHorseBelongsToOwner(request.getHorseId(), ownerRoleId);
        }
        raceRegistrationMapper.updateRegistration(registration, request);

        return raceRegistrationMapper.toResponse(raceRegistrationsRepository.save(registration));
    }

    @Override
    @Transactional
    public RaceRegistrationResponse approveRegistration(Integer id, RaceRegistrationApprovalRequest request) {
        RaceRegistrations registration = findRegistration(id);

        registration.setStatus(request.getStatus().trim());
        registration.setApprovedAt(request.getApprovedAt() == null ? Instant.now() : request.getApprovedAt());
        Users approvedBy = new Users();
        approvedBy.setId(authService.getCurrentUserId());
        registration.setApprovedBy(approvedBy);

        return raceRegistrationMapper.toResponse(raceRegistrationsRepository.save(registration));
    }

    @Override
    @Transactional
    public void deleteRegistration(Integer id) {
        RaceRegistrations registration = findRegistrationForCurrentOwner(
                id,
                authService.getCurrentUserRoleId(ROLE_HORSE_OWNER)
        );
        raceRegistrationsRepository.delete(registration);
    }

    private RaceRegistrations findRegistration(Integer id) {
        return raceRegistrationsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Race registration not found"));
    }

    private RaceRegistrations findRegistrationForCurrentOwner(Integer id, Integer ownerRoleId) {
        RaceRegistrations registration = findRegistration(id);

        if (!Objects.equals(registration.getOwnerRoles().getId(), ownerRoleId)) {
            throw new AccessDeniedException("You do not own this race registration");
        }

        return registration;
    }

    private void validateCreateReferences(RaceRegistrationCreateRequest request) {
        validateTournamentExists(request.getTournamentId());
        validateRaceExists(request.getRaceId());
        validateHorseExists(request.getHorseId());
        validateRoleExists(request.getOwnerRoleId(), "Owner role not found");
        if (request.getJockeyRoleId() != null) {
            validateRoleExists(request.getJockeyRoleId(), "Jockey role not found");
        }
    }

    private void validateUpdateReferences(RaceRegistrationUpdateRequest request) {
        if (request.getTournamentId() != null) {
            validateTournamentExists(request.getTournamentId());
        }
        if (request.getRaceId() != null) {
            validateRaceExists(request.getRaceId());
        }
        if (request.getHorseId() != null) {
            validateHorseExists(request.getHorseId());
        }
        if (request.getOwnerRoleId() != null) {
            validateRoleExists(request.getOwnerRoleId(), "Owner role not found");
        }
        if (request.getJockeyRoleId() != null) {
            validateRoleExists(request.getJockeyRoleId(), "Jockey role not found");
        }
    }

    private void validateTournamentExists(Integer id) {
        if (!tournamentsRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tournament not found");
        }
    }

    private void validateRaceExists(Integer id) {
        if (!racesRepository.existsById(id)) {
            throw new ResourceNotFoundException("Race not found");
        }
    }

    private void validateHorseExists(Integer id) {
        if (!horsesRepository.existsById(id)) {
            throw new ResourceNotFoundException("Horse not found");
        }
    }

    private void validateHorseBelongsToOwner(Integer horseId, Integer ownerRoleId) {
        boolean belongsToOwner = horsesRepository.findById(horseId)
                .map(horse -> Objects.equals(horse.getOwnerRoles().getId(), ownerRoleId))
                .orElse(false);

        if (!belongsToOwner) {
            throw new AccessDeniedException("You do not own this horse");
        }
    }

    private void validateRoleExists(Integer id, String message) {
        if (!rolesRepository.existsById(id)) {
            throw new ResourceNotFoundException(message);
        }
    }

}
