package com.slambook.dto.response;

import com.slambook.model.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private String id;
    private Notification.NotificationType type;
    private Notification.NotificationContent content;
    private Notification.RelatedEntity relatedEntity;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
