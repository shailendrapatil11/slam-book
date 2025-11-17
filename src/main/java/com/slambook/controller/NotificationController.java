package com.slambook.controller;

import com.slambook.dto.response.ApiResponse;
import com.slambook.dto.response.NotificationResponse;
import com.slambook.security.CustomUserDetails;
import com.slambook.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<NotificationResponse>>>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Get notifications for user: {}", userDetails.getUserId());
        return notificationService.getUserNotifications(userDetails)
                .collectList()
                .map(notifications -> ResponseEntity.ok(ApiResponse.success(notifications)));
    }

    @GetMapping("/unread")
    public Mono<ResponseEntity<ApiResponse<List<NotificationResponse>>>> getUnreadNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Get unread notifications for user: {}", userDetails.getUserId());
        return notificationService.getUnreadNotifications(userDetails)
                .collectList()
                .map(notifications -> ResponseEntity.ok(ApiResponse.success(notifications)));
    }

    @GetMapping("/unread-count")
    public Mono<ResponseEntity<ApiResponse<Long>>> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return notificationService.getUnreadCount(userDetails)
                .map(count -> ResponseEntity.ok(ApiResponse.success(count)));
    }

    @PutMapping("/{id}/read")
    public Mono<ResponseEntity<ApiResponse<NotificationResponse>>> markAsRead(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Mark notification as read: {}", id);
        return notificationService.markAsRead(id, userDetails)
                .map(notification -> ResponseEntity.ok(ApiResponse.success("Marked as read", notification)));
    }

    @PutMapping("/read-all")
    public Mono<ResponseEntity<ApiResponse<Void>>> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Mark all notifications as read for user: {}", userDetails.getUserId());
        return notificationService.markAllAsRead(userDetails)
                .then(Mono.just(ResponseEntity.ok(ApiResponse.<Void>success("All notifications marked as read", null))));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteNotification(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Delete notification: {}", id);
        return notificationService.deleteNotification(id, userDetails)
                .then(Mono.just(ResponseEntity.ok(ApiResponse.<Void>success("Notification deleted", null))));
    }
}