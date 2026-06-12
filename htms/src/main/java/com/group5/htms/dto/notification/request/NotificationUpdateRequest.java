package com.group5.htms.dto.notification.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class NotificationUpdateRequest {
    @Schema(hidden = true)
    private Integer userId;

    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    private String message;

    @Size(max = 50, message = "Type must not exceed 50 characters")
    private String type;

    private Integer refId;

    @Size(max = 50, message = "Ref type must not exceed 50 characters")
    private String refType;

    private Boolean isRead;
    private Instant createdAt;
}
