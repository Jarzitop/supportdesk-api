package com.joserojas.supportdesk.dto.response;

import java.time.LocalDateTime;

public record TicketCommentResponse(
        Long id,
        Long ticketId,
        Long authorId,
        String authorName,
        String content,
        LocalDateTime createdAt) {
}
