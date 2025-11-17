package com.slambook.dto.request;

import com.slambook.model.SlamBookEntry;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlamBookEntryCreateRequest {
    @NotBlank(message = "Written for user ID is required")
    private String writtenFor;

    private Boolean isAnonymous;

    @NotNull(message = "Responses are required")
    private Map<String, String> responses;

    private Map<String, Integer> ratings;
    private SlamBookEntry.Visibility visibility;
}
