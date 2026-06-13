package com.group5.htms.service.impl;

import com.group5.htms.dto.race.response.RaceListResponse;
import com.group5.htms.entity.Races;
import com.group5.htms.exception.ResourceNotFoundException;
import com.group5.htms.mapper.RaceMapper;
import com.group5.htms.repository.JockeyHorseAssignmentsRepository;
import com.group5.htms.repository.RaceRefereeAssignmentsRepository;
import com.group5.htms.repository.RaceRegistrationsRepository;
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
    private final RaceRegistrationsRepository raceRegistrationsRepository;
    private final JockeyHorseAssignmentsRepository jockeyHorseAssignmentsRepository;
    private final RaceRefereeAssignmentsRepository raceRefereeAssignmentsRepository;
    private final RaceMapper raceMapper;

    @Override
    public List<RaceListResponse> getRacesByTournament(Integer tournamentId, String status) {
        if (!tournamentsRepository.existsById(tournamentId)) {
            throw new ResourceNotFoundException("Tournament not found");
        }

        if (status != null && !status.isBlank()) {
            return racesRepository
                    .findBySchedule_Tournaments_IdAndStatusIgnoreCaseOrderByScheduledAtAsc(tournamentId, status.trim())
                    .stream()
                    .map(this::toResponseWithCounts)
                    .toList();
        }

        return racesRepository.findBySchedule_Tournaments_IdOrderByScheduledAtAsc(tournamentId)
                .stream()
                .map(this::toResponseWithCounts)
                .toList();
    }

    private RaceListResponse toResponseWithCounts(Races race) {
        Integer raceId = race.getId();

        return raceMapper.toListResponse(
                race,
                raceRegistrationsRepository.countByRaces_Id(raceId),
                jockeyHorseAssignmentsRepository.countByRaces_IdAndStatusIgnoreCase(raceId, "accepted"),
                raceRefereeAssignmentsRepository.countByRaces_Id(raceId)
        );
    }
}
