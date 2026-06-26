package com.group5.htms.service.impl;

import com.group5.htms.dto.raceregistration.request.RaceRegistrationApprovalRequest;
import com.group5.htms.dto.raceregistration.request.RaceRegistrationApproveRequest;
import com.group5.htms.dto.raceregistration.request.RaceRegistrationCreateRequest;
import com.group5.htms.dto.raceregistration.request.RaceRegistrationRejectRequest;
import com.group5.htms.dto.raceregistration.request.RaceRegistrationUpdateRequest;
import com.group5.htms.dto.raceregistration.response.RaceRegistrationListResponse;
import com.group5.htms.dto.raceregistration.response.RaceRegistrationResponse;
import com.group5.htms.entity.HorseOwnerProfiles;
import com.group5.htms.entity.Horses;
import com.group5.htms.entity.RaceRegistrations;
import com.group5.htms.entity.Races;
import com.group5.htms.entity.Tournaments;
import com.group5.htms.entity.Users;
import com.group5.htms.enums.RaceRegistrationStatus;
import com.group5.htms.exception.ResourceNotFoundException;
import com.group5.htms.mapper.RaceRegistrationMapper;
import com.group5.htms.repository.HorseOwnerProfilesRepository;
import com.group5.htms.repository.HorsesRepository;
import com.group5.htms.repository.RaceRegistrationsRepository;
import com.group5.htms.repository.RacesRepository;
import com.group5.htms.repository.TournamentsRepository;
import com.group5.htms.service.AuthService;
import com.group5.htms.service.RaceRegistrationService;
import com.group5.htms.validation.RaceRegistrationValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RaceRegistrationServiceImpl implements RaceRegistrationService {

    private final RaceRegistrationsRepository raceRegistrationsRepository;
    private final TournamentsRepository tournamentsRepository;
    private final RacesRepository racesRepository;
    private final HorsesRepository horsesRepository;
    private final HorseOwnerProfilesRepository horseOwnerProfilesRepository;
    private final AuthService authService;
    private final RaceRegistrationMapper raceRegistrationMapper;
    private final RaceRegistrationValidator raceRegistrationValidator;

    @Override
    @Transactional(readOnly = true)
    public List<RaceRegistrationListResponse> getAllRegistrations() {
        return raceRegistrationsRepository.findAll()
                .stream()
                .map(raceRegistrationMapper::toListResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RaceRegistrationListResponse> getMyRegistrations() {
        Integer ownerId = authService.getCurrentUserId();

        return raceRegistrationsRepository.findByOwner_IdOrderByRegisteredAtDesc(ownerId)
                .stream()
                .map(raceRegistrationMapper::toListResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RaceRegistrationResponse getRegistrationById(Integer id) {
        return raceRegistrationMapper.toResponse(findRegistration(id));
    }

    @Override
    @Transactional
    public RaceRegistrationResponse createRegistration(RaceRegistrationCreateRequest request) {
        Integer ownerId = authService.getCurrentUserId();
        request.setOwnerId(ownerId);

        Tournaments tournament = findTournament(request.getTournamentId());
        Races race = findRace(request.getRaceId());
        Horses horse = findHorse(request.getHorseId());
        HorseOwnerProfiles owner = findOwner(ownerId);

        raceRegistrationValidator.ensureRaceBelongsToTournament(race, tournament.getId());
        raceRegistrationValidator.ensureHorseBelongsToOwner(horse, ownerId);
        raceRegistrationValidator.ensureRegistrationOpen(tournament, race);
        raceRegistrationValidator.ensureHorseNotRegisteredInTournament(
                raceRegistrationsRepository.existsByTournaments_IdAndHorses_Id(tournament.getId(), horse.getId())
        );

        RaceRegistrations registration = raceRegistrationMapper.toEntity(request);
        registration.setTournaments(tournament);
        registration.setRaces(race);
        registration.setHorses(horse);
        registration.setOwner(owner);
        registration.setJockey(null);
        registration.setStatus(RaceRegistrationStatus.PENDING.getValue());
        registration.setOwnerConfirmationStatus(RaceRegistrationStatus.PENDING.getValue());
        registration.setRegisteredAt(Instant.now());

        return raceRegistrationMapper.toResponse(raceRegistrationsRepository.save(registration));
    }

    @Override
    @Transactional
    public RaceRegistrationResponse updateRegistration(Integer id, RaceRegistrationUpdateRequest request) {
        Integer ownerId = authService.getCurrentUserId();
        RaceRegistrations registration = findRegistrationForCurrentOwner(id, ownerId);

        raceRegistrationValidator.ensureNoWorkflowFields(request);

        Tournaments tournament = request.getTournamentId() == null
                ? registration.getTournaments()
                : findTournament(request.getTournamentId());
        Races race = request.getRaceId() == null
                ? registration.getRaces()
                : findRace(request.getRaceId());
        Horses horse = request.getHorseId() == null
                ? registration.getHorses()
                : findHorse(request.getHorseId());

        raceRegistrationValidator.ensureRaceBelongsToTournament(race, tournament.getId());
        raceRegistrationValidator.ensureHorseBelongsToOwner(horse, ownerId);
        raceRegistrationValidator.ensureHorseNotRegisteredInTournamentForUpdate(
                raceRegistrationsRepository.existsByTournaments_IdAndHorses_IdAndIdNot(
                        tournament.getId(),
                        horse.getId(),
                        registration.getId()
                )
        );

        registration.setTournaments(tournament);
        registration.setRaces(race);
        registration.setHorses(horse);

        return raceRegistrationMapper.toResponse(raceRegistrationsRepository.save(registration));
    }

    @Override
    @Transactional
    public RaceRegistrationResponse approveRegistration(Integer id, RaceRegistrationApprovalRequest request) {
        String status = request == null ? null : request.getStatus();
        raceRegistrationValidator.ensureApproveStatusRequested(status);

        return approveRegistration(id, new RaceRegistrationApproveRequest());
    }

    @Override
    @Transactional
    public RaceRegistrationResponse approveRegistration(Integer id, RaceRegistrationApproveRequest request) {
        RaceRegistrations registration = findRegistration(id);
        raceRegistrationValidator.ensureCanApprove(registration);

        registration.setStatus(RaceRegistrationStatus.APPROVED.getValue());
        registration.setApprovedAt(Instant.now());
        registration.setApprovedBy(currentUserReference());

        return raceRegistrationMapper.toResponse(raceRegistrationsRepository.save(registration));
    }

    @Override
    @Transactional
    public RaceRegistrationResponse rejectRegistration(Integer id, RaceRegistrationRejectRequest request) {
        RaceRegistrations registration = findRegistration(id);

        raceRegistrationValidator.ensureCanReject(registration);

        registration.setStatus(RaceRegistrationStatus.REJECTED.getValue());

        return raceRegistrationMapper.toResponse(raceRegistrationsRepository.save(registration));
    }


    private RaceRegistrations findRegistration(Integer id) {
        return raceRegistrationsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Race registration not found"));
    }

    private RaceRegistrations findRegistrationForCurrentOwner(Integer id, Integer ownerId) {
        RaceRegistrations registration = findRegistration(id);

        raceRegistrationValidator.ensureOwnerCanManageRegistration(registration, ownerId);

        return registration;
    }

    private Tournaments findTournament(Integer id) {
        return tournamentsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found"));
    }

    private Races findRace(Integer id) {
        return racesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Race not found"));
    }

    private Horses findHorse(Integer id) {
        return horsesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Horse not found"));
    }

    private HorseOwnerProfiles findOwner(Integer id) {
        return horseOwnerProfilesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Horse owner profile not found"));
    }

    private Users currentUserReference() {
        Users user = new Users();
        user.setId(authService.getCurrentUserId());
        return user;
    }

}