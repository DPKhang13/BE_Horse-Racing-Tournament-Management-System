package com.group5.htms.dto.withdrawal.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WithdrawalCreateRequest {

    @NotNull(message = "Points amount is required")
    @DecimalMin(value = "1.00", message = "Points amount must be greater than 0")
    private BigDecimal pointsAmount;

    @NotBlank(message = "Bank name is required")
    @Size(max = 100, message = "Bank name must not exceed 100 characters")
    private String bankName;

    @NotBlank(message = "Bank account number is required")
    @Size(max = 50, message = "Bank account number must not exceed 50 characters")
    private String bankAccountNumber;

    @NotBlank(message = "Bank account name is required")
    @Size(max = 100, message = "Bank account name must not exceed 100 characters")
    private String bankAccountName;
}