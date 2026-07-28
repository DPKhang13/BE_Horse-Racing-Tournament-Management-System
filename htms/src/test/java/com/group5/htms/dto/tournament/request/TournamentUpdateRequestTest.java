package com.group5.htms.dto.tournament.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TournamentUpdateRequestTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void ignoresStatusWhenDeserializingTournamentUpdateRequest() throws Exception {
        TournamentUpdateRequest request = objectMapper.readValue(
                "{\"prizePool\":2000000,\"status\":\"upcoming\"}",
                TournamentUpdateRequest.class
        );

        assertThat(request.getPrizePool()).isEqualByComparingTo(new BigDecimal("2000000"));
    }
}
