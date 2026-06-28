package com.joserojas.supportdesk.dto.request;

import com.joserojas.supportdesk.enums.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(
        @NotBlank(message = "Full name is required") String fullName,
        @NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email,
        @NotNull(message = "Role is required") Role role) {
}
