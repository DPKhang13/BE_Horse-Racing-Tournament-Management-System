package com.group5.htms.service.impl;

import com.group5.htms.dto.raceresult.request.RaceResultCreateRequest;
import com.group5.htms.dto.raceresult.request.RaceResultPublishRequest;
import com.group5.htms.dto.raceresult.request.RaceResultUpdateRequest;
import com.group5.htms.dto.raceresult.response.RaceResultListResponse;
import com.group5.htms.dto.raceresult.response.RaceResultResponse;
import com.group5.htms.entity.JockeyHorseAssignments;
import com.group5.htms.entity.RaceResults;
import com.group5.htms.entity.RaceRounds;
import com.group5.htms.enums.RaceResultStatus;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.exception.ResourceNotFoundException;
import com.group5.htms.mapper.RaceResultMapper;
import com.group5.htms.repository.JockeyHorseAssignmentsRepository;
import com.group5.htms.repository.RacePointRulesRepository;
import com.group5.htms.repository.RaceResultsRepository;
import com.group5.htms.repository.RaceRoundsRepository;
import com.group5.htms.repository.RefereeReportsRepository;
import com.group5.htms.service.RaceResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RaceResultServiceImpl implements RaceResultService {

    private final RaceResultsRepository raceResultsRepository;
    private final RaceRoundsRepository raceRoundsRepository;
    private final JockeyHorseAssignmentsRepository jockeyHorseAssignmentsRepository;
    private final RacePointRulesRepository racePointRulesRepository;
    private final RefereeReportsRepository refereeReportsRepository;
    private final RaceResultMapper raceResultMapper;

    @Override
    public List<RaceResultListResponse> getAllResults() {
        return raceResultsRepository.findAll()
                .stream()
                .map(raceResultMapper::toListResponse)
                .toList();
    }

    @Override
    public RaceResultResponse getResultById(Integer id) {
        return raceResultMapper.toResponse(findResult(id));
    }

    @Override
    @Transactional
    public RaceResultResponse createResult(RaceResultCreateRequest request) {
        JockeyHorseAssignments assignment = validateCreateReferences(request);
        RaceResults result = raceResultMapper.toEntity(request, assignment);
        applyPointRule(result);

        return raceResultMapper.toResponse(raceResultsRepository.save(result));
    }

    @Override
    @Transactional
    public RaceResultResponse updateResult(Integer id, RaceResultUpdateRequest request) {
        RaceResults result = findResult(id);
        JockeyHorseAssignments assignment = validateUpdateReferences(request);
        raceResultMapper.updateResult(result, request, assignment);
        applyPointRule(result);

        return raceResultMapper.toResponse(raceResultsRepository.save(result));
    }

    @Override
    @Transactional
    public RaceResultResponse publishResult(Integer id, RaceResultPublishRequest request) {
        RaceResults result = findResult(id);

        applyPointRule(result);
        result.setStatus(request.getStatus().trim());
        result.setPublishedAt(request.getPublishedAt() == null ? Instant.now() : request.getPublishedAt());

        return raceResultMapper.toResponse(raceResultsRepository.save(result));
    }

    @Override
    @Transactional
    public List<RaceResultListResponse> calculateResultsFromRounds(Integer raceId) {
        List<RaceRounds> rounds = raceRoundsRepository.findByRaces_IdOrderByAssignment_IdAscRoundNumberAsc(raceId);
        if (rounds.isEmpty()) {
            throw new BadRequestException("Race has no rounds to calculate results");
        }

        Map<Integer, AssignmentLapSummary> summariesByAssignment = new LinkedHashMap<>();
        for (RaceRounds round : rounds) {
            if (round.getLapTimeSec() == null) {
                throw new BadRequestException("All race rounds must have lap time before calculating results");
            }

            Integer assignmentId = round.getAssignment().getId();
            AssignmentLapSummary summary = summariesByAssignment.computeIfAbsent(
                    assignmentId,
                    ignored -> new AssignmentLapSummary(round.getAssignment(), round.getRoundNumber())
            );
            summary.totalTimeSec = summary.totalTimeSec.add(round.getLapTimeSec());
            summary.finalRound = Math.max(summary.finalRound, round.getRoundNumber());
        }

        List<AssignmentLapSummary> rankedSummaries = new ArrayList<>(summariesByAssignment.values());
        rankedSummaries.sort(Comparator.comparing(summary -> summary.totalTimeSec));

        List<RaceResults> savedResults = new ArrayList<>();
        int finishPosition = 1;
        for (AssignmentLapSummary summary : rankedSummaries) {
            RaceResults result = raceResultsRepository
                    .findByRaces_IdAndAssignment_Id(raceId, summary.assignment.getId())
                    .orElseGet(() -> newResultFromAssignment(summary.assignment));

            result.setFinalRound(summary.finalRound);
            result.setFinishPosition(finishPosition);
            result.setFinishTimeSec(summary.totalTimeSec);
            result.setIsDisqualified(false);
            result.setDisqualifyReason(null);
            if (result.getStatus() == null || result.getStatus().isBlank()) {
                result.setStatus(RaceResultStatus.DRAFT.getValue());
            }
            if (result.getRecordedAt() == null) {
                result.setRecordedAt(Instant.now());
            }
            applyPointRule(result);

            savedResults.add(raceResultsRepository.save(result));
            finishPosition++;
        }

        return savedResults.stream()
                .sorted(Comparator.comparing(RaceResults::getFinishPosition))
                .map(raceResultMapper::toListResponse)
                .toList();
    }

    private RaceResults newResultFromAssignment(JockeyHorseAssignments assignment) {
        return RaceResults.builder()
                .assignment(assignment)
                .races(assignment.getRaces())
                .horses(assignment.getReg().getHorses())
                .owner(assignment.getReg().getOwner())
                .pointsAwarded(0)
                .isDisqualified(false)
                .status(RaceResultStatus.DRAFT.getValue())
                .recordedAt(Instant.now())
                .build();
    }

    private void applyPointRule(RaceResults result) {
        if (Boolean.TRUE.equals(result.getIsDisqualified()) || result.getFinishPosition() == null) {
            result.setPointsAwarded(0);
            return;
        }

        Integer raceId = result.getRaces().getId();
        Integer finishPosition = result.getFinishPosition();
        Integer points = racePointRulesRepository.findByRace_IdAndFinishPosition(raceId, finishPosition)
                .map(rule -> rule.getPoints() == null ? 0 : rule.getPoints())
                .orElse(0);

        result.setPointsAwarded(points);
    }

    private RaceResults findResult(Integer id) {
        return raceResultsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Race result not found"));
    }

    private JockeyHorseAssignments validateCreateReferences(RaceResultCreateRequest request) {
        JockeyHorseAssignments assignment = getAssignment(request.getAssignmentId());
        if (request.getReportId() != null) {
            validateReportExists(request.getReportId());
        }
        return assignment;
    }

    private JockeyHorseAssignments validateUpdateReferences(RaceResultUpdateRequest request) {
        if (request.getReportId() != null) {
            validateReportExists(request.getReportId());
        }
        if (request.getAssignmentId() != null) {
            return getAssignment(request.getAssignmentId());
        }
        return null;
    }

    private JockeyHorseAssignments getAssignment(Integer id) {
        return jockeyHorseAssignmentsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jockey assignment not found"));
    }

    private void validateReportExists(Integer id) {
        if (!refereeReportsRepository.existsById(id)) {
            throw new ResourceNotFoundException("Referee report not found");
        }
    }

    private static class AssignmentLapSummary {
        private final JockeyHorseAssignments assignment;
        private BigDecimal totalTimeSec = BigDecimal.ZERO;
        private Integer finalRound;

        private AssignmentLapSummary(JockeyHorseAssignments assignment, Integer finalRound) {
            this.assignment = assignment;
            this.finalRound = finalRound;
        }
    }
}
