package com.group5.htms.validation;

import com.group5.htms.dto.bet.request.BetUpdateRequest;
import com.group5.htms.entity.BetOptions;
import com.group5.htms.entity.Wallets;
import com.group5.htms.enums.BetStatus;
import com.group5.htms.enums.RaceStatus;
import com.group5.htms.enums.WalletStatus;
import com.group5.htms.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class BetValidator {
    public void ensureRaceOpenForBetting(BetOptions option) {
        if (option == null || option.getRaces() == null
                || !RaceStatus.OPEN_FOR_BETTING.equalsValue(option.getRaces().getStatus())) {
            throw new BadRequestException("Betting is not open for this race");
        }
    }

    public void ensurePredictionStillOpen(BetOptions option) {
        Instant closesAt = option.getRaces().getPredictionClosesAt();
        if (closesAt != null && !Instant.now().isBefore(closesAt)) {
            throw new BadRequestException("Prediction time has closed");
        }
    }

    public void ensureWalletActive(Wallets wallet) {
        if (wallet == null || !WalletStatus.ACTIVE.getValue().equalsIgnoreCase(wallet.getStatus())) {
            throw new BadRequestException("Wallet is not active");
        }
    }

    public void ensureEnoughPoints(java.math.BigDecimal balance, java.math.BigDecimal betPoints) {
        if (balance.compareTo(betPoints) < 0) {
            throw new BadRequestException("Wallet balance is not enough to place this bet");
        }
    }

    public void ensureNoBackendManagedUpdateFields(BetUpdateRequest request) {
        if (request.getUserId() != null
                || request.getBetRate() != null
                || request.getRewardPoints() != null
                || hasText(request.getStatus())
                || request.getPlacedAt() != null
                || request.getSettledAt() != null) {
            throw new BadRequestException("Bet owner, rate, reward, status and timestamps are managed by backend");
        }
    }

    public String normalizeSettlementStatus(String status) {
        if (!BetStatus.WON.getValue().equalsIgnoreCase(clean(status))
                && !BetStatus.LOST.getValue().equalsIgnoreCase(clean(status))
                && !BetStatus.CANCELLED.getValue().equalsIgnoreCase(clean(status))
                && !BetStatus.REFUNDED.getValue().equalsIgnoreCase(clean(status))) {
            throw new BadRequestException("Invalid bet settlement status");
        }
        return clean(status);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String clean(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }
}
