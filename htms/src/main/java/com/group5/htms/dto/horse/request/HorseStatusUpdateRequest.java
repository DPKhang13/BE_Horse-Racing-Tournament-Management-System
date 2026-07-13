package com.group5.htms.dto.horse.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HorseStatusUpdateRequest {
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "active|inactive|retired", message = "Status must be active, inactive or retired")
    private String status;
}
