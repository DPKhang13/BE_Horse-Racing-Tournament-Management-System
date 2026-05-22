package com.group5.htms.service.impl;

import com.group5.htms.common.exceptions.ResourceNotFoundException;
import com.group5.htms.dto.notification.request.NotificationCreateRequest;
import com.group5.htms.dto.notification.request.NotificationUpdateRequest;
import com.group5.htms.dto.notification.response.NotificationResponse;
import com.group5.htms.entity.Notifications;
import com.group5.htms.mapper.NotificationMapper;
import com.group5.htms.repository.NotificationsRepository;
import com.group5.htms.repository.UsersRepository;
import com.group5.htms.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationsRepository notificationsRepository;
    private final UsersRepository usersRepository;
    private final NotificationMapper notificationMapper;

    @Override
    public List<NotificationResponse> getAllNotifications() {
        return notificationsRepository.findAll()
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Override
    public NotificationResponse getNotificationById(Integer id) {
        return notificationMapper.toResponse(findNotification(id));
    }

    @Override
    @Transactional
    public NotificationResponse createNotification(NotificationCreateRequest request) {
        validateUserExists(request.getUserId());
        Notifications notification = notificationMapper.toEntity(request);

        return notificationMapper.toResponse(notificationsRepository.save(notification));
    }

    @Override
    @Transactional
    public NotificationResponse updateNotification(Integer id, NotificationUpdateRequest request) {
        Notifications notification = findNotification(id);
        if (request.getUserId() != null) {
            validateUserExists(request.getUserId());
        }
        notificationMapper.updateNotification(notification, request);

        return notificationMapper.toResponse(notificationsRepository.save(notification));
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Integer id) {
        Notifications notification = findNotification(id);
        notification.setIsRead(true);

        return notificationMapper.toResponse(notificationsRepository.save(notification));
    }

    @Override
    @Transactional
    public void deleteNotification(Integer id) {
        Notifications notification = findNotification(id);
        notificationsRepository.delete(notification);
    }

    private Notifications findNotification(Integer id) {
        return notificationsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
    }

    private void validateUserExists(Integer userId) {
        if (!usersRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }
    }
}
