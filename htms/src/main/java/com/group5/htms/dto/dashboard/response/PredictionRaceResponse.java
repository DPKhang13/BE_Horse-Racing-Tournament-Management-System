package com.group5.htms.dto.dashboard.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Builder
@Getter
public class PredictionRaceResponse {
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
    private List<OptionItem> options;

    @Builder
    @Getter
    public static class OptionItem {
        private Integer optionId;
        private Integer assignmentId;
        private Integer horseId;
        private String horseName;
        private String horseAvatarUrl;
        private Integer jockeyId;
        private String jockeyFullName;
        private Integer gateNumber;
        private BigDecimal currentRate;
        private BigDecimal totalBetPoints;
        private Integer totalBetCount;
        private Instant updatedAt;
    }
}
