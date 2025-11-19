package com.slambook.dto.request;

import com.slambook.model.Template;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateQuestionRequest {
    @NotBlank(message = "Question text is required")
    private String text;

    @NotNull(message = "Question type is required")
    private Template.QuestionType type;

    private Boolean required;
    private List<String> options;
    private Integer maxLength;
    private Integer minValue;
    private Integer maxValue;
    private String placeholder;
    private Integer order;
}
