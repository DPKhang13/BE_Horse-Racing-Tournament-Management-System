package com.group5.htms.service.impl;

import com.group5.htms.dto.raceregistration.request.RaceRegistrationApprovalRequest;
import com.group5.htms.dto.raceregistration.request.RaceRegistrationCreateRequest;
import com.group5.htms.dto.raceregistration.request.RaceRegistrationUpdateRequest;
import com.group5.htms.dto.raceregistration.response.RaceRegistrationListResponse;
import com.group5.htms.dto.raceregistration.response.RaceRegistrationResponse;
import com.group5.htms.entity.RaceRegistrations;
import com.group5.htms.entity.Users;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.exception.ResourceNotFoundException;
import com.group5.htms.mapper.RaceRegistrationMapper;
import com.group5.htms.repository.HorseOwnerProfilesRepository;
import com.group5.htms.repository.HorsesRepository;
import com.group5.htms.repository.JockeyProfilesRepository;
import com.group5.htms.repository.RaceRegistrationsRepository;
import com.group5.htms.repository.RacesRepository;
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
    private static final String STATUS_DELETED = "deleted";

    private final RaceRegistrationsRepository raceRegistrationsRepository;
    private final TournamentsRepository tournamentsRepository;
    private final RacesRepository racesRepository;
    private final HorsesRepository horsesRepository;
    private final HorseOwnerProfilesRepository horseOwnerProfilesRepository;
    private final JockeyProfilesRepository jockeyProfilesRepository;
    private final AuthService authService;
    private final RaceRegistrationMapper raceRegistrationMapper;

    @Override
    public List<RaceRegistrationListResponse> getAllRegistrations() {
        return raceRegistrationsRepository.findAll()
                .stream()
                .filter(registration -> !isDeleted(registration.getStatus()))
                .map(raceRegistrationMapper::toListResponse)
                .toList();
    }

    @Override
    public List<RaceRegistrationListResponse> getMyRegistrations(String status) {
        Integer ownerId = authService.getCurrentUserId();

        return findRegistrationsByOwner(ownerId, status)
                .stream()
                .filter(registration -> !isDeleted(registration.getStatus()))
                .map(raceRegistrationMapper::toListResponse)
                .toList();
    }

    @Override
    public RaceRegistrationResponse getRegistrationById(Integer id) {
        return raceRegistrationMapper.toResponse(findRegistration(id));
    }

    @Override
    @Transactional
    public RaceRegistrationResponse createRegistration(RaceRegistrationCreateRequest request) {
        Integer ownerId = authService.getCurrentUserId();
        request.setOwnerId(ownerId);
        request.setStatus(null);
        request.setApprovedAt(null);
        request.setApprovedById(null);
        validateCreateReferences(request);
        validateRaceBelongsToTournament(request.getRaceId(), request.getTournamentId());
        validateHorseBelongsToOwner(request.getHorseId(), ownerId);
        RaceRegistrations registration = raceRegistrationMapper.toEntity(request);

        return raceRegistrationMapper.toResponse(raceRegistrationsRepository.save(registration));
    }

    @Override
    @Transactional
    public RaceRegistrationResponse updateRegistration(Integer id, RaceRegistrationUpdateRequest request) {
        Integer ownerId = authService.getCurrentUserId();
        RaceRegistrations registration = findRegistrationForCurrentOwner(id, ownerId);
        request.setOwnerId(null);
        request.setStatus(null);
        request.setApprovedAt(null);
        request.setApprovedById(null);
        validateUpdateReferences(request);
        Integer tournamentId = request.getTournamentId() == null
                ? registration.getTournaments().getId()
                : request.getTournamentId();
        Integer raceId = request.getRaceId() == null
                ? registration.getRaces().getId()
                : request.getRaceId();
        validateRaceBelongsToTournament(raceId, tournamentId);
        if (request.getHorseId() != null) {
            validateHorseBelongsToOwner(request.getHorseId(), ownerId);
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
                authService.getCurrentUserId()
        );
        registration.setStatus(STATUS_DELETED);
        raceRegistrationsRepository.save(registration);
    }

    private RaceRegistrations findRegistration(Integer id) {
        return raceRegistrationsRepository.findById(id)
                .filter(registration -> !isDeleted(registration.getStatus()))
                .orElseThrow(() -> new ResourceNotFoundException("Race registration not found"));
    }

    private RaceRegistrations findRegistrationForCurrentOwner(Integer id, Integer ownerId) {
        RaceRegistrations registration = findRegistration(id);

        if (!Objects.equals(registration.getOwner().getId(), ownerId)) {
            throw new AccessDeniedException("You do not own this race registration");
        }

        return registration;
    }

    private void validateCreateReferences(RaceRegistrationCreateRequest request) {
        validateTournamentExists(request.getTournamentId());
        validateRaceExists(request.getRaceId());
        validateHorseExists(request.getHorseId());
        validateOwnerExists(request.getOwnerId());
        if (request.getJockeyId() != null) {
            validateJockeyExists(request.getJockeyId());
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
        if (request.getOwnerId() != null) {
            validateOwnerExists(request.getOwnerId());
        }
        if (request.getJockeyId() != null) {
            validateJockeyExists(request.getJockeyId());
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

    private void validateRaceBelongsToTournament(Integer raceId, Integer tournamentId) {
        boolean belongsToTournament = racesRepository.findById(raceId)
                .map(race -> race.getSchedule() != null
                        && race.getSchedule().getTournaments() != null
                        && Objects.equals(race.getSchedule().getTournaments().getId(), tournamentId))
                .orElse(false);

        if (!belongsToTournament) {
            throw new BadRequestException("Race does not belong to this tournament");
        }
    }

    private void validateHorseExists(Integer id) {
        if (!horsesRepository.existsById(id)) {
            throw new ResourceNotFoundException("Horse not found");
        }
    }

    private void validateHorseBelongsToOwner(Integer horseId, Integer ownerId) {
        boolean belongsToOwner = horsesRepository.findById(horseId)
                .map(horse -> Objects.equals(horse.getOwner().getId(), ownerId))
                .orElse(false);

        if (!belongsToOwner) {
            throw new AccessDeniedException("You do not own this horse");
        }
    }

    private void validateOwnerExists(Integer id) {
        if (!horseOwnerProfilesRepository.existsById(id)) {
            throw new ResourceNotFoundException("Horse owner profile not found");
        }
    }

    private void validateJockeyExists(Integer id) {
        if (!jockeyProfilesRepository.existsById(id)) {
            throw new ResourceNotFoundException("Jockey profile not found");
        }
    }

    private boolean isDeleted(String status) {
        return STATUS_DELETED.equalsIgnoreCase(status);
    }

    private List<RaceRegistrations> findRegistrationsByOwner(Integer ownerId, String status) {
        if (status != null && !status.isBlank()) {
            return raceRegistrationsRepository
                    .findByOwner_IdAndStatusIgnoreCaseOrderByRegisteredAtDesc(ownerId, status.trim());
        }

        return raceRegistrationsRepository.findByOwner_IdOrderByRegisteredAtDesc(ownerId);
    }

}
