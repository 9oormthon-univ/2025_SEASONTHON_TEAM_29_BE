package com.wedit.backend.api.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wedit.backend.api.member.entity.Member;
import com.wedit.backend.api.member.entity.Role;
import com.wedit.backend.api.member.repository.MemberRepository;
import com.wedit.backend.api.notification.dto.NotificationEvent;
import com.wedit.backend.api.notification.dto.NotificationResponseDTO;
import com.wedit.backend.api.notification.entity.Notification;
import com.wedit.backend.api.notification.entity.NotificationType;
import com.wedit.backend.api.notification.entity.TargetDomainType;
import com.wedit.backend.api.notification.repository.NotificationRepository;
import com.wedit.backend.common.exception.NotFoundException;
import com.wedit.backend.common.exception.UnauthorizedException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final NotificationDispatcher notificationDispatcher;
    private final EntityManager em;
    private final ObjectMapper objectMapper;

    private static final int BATCH_SIZE = 100;

    // 사용자에게 알림 생성, 만약 커플일 시 커플 상대방에게도 발송
    public void createNotificationForUserAction(NotificationEvent event) {

        log.info("개인 알림 생성 및 발송 요청. 사용자 ID: {}, 타입: {}", event.initiator().getId(), event.type());

        String title = formatMessage(event.type().getDefaultTitle(), event.arguments());
        String content = formatMessage(event.type().getMessageFormat(), event.arguments());
        String argumentsJson = convertArgumentsToJson(event.arguments());

        Notification notification = createAndSaveNotification(event.initiator(), event.type(), title, content, argumentsJson, event.targetDomainType(), event.targetDomainId());

        notificationDispatcher.dispatch(notification);

        if (event.type().isCoupleShared()) {
            event.initiator().getPartner().ifPresent(partner -> {
                log.info("커플 공유 알림 생성 및 발송. 원본 사용자 ID: {}, 파트너 ID: {}", event.initiator().getId(), partner.getId());
                Notification partnerNotification = createAndSaveNotification(partner, event.type(), title, content, argumentsJson, event.targetDomainType(), event.targetDomainId());
                notificationDispatcher.dispatch(partnerNotification);
            });
        }
    }

    // 모든 사용자에 대해 전체 알림을 비동기 + 배치 처리로 생성
    @Async
    @Transactional
    public void createBroadcastNotification(NotificationType type, Map<String, String> arguments, TargetDomainType targetDomainType, Long targetDomainId) {
        log.info("전체 공지 알림 생성 요청. 타입: {}", type);

        if (type != NotificationType.SERVICE_NOTICE) {
            log.warn("전체 알림이 아닌 타입으로 알림 요청 발생. 타입: {}", type);
            throw new IllegalArgumentException("전체 알림 타입이 아닙니다. 타입: " + type);
        }

        String title = formatMessage(type.getDefaultTitle(), arguments);
        String content = formatMessage(type.getMessageFormat(), arguments);
        String argumentsJson = convertArgumentsToJson(arguments);

        Page<Member> currentPage;
        int pageNumber = 0;
        long totalProcessed = 0;

        do {
            Pageable pageable = PageRequest.of(pageNumber, BATCH_SIZE);
            currentPage = memberRepository.findByRole(Role.ROLE_USER, pageable);

            if (currentPage.hasContent()) {
                List<Notification> notifications = new ArrayList<>();

                for (Member user : currentPage.getContent()) {
                    notifications.add(buildNotification(user, type, title, content, argumentsJson, targetDomainType, targetDomainId));
                }

                notificationRepository.saveAll(notifications);

                notifications.forEach(notificationDispatcher::dispatch);

                em.flush();
                em.clear();

                totalProcessed += currentPage.getNumberOfElements();
                log.info("[Async] 전체 알림 배치 처리 중. 처리된 사용자 수: {}", totalProcessed);
            }
            pageNumber++;
        } while (currentPage.hasNext());

        log.info("[Async] 총 {}명의 사용자에게 전체 알림 발송을 완료.", totalProcessed);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponseDTO> getMyNotifications(Member member, String category, Pageable pageable) {

        log.debug("알림 목록 조회 요청. 사용자 ID: {}, 카테고리: {}", member.getId(), category);
        Page<Notification> notifications;

        if (category == null || category.isBlank() || "전체".equalsIgnoreCase(category)) {
            notifications = notificationRepository.findByMemberOrderByCreatedAtDesc(member, pageable);
        } else {
            List<NotificationType> filteredTypes = Arrays.stream(NotificationType.values())
                    .filter(type -> category.equals(type.getCategory()))
                    .toList();

            if (filteredTypes.isEmpty()) {
                return Page.empty(pageable);
            }

            notifications = notificationRepository.findByMemberAndNotificationTypeInOrderByCreatedAtDesc(member, filteredTypes, pageable);
        }

        return notifications.map(NotificationResponseDTO::from);
    }

    @Transactional(readOnly = true)
    public long getUnreadNotificationCount(Member member) {

        log.debug("안 읽은 알림 개수 조회 요청. 사용자 ID: {}", member.getId());

        return notificationRepository.countByMemberAndIsReadFalse(member);
    }

    public void readNotification(Long notificationId, Member member) {

        log.info("알림 읽음 처리 요청. 알림 ID: {}, 사용자 ID: {}", notificationId, member.getId());

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("해당 알림을 찾을 수 없습니다. ID: " + notificationId));

        if (!notification.getMember().getId().equals(member.getId())) {
            log.warn("알림 읽기 권한 없음. 요청자 ID: {}, 알림 소유자 ID: {}", member.getId(), notificationId);
            throw new UnauthorizedException("자신의 알림만 읽음 처리할 수 있습니다.");
        }

        notification.read();
        log.info("알림 읽음 처리 완료. 알림 ID: {}", notificationId);
    }

    private Notification createAndSaveNotification(Member member, NotificationType type, String title, String content, String argumentsJson, TargetDomainType targetDomainType, Long targetDomainId) {
        Notification notification = buildNotification(member, type, title, content, argumentsJson, targetDomainType, targetDomainId);
        return notificationRepository.save(notification);
    }

    private Notification buildNotification(Member member, NotificationType type, String title, String content, String argumentsJson, TargetDomainType targetDomainType, Long targetDomainId) {
        return Notification.builder()
                .member(member)
                .type(type)
                .title(title)
                .content(content)
                .arguments(argumentsJson)
                .targetDomainType(targetDomainType)
                .targetDomainId(targetDomainId)
                .build();
    }

    private String formatMessage(String format, Map<String, String> arguments) {

        String result = format;
        if (arguments != null) {
            for (Map.Entry<String, String> entry : arguments.entrySet()) {
                result = result.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return result;
    }

    private String convertArgumentsToJson(Map<String, String> arguments) {
        if (arguments == null || arguments.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(arguments);
        } catch (JsonProcessingException e) {
            log.error("알림 arguments를 JSON으로 변환하는데 실패. Arguments: {}", arguments, e);
            return "{}";
        }
    }
}
