package com.group5.htms.service.impl;

import com.group5.htms.dto.prize.response.PrizeAwardResponse;
import com.group5.htms.entity.HorseOwnerProfiles;
import com.group5.htms.entity.Horses;
import com.group5.htms.entity.PrizeAwards;
import com.group5.htms.entity.PrizeDistributions;
import com.group5.htms.entity.RaceResults;
import com.group5.htms.entity.Races;
import com.group5.htms.entity.Tournaments;
import com.group5.htms.entity.Users;
import com.group5.htms.enums.PrizeAwardStatus;
import com.group5.htms.enums.RaceResultStatus;
import com.group5.htms.enums.TournamentStatus;
import com.group5.htms.mapper.PrizeAwardMapper;
import com.group5.htms.repository.PrizeAwardsRepository;
import com.group5.htms.repository.PrizeRepository;
import com.group5.htms.repository.RaceResultsRepository;
import com.group5.htms.repository.TournamentsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrizeAwardServiceImplTest {

    @Mock
    private TournamentsRepository tournamentsRepository;

    @Mock
    private PrizeRepository prizeRepository;

    @Mock
    private PrizeAwardsRepository prizeAwardsRepository;

    @Mock
    private RaceResultsRepository raceResultsRepository;

    @Mock
    private PrizeAwardMapper prizeAwardMapper;

    @Captor
    private ArgumentCaptor<Iterable<PrizeAwards>> awardsCaptor;

    private PrizeAwardServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PrizeAwardServiceImpl(
                tournamentsRepository,
                prizeRepository,
                prizeAwardsRepository,
                raceResultsRepository,
                prizeAwardMapper
        );
    }

    @Test
    void awardTournamentPrizesCreatesExternalAwardRecordsWithoutWalletCredit() {
        Tournaments tournament = tournament();
        when(tournamentsRepository.findById(1)).thenReturn(Optional.of(tournament));
        when(prizeAwardsRepository.existsByTournaments_Id(1)).thenReturn(false);
        when(prizeRepository.findByTournamentsIdOrderByFinishPositionAsc(1)).thenReturn(List.of(
                prize(1, "300.00"),
                prize(2, "200.00"),
                prize(3, "100.00")
        ));
        when(raceResultsRepository.findPublishedResultsByTournamentId(1, RaceResultStatus.PUBLISHED.getValue()))
                .thenReturn(List.of(
                        result(1, 101, 201, 10, 1),
                        result(2, 102, 202, 7, 2),
                        result(3, 103, 203, 5, 3)
                ));
        when(prizeAwardsRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(prizeAwardMapper.toResponse(any(PrizeAwards.class))).thenAnswer(invocation -> {
            PrizeAwards award = invocation.getArgument(0);
            return PrizeAwardResponse.builder()
                    .awardId(award.getId())
                    .status(award.getStatus())
                    .build();
        });

        List<PrizeAwardResponse> response = service.awardTournamentPrizes(1);

        verify(prizeAwardsRepository).saveAll(awardsCaptor.capture());
        List<PrizeAwards> awards = StreamSupport.stream(
                awardsCaptor.getValue().spliterator(),
                false
        ).toList();
        assertThat(awards).hasSize(3);
        assertThat(awards)
                .extracting(PrizeAwards::getFinishPosition)
                .containsExactly(1, 2, 3);
        assertThat(awards)
                .extracting(PrizeAwards::getStatus)
                .containsOnly(PrizeAwardStatus.ANNOUNCED.getValue());
        assertThat(response)
                .extracting(PrizeAwardResponse::getStatus)
                .containsOnly(PrizeAwardStatus.ANNOUNCED.getValue());
    }

    @Test
    void markPrizeAwardedMarksAnAnnouncedExternalAward() {
        Tournaments tournament = tournament();
        PrizeAwards award = PrizeAwards.builder()
                .id(10)
                .tournaments(tournament)
                .status(PrizeAwardStatus.ANNOUNCED.getValue())
                .build();
        PrizeAwardResponse expected = PrizeAwardResponse.builder()
                .awardId(10)
                .status(PrizeAwardStatus.AWARDED.getValue())
                .build();

        when(tournamentsRepository.findById(1)).thenReturn(Optional.of(tournament));
        when(prizeAwardsRepository.findById(10)).thenReturn(Optional.of(award));
        when(prizeAwardsRepository.save(award)).thenReturn(award);
        when(prizeAwardMapper.toResponse(award)).thenReturn(expected);

        PrizeAwardResponse response = service.markPrizeAwarded(1, 10);

        assertThat(response).isSameAs(expected);
        assertThat(award.getStatus()).isEqualTo(PrizeAwardStatus.AWARDED.getValue());
        assertThat(award.getAwardedAt()).isNotNull();
        verify(prizeAwardsRepository).save(award);
    }

    private Tournaments tournament() {
        return Tournaments.builder()
                .id(1)
                .prizePool(new BigDecimal("600.00"))
                .status(TournamentStatus.COMPLETED.getValue())
                .build();
    }

    private PrizeDistributions prize(Integer position, String amount) {
        return PrizeDistributions.builder()
                .id(position)
                .finishPosition(position)
                .amount(new BigDecimal(amount))
                .build();
    }

    private RaceResults result(
            Integer resultId,
            Integer horseId,
            Integer ownerId,
            Integer points,
            Integer finishPosition
    ) {
        HorseOwnerProfiles owner = HorseOwnerProfiles.builder()
                .id(ownerId)
                .users(Users.builder().id(ownerId).fullName("Owner " + ownerId).build())
                .build();

        return RaceResults.builder()
                .id(resultId)
                .races(Races.builder().id(resultId).build())
                .horses(Horses.builder().id(horseId).name("Horse " + horseId).build())
                .owner(owner)
                .finishPosition(finishPosition)
                .pointsAwarded(points)
                .isDisqualified(false)
                .status(RaceResultStatus.PUBLISHED.getValue())
                .build();
    }
}
