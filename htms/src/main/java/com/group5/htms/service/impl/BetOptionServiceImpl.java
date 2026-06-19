package com.group5.htms.service.impl;

import com.group5.htms.dto.betoption.request.BetOptionRateUpdateRequest;
import com.group5.htms.dto.betoption.response.BetOptionResponse;
import com.group5.htms.entity.BetOptions;
import com.group5.htms.entity.JockeyHorseAssignments;
import com.group5.htms.entity.Races;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.exception.ResourceNotFoundException;
import com.group5.htms.mapper.BetOptionMapper;
import com.group5.htms.repository.BetOptionsRepository;
import com.group5.htms.repository.JockeyHorseAssignmentsRepository;
import com.group5.htms.repository.RacesRepository;
import com.group5.htms.service.BetOptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BetOptionServiceImpl implements BetOptionService {
    private static final String ASSIGNMENT_STATUS_ACCEPTED = "accepted";
    private static final String ASSIGNMENT_STATUS_CONFIRMED = "confirmed";
    private static final BigDecimal DEFAULT_CURRENT_RATE = BigDecimal.ONE;

    private final BetOptionsRepository betOptionsRepository;
    private final JockeyHorseAssignmentsRepository jockeyHorseAssignmentsRepository;
    private final RacesRepository racesRepository;
    private final BetOptionMapper betOptionMapper;

    @Override
    @Transactional
    public List<BetOptionResponse> generateBetOptionsForRace(Integer raceId) {
        Races race = racesRepository.findById(raceId)
                .orElseThrow(() -> new ResourceNotFoundException("Race not found"));

        List<JockeyHorseAssignments> assignments = new ArrayList<>();
        assignments.addAll(jockeyHorseAssignmentsRepository
                .findByRaces_IdAndStatusIgnoreCase(raceId, ASSIGNMENT_STATUS_ACCEPTED));
        assignments.addAll(jockeyHorseAssignmentsRepository
                .findByRaces_IdAndStatusIgnoreCase(raceId, ASSIGNMENT_STATUS_CONFIRMED));

        if (assignments.isEmpty()) {
            throw new BadRequestException("No accepted or confirmed assignments found for this race");
        }

        List<BetOptionResponse> responses = new ArrayList<>();

        for (JockeyHorseAssignments assignment : assignments) {
            Integer horseId = assignment.getReg().getHorses().getId();

            BetOptions option = betOptionsRepository
                    .findByRaces_IdAndHorses_Id(raceId, horseId)
                    .orElseGet(() -> createBetOption(race, assignment));

            responses.add(betOptionMapper.toResponse(option));
        }

        return responses;
    }

    @Override
    @Transactional
    public BetOptionResponse updateCurrentRate(Integer optionId, BetOptionRateUpdateRequest request) {
        if (request == null) {
            throw new BadRequestException("Rate update request is required");
        }

        if (request.getCurrentRate() == null || request.getCurrentRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Current rate must be greater than 0");
        }

        BetOptions option = findBetOption(optionId);
        option.setCurrentRate(request.getCurrentRate());
        option.setUpdatedAt(Instant.now());

        return betOptionMapper.toResponse(betOptionsRepository.save(option));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BetOptionResponse> getAllBetOptions() {
        return betOptionsRepository.findAll()
                .stream()
                .map(betOptionMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BetOptionResponse> getBetOptionsByRace(Integer raceId) {
        if (!racesRepository.existsById(raceId)) {
            throw new ResourceNotFoundException("Race not found");
        }

        return betOptionsRepository.findByRaces_IdOrderByCurrentRateAsc(raceId)
                .stream()
                .map(betOptionMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BetOptionResponse getBetOptionById(Integer id) {
        return betOptionMapper.toResponse(findBetOption(id));
    }

    private BetOptions createBetOption(Races race, JockeyHorseAssignments assignment) {
        BetOptions option = BetOptions.builder()
                .races(race)
                .assignment(assignment)
                .horses(assignment.getReg().getHorses())
                .currentRate(DEFAULT_CURRENT_RATE)
                .totalBetPoints(BigDecimal.ZERO)
                .totalBetCount(0)
                .updatedAt(Instant.now())
                .build();

        try {
            return betOptionsRepository.save(option);
        } catch (DataIntegrityViolationException ex) {
            return betOptionsRepository
                    .findByRaces_IdAndHorses_Id(race.getId(), assignment.getReg().getHorses().getId())
                    .orElseThrow(() -> new BadRequestException("Bet option already exists for this race and horse"));
        }
    }

    private BetOptions findBetOption(Integer optionId) {
        if (optionId == null) {
            throw new BadRequestException("Option id is required");
        }

        return betOptionsRepository.findById(optionId)
                .orElseThrow(() -> new ResourceNotFoundException("Bet option not found"));
    }
}
