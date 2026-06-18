package com.group5.htms.service.impl;

import com.group5.htms.dto.betoption.response.BetOptionResponse;
import com.group5.htms.exception.ResourceNotFoundException;
import com.group5.htms.mapper.BetOptionMapper;
import com.group5.htms.repository.BetOptionsRepository;
import com.group5.htms.service.BetOptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BetOptionServiceImpl implements BetOptionService {
    private final BetOptionsRepository betOptionsRepository;
    private final BetOptionMapper betOptionMapper;

    @Override
    public List<BetOptionResponse> getAllBetOptions() {
        return betOptionsRepository.findAll()
                .stream()
                .map(betOptionMapper::toResponse)
                .toList();
    }

    @Override
    public List<BetOptionResponse> getBetOptionsByRace(Integer raceId) {
        return betOptionsRepository.findByRaces_IdOrderByCurrentRateAsc(raceId)
                .stream()
                .map(betOptionMapper::toResponse)
                .toList();
    }

    @Override
    public BetOptionResponse getBetOptionById(Integer id) {
        return betOptionsRepository.findById(id)
                .map(betOptionMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Bet option not found"));
    }
}
