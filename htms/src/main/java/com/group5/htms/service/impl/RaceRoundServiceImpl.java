package com.group5.htms.service.impl;

import com.group5.htms.dto.raceround.request.RaceRoundCreateRequest;
import com.group5.htms.dto.raceround.request.RaceRoundUpdateRequest;
import com.group5.htms.dto.raceround.response.RaceRoundResponse;
import com.group5.htms.entity.JockeyHorseAssignments;
import com.group5.htms.entity.RaceRounds;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.exception.ResourceNotFoundException;
import com.group5.htms.mapper.RaceRoundMapper;
import com.group5.htms.repository.JockeyHorseAssignmentsRepository;
import com.group5.htms.repository.RaceRoundsRepository;
import com.group5.htms.service.RaceRoundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RaceRoundServiceImpl implements RaceRoundService {
    private final RaceRoundsRepository raceRoundsRepository;
    private final JockeyHorseAssignmentsRepository jockeyHorseAssignmentsRepository;
    private final RaceRoundMapper raceRoundMapper;

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
        validateRoundNumber(request.getRoundNumber(), assignment);
        validateUniqueRound(null, assignment.getRaces().getId(), assignment.getId(), request.getRoundNumber(), request.getPosition());

        RaceRounds round = raceRoundMapper.toEntity(request, assignment);
        return raceRoundMapper.toResponse(raceRoundsRepository.save(round));
    }

    @Override
    @Transactional
    public RaceRoundResponse updateRound(Integer id, RaceRoundUpdateRequest request) {
        RaceRounds round = findRound(id);
        JockeyHorseAssignments assignment = request.getAssignmentId() == null
                ? getAssignment(round.getAssignment().getId())
                : getAssignment(request.getAssignmentId());

        Integer roundNumber = request.getRoundNumber() == null ? round.getRoundNumber() : request.getRoundNumber();
        Integer position = request.getPosition() == null ? round.getPosition() : request.getPosition();
        validateRoundNumber(roundNumber, assignment);
        validateUniqueRound(id, assignment.getRaces().getId(), assignment.getId(), roundNumber, position);

        raceRoundMapper.updateRound(round, request, assignment);
        return raceRoundMapper.toResponse(raceRoundsRepository.save(round));
    }

    @Override
    @Transactional
    public void deleteRound(Integer id) {
        RaceRounds round = findRound(id);
        raceRoundsRepository.delete(round);
    }

    private RaceRounds findRound(Integer id) {
        return raceRoundsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Race round not found"));
    }

    private JockeyHorseAssignments getAssignment(Integer id) {
        return jockeyHorseAssignmentsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jockey assignment not found"));
    }

    private void validateRoundNumber(Integer roundNumber, JockeyHorseAssignments assignment) {
        Integer lapCount = assignment.getRaces().getLapCount();
        if (lapCount != null && roundNumber > lapCount) {
            throw new BadRequestException("Round number must not be greater than race lap count");
        }
    }

    private void validateUniqueRound(Integer id, Integer raceId, Integer assignmentId, Integer roundNumber, Integer position) {
        boolean duplicateAssignmentRound = id == null
                ? raceRoundsRepository.existsByRaces_IdAndAssignment_IdAndRoundNumber(raceId, assignmentId, roundNumber)
                : raceRoundsRepository.existsByRaces_IdAndAssignment_IdAndRoundNumberAndIdNot(raceId, assignmentId, roundNumber, id);
        if (duplicateAssignmentRound) {
            throw new BadRequestException("This assignment already has a result for this round");
        }

        boolean duplicatePosition = id == null
                ? raceRoundsRepository.existsByRaces_IdAndRoundNumberAndPosition(raceId, roundNumber, position)
                : raceRoundsRepository.existsByRaces_IdAndRoundNumberAndPositionAndIdNot(raceId, roundNumber, position, id);
        if (duplicatePosition) {
            throw new BadRequestException("This position is already used in this race round");
        }
    }
}
