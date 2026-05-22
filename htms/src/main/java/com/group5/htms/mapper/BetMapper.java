package com.group5.htms.mapper;

import com.group5.htms.dto.bet.request.BetCreateRequest;
import com.group5.htms.dto.bet.request.BetUpdateRequest;
import com.group5.htms.dto.bet.response.BetResponse;
import com.group5.htms.entity.Bets;
import com.group5.htms.entity.JockeyHorseAssignments;
import com.group5.htms.entity.Roles;
import com.group5.htms.entity.Users;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

@Component
public class BetMapper {
    public Bets toEntity(BetCreateRequest request) {
        BigDecimal potentialPayout = request.getPotentialPayoutPoints() == null
                ? request.getStakePoints().multiply(request.getOddsDecimal())
                : request.getPotentialPayoutPoints();

        return Bets.builder()
                .spectatorRoles(toRole(request.getSpectatorRoleId()))
                .assignment(toAssignment(request.getAssignmentId()))
                .marketType(defaultText(request.getMarketType(), "winner"))
                .predictedPosition(request.getPredictedPosition())
                .stakePoints(request.getStakePoints())
                .oddsDecimal(request.getOddsDecimal())
                .potentialPayoutPoints(potentialPayout)
                .payoutPoints(defaultBigDecimal(request.getPayoutPoints()))
                .status(defaultText(request.getStatus(), "pending"))
                .placedAt(defaultInstant(request.getPlacedAt()))
                .settledAt(request.getSettledAt())
                .settledBy(toNullableUser(request.getSettledById()))
                .settledType(trim(request.getSettledType()))
                .build();
    }

    public void updateBet(Bets bet, BetUpdateRequest request) {
        if (request.getSpectatorRoleId() != null) {
            bet.setSpectatorRoles(toRole(request.getSpectatorRoleId()));
        }
        if (request.getAssignmentId() != null) {
            bet.setAssignment(toAssignment(request.getAssignmentId()));
        }
        if (request.getMarketType() != null && !request.getMarketType().isBlank()) {
            bet.setMarketType(request.getMarketType().trim());
        }
        if (request.getPredictedPosition() != null) {
            bet.setPredictedPosition(request.getPredictedPosition());
        }
        if (request.getStakePoints() != null) {
            bet.setStakePoints(request.getStakePoints());
        }
        if (request.getOddsDecimal() != null) {
            bet.setOddsDecimal(request.getOddsDecimal());
        }
        if (request.getPotentialPayoutPoints() != null) {
            bet.setPotentialPayoutPoints(request.getPotentialPayoutPoints());
        }
        if (request.getPayoutPoints() != null) {
            bet.setPayoutPoints(request.getPayoutPoints());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            bet.setStatus(request.getStatus().trim());
        }
        if (request.getPlacedAt() != null) {
            bet.setPlacedAt(request.getPlacedAt());
        }
        if (request.getSettledAt() != null) {
            bet.setSettledAt(request.getSettledAt());
        }
        if (request.getSettledById() != null) {
            bet.setSettledBy(toUser(request.getSettledById()));
        }
        if (request.getSettledType() != null) {
            bet.setSettledType(request.getSettledType().trim());
        }
    }

    public BetResponse toResponse(Bets bet) {
        return BetResponse.builder()
                .id(bet.getId())
                .spectatorRoleId(bet.getSpectatorRoles().getId())
                .assignmentId(bet.getAssignment().getId())
                .marketType(bet.getMarketType())
                .predictedPosition(bet.getPredictedPosition())
                .stakePoints(bet.getStakePoints())
                .oddsDecimal(bet.getOddsDecimal())
                .potentialPayoutPoints(bet.getPotentialPayoutPoints())
                .payoutPoints(bet.getPayoutPoints())
                .status(bet.getStatus())
                .placedAt(bet.getPlacedAt())
                .settledAt(bet.getSettledAt())
                .settledById(bet.getSettledBy() == null ? null : bet.getSettledBy().getId())
                .settledType(bet.getSettledType())
                .build();
    }

    private Roles toRole(Integer id) {
        Roles role = new Roles();
        role.setId(id);
        return role;
    }

    private JockeyHorseAssignments toAssignment(Integer id) {
        JockeyHorseAssignments assignment = new JockeyHorseAssignments();
        assignment.setId(id);
        return assignment;
    }

    private Users toUser(Integer id) {
        Users user = new Users();
        user.setId(id);
        return user;
    }

    private Users toNullableUser(Integer id) {
        return id == null ? null : toUser(id);
    }

    private BigDecimal defaultBigDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String defaultText(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private Instant defaultInstant(Instant value) {
        return value == null ? Instant.now() : value;
    }
}
