package com.group5.htms.service.impl;

import com.group5.htms.dto.raceround.request.RaceRoundCreateRequest;
import com.group5.htms.dto.raceround.request.RaceRoundUpdateRequest;
import com.group5.htms.dto.raceround.response.RaceRoundResponse;
import com.group5.htms.entity.JockeyHorseAssignments;
import com.group5.htms.entity.RaceRounds;
import com.group5.htms.enums.RaceResultStatus;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.exception.ResourceNotFoundException;
import com.group5.htms.mapper.RaceRoundMapper;
import com.group5.htms.repository.JockeyHorseAssignmentsRepository;
import com.group5.htms.repository.RaceResultsRepository;
import com.group5.htms.repository.RaceRoundsRepository;
import com.group5.htms.service.RaceRoundService;
import com.group5.htms.validation.RaceRoundValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RaceRoundServiceImpl implements RaceRoundService {
    private final RaceRoundsRepository raceRoundsRepository;
    private final RaceResultsRepository raceResultsRepository;
    private final JockeyHorseAssignmentsRepository jockeyHorseAssignmentsRepository;
    private final RaceRoundMapper raceRoundMapper;
    private final RaceRoundValidator raceRoundValidator;

    @Override
    @Transactional(readOnly = true)
    public List<RaceRoundResponse> getAllRounds(Integer raceId, Integer assignmentId) {
        if (raceId != null) {
            return raceRoundsRepository.findByRaces_IdOrderByRoundNumberAscPositionAsc(raceId)
                    .stream()
                    .map(raceRoundMapper::toResponse)
                    .toList();
        }
        if (assignmentId != null) {
            return raceRoundsRepository.findByAssignment_IdOrderByRoundNumberAsc(assignmentId)
                    .stream()
                    .map(raceRoundMapper::toResponse)
                    .toList();
        }
        return raceRoundsRepository.findAll()
                .stream()
                .map(raceRoundMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RaceRoundResponse getRoundById(Integer id) {
        return raceRoundMapper.toResponse(findRound(id));
    }

    @Override
    @Transactional
    public RaceRoundResponse createRound(RaceRoundCreateRequest request) {
        JockeyHorseAssignments assignment = getAssignment(request.getAssignmentId());
        ensureRaceResultsEditable(assignment.getRaces().getId());
        raceRoundValidator.ensureRoundNumberWithinLapCount(request.getRoundNumber(), assignment);
        checkRoundUniqueness(null, assignment.getRaces().getId(), assignment.getId(), request.getRoundNumber(), request.getPosition());

        RaceRounds round = raceRoundMapper.toEntity(request, assignment);
        return raceRoundMapper.toResponse(raceRoundsRepository.save(round));
    }

    @Override
    @Transactional
    public RaceRoundResponse updateRound(Integer id, RaceRoundUpdateRequest request) {
        RaceRounds round = findRound(id);
        ensureRaceResultsEditable(round.getRaces().getId());
        JockeyHorseAssignments assignment = request.getAssignmentId() == null
                ? getAssignment(round.getAssignment().getId())
                : getAssignment(request.getAssignmentId());
        if (!assignment.getRaces().getId().equals(round.getRaces().getId())) {
            ensureRaceResultsEditable(assignment.getRaces().getId());
        }

        Integer roundNumber = request.getRoundNumber() == null ? round.getRoundNumber() : request.getRoundNumber();
        Integer position = request.getPosition() == null ? round.getPosition() : request.getPosition();
        raceRoundValidator.ensureRoundNumberWithinLapCount(roundNumber, assignment);
        checkRoundUniqueness(id, assignment.getRaces().getId(), assignment.getId(), roundNumber, position);

        raceRoundMapper.updateRound(round, request, assignment);
        return raceRoundMapper.toResponse(raceRoundsRepository.save(round));
    }


    private RaceRounds findRound(Integer id) {
        return raceRoundsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Race round not found"));
    }

    private JockeyHorseAssignments getAssignment(Integer id) {
        return jockeyHorseAssignmentsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jockey assignment not found"));
    }

    private void checkRoundUniqueness(Integer id, Integer raceId, Integer assignmentId, Integer roundNumber, Integer position) {
        boolean duplicateAssignmentRound = id == null
                ? raceRoundsRepository.existsByRaces_IdAndAssignment_IdAndRoundNumber(raceId, assignmentId, roundNumber)
                : raceRoundsRepository.existsByRaces_IdAndAssignment_IdAndRoundNumberAndIdNot(raceId, assignmentId, roundNumber, id);
        raceRoundValidator.ensureUniqueAssignmentRound(duplicateAssignmentRound);

        boolean duplicatePosition = id == null
                ? raceRoundsRepository.existsByRaces_IdAndRoundNumberAndPosition(raceId, roundNumber, position)
                : raceRoundsRepository.existsByRaces_IdAndRoundNumberAndPositionAndIdNot(raceId, roundNumber, position, id);
        raceRoundValidator.ensureUniqueRoundPosition(duplicatePosition);
    }

    private void ensureRaceResultsEditable(Integer raceId) {
        if (raceResultsRepository.existsByRaces_IdAndStatusIgnoreCase(raceId, RaceResultStatus.PUBLISHED.getValue())
                || raceResultsRepository.existsByRaces_IdAndStatusIgnoreCase(raceId, RaceResultStatus.CONFIRMED.getValue())) {
            throw new BadRequestException("Lap results cannot be changed after race results are confirmed or published");
        }
    }
}
