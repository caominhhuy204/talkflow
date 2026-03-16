package com.taskflow.controller;

import com.taskflow.dto.request.ForgotPasswordRequest;
import com.taskflow.dto.request.GoogleLoginRequest;
import com.taskflow.dto.request.LoginRequest;
import com.taskflow.dto.request.RegisterRequest;
import com.taskflow.dto.request.ResetPasswordRequest;
import com.taskflow.dto.request.UpdateProfileRequest;
import com.taskflow.dto.response.ApiResponse;
import com.taskflow.dto.response.AuthResponse;
import com.taskflow.dto.response.PasswordResetInitResponse;
import com.taskflow.dto.response.UserProfileResponse;
import com.taskflow.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/google")
    public AuthResponse loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        return authService.loginWithGoogle(request);
    }

    @PostMapping("/forgot-password")
    public ApiResponse<PasswordResetInitResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        PasswordResetInitResponse data = authService.forgotPassword(request);
        return ApiResponse.<PasswordResetInitResponse>builder()
                .message("If the email exists, a password reset token has been generated.")
                .data(data)
                .build();
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.<Void>builder()
                .message("Password has been reset successfully.")
                .build();
    }

    @GetMapping("/me")
    public UserProfileResponse me(Principal principal) {
        return authService.me(principal.getName());
    }

    @PutMapping("/me")
    public UserProfileResponse updateMe(Principal principal, @Valid @RequestBody UpdateProfileRequest request) {
        return authService.updateMe(principal.getName(), request);
    }
}
