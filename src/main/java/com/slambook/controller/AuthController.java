package com.slambook.controller;

import com.slambook.dto.request.LoginRequest;
import com.slambook.dto.request.RefreshTokenRequest;
import com.slambook.dto.request.RegisterRequest;
import com.slambook.dto.response.ApiResponse;
import com.slambook.dto.response.AuthResponse;
import com.slambook.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Mono<ResponseEntity<ApiResponse<AuthResponse>>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());
        return authService.login(request)
                .map(authResponse -> ResponseEntity.ok(ApiResponse.success("Login successful", authResponse)));
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<ApiResponse<AuthResponse>>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration attempt for email: {} at college: {}",
                request.getEmail(), request.getCollegeCode());
        return authService.register(request)
                .map(authResponse -> {
                    if (authResponse.getAccessToken() != null) {
                        return ResponseEntity.ok(ApiResponse.success("Registration successful", authResponse));
                    } else {
                        return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success("Registration successful. Waiting for admin approval.", authResponse));
                    }
                });
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<ApiResponse<AuthResponse>>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Refresh token attempt");
        return authService.refreshToken(request.getRefreshToken())
                .map(authResponse -> ResponseEntity.ok(ApiResponse.success("Token refreshed", authResponse)));
    }

    @GetMapping("/verify-email")
    public Mono<ResponseEntity<ApiResponse<String>>> verifyEmail(@RequestParam String token) {
        log.info("Email verification attempt");
        // TODO: Implement email verification
        return Mono.just(ResponseEntity.ok(ApiResponse.success("Email verified successfully", "OK")));
    }

    @PostMapping("/forgot-password")
    public Mono<ResponseEntity<ApiResponse<String>>> forgotPassword(@Valid @RequestBody com.slambook.dto.request.ForgotPasswordRequest request) {
        log.info("Forgot password request for email: {}", request.getEmail());
        // TODO: Implement forgot password
        return Mono.just(ResponseEntity.ok(ApiResponse.success("Password reset email sent", "OK")));
    }
}