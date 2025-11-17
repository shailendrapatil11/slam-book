package com.slambook.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
@CompoundIndex(name = "email_college_idx", def = "{'email': 1, 'collegeId': 1}", unique = true)
public class User {

    @Id
    private String id;

    @Indexed
    private String collegeId;

    private String email;
    private String password;

    private UserRole role;
    private UserProfile profile;
    private SlamBookSettings slamBookSettings;
    private JoinRequest joinRequest;

    private Boolean emailVerified;
    private String verificationToken;
    private LocalDateTime verificationTokenExpiry;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    private Boolean isActive;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserProfile {
        private String firstName;
        private String lastName;
        private String nickname;
        private String profilePicture;
        private String course;
        private String batch;
        private String rollNumber;
        private String bio;
        private List<String> interests;
        private Map<String, String> socialLinks;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SlamBookSettings {
        private Boolean isPublic;
        private Boolean allowAnonymous;
        private List<String> customQuestionIds;
        private String theme;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JoinRequest {
        private JoinRequestStatus status;
        private LocalDateTime requestedAt;
        private String approvedBy;
        private LocalDateTime approvedAt;
        private String rejectionReason;
    }

    public enum UserRole {
        SUPER_ADMIN, COLLEGE_ADMIN, STUDENT
    }

    public enum JoinRequestStatus {
        PENDING, APPROVED, REJECTED
    }

    public String getFullName() {
        if (profile != null) {
            return profile.getFirstName() + " " + profile.getLastName();
        }
        return email;
    }
}