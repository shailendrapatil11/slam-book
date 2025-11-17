package com.slambook.service;

import com.slambook.dto.request.CollegeCreateRequest;
import com.slambook.dto.request.CollegeSettingsRequest;
import com.slambook.dto.request.CollegeUpdateRequest;
import com.slambook.dto.response.CollegeResponse;
import com.slambook.dto.response.CollegeStatsResponse;
import com.slambook.exception.BadRequestException;
import com.slambook.exception.NotFoundException;
import com.slambook.model.College;
import com.slambook.model.User;
import com.slambook.repository.CollegeRepository;
import com.slambook.repository.SlamBookEntryRepository;
import com.slambook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollegeService {

    private final CollegeRepository collegeRepository;
    private final UserRepository userRepository;
    private final SlamBookEntryRepository slamBookEntryRepository;
    private final PasswordEncoder passwordEncoder;

    public Mono<CollegeResponse> createCollege(CollegeCreateRequest request) {
        // Generate unique college code
        String collegeCode = generateCollegeCode(request.getName());

        return collegeRepository.existsByCollegeCode(collegeCode)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new BadRequestException("College code already exists"));
                    }

                    College college = College.builder()
                            .collegeCode(collegeCode)
                            .name(request.getName())
                            .email(request.getEmail())
                            .phone(request.getPhone())
                            .address(mapToAddress(request.getAddress()))
                            .subscriptionPlan(request.getSubscriptionPlan())
                            .subscriptionStatus(College.SubscriptionStatus.ACTIVE)
                            .subscriptionExpiry(request.getSubscriptionExpiry())
                            .maxUsers(request.getMaxUsers() != null ? request.getMaxUsers() : getDefaultMaxUsers(request.getSubscriptionPlan()))
                            .settings(College.CollegeSettings.builder()
                                    .requireApproval(true)
                                    .allowPublicProfiles(true)
                                    .allowAnonymousEntries(true)
                                    .enabledFeatures(new ArrayList<>())
                                    .build())
                            .stats(College.CollegeStats.builder()
                                    .totalUsers(0)
                                    .activeUsers(0)
                                    .totalSlamBookEntries(0)
                                    .pendingJoinRequests(0)
                                    .build())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .isActive(true)
                            .build();

                    return collegeRepository.save(college);
                })
                .flatMap(college -> {
                    // Create default admin user
                    User adminUser = User.builder()
                            .collegeId(college.getId())
                            .email(request.getEmail())
                            .password(passwordEncoder.encode("Admin@123"))  // Default password
                            .role(User.UserRole.COLLEGE_ADMIN)
                            .profile(User.UserProfile.builder()
                                    .firstName("Admin")
                                    .lastName(college.getName())
                                    .build())
                            .joinRequest(User.JoinRequest.builder()
                                    .status(User.JoinRequestStatus.APPROVED)
                                    .requestedAt(LocalDateTime.now())
                                    .approvedAt(LocalDateTime.now())
                                    .build())
                            .emailVerified(true)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .isActive(true)
                            .build();

                    return userRepository.save(adminUser).thenReturn(college);
                })
                .map(this::mapToCollegeResponse)
                .doOnSuccess(response -> log.info("College created: {} with code: {}",
                        request.getName(), response.getCollegeCode()));
    }

    public Mono<CollegeResponse> updateCollege(String id, CollegeUpdateRequest request) {
        return collegeRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("College not found")))
                .flatMap(college -> {
                    if (request.getName() != null) college.setName(request.getName());
                    if (request.getEmail() != null) college.setEmail(request.getEmail());
                    if (request.getPhone() != null) college.setPhone(request.getPhone());
                    if (request.getAddress() != null) college.setAddress(mapToAddress(request.getAddress()));
                    if (request.getLogo() != null) college.setLogo(request.getLogo());

                    college.setUpdatedAt(LocalDateTime.now());

                    return collegeRepository.save(college);
                })
                .map(this::mapToCollegeResponse);
    }

    public Mono<CollegeResponse> updateCollegeSettings(String collegeId, CollegeSettingsRequest request) {
        return collegeRepository.findById(collegeId)
                .switchIfEmpty(Mono.error(new NotFoundException("College not found")))
                .flatMap(college -> {
                    College.CollegeSettings settings = college.getSettings();
                    if (settings == null) {
                        settings = new College.CollegeSettings();
                    }

                    if (request.getRequireApproval() != null) {
                        settings.setRequireApproval(request.getRequireApproval());
                    }
                    if (request.getAllowPublicProfiles() != null) {
                        settings.setAllowPublicProfiles(request.getAllowPublicProfiles());
                    }
                    if (request.getAllowAnonymousEntries() != null) {
                        settings.setAllowAnonymousEntries(request.getAllowAnonymousEntries());
                    }
                    if (request.getEnabledFeatures() != null) {
                        settings.setEnabledFeatures(request.getEnabledFeatures());
                    }

                    college.setSettings(settings);
                    college.setUpdatedAt(LocalDateTime.now());

                    return collegeRepository.save(college);
                })
                .map(this::mapToCollegeResponse);
    }

    public Mono<CollegeResponse> getCollegeById(String id) {
        return collegeRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("College not found")))
                .flatMap(this::enrichCollegeStats)
                .map(this::mapToCollegeResponse);
    }

    public Flux<CollegeResponse> getAllColleges() {
        return collegeRepository.findAll()
                .flatMap(this::enrichCollegeStats)
                .map(this::mapToCollegeResponse);
    }

    public Mono<CollegeResponse> getCollegeByCode(String code) {
        return collegeRepository.findByCollegeCode(code)
                .switchIfEmpty(Mono.error(new NotFoundException("College not found with code: " + code)))
                .flatMap(this::enrichCollegeStats)
                .map(this::mapToCollegeResponse);
    }

    public Mono<Void> deleteCollege(String id) {
        return collegeRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("College not found")))
                .flatMap(college -> {
                    college.setIsActive(false);
                    college.setUpdatedAt(LocalDateTime.now());
                    return collegeRepository.save(college);
                })
                .then();
    }

    private Mono<College> enrichCollegeStats(College college) {
        return Mono.zip(
                userRepository.countByCollegeId(college.getId()),
                userRepository.countByCollegeIdAndIsActive(college.getId(), true),
                slamBookEntryRepository.countByCollegeId(college.getId()),
                userRepository.countByCollegeIdAndJoinRequest_Status(college.getId(), User.JoinRequestStatus.PENDING)
        ).map(tuple -> {
            college.setStats(College.CollegeStats.builder()
                    .totalUsers(tuple.getT1().intValue())
                    .activeUsers(tuple.getT2().intValue())
                    .totalSlamBookEntries(tuple.getT3().intValue())
                    .pendingJoinRequests(tuple.getT4().intValue())
                    .build());
            return college;
        });
    }

    private String generateCollegeCode(String collegeName) {
        String base = collegeName.toUpperCase()
                .replaceAll("[^A-Z0-9]", "")
                .substring(0, Math.min(4, collegeName.length()));
        String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return base + random;
    }

    private Integer getDefaultMaxUsers(College.SubscriptionPlan plan) {
        return switch (plan) {
            case BASIC -> 100;
            case PREMIUM -> 500;
            case ENTERPRISE -> Integer.MAX_VALUE;
        };
    }

    private College.Address mapToAddress(com.slambook.dto.request.AddressRequest addressRequest) {
        return College.Address.builder()
                .street(addressRequest.getStreet())
                .city(addressRequest.getCity())
                .state(addressRequest.getState())
                .country(addressRequest.getCountry())
                .pincode(addressRequest.getPincode())
                .build();
    }

    private CollegeResponse mapToCollegeResponse(College college) {
        return CollegeResponse.builder()
                .id(college.getId())
                .collegeCode(college.getCollegeCode())
                .name(college.getName())
                .email(college.getEmail())
                .phone(college.getPhone())
                .address(college.getAddress())
                .logo(college.getLogo())
                .subscriptionPlan(college.getSubscriptionPlan())
                .subscriptionStatus(college.getSubscriptionStatus())
                .subscriptionExpiry(college.getSubscriptionExpiry())
                .maxUsers(college.getMaxUsers())
                .settings(college.getSettings())
                .stats(college.getStats() != null ? CollegeStatsResponse.builder()
                        .totalUsers(college.getStats().getTotalUsers())
                        .activeUsers(college.getStats().getActiveUsers())
                        .totalSlamBookEntries(college.getStats().getTotalSlamBookEntries())
                        .pendingJoinRequests(college.getStats().getPendingJoinRequests())
                        .build() : null)
                .createdAt(college.getCreatedAt())
                .isActive(college.getIsActive())
                .build();
    }
}