package com.slambook.service;

import com.slambook.dto.response.NotificationResponse;
import com.slambook.model.Notification;
import com.slambook.model.SlamBookEntry;
import com.slambook.model.User;
import com.slambook.repository.NotificationRepository;
import com.slambook.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Mono<Notification> sendNewEntryNotification(SlamBookEntry entry) {
        if (entry.getIsAnonymous()) {
            return createNotification(
                    entry.getCollegeId(),
                    entry.getWrittenFor(),
                    Notification.NotificationType.NEW_ENTRY,
                    "New Slam Book Entry",
                    "Someone wrote anonymously in your slam book!",
                    "/slambook/entries/" + entry.getId(),
                    Notification.EntityType.SLAM_BOOK_ENTRY,
                    entry.getId()
            );
        } else {
            return createNotification(
                    entry.getCollegeId(),
                    entry.getWrittenFor(),
                    Notification.NotificationType.NEW_ENTRY,
                    "New Slam Book Entry",
                    "Someone wrote in your slam book!",
                    "/slambook/entries/" + entry.getId(),
                    Notification.EntityType.SLAM_BOOK_ENTRY,
                    entry.getId()
            );
        }
    }

    public Mono<Notification> sendJoinApprovedNotification(User user) {
        return createNotification(
                user.getCollegeId(),
                user.getId(),
                Notification.NotificationType.JOIN_APPROVED,
                "Welcome!",
                "Your join request has been approved. You can now access all features!",
                "/dashboard",
                null,
                null
        );
    }

    public Mono<Notification> sendJoinRejectedNotification(User user, String reason) {
        return createNotification(
                user.getCollegeId(),
                user.getId(),
                Notification.NotificationType.JOIN_REJECTED,
                "Join Request Rejected",
                "Your join request was rejected. Reason: " + reason,
                null,
                null,
                null
        );
    }

    public Mono<Notification> sendReactionNotification(SlamBookEntry entry, String reactedByUserId) {
        // Don't notify if reacting to own entry
        if (entry.getWrittenBy().equals(reactedByUserId)) {
            return Mono.empty();
        }

        return createNotification(
                entry.getCollegeId(),
                entry.getWrittenBy(),
                Notification.NotificationType.REACTION,
                "New Reaction",
                "Someone reacted to your slam book entry!",
                "/slambook/entries/" + entry.getId(),
                Notification.EntityType.SLAM_BOOK_ENTRY,
                entry.getId()
        );
    }

    public Mono<Notification> sendNewJoinRequestNotification(User user, String adminId) {
        return createNotification(
                user.getCollegeId(),
                adminId,
                Notification.NotificationType.NEW_JOIN_REQUEST,
                "New Join Request",
                user.getProfile().getFirstName() + " " + user.getProfile().getLastName() + " requested to join",
                "/admin/join-requests/" + user.getId(),
                Notification.EntityType.USER,
                user.getId()
        );
    }

    public Flux<NotificationResponse> getUserNotifications(CustomUserDetails userDetails) {
        return notificationRepository.findByUserId(userDetails.getUserId())
                .map(this::mapToNotificationResponse)
                .sort((n1, n2) -> n2.getCreatedAt().compareTo(n1.getCreatedAt()));
    }

    public Flux<NotificationResponse> getUnreadNotifications(CustomUserDetails userDetails) {
        return notificationRepository.findByUserIdAndIsRead(userDetails.getUserId(), false)
                .map(this::mapToNotificationResponse)
                .sort((n1, n2) -> n2.getCreatedAt().compareTo(n1.getCreatedAt()));
    }

    public Mono<NotificationResponse> markAsRead(String notificationId, CustomUserDetails userDetails) {
        return notificationRepository.findById(notificationId)
                .filter(notification -> notification.getUserId().equals(userDetails.getUserId()))
                .flatMap(notification -> {
                    notification.setIsRead(true);
                    notification.setReadAt(LocalDateTime.now());
                    return notificationRepository.save(notification);
                })
                .map(this::mapToNotificationResponse);
    }

    public Mono<Void> markAllAsRead(CustomUserDetails userDetails) {
        return notificationRepository.findByUserIdAndIsRead(userDetails.getUserId(), false)
                .flatMap(notification -> {
                    notification.setIsRead(true);
                    notification.setReadAt(LocalDateTime.now());
                    return notificationRepository.save(notification);
                })
                .then();
    }

    public Mono<Void> deleteNotification(String notificationId, CustomUserDetails userDetails) {
        return notificationRepository.findById(notificationId)
                .filter(notification -> notification.getUserId().equals(userDetails.getUserId()))
                .flatMap(notificationRepository::delete);
    }

    public Mono<Long> getUnreadCount(CustomUserDetails userDetails) {
        return notificationRepository.countByUserIdAndIsRead(userDetails.getUserId(), false);
    }

    private Mono<Notification> createNotification(
            String collegeId,
            String userId,
            Notification.NotificationType type,
            String title,
            String message,
            String actionUrl,
            Notification.EntityType entityType,
            String entityId
    ) {
        Notification.NotificationContent content = Notification.NotificationContent.builder()
                .title(title)
                .message(message)
                .actionUrl(actionUrl)
                .build();

        Notification.RelatedEntity relatedEntity = null;
        if (entityType != null && entityId != null) {
            relatedEntity = Notification.RelatedEntity.builder()
                    .type(entityType)
                    .id(entityId)
                    .build();
        }

        Notification notification = Notification.builder()
                .collegeId(collegeId)
                .userId(userId)
                .type(type)
                .content(content)
                .relatedEntity(relatedEntity)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        return notificationRepository.save(notification);
    }

    private NotificationResponse mapToNotificationResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .content(notification.getContent())
                .relatedEntity(notification.getRelatedEntity())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}