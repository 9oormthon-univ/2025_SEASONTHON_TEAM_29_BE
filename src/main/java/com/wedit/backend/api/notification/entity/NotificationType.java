package com.wedit.backend.api.notification.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

    // 일정: 예약, 계약, 캘린더 관련 (수신 거부 불가)
    // 상담 예약
    RESERVATION_CONFIRMED("일정", "예약 확정", "'{vendorName}' 상담 예약이 확정되었어요.",
            NotificationChannel.PUSH_ONLY, ClientAction.SHOW_MODAL, true),
    RESERVATION_CHANGED("일정", "예약 변경", "'{vendorName}' 상담 예약이 '{newDateTime}'으로 변경되었어요.",
            NotificationChannel.PUSH_ONLY, ClientAction.SHOW_MODAL, true),
    RESERVATION_CANCELLED("일정", "예약 취소", "'{vendorName}' 상담 예약이 취소되었어요.",
            NotificationChannel.PUSH_ONLY, ClientAction.SHOW_TOAST, true),

    // 계약
    CONTRACT_CONFIRMED("일정", "계약 완료", "'{vendorName}' 업체와의 계약이 완료되었어요.",
            NotificationChannel.PUSH_ONLY, ClientAction.SHOW_MODAL, true),
    CONTRACT_CHANGED("일정", "계약 변경", "'{vendorName}' 업체와의 계약 내용이 변경되었어요.",
            NotificationChannel.PUSH_ONLY, ClientAction.SHOW_MODAL, true),
    CONTRACT_CANCELLED("일정", "계약 취소", "'{vendorName}' 업체와의 계약이 취소되었어요.",
            NotificationChannel.PUSH_ONLY, ClientAction.SHOW_TOAST, true),
    CONTRACT_REJECTED_BY_VENDOR("일정", "계약 취소", "업체 사정으로 '{vendorName}' 업체와의 계약이 취소되었어요.",
            NotificationChannel.PUSH_ONLY, ClientAction.SHOW_MODAL, true),

    // 캘린더
    UPCOMING_SCHEDULE_REMINDER("일정", "일정 미리 알림", "잠시 후 '{scheduleTitle}' 일정이 시작돼요!",
            NotificationChannel.PUSH_ONLY, ClientAction.SHOW_TOAST, true),
    CALENDAR_SCHEDULE_REGISTERED("일정", "새 일정 등록", "'{scheduleTitle}' 일정이 새롭게 등록되었어요.",
            NotificationChannel.IN_APP_ONLY, ClientAction.SILENT, true),
    CALENDAR_SCHEDULE_DELETED("일정", "일정 삭제", "'{scheduleTitle}' 일정이 삭제되었어요.",
            NotificationChannel.IN_APP_ONLY, ClientAction.SILENT, true),


    // 활동/소식: 사용자 인터래션 관련 (수신 거부 가능)
    COUPLE_CONNECTION_COMPLETED("활동소식", "커플 연동 완료", "'{partnerName}'님과 커플 연동이 완료되었어요!",
            NotificationChannel.PUSH_ONLY, ClientAction.SHOW_MODAL, false),
    REVIEW_SCRAPPED("활동소식", "내 후기 스크랩", "다른 사용자가 회원님의 '{reviewTitle}' 후기를 스크랩했어요.",
            NotificationChannel.IN_APP_ONLY, ClientAction.SILENT, false),
    REVIEW_ENCOURAGEMENT("활동소식", "후기를 작성해보세요", "'{vendorName}'에서의 경험은 어떠셨나요? 후기를 남겨주세요.",
            NotificationChannel.IN_APP_ONLY, ClientAction.SILENT, false),


    // 혜택/이벤트: 마케팅 및 프로모션 (수신 거부 가능)
    PROMOTION_ARRIVED("혜택/이벤트", "새로운 프로모션 도착", "{promotionTitle} 프로모션이 도착했어요!",
            NotificationChannel.PUSH_ONLY, ClientAction.SILENT, false),
    MARKETING_EVENT("혜택/이벤트", "특별 이벤트 안내", "놓치면 후회할 '{eventName}' 이벤트가 진행 중이에요.",
            NotificationChannel.PUSH_ONLY, ClientAction.SILENT, false),

    // 중요공지: 결제 실패, 서비스 공지 (수신 거부 불가)
    PAYMENT_FAILED("전체", "결제 실패", "'{productName}' 상품의 결제에 실패했어요. 확인 후 다시 시도해주세요.",
            NotificationChannel.PUSH_ONLY, ClientAction.SHOW_MODAL, false),
    SERVICE_NOTICE("전체", "Wedit 공지사항", "{noticeTitle}",
            NotificationChannel.PUSH_ONLY, ClientAction.SHOW_MODAL, false);


    private final String category;              // 알림 카테고리
    private final String defaultTitle;          // 알림 타입 별 기본 제목
    private final String messageFormat;         // 동적 컨텐츠용 메시지 포맷
    private final NotificationChannel channel;  // 알림 채널 정책
    private final ClientAction clientAction;    // 클라이언트 동작 정의
    private final boolean isCoupleShared;       // 커플 간 알림 공유 여부


    // 알림을 보낼 채널
    @Getter
    @RequiredArgsConstructor
    public enum NotificationChannel {

        PUSH_ONLY("푸시 전용"),
        IN_APP_ONLY("인앱 전용");

        private final String description;
    }

    // In-App 알림을 받은 클라이언트의 UI 동작
    @Getter
    @RequiredArgsConstructor
    public enum ClientAction {

        SHOW_MODAL("모달"),       // 모달 팝업으로 표시
        SHOW_TOAST("토스트"),      // 토스트 메시지 표시
        SILENT("없음");           // 별도 표시 없음

        private final String description;
    }
}
