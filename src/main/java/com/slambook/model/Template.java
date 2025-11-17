package com.slambook.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "templates")
public class Template {

    @Id
    private String id;

    @Indexed
    private String collegeId;  // null for system templates

    private String name;
    private String description;
    private List<Question> questions;

    private Boolean isDefault;
    private Boolean isActive;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Question {
        private String id;
        private String text;
        private QuestionType type;
        private Boolean required;
        private List<String> options;  // for CHOICE type
        private Integer maxLength;
        private Integer minValue;      // for RATING type
        private Integer maxValue;      // for RATING type
        private String placeholder;
        private Integer order;
    }

    public enum QuestionType {
        TEXT,           // Short text input
        TEXTAREA,       // Long text input
        RATING,         // 1-10 rating
        CHOICE,         // Multiple choice (single select)
        MULTI_CHOICE,   // Multiple choice (multi select)
        DATE,           // Date picker
        FILE            // File upload
    }
}