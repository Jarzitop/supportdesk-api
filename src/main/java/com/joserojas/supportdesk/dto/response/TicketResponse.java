package com.joserojas.supportdesk.dto.response;

import java.time.LocalDateTime;

import com.joserojas.supportdesk.enums.Priority;
import com.joserojas.supportdesk.enums.TicketStatus;

public record TicketResponse(
        Long id,
        String title,
        String description,
        TicketStatus status,
        Priority priority,
        Long requesterId,
        String requesterName,
        Long assignedAgentId,
        String assignedAgentName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime dueAt,
        LocalDateTime resolvedAt,
        LocalDateTime closedAt) {
}
