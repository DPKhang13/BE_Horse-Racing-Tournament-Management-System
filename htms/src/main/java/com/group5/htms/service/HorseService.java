package com.group5.htms.service;

import com.group5.htms.dto.horse.request.HorseCreateRequest;
import com.group5.htms.dto.horse.request.HorseUpdateRequest;
import com.group5.htms.dto.horse.response.HorseCountResponse;
import com.group5.htms.dto.horse.response.HorseListResponse;
import com.group5.htms.dto.horse.response.HorseRankingResponse;
import com.group5.htms.dto.horse.response.HorseResponse;

import java.util.List;

public interface HorseService {
    HorseCountResponse getHorseCount();

    List<HorseListResponse> getAllHorses();

    List<HorseRankingResponse> getHorseRanking();

    HorseResponse getHorseById(Integer id);

    HorseResponse createHorse(HorseCreateRequest request);

    HorseResponse updateHorse(Integer id, HorseUpdateRequest request);

    void deleteHorse(Integer id);
}
