package com.group5.htms.dto.reward.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class RewardCalculateRequest {
    private BigDecimal payoutPoints;

    @NotBlank(message = "Status is required")
    private String status;

    private Integer settledById;
    private Instant settledAt;

    @NotBlank(message = "Settled type is required")
    private String settledType;
}
