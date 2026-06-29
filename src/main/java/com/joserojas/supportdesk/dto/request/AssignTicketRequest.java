package com.joserojas.supportdesk.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AssignTicketRequest(
        @NotNull(message = "Assigned agent id is required")
        @Positive(message = "Assigned agent id must be positive")
        Long assignedAgentId) {
}
