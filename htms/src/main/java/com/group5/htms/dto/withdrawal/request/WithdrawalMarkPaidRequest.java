package com.group5.htms.dto.withdrawal.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WithdrawalMarkPaidRequest {

    @NotBlank(message = "Bank transaction code is required")
    @Size(max = 100, message = "Bank transaction code must not exceed 100 characters")
    private String bankTransactionCode;

    @Size(max = 255, message = "Payment note must not exceed 255 characters")
    private String paymentNote;
}