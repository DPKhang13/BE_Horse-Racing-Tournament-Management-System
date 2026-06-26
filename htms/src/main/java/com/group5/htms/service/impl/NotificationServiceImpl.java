package com.group5.htms.service.impl;

import com.group5.htms.exception.ResourceNotFoundException;
import com.group5.htms.dto.notification.request.NotificationCreateRequest;
import com.group5.htms.dto.notification.request.NotificationUpdateRequest;
import com.group5.htms.dto.notification.response.NotificationListResponse;
import com.group5.htms.dto.notification.response.NotificationResponse;
import com.group5.htms.entity.Notifications;
import com.group5.htms.mapper.NotificationMapper;
import com.group5.htms.repository.NotificationsRepository;
import com.group5.htms.service.AuthService;
import com.group5.htms.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationsRepository notificationsRepository;
    private final AuthService authService;
    private final NotificationMapper notificationMapper;

    @Override
    public List<NotificationListResponse> getAllNotifications() {
        Integer currentUserId = authService.getCurrentUserId();

        return notificationsRepository.findByUsers_Id(currentUserId)
                .stream()
                .map(notificationMapper::toListResponse)
                .toList();
    }

    @Override
    public NotificationResponse getNotificationById(Integer id) {
        return notificationMapper.toResponse(findNotificationForCurrentUser(id));
    }

    @Override
    @Transactional
    public NotificationResponse createNotification(NotificationCreateRequest request) {
        request.setUserId(authService.getCurrentUserId());
        Notifications notification = notificationMapper.toEntity(request);

        return notificationMapper.toResponse(notificationsRepository.save(notification));
    }

    @Override
    @Transactional
    public NotificationResponse updateNotification(Integer id, NotificationUpdateRequest request) {
        Notifications notification = findNotificationForCurrentUser(id);
        request.setUserId(null);
        notificationMapper.updateNotification(notification, request);

        return notificationMapper.toResponse(notificationsRepository.save(notification));
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Integer id) {
        Notifications notification = findNotificationForCurrentUser(id);
        notification.setIsRead(true);

        return notificationMapper.toResponse(notificationsRepository.save(notification));
    }


    private Notifications findNotification(Integer id) {
        return notificationsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
    }

    private Notifications findNotificationForCurrentUser(Integer id) {
        Notifications notification = findNotification(id);
        Integer currentUserId = authService.getCurrentUserId();

        if (!Objects.equals(notification.getUsers().getId(), currentUserId)) {
            throw new AccessDeniedException("You do not own this notification");
        }

        return notification;
    }
}
