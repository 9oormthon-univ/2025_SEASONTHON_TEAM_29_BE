package com.wedit.backend.common.response;

import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum ErrorStatus {

    /// 400 BAD REQUEST
    BAD_REQUEST_MISSING_PARAM(HttpStatus.BAD_REQUEST, "요청 값이 입력되지 않았습니다."),
    BAD_REQUEST_MISSING_REQUIRED_FIELD(HttpStatus.BAD_REQUEST, "필수 입력값이 누락되었습니다."),
    BAD_REQUEST_INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "잘못된 비밀번호입니다."),
    BAD_REQUEST_DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "이미 사용 중인 이메일입니다."),
    BAD_REQUEST_DUPLICATE_PHONE(HttpStatus.BAD_REQUEST, "이미 사용 중인 전화번호입니다."),
    BAD_REQUEST_INVALID_EMAIL(HttpStatus.BAD_REQUEST, "잘못된 이메일입니다."),
    BAD_REQUEST_INVALID_PHONE(HttpStatus.BAD_REQUEST, "잘못된 전화번호입니다."),
    BAD_REQUEST_RESERVATION_CONFLICT(HttpStatus.BAD_REQUEST, "예약 시간이 겹칩니다."),
    BAD_REQUEST_VALID_FAILED(HttpStatus.BAD_REQUEST, "DTO 유효성 검증에 실패했습니다."),
    BAD_REQUEST_NOT_SUPPORTED_MEDIA_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 미디어 타입입니다."),
    BAD_REQUEST_INVALID_IMAGE_SIZE(HttpStatus.BAD_REQUEST, "이미지 파일 크기가 15MB 보다 큽니다."),
    BAD_REQUEST_INVALID_VIDEO_SIZE(HttpStatus.BAD_REQUEST, "동영상 파일 크기가 100MB 보다 큽니다."),
    BAD_REQUEST_FAILED_SMS_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "SMS 인증코드가 올바르지 않습니다."),
    BAD_REQUEST_VALIDATION_PHONE_FORMAT(HttpStatus.BAD_REQUEST, "핸드폰 번호 형식이 올바르지 않습니다."),
    BAD_REQUEST_MISSING_PHONE_NUMBER_VERIFICATION(HttpStatus.BAD_REQUEST, "전화번호 인증을 진행해주세요."),
    BAD_REQUEST_INVALID_COUPLE_CODE(HttpStatus.BAD_REQUEST, "올바르지 않은 커플 코드입니다."),
    BAD_REQUEST_ALREADY_REGISTRATION_GROOM(HttpStatus.BAD_REQUEST, "이미 다른 신랑이 등록된 커플입니다."),
    BAD_REQUEST_ALREADY_REGISTRATION_BRIDE(HttpStatus.BAD_REQUEST, "이미 다른 신부가 등록된 커플입니다."),
    BAD_REQUEST_ALREADY_DISCONNECT_COUPLE(HttpStatus.BAD_REQUEST, "이미 커플이 아니거나 해제된 상태입니다."),
    BAD_REQUEST_MEMBER_TOUR_ACCESS(HttpStatus.BAD_REQUEST, "해당 유저만 투어일지를 작성할 수 있습니다."),
    BAD_REQUEST_NOT_SUPPORTED_DOMAIN(HttpStatus.BAD_REQUEST, "지원하지 않는 도메인입니다."),
    BAD_REQUEST_COUPLE_CONNECT_MYSELF(HttpStatus.BAD_REQUEST, "자기 자신과는 연결할 수 없습니다."),
    BAD_REQUEST_CONNECT_BRIDE_TO_GROOM(HttpStatus.BAD_REQUEST, "잘못된 연결 요청입니다. 신부를 등록할 수 없는 상태입니다."),
    BAD_REQUEST_CONNECT_GROOM_TO_BRIDE(HttpStatus.BAD_REQUEST, "잘못된 연결 요청입니다. 신랑을 등록할 수 없는 상태입니다."),
    BAD_REQUEST_REQUIRED_LEAST_REGION_CODE(HttpStatus.BAD_REQUEST, "업체 지역은 읍/면/동 단위(level=3)여야 합니다."),
    BAD_REQUEST_ALREADY_BOOKED(HttpStatus.BAD_REQUEST, "이미 예약된 슬롯입니다. 다른 시간을 선택해주세요."),
	BAD_REQUEST_ESTIMATE_CONFLICT(HttpStatus.BAD_REQUEST, "견적서 시간이 겹칩니다."),
	BAD_REQUEST_ALREADY_HAVE_INVITATION(HttpStatus.BAD_REQUEST, "이미 청첩장을 가지고 있습니다."),
	BAD_REQUEST_ALREADY_OTHER_MEMBER_HAVE_INVITATION(HttpStatus.BAD_REQUEST, "이미 다른 멤버가 청첩장을 가지고 있습니다."),
    BAD_REQUEST_ALREADY_EXIST_CART_ITEM(HttpStatus.BAD_REQUEST, "이미 같은 시간의 상품이 담겨 있습니다."),
    BAD_REQUEST_ALREADY_WRITE_REVIEW(HttpStatus.BAD_REQUEST, "이미 후기를 작성한 계약입니다."),

	/// 401 UNAUTHORIZED
	UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),
	UNAUTHORIZED_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
	UNAUTHORIZED_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
	UNAUTHORIZED_UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "지원되지 않는 토큰입니다."),
	UNAUTHORIZED_EMPTY_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 비어있습니다."),
	UNAUTHORIZED_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
	UNAUTHORIZED_EMAIL_OR_PASSWORD(HttpStatus.UNAUTHORIZED, "이메일 혹은 비밀번호를 다시 확인하세요."),
	UNAUTHORIZED_TOKEN_REISSUE_FAILED(HttpStatus.UNAUTHORIZED, "토큰 재발급에 실패했습니다."),
	UNAUTHORIZED_FAILED_SMS_VERIFICATION_CODE(HttpStatus.UNAUTHORIZED, "SMS 인증코드가 만료되었습니다, 재인증 해주세요."),
	UNAUTHORIZED_WRITER_NOT_SAME_USER(HttpStatus.UNAUTHORIZED, "작성자와 요청자가 다릅니다."),

    /// 403 FORBIDDEN
    FORBIDDEN_RESOURCE_ACCESS(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    FORBIDDEN_REVIEW_MODIFY(HttpStatus.FORBIDDEN, "리뷰 수정 권한이 없습니다."),
    FORBIDDEN_TODO_ACCESS(HttpStatus.FORBIDDEN, "할 일 접근 권한이 없습니다."),

    /// 404 NOT FOUND
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다."),
    NOT_FOUND_RESOURCE(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    NOT_FOUND_EMAIL(HttpStatus.NOT_FOUND, "해당 이메일을 찾을 수 없습니다."),
    NOT_FOUND_VENDOR(HttpStatus.NOT_FOUND, "해당 업체를 찾을 수 없습니다."),
    NOT_FOUND_TOUR(HttpStatus.NOT_FOUND, "해당 투어를 찾을 수 없습니다."),
    NOT_FOUND_TOUR_ROMANCE(HttpStatus.NOT_FOUND, "해당 투어로망을 찾을 수 없습니다."),
    NOT_FOUND_REVIEW(HttpStatus.NOT_FOUND, "해당 리뷰를 찾을 수 없습니다."),
    NOT_FOUND_MEMBER(HttpStatus.NOT_FOUND, "해당 회원을 찾을 수 없습니다."),
    NOT_FOUND_TODO(HttpStatus.NOT_FOUND, "해당 할 일을 찾을 수 없습니다."),
    NOT_FOUND_PRODUCT(HttpStatus.NOT_FOUND, "해당 상품을 찾을 수 없습니다."),
    NOT_FOUND_CART_ITEM(HttpStatus.NOT_FOUND, "해당 상품이 견적서에 없습니다."),
    NOT_FOUND_RESERVATION(HttpStatus.NOT_FOUND, "해당 상담 예약은 존재하지 않습니다."),
    NOT_FOUND_SLOT(HttpStatus.NOT_FOUND, "해당 슬롯은 존재하지 않습니다."),
    NOT_FOUND_CONTRACT(HttpStatus.NOT_FOUND, "해당 계약은 존재하지 않습니다."),
	NOT_FOUND_INVITATION(HttpStatus.NOT_FOUND, "청첩장을 찾을 수 없습니다."),

	/// 409 CONFLICT
	CONFLICT_DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "중복된 리소스가 존재합니다."),
    CONFLICT_MEMBER_DEVICE_FCM_TOKEN(HttpStatus.CONFLICT, "이미 다른 사용자가 등록한 FCM 토큰입니다."),

	/// 415 UNSUPPORTED MEDIA TYPE
	UNSUPPORTED_MEDIA_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 Content-Type 입니다."),

	/// 500 SERVER ERROR
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
	IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드에 실패했습니다."),
	IMAGE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 삭제에 실패했습니다."),
	SMS_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SMS 전송에 실패했습니다."),

	/// 503 SERVICE UNAVAILABLE
	SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "서버에 연결할 수 없습니다."),

	;

	private final HttpStatus httpStatus;
	private final String message;

	public int getStatusCode() {
		return this.httpStatus.value();
	}
}
