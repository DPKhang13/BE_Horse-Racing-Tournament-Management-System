package com.group5.htms.service.impl;

import com.group5.htms.exception.ResourceNotFoundException;
import com.group5.htms.enums.HorseStatus;
import com.group5.htms.dto.horse.request.HorseCreateRequest;
import com.group5.htms.dto.horse.request.HorseUpdateRequest;
import com.group5.htms.dto.horse.response.HorseCountResponse;
import com.group5.htms.dto.horse.response.HorseListResponse;
import com.group5.htms.dto.horse.response.HorseRankingResponse;
import com.group5.htms.dto.horse.response.HorseResponse;
import com.group5.htms.entity.HorseOwnerProfiles;
import com.group5.htms.entity.Horses;
import com.group5.htms.mapper.HorseMapper;
import com.group5.htms.repository.HorseOwnerProfilesRepository;
import com.group5.htms.repository.HorsesRepository;
import com.group5.htms.service.AuthService;
import com.group5.htms.service.HorseService;
import com.group5.htms.validation.HorseValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HorseServiceImpl implements HorseService {

    private final HorsesRepository horsesRepository;
    private final HorseOwnerProfilesRepository horseOwnerProfilesRepository;
    private final AuthService authService;
    private final HorseMapper horseMapper;
    private final HorseValidator horseValidator;

    @Override
    @Transactional(readOnly = true)
    public HorseCountResponse getHorseCount() {
        return HorseCountResponse.builder()
                .horseCount(horsesRepository.count())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HorseListResponse> getAllHorses() {
        return horsesRepository.findAll()
                .stream()
                .map(horseMapper::toListResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HorseRankingResponse> getHorseRanking() {
        List<Horses> horses = horsesRepository
                .findByStatusIgnoreCaseOrderByRankingPointsDescTotalWinsDescNameAsc(HorseStatus.ACTIVE.getValue());

        return java.util.stream.IntStream.range(0, horses.size())
                .mapToObj(index -> horseMapper.toRankingResponse(horses.get(index), index + 1))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public HorseResponse getHorseById(Integer id) {
        return horseMapper.toResponse(findHorse(id));
    }

    @Override
    @Transactional
    public HorseResponse createHorse(HorseCreateRequest request) {
        Integer ownerId = authService.getCurrentUserId();
        HorseOwnerProfiles owner = findOwnerProfile(ownerId);
        request.setOwnerId(ownerId);
        Horses horse = horseMapper.toEntity(request);
        horse.setOwner(owner);

        return horseMapper.toResponse(horsesRepository.save(horse));
    }

    @Override
    @Transactional
    public HorseResponse updateHorse(Integer id, HorseUpdateRequest request) {
        Horses horse = findHorseForCurrentOwner(id);
        horseValidator.ensureNoBackendManagedFields(request);
        horseMapper.updateHorse(horse, request);

        return horseMapper.toResponse(horsesRepository.save(horse));
    }


    private Horses findHorse(Integer id) {
        return horsesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Horse not found"));
    }

    private Horses findHorseForCurrentOwner(Integer id) {
        Horses horse = findHorse(id);
        Integer ownerId = authService.getCurrentUserId();

        horseValidator.ensureOwnerCanManageHorse(horse, ownerId);

        return horse;
    }

    private HorseOwnerProfiles findOwnerProfile(Integer ownerId) {
        return horseOwnerProfilesRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Horse owner profile not found"));
    }


}


