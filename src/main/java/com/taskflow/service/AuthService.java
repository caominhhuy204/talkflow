package com.taskflow.service;

import com.taskflow.dto.request.ForgotPasswordRequest;
import com.taskflow.dto.request.GoogleLoginRequest;
import com.taskflow.dto.request.LoginRequest;
import com.taskflow.dto.request.RegisterRequest;
import com.taskflow.dto.request.ResetPasswordRequest;
import com.taskflow.dto.request.UpdateProfileRequest;
import com.taskflow.dto.response.AuthResponse;
import com.taskflow.dto.response.PasswordResetInitResponse;
import com.taskflow.dto.response.UserProfileResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse loginWithGoogle(GoogleLoginRequest request);
    PasswordResetInitResponse forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
    UserProfileResponse me(String email);
    UserProfileResponse updateMe(String email, UpdateProfileRequest request);

}
