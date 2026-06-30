package com.group5.htms.dto.admin.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserStatusUpdateRequest {
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "active|inactive", message = "Status must be active or inactive")
    private String status;
}
