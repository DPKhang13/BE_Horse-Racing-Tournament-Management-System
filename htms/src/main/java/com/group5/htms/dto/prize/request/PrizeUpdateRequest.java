package com.group5.htms.dto.prize.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrizeUpdateRequest {

    @Min(value = 1, message = "Finish position must be greater than or equal to 1")
    @Max(value = 3, message = "Only finish positions 1, 2 and 3 can receive prizes")
    private Integer finishPosition;

    @Size(max = 100, message = "Prize name must not exceed 100 characters")
    private String prizeName;

    @DecimalMin(value = "0.01", inclusive = true, message = "Prize amount must be greater than 0")
    @Digits(integer = 16, fraction = 2, message = "Prize amount must have at most 16 integer digits and 2 decimal places")
    private BigDecimal amount;

    @Size(max = 255, message = "Note must not exceed 255 characters")
    private String note;
}
