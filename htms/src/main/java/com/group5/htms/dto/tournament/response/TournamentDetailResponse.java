package com.group5.htms.dto.tournament.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentDetailResponse {

    private Integer id;

    private Integer tournamentId;

    private String name;

    private String location;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal prizePool;

    private String status;

    private Integer createdByUserId;

    private Integer createdBy;

    private String createdByUsername;

    private String createdByFullName;

    private Instant createdAt;

    private List<TournamentScheduleResponse> schedules;

    private List<TournamentPrizeResponse> prizes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TournamentScheduleResponse {
        private Integer scheduleId;
        private Integer tournamentId;
        private LocalDate raceDate;
        private Integer dayNumber;
        private String title;
        private String note;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TournamentPrizeResponse {
        private Integer prizeId;
        private Integer tournamentId;
        private Integer finishPosition;
        private String prizeName;
        private BigDecimal amount;
        private String note;
    }
}
