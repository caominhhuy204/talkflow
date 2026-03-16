package com.taskflow.service.impl;

import com.taskflow.dto.request.ForgotPasswordRequest;
import com.taskflow.dto.request.GoogleLoginRequest;
import com.taskflow.dto.request.LoginRequest;
import com.taskflow.dto.request.RegisterRequest;
import com.taskflow.dto.request.ResetPasswordRequest;
import com.taskflow.dto.request.UpdateProfileRequest;
import com.taskflow.dto.response.AuthResponse;
import com.taskflow.dto.response.PasswordResetInitResponse;
import com.taskflow.dto.response.UserProfileResponse;
import com.taskflow.entity.Role;
import com.taskflow.entity.User;
import com.taskflow.exception.BadRequestException;
import com.taskflow.repository.UserRepository;
import com.taskflow.security.JwtService;
import com.taskflow.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final Set<String> allowedGoogleClientIds;
    private final SecureRandom secureRandom = new SecureRandom();
    private final long resetTokenTtlMinutes;
    private final boolean exposeResetTokenInResponse;
    private final RestClient restClient = RestClient.create();

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtService jwtService,
                           @Value("${app.auth.google-client-id:}") String googleClientId,
                           @Value("${app.auth.google-client-ids:}") String googleClientIds,
                           @Value("${app.auth.password-reset.token-ttl-minutes:30}") long resetTokenTtlMinutes,
                           @Value("${app.auth.password-reset.expose-token:false}") boolean exposeResetTokenInResponse) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.allowedGoogleClientIds = buildAllowedGoogleClientIds(googleClientId, googleClientIds);
        this.resetTokenTtlMinutes = resetTokenTtlMinutes;
        this.exposeResetTokenInResponse = exposeResetTokenInResponse;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }
        if (request.getRole() == Role.ADMIN) {
            throw new BadRequestException("Cannot register with ADMIN role");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser.getEmail());

        return AuthResponse.builder()
                .token(token)
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        String token = jwtService.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public AuthResponse loginWithGoogle(GoogleLoginRequest request) {
        GoogleTokenPayload tokenPayload = verifyGoogleToken(request.getIdToken());

        User user = userRepository.findByEmail(tokenPayload.email())
                .orElseGet(() -> registerGoogleUser(tokenPayload));

        String token = jwtService.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public PasswordResetInitResponse forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String resetToken = null;
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            resetToken = generateResetToken();
            user.setResetPasswordTokenHash(hashToken(resetToken));
            user.setResetPasswordTokenExpiresAt(LocalDateTime.now().plusMinutes(resetTokenTtlMinutes));
            userRepository.save(user);
            log.info("Password reset token generated for user {}", email);
        }

        return PasswordResetInitResponse.builder()
                .resetToken(exposeResetTokenInResponse ? resetToken : null)
                .expiresInMinutes(resetTokenTtlMinutes)
                .build();
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        String tokenHash = hashToken(request.getToken().trim());
        LocalDateTime now = LocalDateTime.now();

        User user = userRepository
                .findByResetPasswordTokenHashAndResetPasswordTokenExpiresAtAfter(tokenHash, now)
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetPasswordTokenHash(null);
        user.setResetPasswordTokenExpiresAt(null);
        userRepository.save(user);
    }

    @Override
    public UserProfileResponse me(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        return toProfileResponse(user);
    }

    @Override
    public UserProfileResponse updateMe(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        boolean hasUpdate = false;

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName().trim());
            hasUpdate = true;
        }

        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            hasUpdate = true;
        }

        if (!hasUpdate) {
            throw new BadRequestException("No valid profile fields to update");
        }

        User updated = userRepository.save(user);
        return toProfileResponse(updated);
    }

    private UserProfileResponse toProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .departmentCode(user.getDepartment() != null ? user.getDepartment().getCode() : null)
                .managerId(user.getManager() != null ? user.getManager().getId() : null)
                .build();
    }

    private GoogleTokenPayload verifyGoogleToken(String idToken) {
        if (allowedGoogleClientIds.isEmpty()) {
            throw new BadRequestException("Google login is not configured");
        }

        Map<String, Object> payload;
        try {
            payload = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("oauth2.googleapis.com")
                            .path("/tokeninfo")
                            .queryParam("id_token", idToken)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (RestClientException ex) {
            throw new BadRequestException("Invalid Google token");
        }

        if (payload == null) {
            throw new BadRequestException("Invalid Google token payload");
        }

        String audience = String.valueOf(payload.getOrDefault("aud", ""));
        if (!allowedGoogleClientIds.contains(audience)) {
            throw new BadRequestException("Google token audience mismatch");
        }

        String email = String.valueOf(payload.getOrDefault("email", "")).trim().toLowerCase();
        if (email.isBlank()) {
            throw new BadRequestException("Google account does not provide email");
        }

        boolean emailVerified = "true".equalsIgnoreCase(String.valueOf(payload.getOrDefault("email_verified", "false")));
        if (!emailVerified) {
            throw new BadRequestException("Google email is not verified");
        }

        String fullName = String.valueOf(payload.getOrDefault("name", "")).trim();
        return new GoogleTokenPayload(email, fullName);
    }

    private User registerGoogleUser(GoogleTokenPayload tokenPayload) {
        String fullName = tokenPayload.fullName().isBlank()
                ? tokenPayload.email().split("@")[0]
                : tokenPayload.fullName();

        User user = User.builder()
                .fullName(fullName)
                .email(tokenPayload.email())
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.EMPLOYEE)
                .enabled(true)
                .build();

        return userRepository.save(user);
    }

    private record GoogleTokenPayload(String email, String fullName) {
    }

    private Set<String> buildAllowedGoogleClientIds(String singleGoogleClientId, String multipleGoogleClientIds) {
        LinkedHashSet<String> allowedIds = new LinkedHashSet<>();

        if (singleGoogleClientId != null && !singleGoogleClientId.isBlank()) {
            allowedIds.add(singleGoogleClientId.trim());
        }

        if (multipleGoogleClientIds != null && !multipleGoogleClientIds.isBlank()) {
            allowedIds.addAll(Arrays.stream(multipleGoogleClientIds.split(","))
                    .map(String::trim)
                    .filter(value -> !value.isBlank())
                    .collect(Collectors.toSet()));
        }

        return Set.copyOf(allowedIds);
    }

    private String generateResetToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm not available", ex);
        }
    }
}
