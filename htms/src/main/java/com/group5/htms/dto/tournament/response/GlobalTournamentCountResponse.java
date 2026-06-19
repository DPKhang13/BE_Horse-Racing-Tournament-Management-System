package com.group5.htms.dto.tournament.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalTournamentCountResponse {

    private Long globalTournamentCount;
}
