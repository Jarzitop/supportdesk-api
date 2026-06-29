package com.joserojas.supportdesk.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateTicketCommentRequest(
        @NotNull(message = "Author id is required")
        @Positive(message = "Author id must be positive")
        Long authorId,
        @NotBlank(message = "Content is required")
        @Size(max = 2000, message = "Content must not exceed 2000 characters")
        String content) {
}
