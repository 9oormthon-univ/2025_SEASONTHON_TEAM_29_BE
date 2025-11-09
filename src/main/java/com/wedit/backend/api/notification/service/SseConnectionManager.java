package com.wedit.backend.api.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SseConnectionManager {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public void add(Long memberId, SseEmitter emitter) {

        this.emitters.put(memberId.toString(), emitter);
        log.info("새로운 SSE 연결 추가. 사용자ID: {}", memberId);

        emitter.onCompletion(() -> {
            log.info("SSE 연결 완료. 사용자ID: {}", memberId);
            this.emitters.remove(memberId.toString());
        });
        emitter.onTimeout(() -> {
            log.info("SSE 연결 타임아웃. 사용자ID: {}", memberId);
            emitter.complete();
        });
    }

    public Optional<SseEmitter> get(Long memberId) {
        return Optional.ofNullable(this.emitters.get(memberId.toString()));
    }

    public void remove(Long memberId) {
        this.emitters.remove(memberId.toString());
        log.info("SSE 연결 제거. 사용자ID: {}", memberId);
    }

    public void sendConnectionEvent(SseEmitter emitter, Long memberId) {
        try {
            emitter.send(SseEmitter.event()
                    .id(memberId + "_" + System.currentTimeMillis())
                    .name("connect")
                    .data("SSE 연결 성공적으로 수립. 사용자ID: " + memberId));
            log.info("SSE 연결 확인 이벤트 발송 성공. 사용자ID: {}", memberId);
        } catch (IOException e) {
            log.error("SSE 연결 확인 이벤트 발송 실패. 사용자ID: {}", memberId, e);
        }
    }
}
