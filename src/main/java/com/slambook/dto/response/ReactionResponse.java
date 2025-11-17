package com.slambook.dto.response;

import com.slambook.model.SlamBookEntry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactionResponse {
    private String userId;
    private String userName;
    private String userProfilePicture;
    private SlamBookEntry.ReactionType type;
    private LocalDateTime createdAt;
}
