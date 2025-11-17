package com.slambook.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollegeSettingsRequest {
    private Boolean requireApproval;
    private Boolean allowPublicProfiles;
    private Boolean allowAnonymousEntries;
    private List<String> enabledFeatures;
}
