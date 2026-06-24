package com.group5.htms.dto.race.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RaceStartRequest {
    private Boolean forceCloseBetting;

    @Size(max = 1000, message = "Note must not exceed 1000 characters")
    private String note;

    public boolean isForceCloseBetting() {
        return Boolean.TRUE.equals(forceCloseBetting);
    }
}
