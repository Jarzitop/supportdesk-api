package com.joserojas.supportdesk.dto.request;

import com.joserojas.supportdesk.enums.Priority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTicketRequest(
        @NotBlank(message = "Title is required") String title,
        @NotBlank(message = "Description is required") String description,
        @NotNull(message = "Priority is required") Priority priority,
        @NotNull(message = "Requester id is required") Long requesterId) {
}
