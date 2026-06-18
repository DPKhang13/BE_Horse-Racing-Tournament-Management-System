package com.group5.htms.mapper;

import com.group5.htms.dto.prize.request.PrizeItemRequest;
import com.group5.htms.dto.prize.request.PrizeUpdateRequest;
import com.group5.htms.dto.prize.response.PrizeResponse;
import com.group5.htms.entity.PrizeDistributions;
import com.group5.htms.entity.Tournaments;
import org.springframework.stereotype.Component;

@Component
public class PrizeMapper {

    public PrizeDistributions toEntity(
            PrizeItemRequest request,
            Integer tournamentId
    ) {
        if (request == null) {
            return null;
        }

        return PrizeDistributions.builder()
                .tournaments(toTournamentShell(tournamentId))
                .finishPosition(request.getFinishPosition())
                .prizeName(clean(request.getPrizeName()))
                .amount(request.getAmount())
                .note(clean(request.getNote()))
                .build();
    }

    public PrizeResponse toResponse(PrizeDistributions prize) {
        if (prize == null) {
            return null;
        }

        Tournaments tournament = prize.getTournaments();

        return PrizeResponse.builder()
                .id(prize.getId())
                .prizeId(prize.getId())
                .tournamentId(tournament != null ? tournament.getId() : null)
                .tournamentName(tournament != null ? tournament.getName() : null)
                .tournamentStatus(tournament != null ? tournament.getStatus() : null)
                .prizePool(tournament != null ? tournament.getPrizePool() : null)
                .finishPosition(prize.getFinishPosition())
                .prizeName(prize.getPrizeName())
                .amount(prize.getAmount())
                .note(prize.getNote())
                .build();
    }

    public void updateEntity(PrizeDistributions prize, PrizeUpdateRequest request) {
        if (prize == null || request == null) {
            return;
        }

        if (request.getFinishPosition() != null) {
            prize.setFinishPosition(request.getFinishPosition());
        }

        if (request.getPrizeName() != null) {
            prize.setPrizeName(clean(request.getPrizeName()));
        }

        if (request.getAmount() != null) {
            prize.setAmount(request.getAmount());
        }

        if (request.getNote() != null) {
            prize.setNote(clean(request.getNote()));
        }
    }

    private Tournaments toTournamentShell(Integer tournamentId) {
        if (tournamentId == null) {
            return null;
        }

        Tournaments tournament = new Tournaments();
        tournament.setId(tournamentId);
        return tournament;
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }

        String cleaned = value.trim();

        return cleaned.isBlank() ? null : cleaned;
    }
}
