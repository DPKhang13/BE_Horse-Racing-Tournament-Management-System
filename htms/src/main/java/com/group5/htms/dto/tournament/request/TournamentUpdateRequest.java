package com.group5.htms.dto.tournament.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentUpdateRequest {

    @Size(max = 200, message = "Tournament name must not exceed 200 characters")
    private String name;

    @Size(max = 255, message = "Location must not exceed 255 characters")
    private String location;

    private LocalDate startDate;

    private LocalDate endDate;

    @DecimalMin(value = "0.0", inclusive = true, message = "Prize pool must be greater than or equal to 0")
    private BigDecimal prizePool;

    @Size(max = 20, message = "Status must not exceed 20 characters")
    private String status;
}