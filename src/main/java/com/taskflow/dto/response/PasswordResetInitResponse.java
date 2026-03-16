package com.taskflow.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PasswordResetInitResponse {
    private String resetToken;
    private Long expiresInMinutes;
}
