package com.wedit.backend.api.notification.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.wedit.backend.api.notification.entity.Notification;
import com.wedit.backend.api.notification.entity.TargetDomainType;

import java.time.LocalDateTime;

public record NotificationResponseDTO(

        Long id,
        String category,
        String title,
        String content,
        TargetDomainType targetDomainType,
        Long targetDomainId,
        boolean isRead,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt
) {

    public static NotificationResponseDTO from(Notification notification) {
        return new NotificationResponseDTO(
                notification.getId(),
                notification.getNotificationType().getCategory(),
                notification.getTitle(),
                notification.getContent(),
                notification.getTargetDomainType(),
                notification.getTargetDomainId(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
