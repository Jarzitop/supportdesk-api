package com.joserojas.supportdesk.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.joserojas.supportdesk.dto.request.CreateUserRequest;
import com.joserojas.supportdesk.dto.response.UserResponse;
import com.joserojas.supportdesk.enums.Role;
import com.joserojas.supportdesk.exception.DuplicateResourceException;
import com.joserojas.supportdesk.exception.ResourceNotFoundException;
import com.joserojas.supportdesk.service.AppUserService;

@WebMvcTest(AppUserController.class)
class AppUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppUserService appUserService;

    @Test
    void createUserReturnsCreatedUser() throws Exception {
        UserResponse response = userResponse();
        when(appUserService.createUser(any(CreateUserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Alex Rivera",
                                  "email": "alex@example.com",
                                  "role": "REQUESTER"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("alex@example.com"));

        verify(appUserService).createUser(any(CreateUserRequest.class));
    }

    @Test
    void getAllUsersReturnsUsers() throws Exception {
        when(appUserService.getAllUsers()).thenReturn(List.of(userResponse()));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].fullName").value("Alex Rivera"));
    }

    @Test
    void getUserByIdReturnsUser() throws Exception {
        when(appUserService.getUserById(1L)).thenReturn(userResponse());

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.role").value("REQUESTER"));
    }

    @Test
    void createUserReturnsBadRequestForInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "",
                                  "email": "invalid-email",
                                  "role": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void createUserReturnsBadRequestForInvalidEnumValue() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Alex Rivera",
                                  "email": "alex@example.com",
                                  "role": "OWNER"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Request body is malformed or contains invalid values"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void getUserReturnsBadRequestForInvalidIdType() throws Exception {
        mockMvc.perform(get("/api/v1/users/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Invalid value 'not-a-number' for parameter 'id'"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createUserReturnsConflictForDuplicateEmail() throws Exception {
        when(appUserService.createUser(any(CreateUserRequest.class)))
                .thenThrow(new DuplicateResourceException("A user with email 'alex@example.com' already exists"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Alex Rivera",
                                  "email": "alex@example.com",
                                  "role": "REQUESTER"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("A user with email 'alex@example.com' already exists"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createUserReturnsSafeConflictForDatabaseIntegrityViolation() throws Exception {
        when(appUserService.createUser(any(CreateUserRequest.class)))
                .thenThrow(new DataIntegrityViolationException("users_email_unique constraint details"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Alex Rivera",
                                  "email": "alex@example.com",
                                  "role": "REQUESTER"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("A resource with the same unique value already exists."))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void getUserReturnsNotFoundForUnknownId() throws Exception {
        when(appUserService.getUserById(99L))
                .thenThrow(new ResourceNotFoundException("User with id 99 was not found"));

        mockMvc.perform(get("/api/v1/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User with id 99 was not found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    private UserResponse userResponse() {
        return new UserResponse(
                1L,
                "Alex Rivera",
                "alex@example.com",
                Role.REQUESTER,
                LocalDateTime.of(2026, 6, 28, 12, 0),
                null);
    }
}
