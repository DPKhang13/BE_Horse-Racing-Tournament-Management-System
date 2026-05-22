package com.group5.htms.service.impl;

import com.group5.htms.common.exceptions.ResourceNotFoundException;
import com.group5.htms.dto.horse.request.HorseCreateRequest;
import com.group5.htms.dto.horse.request.HorseUpdateRequest;
import com.group5.htms.dto.horse.response.HorseResponse;
import com.group5.htms.entity.Horses;
import com.group5.htms.mapper.HorseMapper;
import com.group5.htms.repository.HorsesRepository;
import com.group5.htms.repository.RolesRepository;
import com.group5.htms.service.HorseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HorseServiceImpl implements HorseService {
    private final HorsesRepository horsesRepository;
    private final RolesRepository rolesRepository;
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
        validateOwnerRoleExists(request.getOwnerRoleId());
        Horses horse = horseMapper.toEntity(request);

        return horseMapper.toResponse(horsesRepository.save(horse));
    }

    @Override
    @Transactional
    public HorseResponse updateHorse(Integer id, HorseUpdateRequest request) {
        Horses horse = findHorse(id);
        if (request.getOwnerRoleId() != null) {
            validateOwnerRoleExists(request.getOwnerRoleId());
        }
        horseMapper.updateHorse(horse, request);

        return horseMapper.toResponse(horsesRepository.save(horse));
    }

    @Override
    @Transactional
    public void deleteHorse(Integer id) {
        Horses horse = findHorse(id);
        horsesRepository.delete(horse);
    }

    private Horses findHorse(Integer id) {
        return horsesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Horse not found"));
    }

    private void validateOwnerRoleExists(Integer ownerRoleId) {
        if (!rolesRepository.existsById(ownerRoleId)) {
            throw new ResourceNotFoundException("Owner role not found");
        }
    }
}
