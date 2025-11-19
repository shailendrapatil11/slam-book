package com.slambook.service;

import com.slambook.dto.request.SlamBookSettingsRequest;
import com.slambook.dto.request.UserProfileUpdateRequest;
import com.slambook.dto.response.UserBasicInfo;
import com.slambook.dto.response.UserProfileResponse;
import com.slambook.dto.response.UserResponse;
import com.slambook.exception.BadRequestException;
import com.slambook.exception.ForbiddenException;
import com.slambook.exception.NotFoundException;
import com.slambook.model.User;
import com.slambook.repository.UserRepository;
import com.slambook.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public Mono<UserResponse> getCurrentUser(CustomUserDetails userDetails) {
        return userRepository.findById(userDetails.getUserId())
                .switchIfEmpty(Mono.error(new NotFoundException("User not found")))
                .map(this::mapToUserResponse);
    }

    public Mono<UserResponse> updateProfile(CustomUserDetails userDetails, UserProfileUpdateRequest request) {
        return userRepository.findById(userDetails.getUserId())
                .switchIfEmpty(Mono.error(new NotFoundException("User not found")))
                .flatMap(user -> {
                    User.UserProfile profile = user.getProfile();
                    if (profile == null) {
                        profile = new User.UserProfile();
                    }

                    if (request.getFirstName() != null) profile.setFirstName(request.getFirstName());
                    if (request.getLastName() != null) profile.setLastName(request.getLastName());
                    if (request.getNickname() != null) profile.setNickname(request.getNickname());
                    if (request.getCourse() != null) profile.setCourse(request.getCourse());
                    if (request.getBatch() != null) profile.setBatch(request.getBatch());
                    if (request.getRollNumber() != null) profile.setRollNumber(request.getRollNumber());
                    if (request.getBio() != null) profile.setBio(request.getBio());
                    if (request.getInterests() != null) profile.setInterests(request.getInterests());
                    if (request.getSocialLinks() != null) profile.setSocialLinks(request.getSocialLinks());

                    user.setProfile(profile);
                    user.setUpdatedAt(LocalDateTime.now());

                    return userRepository.save(user);
                })
                .map(this::mapToUserResponse);
    }

    /**
     * Update profile picture URL
     */
    public Mono<UserResponse> updateProfilePicture(CustomUserDetails userDetails, String profilePictureUrl) {
        return userRepository.findById(userDetails.getUserId())
                .switchIfEmpty(Mono.error(new NotFoundException("User not found")))
                .flatMap(user -> {
                    User.UserProfile profile = user.getProfile();
                    if (profile == null) {
                        profile = new User.UserProfile();
                    }

                    profile.setProfilePicture(profilePictureUrl);
                    user.setProfile(profile);
                    user.setUpdatedAt(LocalDateTime.now());

                    return userRepository.save(user);
                })
                .map(this::mapToUserResponse)
                .doOnSuccess(response -> log.info("Profile picture updated for user: {}", userDetails.getUserId()));
    }

    /**
     * Delete profile picture
     */
    public Mono<UserResponse> deleteProfilePicture(CustomUserDetails userDetails) {
        return userRepository.findById(userDetails.getUserId())
                .switchIfEmpty(Mono.error(new NotFoundException("User not found")))
                .flatMap(user -> {
                    if (user.getProfile() != null) {
                        user.getProfile().setProfilePicture(null);
                        user.setUpdatedAt(LocalDateTime.now());
                        return userRepository.save(user);
                    }
                    return Mono.just(user);
                })
                .map(this::mapToUserResponse)
                .doOnSuccess(response -> log.info("Profile picture deleted for user: {}", userDetails.getUserId()));
    }

    public Mono<UserResponse> updateSlamBookSettings(CustomUserDetails userDetails, SlamBookSettingsRequest request) {
        return userRepository.findById(userDetails.getUserId())
                .switchIfEmpty(Mono.error(new NotFoundException("User not found")))
                .flatMap(user -> {
                    User.SlamBookSettings settings = user.getSlamBookSettings();
                    if (settings == null) {
                        settings = new User.SlamBookSettings();
                    }

                    if (request.getIsPublic() != null) settings.setIsPublic(request.getIsPublic());
                    if (request.getAllowAnonymous() != null) settings.setAllowAnonymous(request.getAllowAnonymous());
                    if (request.getCustomQuestionIds() != null) settings.setCustomQuestionIds(request.getCustomQuestionIds());
                    if (request.getTheme() != null) settings.setTheme(request.getTheme());

                    user.setSlamBookSettings(settings);
                    user.setUpdatedAt(LocalDateTime.now());

                    return userRepository.save(user);
                })
                .map(this::mapToUserResponse);
    }

    public Mono<UserResponse> getUserById(String userId, CustomUserDetails currentUser) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("User not found")))
                .flatMap(user -> {
                    // Check if user is in same college or is admin
                    if (!user.getCollegeId().equals(currentUser.getCollegeId()) &&
                            !"SUPER_ADMIN".equals(currentUser.getRole())) {
                        return Mono.error(new ForbiddenException("Cannot access user from different college"));
                    }
                    return Mono.just(user);
                })
                .map(this::mapToUserResponse);
    }

    public Flux<UserResponse> searchUsers(CustomUserDetails userDetails, String query, String batch, String course) {
        return userRepository.findByCollegeIdAndIsActive(userDetails.getCollegeId(), true)
                .filter(user -> {
                    if (query != null && !query.isEmpty()) {
                        String lowerQuery = query.toLowerCase();
                        return (user.getProfile() != null &&
                                (user.getProfile().getFirstName().toLowerCase().contains(lowerQuery) ||
                                        user.getProfile().getLastName().toLowerCase().contains(lowerQuery) ||
                                        (user.getProfile().getNickname() != null &&
                                                user.getProfile().getNickname().toLowerCase().contains(lowerQuery)))) ||
                                user.getEmail().toLowerCase().contains(lowerQuery);
                    }
                    return true;
                })
                .filter(user -> {
                    if (batch != null && !batch.isEmpty()) {
                        return user.getProfile() != null && batch.equals(user.getProfile().getBatch());
                    }
                    return true;
                })
                .filter(user -> {
                    if (course != null && !course.isEmpty()) {
                        return user.getProfile() != null && course.equals(user.getProfile().getCourse());
                    }
                    return true;
                })
                .filter(user -> user.getJoinRequest() != null &&
                        user.getJoinRequest().getStatus() == User.JoinRequestStatus.APPROVED)
                .map(this::mapToUserResponse);
    }

    public Flux<UserResponse> getPendingJoinRequests(CustomUserDetails userDetails) {
        return userRepository.findByCollegeIdAndJoinRequest_Status(
                userDetails.getCollegeId(),
                User.JoinRequestStatus.PENDING
        ).map(this::mapToUserResponse);
    }

    public Mono<UserResponse> approveJoinRequest(String userId, CustomUserDetails adminDetails) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("User not found")))
                .flatMap(user -> {
                    if (!user.getCollegeId().equals(adminDetails.getCollegeId())) {
                        return Mono.error(new ForbiddenException("Cannot approve user from different college"));
                    }

                    if (user.getJoinRequest().getStatus() != User.JoinRequestStatus.PENDING) {
                        return Mono.error(new BadRequestException("Join request is not pending"));
                    }

                    user.getJoinRequest().setStatus(User.JoinRequestStatus.APPROVED);
                    user.getJoinRequest().setApprovedBy(adminDetails.getUserId());
                    user.getJoinRequest().setApprovedAt(LocalDateTime.now());
                    user.setUpdatedAt(LocalDateTime.now());

                    return userRepository.save(user);
                })
                .flatMap(user -> {
                    // Send notification
                    return notificationService.sendJoinApprovedNotification(user)
                            .thenReturn(user);
                })
                .map(this::mapToUserResponse);
    }

    public Mono<UserResponse> rejectJoinRequest(String userId, CustomUserDetails adminDetails, String reason) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("User not found")))
                .flatMap(user -> {
                    if (!user.getCollegeId().equals(adminDetails.getCollegeId())) {
                        return Mono.error(new ForbiddenException("Cannot reject user from different college"));
                    }

                    if (user.getJoinRequest().getStatus() != User.JoinRequestStatus.PENDING) {
                        return Mono.error(new BadRequestException("Join request is not pending"));
                    }

                    user.getJoinRequest().setStatus(User.JoinRequestStatus.REJECTED);
                    user.getJoinRequest().setRejectionReason(reason);
                    user.setUpdatedAt(LocalDateTime.now());

                    return userRepository.save(user);
                })
                .flatMap(user -> {
                    // Send notification
                    return notificationService.sendJoinRejectedNotification(user, reason)
                            .thenReturn(user);
                })
                .map(this::mapToUserResponse);
    }

    public Flux<UserResponse> getAllUsers(CustomUserDetails userDetails) {
        return userRepository.findByCollegeId(userDetails.getCollegeId())
                .map(this::mapToUserResponse);
    }

    public Mono<UserBasicInfo> getUserBasicInfo(String userId) {
        return userRepository.findById(userId)
                .map(this::mapToUserBasicInfo);
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

    private UserBasicInfo mapToUserBasicInfo(User user) {
        if (user.getProfile() == null) {
            return UserBasicInfo.builder()
                    .id(user.getId())
                    .build();
        }

        return UserBasicInfo.builder()
                .id(user.getId())
                .firstName(user.getProfile().getFirstName())
                .lastName(user.getProfile().getLastName())
                .nickname(user.getProfile().getNickname())
                .profilePicture(user.getProfile().getProfilePicture())
                .course(user.getProfile().getCourse())
                .batch(user.getProfile().getBatch())
                .build();
    }
}