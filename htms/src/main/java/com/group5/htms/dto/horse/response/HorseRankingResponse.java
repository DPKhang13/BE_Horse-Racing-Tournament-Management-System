package com.group5.htms.dto.horse.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HorseRankingResponse {
    private Integer rank;
    private Integer id;
    private String name;
    private String breed;
    private String rankGroup;
    private Integer rankingPoints;
    private Integer totalWins;
    private String avatarUrl;
    private String status;
}
