package com.slambook.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class TopContributorResponse {
    private String userId;
    private String name;
    private String profilePicture;
    private Integer entryCount;
}
