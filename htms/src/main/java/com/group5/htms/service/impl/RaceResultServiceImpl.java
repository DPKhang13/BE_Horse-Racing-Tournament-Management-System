package com.group5.htms.service.impl;

import com.group5.htms.dto.raceresult.request.RaceResultCreateRequest;
import com.group5.htms.dto.raceresult.request.RaceResultPublishRequest;
import com.group5.htms.dto.raceresult.request.RaceResultUpdateRequest;
import com.group5.htms.dto.raceresult.response.RaceResultListResponse;
import com.group5.htms.dto.raceresult.response.RaceResultResponse;
import com.group5.htms.entity.JockeyHorseAssignments;
import com.group5.htms.entity.RaceResults;
import com.group5.htms.exception.ResourceNotFoundException;
import com.group5.htms.mapper.RaceResultMapper;
import com.group5.htms.repository.JockeyHorseAssignmentsRepository;
import com.group5.htms.repository.RacePointRulesRepository;
import com.group5.htms.repository.RaceResultsRepository;
import com.group5.htms.repository.RefereeReportsRepository;
import com.group5.htms.service.RaceResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RaceResultServiceImpl implements RaceResultService {

    private final RaceResultsRepository raceResultsRepository;
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
}
