package com.slambook.dto.request;

import com.slambook.model.SlamBookEntry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlamBookEntryUpdateRequest {
    private Map<String, String> responses;
    private Map<String, Integer> ratings;
    private SlamBookEntry.Visibility visibility;
}
