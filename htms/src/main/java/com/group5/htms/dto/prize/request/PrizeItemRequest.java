package com.group5.htms.dto.prize.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrizeItemRequest {

    @NotNull(message = "Finish position is required")
    @Min(value = 1, message = "Finish position must be greater than or equal to 1")
    @Max(value = 3, message = "Only finish positions 1, 2 and 3 can receive prizes")
    private Integer finishPosition;

    @NotBlank(message = "Prize name is required")
    @Size(max = 100, message = "Prize name must not exceed 100 characters")
    private String prizeName;

    @NotNull(message = "Prize amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Prize amount must be greater than 0")
    private BigDecimal amount;

    @Size(max = 1000, message = "Note must not exceed 1000 characters")
    private String note;
}