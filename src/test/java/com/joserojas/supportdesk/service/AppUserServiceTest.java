package com.joserojas.supportdesk.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.joserojas.supportdesk.dto.request.CreateUserRequest;
import com.joserojas.supportdesk.dto.response.UserResponse;
import com.joserojas.supportdesk.entity.AppUser;
import com.joserojas.supportdesk.enums.Role;
import com.joserojas.supportdesk.exception.DuplicateResourceException;
import com.joserojas.supportdesk.repository.AppUserRepository;

@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private AppUserService appUserService;

    @Test
    void normalizesEmailBeforeCheckingAndSavingUser() {
        CreateUserRequest request = new CreateUserRequest(
                "Jose Rojas",
                " Jose@Example.com ",
                Role.REQUESTER);
        when(appUserRepository.existsByEmail("jose@example.com")).thenReturn(false);
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = appUserService.createUser(request);

        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).existsByEmail("jose@example.com");
        verify(appUserRepository).save(userCaptor.capture());
        assertEquals("jose@example.com", userCaptor.getValue().getEmail());
        assertEquals("jose@example.com", response.email());
    }

    @Test
    void detectsDuplicateEmailRegardlessOfCaseAndWhitespace() {
        CreateUserRequest request = new CreateUserRequest(
                "Jose Rojas",
                " JOSE@EXAMPLE.COM ",
                Role.REQUESTER);
        when(appUserRepository.existsByEmail("jose@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> appUserService.createUser(request));

        verify(appUserRepository).existsByEmail("jose@example.com");
    }
}
