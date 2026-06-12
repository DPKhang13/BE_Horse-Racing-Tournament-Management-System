package com.group5.htms.service.impl;

import com.group5.htms.exception.ResourceNotFoundException;
import com.group5.htms.dto.horse.request.HorseCreateRequest;
import com.group5.htms.dto.horse.request.HorseUpdateRequest;
import com.group5.htms.dto.horse.response.HorseRankingResponse;
import com.group5.htms.dto.horse.response.HorseResponse;
import com.group5.htms.entity.Horses;
import com.group5.htms.mapper.HorseMapper;
import com.group5.htms.repository.HorseOwnerProfilesRepository;
import com.group5.htms.repository.HorsesRepository;
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
    private static final String STATUS_DELETED = "deleted";
    private static final String STATUS_ACTIVE = "active";

    private final HorsesRepository horsesRepository;
    private final HorseOwnerProfilesRepository horseOwnerProfilesRepository;
    private final AuthService authService;
    private final HorseMapper horseMapper;

    @Override
    public List<HorseResponse> getAllHorses() {
        return horsesRepository.findAll()
                .stream()
                .filter(horse -> !isDeleted(horse.getStatus()))
                .map(horseMapper::toResponse)
                .toList();
    }

    @Override
    public List<HorseRankingResponse> getHorseRanking(String status, Integer limit) {
        String normalizedStatus = status == null || status.isBlank() ? STATUS_ACTIVE : status.trim();
        List<Horses> horses = horsesRepository
                .findByStatusIgnoreCaseOrderByRankingPointsDescTotalWinsDescNameAsc(normalizedStatus);
        int maxResult = normalizeLimit(limit, horses.size());

        return java.util.stream.IntStream.range(0, maxResult)
                .mapToObj(index -> horseMapper.toRankingResponse(horses.get(index), index + 1))
                .toList();
    }

    @Override
    public HorseResponse getHorseById(Integer id) {
        return horseMapper.toResponse(findHorse(id));
    }

    @Override
    @Transactional
    public HorseResponse createHorse(HorseCreateRequest request) {
        Integer ownerId = authService.getCurrentUserId();
        validateOwnerProfileExists(ownerId);
        request.setOwnerId(ownerId);
        Horses horse = horseMapper.toEntity(request);

        return horseMapper.toResponse(horsesRepository.save(horse));
    }

    @Override
    @Transactional
    public HorseResponse updateHorse(Integer id, HorseUpdateRequest request) {
        Horses horse = findHorseForCurrentOwner(id);
        request.setOwnerId(null);
        horseMapper.updateHorse(horse, request);

        return horseMapper.toResponse(horsesRepository.save(horse));
    }

    @Override
    @Transactional
    public void deleteHorse(Integer id) {
        Horses horse = findHorseForCurrentOwner(id);
        horse.setStatus(STATUS_DELETED);
        horsesRepository.save(horse);
    }

    private Horses findHorse(Integer id) {
        return horsesRepository.findById(id)
                .filter(horse -> !isDeleted(horse.getStatus()))
                .orElseThrow(() -> new ResourceNotFoundException("Horse not found"));
    }

    private Horses findHorseForCurrentOwner(Integer id) {
        Horses horse = findHorse(id);
        Integer ownerId = authService.getCurrentUserId();

        if (!Objects.equals(horse.getOwner().getId(), ownerId)) {
            throw new AccessDeniedException("You do not own this horse");
        }

        return horse;
    }

    private void validateOwnerProfileExists(Integer ownerId) {
        if (!horseOwnerProfilesRepository.existsById(ownerId)) {
            throw new ResourceNotFoundException("Horse owner profile not found");
        }
    }

    private boolean isDeleted(String status) {
        return STATUS_DELETED.equalsIgnoreCase(status);
    }

    private int normalizeLimit(Integer limit, int total) {
        if (limit == null || limit <= 0 || limit > total) {
            return total;
        }

        return limit;
    }
}
