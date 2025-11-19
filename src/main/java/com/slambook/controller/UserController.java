package com.slambook.controller;

import com.slambook.dto.request.SlamBookSettingsRequest;
import com.slambook.dto.request.UserProfileUpdateRequest;
import com.slambook.dto.response.ApiResponse;
import com.slambook.dto.response.UserResponse;
import com.slambook.security.CustomUserDetails;
import com.slambook.service.FileStorageService;
import com.slambook.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FileStorageService fileStorageService;

    @GetMapping("/me")
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Get current user: {}", userDetails.getUserId());
        return userService.getCurrentUser(userDetails)
                .map(user -> ResponseEntity.ok(ApiResponse.success(user)));
    }

    @PutMapping("/me")
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        log.info("Update profile for user: {}", userDetails.getUserId());
        return userService.updateProfile(userDetails, request)
                .map(user -> ResponseEntity.ok(ApiResponse.success("Profile updated successfully", user)));
    }

    /**
     * Upload profile picture and update user profile
     */
    @PostMapping("/me/profile-picture")
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> uploadProfilePicture(
            @RequestPart("file") FilePart file,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Upload profile picture for user: {}", userDetails.getUserId());

        return fileStorageService.uploadProfilePicture(file, userDetails.getUserId())
                .flatMap(url -> {
                    // Update user profile with new picture URL
                    return userService.updateProfilePicture(userDetails, url)
                            .map(user -> ResponseEntity.ok(
                                    ApiResponse.success("Profile picture uploaded successfully", user)
                            ));
                })
                .doOnSuccess(response -> log.info("Profile picture uploaded: {}", userDetails.getUserId()))
                .doOnError(error -> log.error("Failed to upload profile picture for user: {}",
                        userDetails.getUserId(), error));
    }

    /**
     * Delete profile picture
     */
    @DeleteMapping("/me/profile-picture")
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> deleteProfilePicture(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Delete profile picture for user: {}", userDetails.getUserId());

        return userService.getCurrentUser(userDetails)
                .flatMap(currentUser -> {
                    // Get current profile picture URL
                    String profilePictureUrl = currentUser.getProfile() != null
                            ? currentUser.getProfile().getProfilePicture()
                            : null;

                    if (profilePictureUrl == null) {
                        return Mono.just(ResponseEntity.ok(
                                ApiResponse.<UserResponse>success("No profile picture to delete", currentUser)
                        ));
                    }

                    // Delete file from storage
                    return fileStorageService.deleteFile(profilePictureUrl)
                            .then(userService.deleteProfilePicture(userDetails))
                            .map(user -> ResponseEntity.ok(
                                    ApiResponse.success("Profile picture deleted successfully", user)
                            ));
                });
    }

    @PutMapping("/me/slambook-settings")
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> updateSlamBookSettings(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SlamBookSettingsRequest request) {
        log.info("Update slam book settings for user: {}", userDetails.getUserId());
        return userService.updateSlamBookSettings(userDetails, request)
                .map(user -> ResponseEntity.ok(ApiResponse.success("Settings updated successfully", user)));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> getUserById(
            @PathVariable String id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Get user by id: {}", id);
        return userService.getUserById(id, userDetails)
                .map(user -> ResponseEntity.ok(ApiResponse.success(user)));
    }

    @GetMapping("/search")
    public Mono<ResponseEntity<ApiResponse<List<UserResponse>>>> searchUsers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String batch,
            @RequestParam(required = false) String course,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Search users with query: {}, batch: {}, course: {}", query, batch, course);
        return userService.searchUsers(userDetails, query, batch, course)
                .collectList()
                .map(users -> ResponseEntity.ok(ApiResponse.success(users)));
    }
}