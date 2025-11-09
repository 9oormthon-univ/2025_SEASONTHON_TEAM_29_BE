package com.wedit.backend.api.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wedit.backend.api.notification.dto.NotificationResponseDTO;
import com.wedit.backend.api.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService implements NotificationSender {

    private final SseConnectionManager connectionManager;
    private final ObjectMapper objectMapper;
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; // 1시간


    public SseEmitter subscribe(Long memberId, String lastEventId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        connectionManager.add(memberId, emitter);

        connectionManager.sendConnectionEvent(emitter, memberId);

        return emitter;
    }

    @Override
    public void send(Notification notification) {

        Long memberId = notification.getMember().getId();

        connectionManager.get(memberId).ifPresent(emitter -> {
            try {
                NotificationResponseDTO dto = NotificationResponseDTO.from(notification);
                String notificationJson = objectMapper.writeValueAsString(dto);

                log.info("SSE Emitter로 In-App 알림 발송. 사용자ID: {}", memberId);

                emitter.send(SseEmitter.event()
                        .id(String.valueOf(notification.getId()))
                        .name("notification")
                        .data(notificationJson));


            } catch (IOException e) {
                log.error("SSE 알림 전송 실패. 사용자ID: {}, 예외: {}", memberId, e.getMessage());
                emitter.completeWithError(e);
            }
        });
    }
}
