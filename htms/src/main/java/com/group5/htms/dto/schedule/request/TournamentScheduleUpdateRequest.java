package com.group5.htms.dto.schedule.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentScheduleUpdateRequest {

    private LocalDate raceDate;

    @Min(value = 1, message = "Day number must be greater than or equal to 1")
    private Integer dayNumber;

    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 1000, message = "Note must not exceed 1000 characters")
    private String note;
}
