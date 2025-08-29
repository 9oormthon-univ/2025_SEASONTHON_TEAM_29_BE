package com.wedit.backend.common.config.security;

import com.wedit.backend.api.member.jwt.filter.FilterExceptionHandler;
import com.wedit.backend.common.config.jwt.JwtConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtConfig jwtConfig;
    private final FilterExceptionHandler filterExceptionHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(Arrays.asList(
                            "https://*.wedit.me",
                            "https://localhost:3000"
                    ));
                    config.setAllowedMethods(Arrays.asList(
                            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
                    ));
                    config.setAllowCredentials(true);
                    config.setAllowedHeaders(Arrays.asList(
                            "Authorization",
                            "X-Refresh-Token",
                            "Content-Type",
                            "X-Requested-With",
                            "Accept",
                            "Origin"
                    ));
                    config.setMaxAge(3600L);
                    config.addExposedHeader("Authorization");
                    config.addExposedHeader("X-Refresh-Token");
                    return config;
                }))
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/api-doc/**",
                                "/api/v3/api-docs/**",
                                "/api/swagger-resources/**",
                                "/api/swagger-ui/**",
                                "/api/swagger-ui.html",
                                "/actuator/health",
                                "/actuator/info"
                        ).permitAll()   // Swagger, Spring Actuator 허가
                        .requestMatchers(
                                "/api/v1/member/**"
                        ).permitAll()   // Member 관련 허가
                        .anyRequest().authenticated()
                )   // OAuth2 도입 시 추가
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(filterExceptionHandler)   // 인증 실패 예외 핸들링
                        .accessDeniedHandler(filterExceptionHandler)        // 인가 실패 예외 핸들링
                );

        http.addFilterBefore(jwtConfig.jwtAuthenticationProcessingFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
