package com.group5.htms.service.impl;

import com.group5.htms.dto.jockey.response.JockeyResponse;
import com.group5.htms.dto.jockey.response.JockeyRankingResponse;
import com.group5.htms.mapper.JockeyMapper;
import com.group5.htms.repository.JockeyProfilesRepository;
import com.group5.htms.service.JockeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JockeyServiceImpl implements JockeyService {
    private static final String STATUS_AVAILABLE = "available";

    private final JockeyProfilesRepository jockeyProfilesRepository;
    private final JockeyMapper jockeyMapper;

    @Override
    public List<JockeyResponse> getAllJockeys(String status) {
        if (status != null && !status.isBlank()) {
            return jockeyProfilesRepository.findByStatusIgnoreCaseOrderByRankingPointsDesc(status.trim())
                    .stream()
                    .map(jockeyMapper::toResponse)
                    .toList();
        }

        return jockeyProfilesRepository.findAllByOrderByRankingPointsDesc()
                .stream()
                .map(jockeyMapper::toResponse)
                .toList();
    }

    @Override
    public List<JockeyRankingResponse> getJockeyRanking(String status, Integer limit) {
        String normalizedStatus = status == null || status.isBlank() ? STATUS_AVAILABLE : status.trim();
        var jockeys = jockeyProfilesRepository
                .findByStatusIgnoreCaseOrderByRankingPointsDescTotalWinsDescExperienceYearsDesc(normalizedStatus);
        int maxResult = normalizeLimit(limit, jockeys.size());

        return java.util.stream.IntStream.range(0, maxResult)
                .mapToObj(index -> jockeyMapper.toRankingResponse(jockeys.get(index), index + 1))
                .toList();
    }

    private int normalizeLimit(Integer limit, int total) {
        if (limit == null || limit <= 0 || limit > total) {
            return total;
        }

        return limit;
    }
}
