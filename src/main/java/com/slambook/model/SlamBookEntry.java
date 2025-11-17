package com.slambook.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "slambook_entries")
@CompoundIndex(name = "written_for_by_idx", def = "{'writtenFor': 1, 'writtenBy': 1}", unique = true)
public class SlamBookEntry {

    @Id
    private String id;

    @Indexed
    private String collegeId;

    @Indexed
    private String writtenFor;  // User ID who owns this slam book page

    @Indexed
    private String writtenBy;   // User ID who filled this entry

    private Boolean isAnonymous;

    // Responses to questions
    private Map<String, String> responses;

    // Ratings
    private Map<String, Integer> ratings;

    // Attachments
    private List<Attachment> attachments;

    // Reactions
    private List<Reaction> reactions;

    // Moderation
    private Boolean isReported;
    private String reportReason;
    private String reportedBy;
    private LocalDateTime reportedAt;

    private Visibility visibility;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Attachment {
        private String id;
        private AttachmentType type;
        private String url;
        private String thumbnail;
        private Long size;
        private String filename;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Reaction {
        private String userId;
        private ReactionType type;
        private LocalDateTime createdAt;
    }

    public enum AttachmentType {
        IMAGE, VIDEO, AUDIO
    }

    public enum ReactionType {
        LOVE, SMILE, SURPRISED, THINKING, FIRE, CLAP
    }

    public enum Visibility {
        PUBLIC, PRIVATE, FRIENDS_ONLY
    }
}