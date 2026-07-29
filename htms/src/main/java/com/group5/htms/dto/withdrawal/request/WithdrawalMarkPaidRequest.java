package com.group5.htms.dto.withdrawal.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WithdrawalMarkPaidRequest {

    @NotBlank(message = "Pickup code is required")
    @Size(max = 20, message = "Pickup code must not exceed 20 characters")
    private String pickupCode;

    @Size(max = 255, message = "Payment note must not exceed 255 characters")
    private String paymentNote;
}