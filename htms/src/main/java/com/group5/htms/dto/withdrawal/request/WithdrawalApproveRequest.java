package com.group5.htms.dto.withdrawal.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WithdrawalApproveRequest {

    @NotBlank(message = "Payout location is required")
    @Size(max = 255, message = "Payout location must not exceed 255 characters")
    private String payoutLocation;

    @NotBlank(message = "Payout counter is required")
    @Size(max = 100, message = "Payout counter must not exceed 100 characters")
    private String payoutCounter;
}