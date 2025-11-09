package com.wedit.backend.api.notification.dto;

import com.wedit.backend.api.member.entity.Member;
import com.wedit.backend.api.notification.entity.NotificationType;
import com.wedit.backend.api.notification.entity.TargetDomainType;

import java.util.Map;

public record NotificationEvent(
        Member initiator,                   // 이벤트 발생 주체
        NotificationType type,              // 알림 종류
        Map<String, String> arguments,      // 메시지 포맷팅에 사용될 동적 데이터
        TargetDomainType targetDomainType,  // 알림 클릭 시 이동할 도메인 타입
        Long targetDomainId                 // 이동할 도메인의 ID
) {

}
