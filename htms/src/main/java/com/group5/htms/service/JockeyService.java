package com.group5.htms.service;

import com.group5.htms.dto.jockey.response.JockeyRankingResponse;
import com.group5.htms.dto.jockey.response.JockeyResponse;

import java.util.List;

public interface JockeyService {
    List<JockeyResponse> getAllJockeys(String status);

    List<JockeyRankingResponse> getJockeyRanking(String status, Integer limit);
}
