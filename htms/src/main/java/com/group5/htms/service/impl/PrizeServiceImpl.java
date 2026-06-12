package com.group5.htms.service.impl;

import com.group5.htms.dto.prize.request.PrizeCreateRequest;
import com.group5.htms.dto.prize.request.PrizeItemRequest;
import com.group5.htms.dto.prize.response.PrizeResponse;
import com.group5.htms.entity.PrizeDistributions;
import com.group5.htms.entity.Tournaments;
import com.group5.htms.exception.BadRequestException;
import com.group5.htms.mapper.PrizeMapper;
import com.group5.htms.repository.PrizeRepository;
import com.group5.htms.repository.TournamentsRepository;
import com.group5.htms.service.PrizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PrizeServiceImpl implements PrizeService {

    private static final String TOURNAMENT_STATUS_UPCOMING = "upcoming";

    private final PrizeRepository prizeRepository;
    private final TournamentsRepository tournamentsRepository;
    private final PrizeMapper prizeMapper;

    @Override
    @Transactional
    public List<PrizeResponse> createPrizes(
            Integer tournamentId,
            PrizeCreateRequest request
    ) {
        if (tournamentId == null) {
            throw new BadRequestException("Tournament id is required");
        }

        if (request == null || request.getPrizes() == null || request.getPrizes().isEmpty()) {
            throw new BadRequestException("Prize list must not be empty");
        }

        Tournaments tournament = tournamentsRepository.findById(tournamentId)
                .orElseThrow(() -> new BadRequestException("Tournament not found"));

        validateTournamentCanCreatePrize(tournament);

        validatePrizePositionsOnlyTopThree(request.getPrizes());

        validateDuplicateFinishPositionInRequest(request.getPrizes());

        validateDuplicateFinishPositionInDatabase(tournamentId, request.getPrizes());

        validateTotalPrizeAmount(tournament, request.getPrizes());

        List<PrizeDistributions> prizes = request.getPrizes()
                .stream()
                .map(item -> {
                    PrizeDistributions prize = prizeMapper.toEntity(item, tournamentId);

                    /*
                     * Mapper dùng object shell, nhưng service đã fetch tournament thật rồi
                     * nên set lại entity thật để response có tournamentName và relation chắc chắn đúng.
                     */
                    prize.setTournaments(tournament);

                    return prize;
                })
                .toList();

        List<PrizeDistributions> savedPrizes = prizeRepository.saveAll(prizes);

        return savedPrizes.stream()
                .map(prizeMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrizeResponse> getPrizesByTournament(Integer tournamentId) {
        if (tournamentId == null) {
            throw new BadRequestException("Tournament id is required");
        }

        if (!tournamentsRepository.existsById(tournamentId)) {
            throw new BadRequestException("Tournament not found");
        }

        return prizeRepository
                .findByTournamentsIdOrderByFinishPositionAsc(tournamentId)
                .stream()
                .map(prizeMapper::toResponse)
                .toList();
    }

    private void validateTournamentCanCreatePrize(Tournaments tournament) {
        if (tournament == null) {
            throw new BadRequestException("Tournament not found");
        }

        String status = tournament.getStatus();

        if (status == null || !TOURNAMENT_STATUS_UPCOMING.equalsIgnoreCase(status.trim())) {
            throw new BadRequestException("Prize distributions can only be created for upcoming tournaments");
        }
    }

    private void validateDuplicateFinishPositionInRequest(
            List<PrizeItemRequest> prizes
    ) {
        Set<Integer> positions = new HashSet<>();

        for (PrizeItemRequest prize : prizes) {
            if (!positions.add(prize.getFinishPosition())) {
                throw new BadRequestException(
                        "Duplicate finish position in request: " + prize.getFinishPosition()
                );
            }
        }
    }

    private void validateDuplicateFinishPositionInDatabase(
            Integer tournamentId,
            List<PrizeItemRequest> prizes
    ) {
        for (PrizeItemRequest prize : prizes) {
            boolean exists = prizeRepository
                    .existsByTournamentsIdAndFinishPosition(
                            tournamentId,
                            prize.getFinishPosition()
                    );

            if (exists) {
                throw new BadRequestException(
                        "Prize for finish position "
                                + prize.getFinishPosition()
                                + " already exists in this tournament"
                );
            }
        }
    }

    private void validateTotalPrizeAmount(
            Tournaments tournament,
            List<PrizeItemRequest> newPrizes
    ) {
        BigDecimal tournamentPrizePool = tournament.getPrizePool() == null
                ? BigDecimal.ZERO
                : tournament.getPrizePool();

        BigDecimal existingTotal = prizeRepository
                .findByTournamentsIdOrderByFinishPositionAsc(tournament.getId())
                .stream()
                .map(PrizeDistributions::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal newTotal = newPrizes.stream()
                .map(PrizeItemRequest::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal finalTotal = existingTotal.add(newTotal);

        if (finalTotal.compareTo(tournamentPrizePool) > 0) {
            throw new BadRequestException(
                    "Total prize amount exceeds tournament prize pool"
            );
        }
    }
    private void validatePrizePositionsOnlyTopThree(
            List<PrizeItemRequest> prizes
    ) {
        for (PrizeItemRequest prize : prizes) {
            Integer finishPosition = prize.getFinishPosition();

            if (finishPosition == null || finishPosition < 1 || finishPosition > 3) {
                throw new BadRequestException("Only finish positions 1, 2 and 3 can receive prizes");
            }
        }
    }
}