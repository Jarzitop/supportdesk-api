package com.joserojas.supportdesk.dto.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.joserojas.supportdesk.enums.Priority;
import com.joserojas.supportdesk.enums.Role;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

class RequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void trimsEmailBeforeRequestValidation() {
        CreateUserRequest request = new CreateUserRequest(
                "Jose Rojas",
                " Jose@Example.com ",
                Role.REQUESTER);

        assertEquals("Jose@Example.com", request.email());
        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void rejectsUserFieldsThatExceedMaximumLengths() {
        CreateUserRequest request = new CreateUserRequest(
                "A".repeat(121),
                "a".repeat(149) + "@example.com",
                Role.REQUESTER);

        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);

        assertHasViolationFor(violations, "fullName", Size.class);
        assertHasViolationFor(violations, "email", Size.class);
    }

    @Test
    void rejectsTicketFieldsThatExceedMaximumLengths() {
        CreateTicketRequest request = new CreateTicketRequest(
                "A".repeat(161),
                "A".repeat(2001),
                Priority.MEDIUM,
                1L);

        Set<ConstraintViolation<CreateTicketRequest>> violations = validator.validate(request);

        assertHasViolationFor(violations, "title", Size.class);
        assertHasViolationFor(violations, "description", Size.class);
    }

    @Test
    void rejectsZeroRequesterId() {
        CreateTicketRequest request = validTicketRequest(0L);

        assertHasViolationFor(validator.validate(request), "requesterId", Positive.class);
    }

    @Test
    void rejectsNegativeRequesterId() {
        CreateTicketRequest request = validTicketRequest(-1L);

        assertHasViolationFor(validator.validate(request), "requesterId", Positive.class);
    }

    private CreateTicketRequest validTicketRequest(Long requesterId) {
        return new CreateTicketRequest(
                "Cannot sign in",
                "The login page rejects valid credentials",
                Priority.HIGH,
                requesterId);
    }

    private <T> void assertHasViolationFor(
            Set<ConstraintViolation<T>> violations,
            String propertyName,
            Class<?> constraintType) {
        assertTrue(
                violations.stream().anyMatch(violation ->
                        violation.getPropertyPath().toString().equals(propertyName)
                                && violation.getConstraintDescriptor().getAnnotation().annotationType()
                                        .equals(constraintType)),
                "Expected a validation error for " + propertyName);
    }
}
