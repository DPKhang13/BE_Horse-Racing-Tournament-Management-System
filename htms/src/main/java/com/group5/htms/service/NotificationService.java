package com.group5.htms.service;

import com.group5.htms.dto.notification.request.NotificationCreateRequest;
import com.group5.htms.dto.notification.request.NotificationUpdateRequest;
import com.group5.htms.dto.notification.response.NotificationListResponse;
import com.group5.htms.dto.notification.response.NotificationResponse;

import java.util.List;

public interface NotificationService {
    List<NotificationListResponse> getAllNotifications();

    NotificationResponse getNotificationById(Integer id);

    NotificationResponse createNotification(NotificationCreateRequest request);

    NotificationResponse updateNotification(Integer id, NotificationUpdateRequest request);

    NotificationResponse markAsRead(Integer id);

    void deleteNotification(Integer id);
}
