package com.joserojas.supportdesk.dto.request;

import com.joserojas.supportdesk.enums.TicketStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateTicketStatusRequest(
        @NotNull(message = "Status is required") TicketStatus status) {
}
