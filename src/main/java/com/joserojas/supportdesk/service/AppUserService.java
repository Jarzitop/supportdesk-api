package com.joserojas.supportdesk.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.joserojas.supportdesk.dto.request.CreateUserRequest;
import com.joserojas.supportdesk.dto.response.UserResponse;
import com.joserojas.supportdesk.entity.AppUser;
import com.joserojas.supportdesk.exception.DuplicateResourceException;
import com.joserojas.supportdesk.exception.ResourceNotFoundException;
import com.joserojas.supportdesk.repository.AppUserRepository;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;

    public AppUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (appUserRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("A user with email '" + request.email() + "' already exists");
        }

        AppUser appUser = new AppUser(request.fullName(), request.email(), request.role());
        AppUser savedUser = appUserRepository.save(appUser);

        return toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return appUserRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        AppUser appUser = appUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " was not found"));

        return toResponse(appUser);
    }

    private UserResponse toResponse(AppUser appUser) {
        return new UserResponse(
                appUser.getId(),
                appUser.getFullName(),
                appUser.getEmail(),
                appUser.getRole(),
                appUser.getCreatedAt(),
                appUser.getUpdatedAt());
    }
}
