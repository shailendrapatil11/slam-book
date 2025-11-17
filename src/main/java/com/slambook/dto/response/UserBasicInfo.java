package com.slambook.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBasicInfo {
    private String id;
    private String firstName;
    private String lastName;
    private String nickname;
    private String profilePicture;
    private String course;
    private String batch;
}
