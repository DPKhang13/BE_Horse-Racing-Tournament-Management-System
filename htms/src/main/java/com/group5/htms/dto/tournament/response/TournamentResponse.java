package com.group5.htms.dto.tournament.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentResponse {

    private Integer id;

    private Integer tournamentId;

    private String name;

    private String location;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal prizePool;

    private String status;

    private Instant registrationOpenAt;

    private Instant registrationCloseAt;

    private Integer createdByUserId;

    private Integer createdBy;

    private String createdByUsername;

    private String createdByFullName;

    private Instant createdAt;
}
