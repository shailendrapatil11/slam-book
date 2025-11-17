package com.slambook.controller;

import com.slambook.dto.request.CollegeSettingsRequest;
import com.slambook.dto.request.JoinRequestActionRequest;
import com.slambook.dto.response.ApiResponse;
import com.slambook.dto.response.CollegeResponse;
import com.slambook.dto.response.UserResponse;
import com.slambook.security.CustomUserDetails;
import com.slambook.service.CollegeService;
import com.slambook.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/college")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('COLLEGE_ADMIN', 'SUPER_ADMIN')")
public class CollegeAdminController {

    private final UserService userService;
    private final CollegeService collegeService;

    @GetMapping("/join-requests")
    public Mono<ResponseEntity<ApiResponse<List<UserResponse>>>> getPendingJoinRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Get pending join requests for college: {}", userDetails.getCollegeId());
        return userService.getPendingJoinRequests(userDetails)
                .collectList()
                .map(users -> ResponseEntity.ok(ApiResponse.success(users)));
    }

    @PutMapping("/join-requests/{userId}/approve")
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> approveJoinRequest(
            @PathVariable String userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Approve join request for user: {} by admin: {}", userId, userDetails.getUserId());
        return userService.approveJoinRequest(userId, userDetails)
                .map(user -> ResponseEntity.ok(ApiResponse.success("Join request approved", user)));
    }

    @PutMapping("/join-requests/{userId}/reject")
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> rejectJoinRequest(
            @PathVariable String userId,
            @Valid @RequestBody JoinRequestActionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Reject join request for user: {} by admin: {}", userId, userDetails.getUserId());
        return userService.rejectJoinRequest(userId, userDetails, request.getRejectionReason())
                .map(user -> ResponseEntity.ok(ApiResponse.success("Join request rejected", user)));
    }

    @GetMapping("/users")
    public Mono<ResponseEntity<ApiResponse<List<UserResponse>>>> getAllUsers(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Get all users for college: {}", userDetails.getCollegeId());
        return userService.getAllUsers(userDetails)
                .collectList()
                .map(users -> ResponseEntity.ok(ApiResponse.success(users)));
    }

    @PutMapping("/settings")
    public Mono<ResponseEntity<ApiResponse<CollegeResponse>>> updateSettings(
            @Valid @RequestBody CollegeSettingsRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Update settings for college: {}", userDetails.getCollegeId());
        return collegeService.updateCollegeSettings(userDetails.getCollegeId(), request)
                .map(college -> ResponseEntity.ok(ApiResponse.success("Settings updated", college)));
    }

    @GetMapping("/info")
    public Mono<ResponseEntity<ApiResponse<CollegeResponse>>> getCollegeInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Get college info: {}", userDetails.getCollegeId());
        return collegeService.getCollegeById(userDetails.getCollegeId())
                .map(college -> ResponseEntity.ok(ApiResponse.success(college)));
    }
}