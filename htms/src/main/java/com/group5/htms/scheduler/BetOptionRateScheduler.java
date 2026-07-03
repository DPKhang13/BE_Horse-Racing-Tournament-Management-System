package com.group5.htms.scheduler;

import com.group5.htms.enums.RaceStatus;
import com.group5.htms.repository.RacesRepository;
import com.group5.htms.service.BetOptionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BetOptionRateScheduler {
    private static final Logger log = LoggerFactory.getLogger(BetOptionRateScheduler.class);

    private final RacesRepository racesRepository;
    private final BetOptionService betOptionService;

    @Scheduled(fixedDelay = 60_000)
    public void refreshOpenBettingRates() {
        racesRepository.findByStatusIgnoreCaseOrderByScheduledAtAsc(RaceStatus.OPEN_FOR_BETTING.getValue())
                .forEach(race -> {
                    try {
                        betOptionService.recalculateRatesForRace(race.getId());
                    } catch (Exception ex) {
                        log.warn("Failed to refresh bet option rates for race {}", race.getId(), ex);
                    }
                });
    }
}
