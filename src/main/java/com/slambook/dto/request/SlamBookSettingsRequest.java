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
public class SlamBookSettingsRequest {
    private Boolean isPublic;
    private Boolean allowAnonymous;
    private List<String> customQuestionIds;
    private String theme;
}
