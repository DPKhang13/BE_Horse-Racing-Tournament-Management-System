package com.group5.htms.service.impl;

import com.group5.htms.common.exceptions.ResourceNotFoundException;
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
import com.group5.htms.repository.UsersRepository;
import com.group5.htms.service.RaceRegistrationService;
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
    private final RolesRepository rolesRepository;
    private final UsersRepository usersRepository;
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
        validateCreateReferences(request);
        RaceRegistrations registration = raceRegistrationMapper.toEntity(request);

        return raceRegistrationMapper.toResponse(raceRegistrationsRepository.save(registration));
    }

    @Override
    @Transactional
    public RaceRegistrationResponse updateRegistration(Integer id, RaceRegistrationUpdateRequest request) {
        RaceRegistrations registration = findRegistration(id);
        validateUpdateReferences(request);
        raceRegistrationMapper.updateRegistration(registration, request);

        return raceRegistrationMapper.toResponse(raceRegistrationsRepository.save(registration));
    }

    @Override
    @Transactional
    public RaceRegistrationResponse approveRegistration(Integer id, RaceRegistrationApprovalRequest request) {
        RaceRegistrations registration = findRegistration(id);

        registration.setStatus(request.getStatus().trim());
        registration.setApprovedAt(request.getApprovedAt() == null ? Instant.now() : request.getApprovedAt());
        if (request.getApprovedById() != null) {
            validateUserExists(request.getApprovedById());
            Users approvedBy = new Users();
            approvedBy.setId(request.getApprovedById());
            registration.setApprovedBy(approvedBy);
        }

        return raceRegistrationMapper.toResponse(raceRegistrationsRepository.save(registration));
    }

    @Override
    @Transactional
    public void deleteRegistration(Integer id) {
        RaceRegistrations registration = findRegistration(id);
        raceRegistrationsRepository.delete(registration);
    }

    private RaceRegistrations findRegistration(Integer id) {
        return raceRegistrationsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Race registration not found"));
    }

    private void validateCreateReferences(RaceRegistrationCreateRequest request) {
        validateTournamentExists(request.getTournamentId());
        validateRaceExists(request.getRaceId());
        validateHorseExists(request.getHorseId());
        validateRoleExists(request.getOwnerRoleId(), "Owner role not found");
        if (request.getJockeyRoleId() != null) {
            validateRoleExists(request.getJockeyRoleId(), "Jockey role not found");
        }
        if (request.getApprovedById() != null) {
            validateUserExists(request.getApprovedById());
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
        if (request.getApprovedById() != null) {
            validateUserExists(request.getApprovedById());
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

    private void validateRoleExists(Integer id, String message) {
        if (!rolesRepository.existsById(id)) {
            throw new ResourceNotFoundException(message);
        }
    }

    private void validateUserExists(Integer id) {
        if (!usersRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found");
        }
    }
}
