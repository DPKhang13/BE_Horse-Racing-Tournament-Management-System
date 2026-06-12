package com.group5.htms.mapper;

import com.group5.htms.dto.bet.request.BetCreateRequest;
import com.group5.htms.dto.bet.request.BetUpdateRequest;
import com.group5.htms.dto.bet.response.BetResponse;
import com.group5.htms.entity.BetOptions;
import com.group5.htms.entity.Bets;
import com.group5.htms.entity.Users;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

@Component
public class BetMapper {
    public Bets toEntity(BetCreateRequest request) {
        return Bets.builder()
                .users(toUser(request.getUserId()))
                .option(toOption(request.getOptionId()))
                .betType(request.getBetType() == null || request.getBetType())
                .betPoints(request.getBetPoints())
                .betRate(request.getBetRate())
                .rewardPoints(defaultBigDecimal(request.getRewardPoints()))
                .status(defaultText(request.getStatus(), "pending"))
                .placedAt(defaultInstant(request.getPlacedAt()))
                .settledAt(request.getSettledAt())
                .build();
    }

    public void updateBet(Bets bet, BetUpdateRequest request) {
        if (request.getUserId() != null) {
            bet.setUsers(toUser(request.getUserId()));
        }
        if (request.getOptionId() != null) {
            bet.setOption(toOption(request.getOptionId()));
        }
        if (request.getBetType() != null) {
            bet.setBetType(request.getBetType());
        }
        if (request.getBetPoints() != null) {
            bet.setBetPoints(request.getBetPoints());
        }
        if (request.getBetRate() != null) {
            bet.setBetRate(request.getBetRate());
        }
        if (request.getRewardPoints() != null) {
            bet.setRewardPoints(request.getRewardPoints());
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
    }

    public BetResponse toResponse(Bets bet) {
        return BetResponse.builder()
                .id(bet.getId())
                .userId(bet.getUsers().getId())
                .optionId(bet.getOption().getId())
                .betType(bet.getBetType())
                .betPoints(bet.getBetPoints())
                .betRate(bet.getBetRate())
                .rewardPoints(bet.getRewardPoints())
                .status(bet.getStatus())
                .placedAt(bet.getPlacedAt())
                .settledAt(bet.getSettledAt())
                .build();
    }

    private Users toUser(Integer id) {
        Users user = new Users();
        user.setId(id);
        return user;
    }

    private BetOptions toOption(Integer id) {
        BetOptions option = new BetOptions();
        option.setId(id);
        return option;
    }

    private BigDecimal defaultBigDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String defaultText(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private Instant defaultInstant(Instant value) {
        return value == null ? Instant.now() : value;
    }
}
