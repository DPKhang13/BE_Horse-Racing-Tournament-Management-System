package com.group5.htms.dto.raceresult.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RaceResultDraftRequest {
    private Integer reportId;

    @Valid
    @NotEmpty(message = "Results are required")
    private List<RaceResultDraftItemRequest> results;
}
