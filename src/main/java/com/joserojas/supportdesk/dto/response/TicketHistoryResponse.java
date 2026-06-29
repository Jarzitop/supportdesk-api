package com.joserojas.supportdesk.dto.response;

import java.time.LocalDateTime;

public record TicketHistoryResponse(
        Long id,
        Long ticketId,
        Long changedById,
        String changedByName,
        String fieldName,
        String oldValue,
        String newValue,
        LocalDateTime changedAt) {
}
