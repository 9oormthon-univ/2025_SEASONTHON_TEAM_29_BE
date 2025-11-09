package com.wedit.backend.api.member.jwt.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wedit.backend.api.member.entity.Member;
import com.wedit.backend.api.member.jwt.entity.RefreshToken;
import com.wedit.backend.api.member.jwt.repository.RefreshTokenRepository;
import com.wedit.backend.api.member.jwt.service.JwtService;
import com.wedit.backend.api.member.repository.MemberRepository;
import com.wedit.backend.common.config.security.entity.SecurityMember;
import com.wedit.backend.common.response.ApiResponse;
import com.wedit.backend.common.response.SuccessStatus;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {

    @Value("${jwt.access.header}")
    private String accessTokenHeader;

    @Value("${jwt.refresh.header}")
    private String refreshTokenHeader;

    private static final String TOKEN_REISSUE_URL = "/api/v1/member/token-reissue";

    private final JwtService jwtService;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    // 필터링 제외 목록
    public static final String[] NOT_FILTER_URLS = {
            "/api/swagger-resources/**",
            "/api/swagger-ui/**",
            "/api/swagger-ui.html",
            "/api/v3/api-docs/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api/webjars/**",
            "/webjars/**"
    };

    /// 엔드포인트 필터링 제외
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String requestURI = request.getRequestURI();

//        log.info("JwtFilter shouldNotFilter 프로세싱 요청: {}", requestURI);

        for (String url : NOT_FILTER_URLS) {
            if (requestURI.startsWith(url)) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        log.info("Value of accessTokenHeader: {}", accessTokenHeader);
        log.info("Value of refreshTokenHeader: {}", refreshTokenHeader);

        String requestURI = request.getRequestURI();

        try {
            // /token-reissue 로 요청했는가?
            if (requestURI.equals(TOKEN_REISSUE_URL)) {

                // 리프레쉬 토큰 요청 헤더에서 추출 후 유효성 검사
                Optional<String> refreshTokenOpt = extractToken(request, refreshTokenHeader)
                        .filter(jwtService::isTokenValid);
                log.info("Extracted Refresh Token: {}", refreshTokenOpt.orElse("없음"));

                // 리프레쉬 토큰이 비어있다면 예외
                if (refreshTokenOpt.isEmpty()) {
                    throw new JwtException("유효하지 않거나 존재하지 않는 리프레쉬 토큰입니다.");
                }

                String refreshToken = refreshTokenOpt.get();
                log.info("Refresh Token to find in DB: {}", refreshToken);

                // 리프레쉬 토큰으로 새 토큰 발급 후 인증 컨텍스트 설정
                handleRefreshToken(response, refreshToken);

                return;
            }

            // 액세스 토큰 요청 헤더에서 추출 후 유효성 검사, 인증 컨텍스트 설정
            Optional<String> accessTokenOpt = extractToken(request, accessTokenHeader)
                    .filter(jwtService::isTokenValid);
            log.info("Extracted Access Token: {}", accessTokenOpt.orElse("없음"));

            accessTokenOpt.ifPresent(token -> jwtService.extractEmail(token)
                    .flatMap(memberRepository::findByEmail)
                    .ifPresent(this::setAuthentication));

            // 이후 필터 체인 진행
            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            // 필터단에서 발생하는 예외를 적절한 타이의 예외로 포장하여 상위로 던짐
            // -> FilterExceptionHandler
            log.error("JwtAuthenticationProcessingFilter - Uncaught exception: [{}], a ServletException will be thrown.", ex.getClass().getName(), ex);
            if (ex instanceof ServletException) {
                throw (ServletException) ex;
            } else if (ex instanceof IOException) {
                throw (IOException) ex;
            }  else {
                throw new ServletException(ex);
            }
        }
    }

    /// Refresh Token을 처리하여 Access Token 재발급 및 인증 처리
    private void handleRefreshToken(HttpServletResponse response, String refreshToken) throws IOException {

        RefreshToken savedRefreshToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new JwtException("저장된 리프레쉬 토큰이 없습니다."));

        // 토큰 만료 여부 검사, 만료 시 예외 발생
        if (savedRefreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new JwtException("만료된 리프레쉬 토큰입니다.");
        }

        Member member = savedRefreshToken.getMember();

        // 새로운 Access, Refresh Token 생성
        Map<String, String> newTokens = jwtService.createAccessAndRefreshToken(member);

        // 응답 코드와 컨텐츠 타입 설정
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");

        // 새로 발급한 토큰을 응답 헤더에 담아 전송
        response.setHeader(accessTokenHeader, "Bearer " + newTokens.get("accessToken"));
        response.setHeader(refreshTokenHeader, "Bearer " + newTokens.get("refreshToken"));

        // 응답 바디 메시지 작성
        String json = new ObjectMapper().writeValueAsString(ApiResponse.success(SuccessStatus.TOKEN_REISSUE_SUCCESS, newTokens));
        response.getWriter().write(json);

        setAuthentication(member);

        log.info("Access, Refresh Token 재발급 완료");
    }

    /// 토큰 추출 유틸 메서드
    private Optional<String> extractToken(HttpServletRequest request, String header) {

        String bearerToken = request.getHeader(header);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return Optional.of(bearerToken.substring(7));
        }

        return Optional.empty();
    }

    /// Member -> SecurityMember 로 생성 후 SecurityContext 에 등록
    private void setAuthentication(Member member) {

        SecurityMember securityMember = SecurityMember.from(member);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                securityMember, null, securityMember.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
