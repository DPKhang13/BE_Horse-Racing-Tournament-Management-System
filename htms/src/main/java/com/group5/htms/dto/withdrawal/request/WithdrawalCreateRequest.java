package com.group5.htms.dto.withdrawal.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WithdrawalCreateRequest {

    @NotNull(message = "Points amount is required")
    @DecimalMin(value = "1.00", message = "Points amount must be greater than 0")
    private BigDecimal pointsAmount;
}