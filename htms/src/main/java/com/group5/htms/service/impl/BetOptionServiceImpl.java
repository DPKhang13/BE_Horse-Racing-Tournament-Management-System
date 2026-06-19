package com.group5.htms.service.impl;

import com.group5.htms.dto.betoption.request.BetOptionRateUpdateRequest;
import com.group5.htms.dto.betoption.response.BetOptionResponse;
import com.group5.htms.entity.BetOptions;
import com.group5.htms.entity.Horses;
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
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BetOptionServiceImpl implements BetOptionService {
    private static final String ASSIGNMENT_STATUS_ACCEPTED = "accepted";
    private static final String ASSIGNMENT_STATUS_CONFIRMED = "confirmed";
    private static final BigDecimal DEFAULT_CURRENT_RATE = new BigDecimal("2.00");
    private static final BigDecimal MIN_RATE = new BigDecimal("1.10");
    private static final BigDecimal MAX_RATE = new BigDecimal("10.00");
    private static final BigDecimal ZERO = BigDecimal.ZERO;

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

        for (JockeyHorseAssignments assignment : assignments) {
            Integer horseId = assignment.getReg().getHorses().getId();

            betOptionsRepository
                    .findByRaces_IdAndHorses_Id(raceId, horseId)
                    .orElseGet(() -> createBetOption(race, assignment));
        }

        return recalculateRatesForRace(raceId);
    }

    @Override
    @Transactional
    public List<BetOptionResponse> recalculateRatesForRace(Integer raceId) {
        if (!racesRepository.existsById(raceId)) {
            throw new ResourceNotFoundException("Race not found");
        }

        List<BetOptions> options = betOptionsRepository.findByRaces_IdOrderByCurrentRateAsc(raceId);

        if (options.isEmpty()) {
            return List.of();
        }

        BigDecimal totalRaceBetPoints = options.stream()
                .map(option -> safeMoney(option.getTotalBetPoints()))
                .reduce(ZERO, BigDecimal::add);
        int maxRankingPoints = options.stream()
                .map(BetOptions::getHorses)
                .mapToInt(horse -> safeInt(horse.getRankingPoints()))
                .max()
                .orElse(0);
        int maxTotalWins = options.stream()
                .map(BetOptions::getHorses)
                .mapToInt(horse -> safeInt(horse.getTotalWins()))
                .max()
                .orElse(0);
        Instant now = Instant.now();

        for (BetOptions option : options) {
            option.setCurrentRate(calculateDynamicRate(
                    option,
                    totalRaceBetPoints,
                    maxRankingPoints,
                    maxTotalWins
            ));
            option.setUpdatedAt(now);
        }

        return betOptionsRepository.saveAll(options)
                .stream()
                .sorted(Comparator.comparing(BetOptions::getCurrentRate))
                .map(betOptionMapper::toResponse)
                .toList();
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

    private BigDecimal calculateDynamicRate(
            BetOptions option,
            BigDecimal totalRaceBetPoints,
            int maxRankingPoints,
            int maxTotalWins
    ) {
        BigDecimal rate = baseRateByHorseStrength(option.getHorses(), maxRankingPoints, maxTotalWins)
                .add(marketAdjustment(option, totalRaceBetPoints));

        if (rate.compareTo(MIN_RATE) < 0) {
            rate = MIN_RATE;
        }

        if (rate.compareTo(MAX_RATE) > 0) {
            rate = MAX_RATE;
        }

        return rate.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal baseRateByHorseStrength(
            Horses horse,
            int maxRankingPoints,
            int maxTotalWins
    ) {
        BigDecimal baseRate = new BigDecimal("4.00");
        BigDecimal rankingScore = ratio(safeInt(horse.getRankingPoints()), maxRankingPoints);
        BigDecimal winScore = ratio(safeInt(horse.getTotalWins()), maxTotalWins);
        BigDecimal strengthScore = rankingScore.multiply(new BigDecimal("0.60"))
                .add(winScore.multiply(new BigDecimal("0.40")));

        return baseRate.subtract(strengthScore.multiply(new BigDecimal("2.50")));
    }

    private BigDecimal marketAdjustment(BetOptions option, BigDecimal totalRaceBetPoints) {
        if (totalRaceBetPoints.compareTo(ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal optionShare = safeMoney(option.getTotalBetPoints())
                .divide(totalRaceBetPoints, 6, RoundingMode.HALF_UP);

        if (optionShare.compareTo(new BigDecimal("0.50")) >= 0) {
            return new BigDecimal("-0.80");
        }

        if (optionShare.compareTo(new BigDecimal("0.35")) >= 0) {
            return new BigDecimal("-0.45");
        }

        if (optionShare.compareTo(new BigDecimal("0.20")) >= 0) {
            return new BigDecimal("-0.20");
        }

        if (optionShare.compareTo(new BigDecimal("0.10")) < 0) {
            return new BigDecimal("0.70");
        }

        return BigDecimal.ZERO;
    }

    private BigDecimal ratio(int value, int maxValue) {
        if (value <= 0 || maxValue <= 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(value)
                .divide(BigDecimal.valueOf(maxValue), 6, RoundingMode.HALF_UP);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private BigDecimal safeMoney(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
