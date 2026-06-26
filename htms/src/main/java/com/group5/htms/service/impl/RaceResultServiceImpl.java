package com.group5.htms.service.impl;

import com.group5.htms.exception.ResourceNotFoundException;
import com.group5.htms.enums.RaceResultStatus;
import com.group5.htms.dto.raceresult.request.RaceResultCreateRequest;
import com.group5.htms.dto.raceresult.request.RaceResultPublishRequest;
import com.group5.htms.dto.raceresult.request.RaceResultUpdateRequest;
import com.group5.htms.dto.raceresult.response.RaceResultListResponse;
import com.group5.htms.dto.raceresult.response.RaceResultResponse;
import com.group5.htms.entity.JockeyHorseAssignments;
import com.group5.htms.entity.RaceResults;
import com.group5.htms.mapper.RaceResultMapper;
import com.group5.htms.repository.JockeyHorseAssignmentsRepository;
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
    private final RaceResultMapper raceResultMapper;

    @Override
    public List<RaceResultListResponse> getAllResults() {
        return raceResultsRepository.findAll()
                .stream()
                .filter(result -> !isDeleted(result.getStatus()))
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

        return raceResultMapper.toResponse(raceResultsRepository.save(result));
    }

    @Override
    @Transactional
    public RaceResultResponse updateResult(Integer id, RaceResultUpdateRequest request) {
        RaceResults result = findResult(id);
        JockeyHorseAssignments assignment = validateUpdateReferences(request);
        raceResultMapper.updateResult(result, request, assignment);

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
        result.setStatus(RaceResultStatus.DELETED.getValue());
        raceResultsRepository.save(result);
    }

    private RaceResults findResult(Integer id) {
        return raceResultsRepository.findById(id)
                .filter(result -> !isDeleted(result.getStatus()))
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
        if (request.getAssignmentId() != null) {
            return getAssignment(request.getAssignmentId());
        }
        if (request.getReportId() != null) {
            validateReportExists(request.getReportId());
        }
        return null;
    }

    private JockeyHorseAssignments getAssignment(Integer id) {
        return jockeyHorseAssignmentsRepository.findById(id)
                .filter(assignment -> !isDeleted(assignment.getStatus()))
                .orElseThrow(() -> new ResourceNotFoundException("Jockey assignment not found"));
    }

    private void validateReportExists(Integer id) {
        if (!refereeReportsRepository.existsById(id)) {
            throw new ResourceNotFoundException("Referee report not found");
        }
    }

    private boolean isDeleted(String status) {
        return RaceResultStatus.DELETED.getValue().equalsIgnoreCase(status);
    }
}


