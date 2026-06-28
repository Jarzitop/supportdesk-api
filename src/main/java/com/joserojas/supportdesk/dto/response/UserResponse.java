package com.joserojas.supportdesk.dto.response;

import java.time.LocalDateTime;

import com.joserojas.supportdesk.enums.Role;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        Role role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
