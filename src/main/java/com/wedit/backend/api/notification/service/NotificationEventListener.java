package com.wedit.backend.api.notification.service;

import com.wedit.backend.api.notification.dto.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationEvent(NotificationEvent event) {
        log.info("[이벤트 수신] 알림 생성 요청 감지. 타입: {}", event.type());

        try {
            notificationService.createNotificationForUserAction(event);
        } catch (Exception e) {
            log.error("알림 이벤트 처리 중 예외 발생. 이벤트: {}", event, e);
        }
    }
}
