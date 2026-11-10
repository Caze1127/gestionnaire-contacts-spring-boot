package com.appli2.accessingdatamysql.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DomainModelTest {

    @Test
    void user_defaultsToUserRole() {
        User user = new User();

        assertEquals("USER", user.getRole());
    }

    @Test
    void contact_hasExpectedDefaults() {
        Contact contact = new Contact();

        assertFalse(contact.getFavorite());
        assertEquals("Autre", contact.getCategory());
    }

    @Test
    void contactPrePersist_setsCreationDateWhenMissing() {
        Contact contact = new Contact();

        LocalDateTime before = LocalDateTime.now();
        contact.prePersist();
        LocalDateTime after = LocalDateTime.now();

        assertNotNull(contact.getCreatedAt());
        assertFalse(contact.getCreatedAt().isBefore(before));
        assertFalse(contact.getCreatedAt().isAfter(after));
    }

    @Test
    void contactPrePersist_doesNotOverwriteExistingCreationDate() {
        Contact contact = new Contact();
        LocalDateTime existing = LocalDateTime.of(2025, 1, 1, 10, 0);
        contact.setCreatedAt(existing);

        contact.prePersist();

        assertEquals(existing, contact.getCreatedAt());
    }

    @Test
    void passwordResetToken_storesTokenExpirationAndUser() {
        User user = new User();
        LocalDateTime expiration = LocalDateTime.now().plusMinutes(30);

        PasswordResetToken token = new PasswordResetToken();
        token.setToken("abc");
        token.setExpiresAt(expiration);
        token.setUser(user);

        assertEquals("abc", token.getToken());
        assertEquals(expiration, token.getExpiresAt());
        assertSame(user, token.getUser());
    }
}
