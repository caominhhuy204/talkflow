package com.taskflow.controller;

import com.taskflow.dto.request.GoogleLoginRequest;
import com.taskflow.dto.request.LoginRequest;
import com.taskflow.dto.request.RegisterRequest;
import com.taskflow.dto.request.UpdateProfileRequest;
import com.taskflow.dto.response.AuthResponse;
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

    @GetMapping("/me")
    public UserProfileResponse me(Principal principal) {
        return authService.me(principal.getName());
    }

    @PutMapping("/me")
    public UserProfileResponse updateMe(Principal principal, @Valid @RequestBody UpdateProfileRequest request) {
        return authService.updateMe(principal.getName(), request);
    }
}
