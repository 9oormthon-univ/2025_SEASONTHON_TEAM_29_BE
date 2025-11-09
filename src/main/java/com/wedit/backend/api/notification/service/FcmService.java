package com.wedit.backend.api.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.wedit.backend.api.member.repository.MemberDeviceRepository;
import com.wedit.backend.api.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService implements NotificationSender {

    private final MemberDeviceRepository memberDeviceRepository;
    private final FirebaseMessaging firebaseMessaging;

    @Override
    public void send(Notification notification) {
        log.info("FCM 푸시 알림 발송 시도. 알림ID: {}",  notification.getId());

        Optional<String> fcmTokenOpt = memberDeviceRepository.findActiveTokenByMember(notification.getMember());

        if (fcmTokenOpt.isEmpty()) {
            log.warn("FCM 토큰이 없어 푸시 알림을 발송할 수 없음. MemberID: {}",  notification.getMember().getId());
            return;
        }
        String fcmToken = fcmTokenOpt.get();

        com.google.firebase.messaging.Notification fcmNotification = com.google.firebase.messaging.Notification.builder()
                .setTitle(notification.getTitle())
                .setBody(notification.getContent())
                .build();

        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(fcmNotification)
                .putData("targetDomainType", notification.getTargetDomainType().name())
                .putData("targetDomainId", String.valueOf(notification.getTargetDomainId()))
                .putData("notificationId", String.valueOf(notification.getId()))
                .build();

        try {
            String response = firebaseMessaging.send(message);
            log.info("FCM 푸시 알림 발송 성공. 알림ID: {}, 응답: {}",  notification.getId(), response);
        } catch (FirebaseMessagingException e) {
            log.error("FCM 푸시 알림 발송 실패. 알림ID: {}, 에러코드: {}", notification.getId(), e.getErrorCode(), e);
        } catch (Exception e) {
            log.error("FCM 푸시 알림 발송 실패. 알림ID: {}", notification.getId(), e);
        }
    }
}
