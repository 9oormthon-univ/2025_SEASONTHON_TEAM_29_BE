package com.wedit.backend.api.member.controller;

import com.wedit.backend.api.member.dto.*;
import com.wedit.backend.api.member.entity.Member;
import com.wedit.backend.api.member.jwt.entity.RefreshToken;
import com.wedit.backend.api.member.jwt.service.JwtService;
import com.wedit.backend.api.member.jwt.service.RefreshTokenService;
import com.wedit.backend.api.member.service.MemberService;
import com.wedit.backend.common.config.security.entity.SecurityMember;
import com.wedit.backend.common.exception.UnauthorizedException;
import com.wedit.backend.common.response.ApiResponse;
import com.wedit.backend.common.response.ErrorStatus;
import com.wedit.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Member", description = "Member 관련 API 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/member")
public class MemberController {

    private final MemberService memberService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Operation(
            summary = "일반 회원가입",
            description = "이메일, 비밀번호 등을 이용해 신규 회원을 등록합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다.", content = @Content)
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(
            @RequestBody MemberSignupRequestDTO memberSignupRequestDTO) {

        memberService.signupMember(memberSignupRequestDTO);

        return ApiResponse.successOnly(SuccessStatus.MEMBER_SIGNUP_SUCCESS);
    }

    @Operation(
            summary = "이메일/비밀번호 로그인",
            description = "이메일과 비밀번호로 사용자를 인증하고 Access/Refresh 토큰을 발급합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "일반 로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<MemberLoginResponseDTO>> login(
            @RequestBody MemberLoginRequestDTO memberLoginRequestDTO) {

        MemberLoginResponseDTO responseDTO = memberService.loginMember(memberLoginRequestDTO);

        return ApiResponse.success(SuccessStatus.FORM_LOGIN_SUCCESS, responseDTO);
    }

    @Operation(
            summary = "액세스 토큰 재발급",
            description = "만료된 액세스 토큰을 재발급합니다. 요청 헤더에 'X-Refresh-Token'으로 유효한 리프레시 토큰을 포함해야 합니다."
                + "[주의] Swagger 로 테스트 시 토큰 앞에 'Bearer ' 을 붙여야 함.",
            security = @SecurityRequirement(name = "X-Refresh-Token")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "리프레쉬 토큰이 없거나 유효하지 않습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원 정보가 존재하지 않습니다.")
    })
    @GetMapping("/token-reissue")
    public ResponseEntity<ApiResponse<Map<String, String>>> reissueToken(
            @RequestHeader(value = "X-Refresh-Token", required = false) String refreshToken) {

        // 리프레쉬 토큰이 존재하지 않을 경우 예외 처리
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new UnauthorizedException(ErrorStatus.UNAUTHORIZED_EMPTY_TOKEN.getMessage());
        }

        // "Bearer " 문자열 제거 후 토큰 검증
        String pureRefreshToken = refreshToken.substring(7);
        if (!jwtService.isTokenValid(pureRefreshToken)) {
            throw new UnauthorizedException(ErrorStatus.UNAUTHORIZED_INVALID_TOKEN.getMessage());
        }

        // DB에서 리프레쉬 토큰 존재여부 및 만료 확인
        RefreshToken savedRefreshToken = refreshTokenService.findByToken(pureRefreshToken)
                .orElseThrow(() -> new UnauthorizedException(ErrorStatus.UNAUTHORIZED_INVALID_TOKEN.getMessage()));

        if (refreshTokenService.isTokenExpired(savedRefreshToken)) {
            throw new UnauthorizedException(ErrorStatus.UNAUTHORIZED_TOKEN_EXPIRED.getMessage());
        }

        Member member = savedRefreshToken.getMember();

        // 새 Access, Refresh Token 생성 후 발급
        Map<String, String> newTokens = jwtService.createAccessAndRefreshToken(member);

        return ApiResponse.success(SuccessStatus.TOKEN_REISSUE_SUCCESS, newTokens);
    }

    @Operation(
            summary = "소셜 로그인 추가 정보 입력 API",
            description = "소셜 로그인 후 추가 정보를 입력합니다. <br>" +
                    "필수 정보: 생년월일, 전화번호, 결혼예정일, 타입(신랑/신부)",
            security = {
                    @SecurityRequirement(name = "Authorization")
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "추가 정보 입력 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력 값 오류 (전화번호 중복, 미인증 등)", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (유효하지 않은 토큰)", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원 정보가 존재하지 않습니다.", content = @Content)
    })
    @PostMapping("/social_login/additional_info")
    public ResponseEntity<ApiResponse<Void>> socialLogin(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestBody SocialMemberAdditionalRequestDTO socialMemberAdditionalRequestDTO
    ) {
        memberService.socialLogin(userDetails.getUsername(), socialMemberAdditionalRequestDTO);

        return ApiResponse.successOnly(SuccessStatus.MEMBER_SIGNUP_SUCCESS);
    }

    @Operation(
            summary = "마이페이지 조회 API",
            description = "현재 회원의 이름, 커플 여부, D-Day 등을 조회합니다. <br>" +
                    "액세스 토큰이 필요합니다.",
            security = {
                    @SecurityRequirement(name = "Authorization")
            }
    )
    @GetMapping("/mypage")
    public ResponseEntity<ApiResponse<MemberMyInfoResponseDTO>> getMyInfo(
            @RequestHeader("Authorization") String reqToken) {

        String token = reqToken.startsWith("Bearer ") ? reqToken.substring(7) : reqToken;
        Long memberId = jwtService.extractMemberId(token)
                .orElseThrow(() -> new UnauthorizedException(ErrorStatus.UNAUTHORIZED_INVALID_TOKEN.getMessage()));

        MemberMyInfoResponseDTO response = memberService.getMemberInfo(memberId);

        return ApiResponse.success(SuccessStatus.MEMBER_MYPAGE_GET_SUCCESS, response);
    }

    @Operation(
            summary = "FCM 디바이스 토큰 등록/업데이트",
            description = "클라이언트(앱)의 FCM 디바이스 토큰을 서버에 등록합니다. <br>" +
                    "로그인 성공 후, 클라이언트가 FCM 토큰을 발급받으면 반드시 호출해야 합니다.",
            security = @SecurityRequirement(name = "Authorization")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "FCM 토큰 값이 비어있음", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 다른 사용자가 등록한 토큰", content = @Content)
    })
    @PostMapping("/device")
    public ResponseEntity<ApiResponse<Void>> registerDeviceToken(
            @Parameter(hidden = true) @AuthenticationPrincipal SecurityMember securityMember,
            @RequestBody @Valid MemberDeviceRequestDTO requestDTO
    ) {
        memberService.registerOrUpdateDeviceToken(securityMember.getMember(), requestDTO);

        return ApiResponse.successOnly(SuccessStatus.MEMBER_DEVICE_FCM_TOKEN_REGISTER_SUCCESS);
    }
}
