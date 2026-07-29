package com.group5.htms.service.impl;

import com.group5.htms.dto.racepointrule.request.RacePointRuleItemRequest;
import com.group5.htms.entity.Races;
import com.group5.htms.repository.RacePointRulesRepository;
import com.group5.htms.repository.RacesRepository;
import com.group5.htms.validation.RacePointRuleValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RacePointRuleServiceImplTest {
    @Mock
    private RacesRepository racesRepository;

    @Mock
    private RacePointRulesRepository racePointRulesRepository;

    @Mock
    private RacePointRuleValidator racePointRuleValidator;

    @Test
    void replacePointRulesFlushesDeletedRulesBeforeSavingReplacementRules() {
        Races race = Races.builder().id(54).build();
        List<RacePointRuleItemRequest> request = List.of(
                RacePointRuleItemRequest.builder().finishPosition(1).points(15).build()
        );
        RacePointRuleServiceImpl service = new RacePointRuleServiceImpl(
                racesRepository,
                racePointRulesRepository,
                racePointRuleValidator
        );
        when(racesRepository.findById(54)).thenReturn(Optional.of(race));
        when(racePointRulesRepository.findByRace_IdOrderByFinishPositionAsc(54)).thenReturn(List.of());

        service.replacePointRules(54, request);

        InOrder inOrder = inOrder(racePointRulesRepository);
        inOrder.verify(racePointRulesRepository).deleteByRace_Id(54);
        inOrder.verify(racePointRulesRepository).flush();
        inOrder.verify(racePointRulesRepository).save(org.mockito.ArgumentMatchers.any());
        verify(racePointRulesRepository).findByRace_IdOrderByFinishPositionAsc(54);
    }
}
