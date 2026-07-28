package com.group5.htms.dto.prize.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrizeCreateRequest {

    @Valid
    @NotEmpty(message = "Prize list must not be empty")
    private List<PrizeItemRequest> prizes;
}