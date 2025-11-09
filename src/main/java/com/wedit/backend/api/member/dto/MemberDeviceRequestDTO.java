package com.wedit.backend.api.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberDeviceRequestDTO {

    @NotBlank(message = "FCM 토큰은 비어있을 수 없습니다.")
    private String fcmToken;
}
