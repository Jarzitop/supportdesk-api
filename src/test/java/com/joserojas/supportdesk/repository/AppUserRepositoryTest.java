package com.joserojas.supportdesk.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.joserojas.supportdesk.entity.AppUser;
import com.joserojas.supportdesk.enums.Role;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AppUserRepositoryTest {

    @Autowired
    private AppUserRepository appUserRepository;

    @Test
    void savesAndFindsUserByEmail() {
        AppUser user = appUserRepository.saveAndFlush(
                new AppUser("Alex Rivera", "alex@example.com", Role.REQUESTER));

        AppUser foundUser = appUserRepository.findByEmail("alex@example.com").orElseThrow();

        assertEquals(user.getId(), foundUser.getId());
        assertTrue(appUserRepository.existsByEmail("alex@example.com"));
    }

    @Test
    void rejectsDuplicateEmailAtDatabaseLevel() {
        appUserRepository.saveAndFlush(
                new AppUser("Alex Rivera", "duplicate@example.com", Role.REQUESTER));
        AppUser duplicate = new AppUser("Sam Lee", "duplicate@example.com", Role.SUPPORT_AGENT);

        assertThrows(DataIntegrityViolationException.class, () -> appUserRepository.saveAndFlush(duplicate));
    }
}
