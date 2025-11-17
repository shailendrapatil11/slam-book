package com.slambook.dto.response;

import com.slambook.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String email;
    private User.UserRole role;
    private String collegeId;
    private String collegeCode;
    private String collegeName;
    private UserProfileResponse profile;
    private User.JoinRequestStatus joinRequestStatus;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}
