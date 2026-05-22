package com.group5.htms.controller;

import com.group5.htms.dto.notification.request.NotificationCreateRequest;
import com.group5.htms.dto.notification.request.NotificationUpdateRequest;
import com.group5.htms.dto.notification.response.NotificationResponse;
import com.group5.htms.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @Operation(summary = "Get all notifications", description = "Lấy danh sách tất cả notification.")
    @GetMapping("/get-all")
    public ResponseEntity<List<NotificationResponse>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    @Operation(summary = "Get notification by id", description = "Lấy notification theo id.")
    @GetMapping("/get-by-id/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable Integer id) {
        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }

    @Operation(summary = "Create notification", description = "Tạo notification cho user đang đăng nhập dựa trên JWT.")
    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody NotificationCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.createNotification(request));
    }

    @Operation(summary = "Update notification", description = "Cập nhật notification. Field nào không gửi lên sẽ giữ nguyên.")
    @PutMapping("/update/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationResponse> updateNotification(
            @PathVariable Integer id,
            @Valid @RequestBody NotificationUpdateRequest request
    ) {
        return ResponseEntity.ok(notificationService.updateNotification(id, request));
    }

    @Operation(summary = "Mark notification as read", description = "Đánh dấu notification là đã đọc.")
    @PutMapping("/mark-read/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Integer id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @Operation(summary = "Delete notification", description = "Xóa notification theo id.")
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteNotification(@PathVariable Integer id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}
