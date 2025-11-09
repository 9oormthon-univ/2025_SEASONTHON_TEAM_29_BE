package com.wedit.backend.api.notification.repository;

import com.wedit.backend.api.member.entity.Member;
import com.wedit.backend.api.notification.entity.Notification;
import com.wedit.backend.api.notification.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByMemberOrderByCreatedAtDesc(Member member, Pageable pageable);

    Page<Notification> findByMemberAndNotificationTypeInOrderByCreatedAtDesc(Member member, List<NotificationType> types, Pageable pageable);

    // Member의 읽지 않은 알림 개수를 조회
    long countByMemberAndIsReadFalse(Member member);
}
