package com.group5.htms.dto.notification.response;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Builder
@Getter
public class NotificationResponse {
    private Integer id;
    private Integer userId;
    private String title;
    private String message;
    private String type;
    private Integer refId;
    private String refType;
    private Boolean isRead;
    private Instant createdAt;
}
