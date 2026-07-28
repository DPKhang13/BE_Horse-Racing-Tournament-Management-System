package com.group5.htms.dto.racepointrule.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RacePointRuleItemRequest {
    @NotNull(message = "Finish position is required")
    @Min(value = 1, message = "Finish position must be greater than or equal to 1")
    private Integer finishPosition;

    @NotNull(message = "Points are required")
    @Min(value = 0, message = "Points must be greater than or equal to 0")
    private Integer points;

    @Size(max = 255, message = "Note must not exceed 255 characters")
    private String note;
}
