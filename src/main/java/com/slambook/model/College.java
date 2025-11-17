package com.slambook.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "colleges")
public class College {

    @Id
    private String id;

    @Indexed(unique = true)
    private String collegeCode;

    private String name;
    private String email;
    private String phone;
    private Address address;
    private String logo;

    private SubscriptionPlan subscriptionPlan;
    private SubscriptionStatus subscriptionStatus;
    private LocalDateTime subscriptionExpiry;
    private Integer maxUsers;

    private CollegeSettings settings;
    private CollegeStats stats;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address {
        private String street;
        private String city;
        private String state;
        private String country;
        private String pincode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CollegeSettings {
        private Boolean requireApproval;
        private Boolean allowPublicProfiles;
        private Boolean allowAnonymousEntries;
        private List<String> enabledFeatures;
        private Map<String, Object> customSettings;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CollegeStats {
        private Integer totalUsers;
        private Integer activeUsers;
        private Integer totalSlamBookEntries;
        private Integer pendingJoinRequests;
    }

    public enum SubscriptionPlan {
        BASIC, PREMIUM, ENTERPRISE
    }

    public enum SubscriptionStatus {
        ACTIVE, SUSPENDED, EXPIRED, TRIAL
    }
}