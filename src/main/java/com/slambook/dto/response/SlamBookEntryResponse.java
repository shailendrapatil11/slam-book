package com.slambook.dto.response;

import com.slambook.model.SlamBookEntry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlamBookEntryResponse {
    private String id;
    private String writtenFor;
    private UserBasicInfo writtenForUser;
    private String writtenBy;
    private UserBasicInfo writtenByUser;
    private Boolean isAnonymous;
    private Map<String, String> responses;
    private Map<String, Integer> ratings;
    private List<SlamBookEntry.Attachment> attachments;
    private List<ReactionResponse> reactions;
    private SlamBookEntry.Visibility visibility;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}