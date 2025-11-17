package com.slambook.service;

import com.slambook.dto.request.LoginRequest;
import com.slambook.dto.request.RegisterRequest;
import com.slambook.dto.response.AuthResponse;
import com.slambook.dto.response.UserProfileResponse;
import com.slambook.dto.response.UserResponse;
import com.slambook.exception.BadRequestException;
import com.slambook.exception.NotFoundException;
import com.slambook.exception.UnauthorizedException;
import com.slambook.model.User;
import com.slambook.repository.CollegeRepository;
import com.slambook.repository.UserRepository;
import com.slambook.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CollegeRepository collegeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public Mono<AuthResponse> login(LoginRequest request) {
        // Super admin login (no college code required)
        if (request.getCollegeCode() == null) {
            return userRepository.findByEmail(request.getEmail())
                    .filter(user -> user.getRole() == User.UserRole.SUPER_ADMIN)
                    .switchIfEmpty(Mono.error(new UnauthorizedException("Invalid credentials")))
                    .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPassword()))
                    .switchIfEmpty(Mono.error(new UnauthorizedException("Invalid credentials")))
                    .flatMap(this::generateAuthResponse)
                    .doOnSuccess(response -> log.info("Super admin logged in: {}", request.getEmail()));
        }

        // Regular user login
        return collegeRepository.findByCollegeCode(request.getCollegeCode())
                .switchIfEmpty(Mono.error(new NotFoundException("College not found")))
                .flatMap(college -> userRepository.findByEmailAndCollegeId(request.getEmail(), college.getId()))
                .switchIfEmpty(Mono.error(new UnauthorizedException("Invalid credentials")))
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .switchIfEmpty(Mono.error(new UnauthorizedException("Invalid credentials")))
                .filter(user -> user.getJoinRequest() != null &&
                        user.getJoinRequest().getStatus() == User.JoinRequestStatus.APPROVED)
                .switchIfEmpty(Mono.error(new UnauthorizedException("Account not approved yet")))
                .flatMap(user -> {
                    user.setLastLoginAt(LocalDateTime.now());
                    return userRepository.save(user);
                })
                .flatMap(this::generateAuthResponse)
                .doOnSuccess(response -> log.info("User logged in: {}", request.getEmail()));
    }

    public Mono<AuthResponse> register(RegisterRequest request) {
        return collegeRepository.findByCollegeCode(request.getCollegeCode())
                .switchIfEmpty(Mono.error(new NotFoundException("College not found with code: " + request.getCollegeCode())))
                .flatMap(college -> {
                    // Check if college is active
                    if (!college.getIsActive()) {
                        return Mono.error(new BadRequestException("College is not active"));
                    }

                    // Check if user already exists
                    return userRepository.existsByEmailAndCollegeId(request.getEmail(), college.getId())
                            .flatMap(exists -> {
                                if (exists) {
                                    return Mono.error(new BadRequestException("User already exists with this email"));
                                }

                                // Create new user
                                User newUser = User.builder()
                                        .collegeId(college.getId())
                                        .email(request.getEmail())
                                        .password(passwordEncoder.encode(request.getPassword()))
                                        .role(User.UserRole.STUDENT)
                                        .profile(User.UserProfile.builder()
                                                .firstName(request.getFirstName())
                                                .lastName(request.getLastName())
                                                .course(request.getCourse())
                                                .batch(request.getBatch())
                                                .rollNumber(request.getRollNumber())
                                                .build())
                                        .slamBookSettings(User.SlamBookSettings.builder()
                                                .isPublic(true)
                                                .allowAnonymous(true)
                                                .build())
                                        .joinRequest(User.JoinRequest.builder()
                                                .status(college.getSettings() != null &&
                                                        college.getSettings().getRequireApproval() != null &&
                                                        college.getSettings().getRequireApproval()
                                                        ? User.JoinRequestStatus.PENDING
                                                        : User.JoinRequestStatus.APPROVED)
                                                .requestedAt(LocalDateTime.now())
                                                .build())
                                        .emailVerified(false)
                                        .verificationToken(UUID.randomUUID().toString())
                                        .verificationTokenExpiry(LocalDateTime.now().plusHours(24))
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .isActive(true)
                                        .build();

                                return userRepository.save(newUser);
                            });
                })
                .flatMap(user -> {
                    // If auto-approved, generate tokens
                    if (user.getJoinRequest().getStatus() == User.JoinRequestStatus.APPROVED) {
                        return generateAuthResponse(user);
                    } else {
                        // If pending approval, return response without tokens
                        return Mono.just(AuthResponse.builder()
                                .user(mapToUserResponse(user))
                                .build());
                    }
                })
                .doOnSuccess(response -> log.info("User registered: {}", request.getEmail()));
    }

    public Mono<AuthResponse> refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            return Mono.error(new UnauthorizedException("Invalid refresh token"));
        }

        if (!"REFRESH".equals(jwtTokenProvider.getTokenType(refreshToken))) {
            return Mono.error(new UnauthorizedException("Invalid token type"));
        }

        String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("User not found")))
                .flatMap(this::generateAuthResponse);
    }

    private Mono<AuthResponse> generateAuthResponse(User user) {
        return collegeRepository.findById(user.getCollegeId())
                .map(college -> {
                    String accessToken = jwtTokenProvider.generateAccessToken(
                            user.getId(),
                            user.getEmail(),
                            user.getRole().name(),
                            user.getCollegeId()
                    );

                    String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

                    UserResponse userResponse = mapToUserResponse(user);
                    userResponse.setCollegeCode(college.getCollegeCode());
                    userResponse.setCollegeName(college.getName());

                    return AuthResponse.builder()
                            .accessToken(accessToken)
                            .refreshToken(refreshToken)
                            .tokenType("Bearer")
                            .expiresIn(900L) // 15 minutes
                            .user(userResponse)
                            .build();
                })
                .switchIfEmpty(Mono.just(AuthResponse.builder()
                        .accessToken(jwtTokenProvider.generateAccessToken(
                                user.getId(),
                                user.getEmail(),
                                user.getRole().name(),
                                user.getCollegeId()
                        ))
                        .refreshToken(jwtTokenProvider.generateRefreshToken(user.getId()))
                        .tokenType("Bearer")
                        .expiresIn(900L)
                        .user(mapToUserResponse(user))
                        .build()));
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .collegeId(user.getCollegeId())
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();

        if (user.getProfile() != null) {
            response.setProfile(UserProfileResponse.builder()
                    .firstName(user.getProfile().getFirstName())
                    .lastName(user.getProfile().getLastName())
                    .nickname(user.getProfile().getNickname())
                    .profilePicture(user.getProfile().getProfilePicture())
                    .course(user.getProfile().getCourse())
                    .batch(user.getProfile().getBatch())
                    .rollNumber(user.getProfile().getRollNumber())
                    .bio(user.getProfile().getBio())
                    .interests(user.getProfile().getInterests())
                    .socialLinks(user.getProfile().getSocialLinks())
                    .build());
        }

        if (user.getJoinRequest() != null) {
            response.setJoinRequestStatus(user.getJoinRequest().getStatus());
        }

        return response;
    }
}