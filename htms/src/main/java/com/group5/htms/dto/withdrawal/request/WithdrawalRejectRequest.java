package com.group5.htms.dto.withdrawal.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WithdrawalRejectRequest {

    @NotBlank(message = "Reject reason is required")
    @Size(max = 255, message = "Reject reason must not exceed 255 characters")
    private String rejectReason;
}