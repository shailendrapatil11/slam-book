package com.slambook.dto.request;

import com.slambook.model.SlamBookEntry;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReactionRequest {
    @NotNull(message = "Reaction type is required")
    private SlamBookEntry.ReactionType type;
}
