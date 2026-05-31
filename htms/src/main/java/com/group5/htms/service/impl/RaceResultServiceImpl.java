package com.group5.htms.service.impl;

import com.group5.htms.exception.ResourceNotFoundException;
import com.group5.htms.dto.raceresult.request.RaceResultCreateRequest;
import com.group5.htms.dto.raceresult.request.RaceResultPublishRequest;
import com.group5.htms.dto.raceresult.request.RaceResultUpdateRequest;
import com.group5.htms.dto.raceresult.response.RaceResultResponse;
import com.group5.htms.entity.RaceResults;
import com.group5.htms.mapper.RaceResultMapper;
import com.group5.htms.repository.JockeyHorseAssignmentsRepository;
import com.group5.htms.repository.PrizeDistributionsRepository;
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
    private final RefereeReportsRepository refereeReportsRepository;
    private final PrizeDistributionsRepository prizeDistributionsRepository;
    private final RaceResultMapper raceResultMapper;

    @Override
    public List<RaceResultResponse> getAllResults() {
        return raceResultsRepository.findAll()
                .stream()
                .map(raceResultMapper::toResponse)
                .toList();
    }

    @Override
    public RaceResultResponse getResultById(Integer id) {
        return raceResultMapper.toResponse(findResult(id));
    }

    @Override
    @Transactional
    public RaceResultResponse createResult(RaceResultCreateRequest request) {
        validateCreateReferences(request);
        RaceResults result = raceResultMapper.toEntity(request);

        return raceResultMapper.toResponse(raceResultsRepository.save(result));
    }

    @Override
    @Transactional
    public RaceResultResponse updateResult(Integer id, RaceResultUpdateRequest request) {
        RaceResults result = findResult(id);
        validateUpdateReferences(request);
        raceResultMapper.updateResult(result, request);

        return raceResultMapper.toResponse(raceResultsRepository.save(result));
    }

    @Override
    @Transactional
    public RaceResultResponse publishResult(Integer id, RaceResultPublishRequest request) {
        RaceResults result = findResult(id);

        result.setStatus(request.getStatus().trim());
        result.setPublishedAt(request.getPublishedAt() == null ? Instant.now() : request.getPublishedAt());

        return raceResultMapper.toResponse(raceResultsRepository.save(result));
    }

    @Override
    @Transactional
    public void deleteResult(Integer id) {
        RaceResults result = findResult(id);
        raceResultsRepository.delete(result);
    }

    private RaceResults findResult(Integer id) {
        return raceResultsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Race result not found"));
    }

    private void validateCreateReferences(RaceResultCreateRequest request) {
        validateAssignmentExists(request.getAssignmentId());
        if (request.getReportId() != null) {
            validateReportExists(request.getReportId());
        }
        if (request.getPrizeId() != null) {
            validatePrizeExists(request.getPrizeId());
        }
    }

    private void validateUpdateReferences(RaceResultUpdateRequest request) {
        if (request.getAssignmentId() != null) {
            validateAssignmentExists(request.getAssignmentId());
        }
        if (request.getReportId() != null) {
            validateReportExists(request.getReportId());
        }
        if (request.getPrizeId() != null) {
            validatePrizeExists(request.getPrizeId());
        }
    }

    private void validateAssignmentExists(Integer id) {
        if (!jockeyHorseAssignmentsRepository.existsById(id)) {
            throw new ResourceNotFoundException("Jockey assignment not found");
        }
    }

    private void validateReportExists(Integer id) {
        if (!refereeReportsRepository.existsById(id)) {
            throw new ResourceNotFoundException("Referee report not found");
        }
    }

    private void validatePrizeExists(Integer id) {
        if (!prizeDistributionsRepository.existsById(id)) {
            throw new ResourceNotFoundException("Prize distribution not found");
        }
    }
}
