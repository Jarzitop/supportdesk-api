package com.joserojas.supportdesk.dto.request;

import com.joserojas.supportdesk.enums.Priority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 160, message = "Title must not exceed 160 characters")
        String title,
        @NotBlank(message = "Description is required")
        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description,
        @NotNull(message = "Priority is required") Priority priority,
        @NotNull(message = "Requester id is required")
        @Positive(message = "Requester id must be positive")
        Long requesterId) {
}
