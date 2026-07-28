package com.group5.htms.dto.dashboard.response;

import com.group5.htms.dto.bet.response.BetListResponse;
import com.group5.htms.dto.notification.response.NotificationListResponse;
import com.group5.htms.dto.raceresult.response.RaceResultListResponse;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Builder
@Getter
public class SpectatorDashboardResponse {
    private WalletSummary wallet;
    private SummaryCount summaryCount;
    private List<UpcomingRaceItem> upcomingRaces;
    private List<BetListResponse> activeBets;
    private List<RaceResultListResponse> latestResults;
    private List<NotificationListResponse> notifications;
    private List<PredictionRaceResponse> openPredictionRaces;

    @Builder
    @Getter
    public static class WalletSummary {
        private Integer walletId;
        private BigDecimal pointBalance;
        private String status;
        private Instant createdAt;
    }

    @Builder
    @Getter
    public static class SummaryCount {
        private long activeBetCount;
        private long settledBetCount;
        private long unreadNotificationCount;
        private long upcomingRaceCount;
        private long openPredictionRaceCount;
    }

    @Builder
    @Getter
    public static class UpcomingRaceItem {
        private Integer raceId;
        private String raceName;
        private Integer raceNumber;
        private Instant scheduledAt;
        private Instant predictionClosesAt;
        private Double distanceM;
        private String trackType;
        private String status;
        private Integer tournamentId;
        private String tournamentName;
        private String location;
    }
}
