package com.group5.htms.service;

import com.group5.htms.dto.jockey.response.JockeyListResponse;
import com.group5.htms.dto.jockey.response.JockeyRankingResponse;

import java.util.List;

public interface JockeyService {
    List<JockeyListResponse> getAllJockeys(String status);

    List<JockeyRankingResponse> getJockeyRanking();
}
