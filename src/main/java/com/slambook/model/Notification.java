package com.slambook.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    @Indexed
    private String collegeId;

    @Indexed
    private String userId;

    private NotificationType type;
    private NotificationContent content;
    private RelatedEntity relatedEntity;

    @Indexed
    private Boolean isRead;

    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationContent {
        private String title;
        private String message;
        private String actionUrl;
        private String iconUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RelatedEntity {
        private EntityType type;
        private String id;
        private String displayName;
    }

    public enum NotificationType {
        NEW_ENTRY,          // Someone wrote in your slam book
        JOIN_APPROVED,      // Your join request was approved
        JOIN_REJECTED,      // Your join request was rejected
        REACTION,           // Someone reacted to your entry
        MENTION,            // Someone mentioned you
        ANNOUNCEMENT,       // College announcement
        SUBSCRIPTION_EXPIRING,  // Subscription expiring soon
        NEW_JOIN_REQUEST    // New join request (for admin)
    }

    public enum EntityType {
        USER, SLAM_BOOK_ENTRY, COLLEGE, TEMPLATE
    }
}