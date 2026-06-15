package com.group5.htms.dto.refereeassignment.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefereeAssignmentResponse {

    private Integer id;

    private Integer refAssignId;

    private Integer raceId;

    private String raceName;

    private Integer refereeId;

    private Integer refereeUserId;

    private String refereeUsername;

    private String refereeFullName;

    private String refereeRole;

    private Instant assignedAt;
}
