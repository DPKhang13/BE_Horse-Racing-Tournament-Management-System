package com.group5.htms.dto.jockeyassignment.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Builder
@Getter
public class JockeyAssignmentListResponse {
    private Integer assignmentId;
    private Integer regId;
    private Integer raceId;
    private Integer jockeyId;
    private Integer gateNumber;
    private String status;
    private Instant invitedAt;
    private Instant respondedAt;
    private String raceName;
    private Integer raceNumber;
    private Instant scheduledAt;
    private Integer horseId;
    private String horseName;
    private String horseAvatarUrl;
    private Integer ownerId;
    private String ownerFullName;
    private String ownerStableName;
    private String jockeyFullName;
    private String jockeyAvatarUrl;
}
