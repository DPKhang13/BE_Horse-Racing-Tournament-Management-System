package com.group5.htms.dto.bet.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class BetCheckRequest {
    @NotBlank(message = "Status is required")
    private String status;

    private BigDecimal payoutPoints;
    private Integer settledById;
    private Instant settledAt;
    private String settledType;
}
