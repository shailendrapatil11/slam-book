package com.slambook.dto.response;

import com.slambook.model.Template;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class TemplateResponse {
    private String id;
    private String collegeId;
    private String name;
    private String description;
    private List<Template.Question> questions;
    private Boolean isDefault;
    private Boolean isActive;
    private String createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
}
