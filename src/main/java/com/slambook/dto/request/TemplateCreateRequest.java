package com.slambook.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class TemplateCreateRequest {
    @NotBlank(message = "Template name is required")
    private String name;

    private String description;

    @NotNull(message = "Questions are required")
    @Size(min = 1, message = "At least one question is required")
    private List<TemplateQuestionRequest> questions;

    private Boolean isDefault;
}
