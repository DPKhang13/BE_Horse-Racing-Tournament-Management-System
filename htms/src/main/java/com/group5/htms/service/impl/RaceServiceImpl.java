package com.group5.htms.service.impl;

import com.group5.htms.dto.race.response.RaceResponse;
import com.group5.htms.exception.ResourceNotFoundException;
import com.group5.htms.mapper.RaceMapper;
import com.group5.htms.repository.RacesRepository;
import com.group5.htms.repository.TournamentsRepository;
import com.group5.htms.service.RaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RaceServiceImpl implements RaceService {
    private final RacesRepository racesRepository;
    private final TournamentsRepository tournamentsRepository;
    private final RaceMapper raceMapper;

    @Override
    public List<RaceResponse> getRacesByTournament(Integer tournamentId, String status) {
        if (!tournamentsRepository.existsById(tournamentId)) {
            throw new ResourceNotFoundException("Tournament not found");
        }

        if (status != null && !status.isBlank()) {
            return racesRepository
                    .findBySchedule_Tournaments_IdAndStatusIgnoreCaseOrderByScheduledAtAsc(tournamentId, status.trim())
                    .stream()
                    .map(raceMapper::toResponse)
                    .toList();
        }

        return racesRepository.findBySchedule_Tournaments_IdOrderByScheduledAtAsc(tournamentId)
                .stream()
                .map(raceMapper::toResponse)
                .toList();
    }
}
