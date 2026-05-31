package com.group5.htms.mapper;

import com.group5.htms.dto.notification.request.NotificationCreateRequest;
import com.group5.htms.dto.notification.request.NotificationUpdateRequest;
import com.group5.htms.dto.notification.response.NotificationResponse;
import com.group5.htms.entity.Notifications;
import com.group5.htms.entity.Users;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class NotificationMapper {
    public Notifications toEntity(NotificationCreateRequest request) {
        return Notifications.builder()
                .users(toUser(request.getUserId()))
                .title(request.getTitle().trim())
                .message(trim(request.getMessage()))
                .type(request.getType().trim())
                .refId(request.getRefId())
                .refType(trim(request.getRefType()))
                .isRead(request.getIsRead() != null && request.getIsRead())
                .createdAt(request.getCreatedAt() == null ? Instant.now() : request.getCreatedAt())
                .build();
    }

    public void updateNotification(Notifications notification, NotificationUpdateRequest request) {
        if (request.getUserId() != null) {
            notification.setUsers(toUser(request.getUserId()));
        }
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            notification.setTitle(request.getTitle().trim());
        }
        if (request.getMessage() != null) {
            notification.setMessage(request.getMessage().trim());
        }
        if (request.getType() != null && !request.getType().isBlank()) {
            notification.setType(request.getType().trim());
        }
        if (request.getRefId() != null) {
            notification.setRefId(request.getRefId());
        }
        if (request.getRefType() != null) {
            notification.setRefType(request.getRefType().trim());
        }
        if (request.getIsRead() != null) {
            notification.setIsRead(request.getIsRead());
        }
        if (request.getCreatedAt() != null) {
            notification.setCreatedAt(request.getCreatedAt());
        }
    }

    public NotificationResponse toResponse(Notifications notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUsers().getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .refId(notification.getRefId())
                .refType(notification.getRefType())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    private Users toUser(Integer id) {
        Users user = new Users();
        user.setId(id);
        return user;
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
