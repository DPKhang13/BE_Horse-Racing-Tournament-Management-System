package com.group5.htms.service.impl;

import com.group5.htms.dto.jockey.response.JockeyListResponse;
import com.group5.htms.dto.jockey.response.JockeyRankingResponse;
import com.group5.htms.entity.JockeyProfiles;
import com.group5.htms.mapper.JockeyMapper;
import com.group5.htms.repository.JockeyHorseAssignmentsRepository;
import com.group5.htms.repository.JockeyProfilesRepository;
import com.group5.htms.repository.RaceResultsRepository;
import com.group5.htms.service.JockeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JockeyServiceImpl implements JockeyService {
    private final JockeyProfilesRepository jockeyProfilesRepository;
    private final JockeyHorseAssignmentsRepository jockeyHorseAssignmentsRepository;
    private final RaceResultsRepository raceResultsRepository;
    private final JockeyMapper jockeyMapper;

    @Override
    public List<JockeyListResponse> getAllJockeys(String status) {
        if (status != null && !status.isBlank()) {
            return jockeyProfilesRepository.findByStatusIgnoreCaseOrderByRankingPointsDesc(status.trim())
                    .stream()
                    .map(jockeyMapper::toListResponse)
                    .toList();
        }

        return jockeyProfilesRepository.findAllByOrderByRankingPointsDesc()
                .stream()
                .map(jockeyMapper::toListResponse)
                .toList();
    }

    @Override
    public List<JockeyRankingResponse> getJockeyRanking() {
        var jockeys = jockeyProfilesRepository
                .findAllByOrderByRankingPointsDescTotalWinsDescExperienceYearsDesc();

        return java.util.stream.IntStream.range(0, jockeys.size())
                .mapToObj(index -> toRankingResponse(jockeys.get(index), index + 1))
                .toList();
    }

    private JockeyRankingResponse toRankingResponse(JockeyProfiles jockey, Integer rank) {
        long totalRaces = jockey.getTotalRaces() == null ? 0 : jockey.getTotalRaces();
        long totalWins = jockey.getTotalWins() == null ? 0 : jockey.getTotalWins();
        double winRate = totalRaces == 0 ? 0 : Math.ceil((double) totalWins * 100 / totalRaces);

        return jockeyMapper.toRankingResponse(jockey, rank, totalRaces, winRate);
    }
}
