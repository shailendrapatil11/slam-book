package com.slambook.dto.request;

import com.slambook.model.College;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeCreateRequest {
    @NotBlank(message = "College name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotNull(message = "Address is required")
    private AddressRequest address;

    @NotNull(message = "Subscription plan is required")
    private College.SubscriptionPlan subscriptionPlan;

    private Integer maxUsers;
    private LocalDateTime subscriptionExpiry;
}
