package com.wedit.backend.api.notification.service;

import com.wedit.backend.api.notification.entity.Notification;
import com.wedit.backend.api.notification.entity.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDispatcher {

    private final FcmService fcmService;
    private final SseService sseService;

    public void dispatch(Notification notification) {

        NotificationType type = notification.getNotificationType();

        log.info("알림 분배 시작. 알림ID: {}, 타입: {}, 채널: {}", notification.getId(), type, type.getChannel());

        switch (type.getChannel()) {
            case PUSH_ONLY -> fcmService.send(notification);
            case IN_APP_ONLY -> sseService.send(notification);
            default -> log.warn("처리할 수 없는 알림 채널. 알림ID: {}, 채널: {}", notification.getId(), type.getChannel());
        }
    }
}
