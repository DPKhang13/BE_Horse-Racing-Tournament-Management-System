package com.group5.htms.dto.betoption.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BetOptionRateUpdateRequest {

    @NotNull(message = "Current rate is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Current rate must be greater than 0")
    private BigDecimal currentRate;
}
