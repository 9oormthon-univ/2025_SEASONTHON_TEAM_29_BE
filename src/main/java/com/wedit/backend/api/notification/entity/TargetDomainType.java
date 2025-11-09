package com.wedit.backend.api.notification.entity;

public enum TargetDomainType {
    RESERVATION,    // 상담 예약
    CONTRACT,       // 업체 상품 계약
    CALENDAR,       // 캘린더
    REVIEW,         // 리뷰
    NOTICE,         // 전체 공지사항
    PROMOTION,      // 프로모션/이벤트
    MY_PAGE,        // 마이페이지
    INVITATION,     // 청첩장
    MAIN,           // 메인페이지
    NONE            // 특정 페이지로 이동하지 않음
}
