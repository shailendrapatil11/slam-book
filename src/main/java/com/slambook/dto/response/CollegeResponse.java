package com.slambook.dto.response;

import com.slambook.model.College;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeResponse {
    private String id;
    private String collegeCode;
    private String name;
    private String email;
    private String phone;
    private College.Address address;
    private String logo;
    private College.SubscriptionPlan subscriptionPlan;
    private College.SubscriptionStatus subscriptionStatus;
    private LocalDateTime subscriptionExpiry;
    private Integer maxUsers;
    private College.CollegeSettings settings;
    private CollegeStatsResponse stats;
    private LocalDateTime createdAt;
    private Boolean isActive;
}
