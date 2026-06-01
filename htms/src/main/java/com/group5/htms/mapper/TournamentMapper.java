package com.group5.htms.mapper;

import com.group5.htms.dto.tournament.request.TournamentCreateRequest;
import com.group5.htms.dto.tournament.request.TournamentUpdateRequest;
import com.group5.htms.dto.tournament.response.TournamentResponse;
import com.group5.htms.entity.Tournaments;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

@Component
public class TournamentMapper {

    private static final String DEFAULT_STATUS = "upcoming";

    public Tournaments toEntity(TournamentCreateRequest request) {
        if (request == null) {
            return null;
        }

        return Tournaments.builder()
                .name(clean(request.getName()))
                .location(clean(request.getLocation()))
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .prizePool(defaultPrizePool(request.getPrizePool()))
                .status(defaultStatus(request.getStatus()))
                .createdAt(Instant.now())
                .build();
    }

    public void updateEntity(Tournaments tournament, TournamentUpdateRequest request) {
        if (tournament == null || request == null) {
            return;
        }

        if (hasText(request.getName())) {
            tournament.setName(clean(request.getName()));
        }

        if (hasText(request.getLocation())) {
            tournament.setLocation(clean(request.getLocation()));
        }

        if (request.getStartDate() != null) {
            tournament.setStartDate(request.getStartDate());
        }

        if (request.getEndDate() != null) {
            tournament.setEndDate(request.getEndDate());
        }

        if (request.getPrizePool() != null) {
            tournament.setPrizePool(request.getPrizePool());
        }

        if (hasText(request.getStatus())) {
            tournament.setStatus(clean(request.getStatus()).toLowerCase());
        }
    }

    public TournamentResponse toResponse(Tournaments tournament) {
        if (tournament == null) {
            return null;
        }

        return TournamentResponse.builder()
                .id(tournament.getId())
                .name(tournament.getName())
                .location(tournament.getLocation())
                .startDate(tournament.getStartDate())
                .endDate(tournament.getEndDate())
                .prizePool(defaultPrizePool(tournament.getPrizePool()))
                .status(tournament.getStatus())
                .createdByUserId(
                        tournament.getCreatedBy() != null
                                ? tournament.getCreatedBy().getId()
                                : null
                )
                .createdByUsername(
                        tournament.getCreatedBy() != null
                                ? tournament.getCreatedBy().getUsername()
                                : null
                )
                .createdByFullName(
                        tournament.getCreatedBy() != null
                                ? tournament.getCreatedBy().getFullName()
                                : null
                )
                .createdAt(tournament.getCreatedAt())
                .build();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isBlank();
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }

        String cleaned = value.trim();

        return cleaned.isBlank() ? null : cleaned;
    }

    private BigDecimal defaultPrizePool(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String defaultStatus(String value) {
        return value == null || value.isBlank()
                ? DEFAULT_STATUS
                : value.trim().toLowerCase();
    }
}