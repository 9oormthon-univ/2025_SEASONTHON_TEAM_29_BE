package com.wedit.backend.api.notification.controller;

import com.wedit.backend.api.member.entity.Member;
import com.wedit.backend.api.notification.dto.NotificationResponseDTO;
import com.wedit.backend.api.notification.service.NotificationService;
import com.wedit.backend.api.notification.service.SseService;
import com.wedit.backend.common.config.security.entity.SecurityMember;
import com.wedit.backend.common.response.ApiResponse;
import com.wedit.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@Tag(name = "Notification", description = "알림 관련 API 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
@SecurityRequirement(name = "Authorization")
public class NotificationController {

    private final NotificationService notificationService;
    private final SseService sseService;


    @Operation(
            summary = "SSE 연결 구독 (In-App 실시간 알림)",
            description = "클라이언트가 실시간 In-App 알림을 받기 위해 SSE 연결을 생성합니다. <br>" +
                    "이 엔드포인트는 `text/event-stream`을 반환하며, 일반적인 JSON 응답과 다릅니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "SSE 스트림 연결 성공",
                    content = @Content(mediaType = "text/event-stream")),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content)
    })
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe(
            @Parameter(hidden = true) @AuthenticationPrincipal SecurityMember securityMember,
            @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId
    ) {
        SseEmitter emitter = sseService.subscribe(securityMember.getMember().getId(), lastEventId);

        return ResponseEntity.ok(emitter);
    }


    @Operation(
            summary = "내 알림 목록 조회",
            description = "현재 로그인한 사용자의 알림 목록을 페이징하여 조회합니다. 카테고리별 필터링이 가능합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "알림 목록 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content)
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponseDTO>>> getMyNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal SecurityMember securityMember,
            @Parameter(description = "필터링할 알림 카테고리 (ex. 전체, 일정, 활동소식, 혜택/마케팅), 미입력 시 '전체'로 동작")
            @RequestParam(required = false, defaultValue = "전체") String category,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable
    ) {
        Member member = securityMember.getMember();
        Page<NotificationResponseDTO> resp = notificationService.getMyNotifications(member, category, pageable);

        return ApiResponse.success(SuccessStatus.NOTIFICATION_LIST_GET_SUCCESS, resp);
    }


    @Operation(
            summary = "안 읽은 알림 개수 조회",
            description = "현재 로그인한 사용자의 읽지 않은 알림 개수를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "안 읽은 알림 개수 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content)
    })
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadNotificationCount(
            @Parameter(hidden = true) @AuthenticationPrincipal SecurityMember securityMember
    ) {
        Member member = securityMember.getMember();
        long count = notificationService.getUnreadNotificationCount(member);

        return ApiResponse.success(SuccessStatus.NOTIFICATION_UNREAD_COUNT_GET_SUCCESS, Map.of("unreadCount", count));
    }


    @Operation(
            summary = "알림 읽음 처리",
            description = "특정 알림을 읽음 상태로 변경합니다. 자신의 알림만 처리할 수 있습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "알림 읽음 처리 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 또는 권한 없음 (자신의 알림이 아님)",
                    content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 알림을 찾을 수 없음",
                    content = @Content)
    })
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> readNotification(
            @Parameter(hidden = true) @AuthenticationPrincipal SecurityMember securityMember,
            @Parameter(description = "읽음 처리할 알림 ID") @PathVariable Long notificationId
    ) {
        Member member = securityMember.getMember();
        notificationService.readNotification(notificationId, member);

        return ApiResponse.successOnly(SuccessStatus.NOTIFICATION_READ_SUCCESS);
    }
}
