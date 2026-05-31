package com.group5.htms.service.impl;

import com.group5.htms.exception.ResourceNotFoundException;
import com.group5.htms.dto.horse.request.HorseCreateRequest;
import com.group5.htms.dto.horse.request.HorseUpdateRequest;
import com.group5.htms.dto.horse.response.HorseResponse;
import com.group5.htms.entity.Horses;
import com.group5.htms.entity.JockeyHorseAssignments;
import com.group5.htms.entity.RaceRegistrations;
import com.group5.htms.mapper.HorseMapper;
import com.group5.htms.repository.BetsRepository;
import com.group5.htms.repository.HorsesRepository;
import com.group5.htms.repository.JockeyHorseAssignmentsRepository;
import com.group5.htms.repository.RaceRegistrationsRepository;
import com.group5.htms.repository.RaceResultsRepository;
import com.group5.htms.service.AuthService;
import com.group5.htms.service.HorseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class HorseServiceImpl implements HorseService {
    private static final String ROLE_HORSE_OWNER = "horse_owner";

    private final HorsesRepository horsesRepository;
    private final RaceRegistrationsRepository raceRegistrationsRepository;
    private final JockeyHorseAssignmentsRepository jockeyHorseAssignmentsRepository;
    private final RaceResultsRepository raceResultsRepository;
    private final BetsRepository betsRepository;
    private final AuthService authService;
    private final HorseMapper horseMapper;

    @Override
    public List<HorseResponse> getAllHorses() {
        return horsesRepository.findAll()
                .stream()
                .map(horseMapper::toResponse)
                .toList();
    }

    @Override
    public HorseResponse getHorseById(Integer id) {
        return horseMapper.toResponse(findHorse(id));
    }

    @Override
    @Transactional
    public HorseResponse createHorse(HorseCreateRequest request) {
        request.setOwnerRoleId(authService.getCurrentUserRoleId(ROLE_HORSE_OWNER));
        Horses horse = horseMapper.toEntity(request);

        return horseMapper.toResponse(horsesRepository.save(horse));
    }

    @Override
    @Transactional
    public HorseResponse updateHorse(Integer id, HorseUpdateRequest request) {
        Horses horse = findHorseForCurrentOwner(id);
        request.setOwnerRoleId(null);
        horseMapper.updateHorse(horse, request);

        return horseMapper.toResponse(horsesRepository.save(horse));
    }

    @Override
    @Transactional
    public void deleteHorse(Integer id) {
        Horses horse = findHorseForCurrentOwner(id);
        deleteRegistrationsByHorse(id);
        horsesRepository.delete(horse);
    }

    private Horses findHorse(Integer id) {
        return horsesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Horse not found"));
    }

    private Horses findHorseForCurrentOwner(Integer id) {
        Horses horse = findHorse(id);
        Integer ownerRoleId = authService.getCurrentUserRoleId(ROLE_HORSE_OWNER);

        if (!Objects.equals(horse.getOwnerRoles().getId(), ownerRoleId)) {
            throw new AccessDeniedException("You do not own this horse");
        }

        return horse;
    }

    private void deleteRegistrationsByHorse(Integer horseId) {
        List<Integer> registrationIds = raceRegistrationsRepository.findByHorses_Id(horseId)
                .stream()
                .map(RaceRegistrations::getId)
                .toList();

        deleteAssignmentsByRegistrations(registrationIds);
        raceRegistrationsRepository.deleteByHorses_Id(horseId);
    }

    private void deleteAssignmentsByRegistrations(List<Integer> registrationIds) {
        if (registrationIds.isEmpty()) {
            return;
        }

        List<Integer> assignmentIds = jockeyHorseAssignmentsRepository.findByReg_IdIn(registrationIds)
                .stream()
                .map(JockeyHorseAssignments::getId)
                .toList();

        deleteAssignmentChildren(assignmentIds);
        jockeyHorseAssignmentsRepository.deleteByReg_IdIn(registrationIds);
    }

    private void deleteAssignmentChildren(List<Integer> assignmentIds) {
        if (assignmentIds.isEmpty()) {
            return;
        }

        betsRepository.deleteByAssignment_IdIn(assignmentIds);
        raceResultsRepository.deleteByAssignment_IdIn(assignmentIds);
    }
}
