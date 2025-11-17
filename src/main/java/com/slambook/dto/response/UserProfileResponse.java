package com.slambook.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
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
