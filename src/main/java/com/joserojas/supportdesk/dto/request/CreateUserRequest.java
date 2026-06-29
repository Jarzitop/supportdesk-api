package com.joserojas.supportdesk.dto.request;

import com.joserojas.supportdesk.enums.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "Full name is required")
        @Size(max = 120, message = "Full name must not exceed 120 characters")
        String fullName,
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 160, message = "Email must not exceed 160 characters")
        String email,
        @NotNull(message = "Role is required") Role role) {

    public CreateUserRequest {
        if (email != null) {
            email = email.trim();
        }
    }
}
