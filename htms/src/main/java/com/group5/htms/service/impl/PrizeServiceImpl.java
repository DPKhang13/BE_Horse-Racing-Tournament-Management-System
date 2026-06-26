package com.group5.htms.service.impl;

import com.group5.htms.dto.prize.request.PrizeCreateRequest;
import com.group5.htms.enums.TournamentStatus;
import com.group5.htms.dto.prize.request.PrizeItemRequest;
import com.group5.htms.dto.prize.request.PrizeUpdateRequest;
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


    private final PrizeRepository prizeRepository;
    private final TournamentsRepository tournamentsRepository;
    private final PrizeMapper prizeMapper;

    @Override
    @Transactional
    public List<PrizeResponse> createPrizes(
            Integer tournamentId,
            PrizeCreateRequest request
    ) {
        if (request == null || request.getPrizes() == null || request.getPrizes().isEmpty()) {
            throw new BadRequestException("Prize list must not be empty");
        }

        Tournaments tournament = getTournamentEntity(tournamentId);

        validateTournamentCanManagePrize(tournament);

        validatePrizeItems(request.getPrizes());
        validateDuplicateFinishPositionInRequest(request.getPrizes());
        validateDuplicateFinishPositionInDatabase(tournamentId, request.getPrizes());
        validateTotalPrizeAmountForCreate(tournament, request.getPrizes());

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
        getTournamentEntity(tournamentId);

        return prizeRepository
                .findByTournamentsIdOrderByFinishPositionAsc(tournamentId)
                .stream()
                .map(prizeMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PrizeResponse getPrizeById(Integer tournamentId, Integer prizeId) {
        return prizeMapper.toResponse(getPrizeEntity(tournamentId, prizeId));
    }

    @Override
    @Transactional
    public PrizeResponse updatePrize(
            Integer tournamentId,
            Integer prizeId,
            PrizeUpdateRequest request
    ) {
        if (request == null) {
            throw new BadRequestException("Prize update request is required");
        }

        validateUpdateRequestHasAtLeastOneField(request);

        PrizeDistributions prize = getPrizeEntity(tournamentId, prizeId);
        Tournaments tournament = prize.getTournaments();

        validateTournamentCanManagePrize(tournament);

        Integer newFinishPosition = request.getFinishPosition() == null
                ? prize.getFinishPosition()
                : request.getFinishPosition();

        BigDecimal newAmount = request.getAmount() == null
                ? prize.getAmount()
                : request.getAmount();

        validatePrizePositionOnlyTopThree(newFinishPosition);
        validatePrizeNameIfPresent(request.getPrizeName());
        validateDuplicateFinishPositionForUpdate(
                tournamentId,
                prizeId,
                newFinishPosition
        );
        validateTotalPrizeAmountForUpdate(tournament, prizeId, newAmount);

        prizeMapper.updateEntity(prize, request);

        return prizeMapper.toResponse(prizeRepository.save(prize));
    }

    @Override
    @Transactional
    public void deletePrize(Integer tournamentId, Integer prizeId) {
        PrizeDistributions prize = getPrizeEntity(tournamentId, prizeId);

        validateTournamentCanManagePrize(prize.getTournaments());

        prizeRepository.delete(prize);
    }

    private Tournaments getTournamentEntity(Integer tournamentId) {
        if (tournamentId == null) {
            throw new BadRequestException("Tournament id is required");
        }

        return tournamentsRepository.findById(tournamentId)
                .orElseThrow(() -> new BadRequestException("Tournament not found"));
    }

    private PrizeDistributions getPrizeEntity(Integer tournamentId, Integer prizeId) {
        if (prizeId == null) {
            throw new BadRequestException("Prize id is required");
        }

        getTournamentEntity(tournamentId);

        return prizeRepository.findByIdAndTournamentsId(prizeId, tournamentId)
                .orElseThrow(() -> new BadRequestException("Prize not found in this tournament"));
    }

    private void validateTournamentCanManagePrize(Tournaments tournament) {
        if (tournament == null) {
            throw new BadRequestException("Tournament not found");
        }

        String status = tournament.getStatus();

        if (status == null || !TournamentStatus.UPCOMING.getValue().equalsIgnoreCase(status.trim())) {
            throw new BadRequestException("Prize distributions can only be managed for upcoming tournaments");
        }
    }

    private void validatePrizeItems(List<PrizeItemRequest> prizes) {
        for (PrizeItemRequest prize : prizes) {
            if (prize == null) {
                throw new BadRequestException("Prize item must not be null");
            }

            validatePrizePositionOnlyTopThree(prize.getFinishPosition());
            validatePrizeNameRequired(prize.getPrizeName());
            validatePrizeAmount(prize.getAmount());
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

    private void validateTotalPrizeAmountForCreate(
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
    private void validatePrizePositionOnlyTopThree(Integer finishPosition) {
        if (finishPosition == null || finishPosition < 1 || finishPosition > 3) {
            throw new BadRequestException("Only finish positions 1, 2 and 3 can receive prizes");
        }
    }

    private void validateDuplicateFinishPositionForUpdate(
            Integer tournamentId,
            Integer prizeId,
            Integer finishPosition
    ) {
        if (prizeRepository.existsByTournamentsIdAndFinishPositionAndIdNot(
                tournamentId,
                finishPosition,
                prizeId
        )) {
            throw new BadRequestException(
                    "Prize for finish position "
                            + finishPosition
                            + " already exists in this tournament"
            );
        }
    }

    private void validateTotalPrizeAmountForUpdate(
            Tournaments tournament,
            Integer updatingPrizeId,
            BigDecimal newAmount
    ) {
        validatePrizeAmount(newAmount);

        BigDecimal tournamentPrizePool = tournament.getPrizePool() == null
                ? BigDecimal.ZERO
                : tournament.getPrizePool();

        BigDecimal otherPrizeTotal = prizeRepository
                .findByTournamentsIdOrderByFinishPositionAsc(tournament.getId())
                .stream()
                .filter(prize -> !prize.getId().equals(updatingPrizeId))
                .map(PrizeDistributions::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (otherPrizeTotal.add(newAmount).compareTo(tournamentPrizePool) > 0) {
            throw new BadRequestException("Total prize amount exceeds tournament prize pool");
        }
    }

    private void validateUpdateRequestHasAtLeastOneField(PrizeUpdateRequest request) {
        if (request.getFinishPosition() == null
                && request.getPrizeName() == null
                && request.getAmount() == null
                && request.getNote() == null) {
            throw new BadRequestException("At least one prize field is required for update");
        }
    }

    private void validatePrizeNameRequired(String prizeName) {
        if (prizeName == null || prizeName.trim().isBlank()) {
            throw new BadRequestException("Prize name is required");
        }
    }

    private void validatePrizeNameIfPresent(String prizeName) {
        if (prizeName != null && prizeName.trim().isBlank()) {
            throw new BadRequestException("Prize name must not be blank");
        }
    }

    private void validatePrizeAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Prize amount must be greater than 0");
        }

        BigDecimal normalizedAmount = amount.stripTrailingZeros();
        int scale = Math.max(normalizedAmount.scale(), 0);
        int integerDigits = Math.max(normalizedAmount.precision() - scale, 0);

        if (integerDigits > 16 || scale > 2) {
            throw new BadRequestException(
                    "Prize amount must have at most 16 integer digits and 2 decimal places"
            );
        }
    }
}


