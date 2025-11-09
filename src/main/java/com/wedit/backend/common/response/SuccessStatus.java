package com.wedit.backend.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum SuccessStatus {

    /// 200 OK
    FORM_LOGIN_SUCCESS(HttpStatus.OK, "폼 로그인 성공"),
    LOGOUT_SUCCESS(HttpStatus.OK, "로그아웃 성공"),
    OAUTH2_LOGIN_SUCCESS(HttpStatus.OK, "OAuth2 로그인 성공"),
    MEMBER_GET_SUCCESS(HttpStatus.OK, "회원 정보 조회 성공"),
    MEMBER_RESIGN_DELETE_SUCCESS(HttpStatus.OK, "회원탈퇴 성공"),
    MEMBER_INFO_GET_SUCCESS(HttpStatus.OK, "현재 사용자 정보 조회 성공"),
    AUTH_SUCCESS(HttpStatus.OK, "인증에 성공했습니다."),
    SCHEDULE_GET_SUCCESS(HttpStatus.OK, "일정 조회 성공"),
    RESERVATION_GET_SUCCESS(HttpStatus.OK, "예약 조회 성공"),
    ESTIMATE_GET_SUCCESS(HttpStatus.OK, "견적서 조회 성공"),
    REVIEW_GET_SUCCESS(HttpStatus.OK, "리뷰 조회 성공"),
    TOKEN_REISSUE_SUCCESS(HttpStatus.OK, "액세스/리프레쉬 토큰 재발급 성공"),
    SEND_SMS_VERIFICATION_CODE(HttpStatus.OK, "SMS 인증코드 발송 성공"),
    SEND_VERIFY_SMS_CODE(HttpStatus.OK, "SMS 인증코드 인증 성공"),
    S3_PUT_URL_CREATE_SUCCESS(HttpStatus.OK, "S3 PUT URL 발급 성공"),
    S3_GET_URL_CREATE_SUCCESS(HttpStatus.OK, "S3 GET URL 발급 성공"),
    VENDOR_GET_SUCCESS(HttpStatus.OK, "업체 조회 성공"),
    COUPLE_CODE_ISSUED(HttpStatus.OK, "커플 코드 발급 성공"),
    COUPLE_CONNECT_SUCCESS(HttpStatus.OK, "커플 연동 성공"),
    COUPLE_DISCONNECT_SUCCESS(HttpStatus.OK, "커플 해제 성공"),
    TOUR_GET_SUCCESS(HttpStatus.OK, "투어일지 조회 성공"),
    TOUR_GET_LIST_SUCCESS(HttpStatus.OK, "내 투어일지 리스트 조회 성공"),
    TOUR_ROMANCE_GET_SUCCESS(HttpStatus.OK, "투어로망 조회 성공"),
    TOUR_ROMANCE_GET_LIST_SUCCESS(HttpStatus.OK, "내 투어로망 리스트 조회 성공"),
    REVIEW_UPDATE_SUCCESS(HttpStatus.OK, "후기 수정 성공"),
    REVIEW_DETAIL_GET_SUCCESS(HttpStatus.OK, "후기 상세 조회 성공"),
    MY_REVIEW_LIST_GET_SUCCESS(HttpStatus.OK, "내 후기 리스팅 조회 성공"),
    ALL_REVIEW_LIST_GET_SUCCESS(HttpStatus.OK, "전체 후기 리스팅 조회 성공"),
    VENDOR_REVIEW_LIST_GET_SUCCESS(HttpStatus.OK, "업체 후기 리스팅 조회 성공"),
    MAIN_BANNER_REVIEW_LIST_GET_SUCCESS(HttpStatus.OK, "메인 배너 후기 페이징 조회 성공"),
    MAIN_BANNER_VENDOR_LIST_GET_SUCCESS(HttpStatus.OK, "메인 배너 업체 페이징 조회 성공"),
    VENDOR_DETAIL_GET_SUCCESS(HttpStatus.OK, "업체 상세 조회 성공"),
    VENDOR_LIST_GET_SUCCESS(HttpStatus.OK, "업체 페이징 리스트 조회 성공"),
    CONTRACT_AVAILABILITY_GET_SUCCESS(HttpStatus.OK, "계약 가능 시간 조회 성공"),
    VENDOR_SEARCH_SUCCESS(HttpStatus.OK, "업체 조건 검색 성공"),
    MEMBER_MYPAGE_GET_SUCCESS(HttpStatus.OK, "회원 내 정보 조회 성공"),
    VENDOR_REVIEW_STATS_SUCCESS(HttpStatus.OK, "업체 후기 통계 조회 성공"),
    TODO_GET_SUCCESS(HttpStatus.OK, "할 일 조회 성공"),
    TODO_LIST_GET_SUCCESS(HttpStatus.OK, "할 일 목록 조회 성공"),
    TODO_PENDING_COUNT_GET_SUCCESS(HttpStatus.OK, "미완료 할 일 개수 조회 성공"),
    TODO_UPDATE_SUCCESS(HttpStatus.OK, "할 일 수정 성공"),
    PRODUCT_CREATE_SUCCESS(HttpStatus.OK, "상품 생성 성공"),
    PRODUCT_GET_DETAIL_SUCCESS(HttpStatus.OK, "상품 상세 조회 성공"),
    CART_GET_SUCCESS(HttpStatus.OK, "견적서(Cart) 조회 성공"),
    CART_ITEM_UPDATE_SUCCESS(HttpStatus.OK, "견적서의 찜한 상품 활성화 성공"),
    TOUR_UPDATE_SUCCESS(HttpStatus.OK, "투어일지 수정 성공"),
    TOUR_ROMANCE_UPDATE_SUCCESS(HttpStatus.OK, "투어로망 수정 성공"),
    RESERVATION_AVAILABILITY_GET_SUCCESS(HttpStatus.OK, "상담 예약 가능 시간 조회 성공"),
    MY_RESERVATION_GET_SUCCESS(HttpStatus.OK, "내 상담 예약 목록 조회 성공"),
    INVITATION_GET_SUCCESS(HttpStatus.OK, "청첩장 조회 성공"),
    MY_CONTRACT_GET_SUCCESS(HttpStatus.OK, "내 계약 페이징 조회 성공"),
    CONTRACT_DETAIL_GET_SUCCESS(HttpStatus.OK, "계약 상세 조회 성공"),
    REVIEWABLE_CONTRACT_GET_SUCCESS(HttpStatus.OK, "후기 작성 가능 계약 목록 조회 성공"),
    CONDITION_SEARCH_SUCCESS(HttpStatus.OK, "조건 검색 성공"),
    CALENDAR_EVENTS_GET_SUCCESS(HttpStatus.OK, "캘린더 월별 일정 조회 성공"),
    CALENDAR_UPDATE_SUCCESS(HttpStatus.OK, "캘린더 일정 수정 성공"),
    VENDOR_ADDRESS_GET_SUCCESS(HttpStatus.OK, "업체 주소 조회 성공"),
    SSE_CONNECTION_SUCCESS(HttpStatus.OK, "SSE 연결 수립 성공"),
    NOTIFICATION_LIST_GET_SUCCESS(HttpStatus.OK, "알림 목록 조회 성공"),
    NOTIFICATION_UNREAD_COUNT_GET_SUCCESS(HttpStatus.OK, "안 읽은 알림 개수 조회 성공"),
    NOTIFICATION_READ_SUCCESS(HttpStatus.OK, "알림 읽음 처리 성공"),
    MEMBER_DEVICE_FCM_TOKEN_REGISTER_SUCCESS(HttpStatus.OK, "FCM 토큰 등록 성공"),
    
    /// 201 CREATED
    MEMBER_SIGNUP_SUCCESS(HttpStatus.CREATED, "회원가입 성공"),
    REVIEW_CREATE_SUCCESS(HttpStatus.CREATED, "리뷰 작성 성공"),
    IMAGE_UPLOAD_CREATE_SUCCESS(HttpStatus.CREATED, "이미지 업로드 성공"),
    VENDOR_CREATE_SUCCESS(HttpStatus.CREATED, "업체 등록 성공"),
    TOUR_CREATE_SUCCESS(HttpStatus.CREATED, "투어일지 생성 성공"),
    TOUR_ROMANCE_CREATE_SUCCESS(HttpStatus.CREATED, "투어로망 생성 성공"),
    TOUR_DRESS_CREATE_SUCCESS(HttpStatus.CREATED, "투어일지 드레스 저장 성공"),
    ESTIMATE_CREATE_SUCCESS(HttpStatus.CREATED, "견적서 생성 성공"),
    INVITATION_CREATE_SUCCESS(HttpStatus.CREATED, "청첩장 생성 성공"),
    TODO_CREATE_SUCCESS(HttpStatus.CREATED, "할 일 생성 성공"),
    CART_ITEM_ADD_SUCCESS(HttpStatus.CREATED, "견적서에 상품 등로 성공"),
    CONSULTATION_RESERVATION_CREATE_SUCCESS(HttpStatus.CREATED, "상담 예약 생성 성공"),
    CONTRACT_CREATE_SUCCESS(HttpStatus.CREATED, "계약 생성 성공"),
    CONSULTATION_TIME_SLOT_CREATE_SUCCESS(HttpStatus.CREATED, "상담 가능 시간 슬롯 생성 성공"),
    AVAILABLE_TIME_SLOT_CREATE_SUCCESS(HttpStatus.CREATED, "계약 가능 시간 슬롯 생성 성공"),
    CALENDAR_CREATE_SUCCESS(HttpStatus.CREATED, "캘린더 사용자 일정 생성 성공"),
    CALENDAR_ADMIN_EVENT_CREATE_SUCCESS(HttpStatus.CREATED, "캘린더 관리자 일정 생성 성공"),


    /// 204 NO CONTENT
    SCHEDULE_DELETE_SUCCESS(HttpStatus.NO_CONTENT,"캘린더 일정 삭제 성공"),
    IMAGE_DELETE_SUCCESS(HttpStatus.NO_CONTENT, "이미지 삭제 성공"),
    REVIEW_DELETE_SUCCESS(HttpStatus.NO_CONTENT, "리뷰 삭제 성공"),
    S3_DELETE_SUCCESS(HttpStatus.NO_CONTENT, "이미지 혹은 동영상 삭제 성공"),
    TODO_DELETE_SUCCESS(HttpStatus.NO_CONTENT, "할 일 삭제 성공"),
    TOUR_DELETE_SUCCESS(HttpStatus.NO_CONTENT, "투어일지 삭제 성공"),
    TOUR_ROMANCE_DELETE_SUCCESS(HttpStatus.NO_CONTENT, "투어로망 삭제 성공"),
    CART_ITEM_DELETE_SUCCESS(HttpStatus.NO_CONTENT, "견적서의 찜한 상품 삭제 성공"),
    CONSULTATION_RESERVATION_CANCEL_SUCCESS(HttpStatus.NO_CONTENT, "상담 예약 취소 성공"),
    CALENDAR_DELETE_SUCCESS(HttpStatus.NO_CONTENT, "캘린더 일정 삭제 성공"),


    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return this.httpStatus.value();
    }
}
