package com.joserojas.supportdesk.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.joserojas.supportdesk.entity.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    boolean existsByEmail(String email);

    Optional<AppUser> findByEmail(String email);
}
