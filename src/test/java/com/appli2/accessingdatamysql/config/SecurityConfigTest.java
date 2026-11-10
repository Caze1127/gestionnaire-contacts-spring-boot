package com.appli2.accessingdatamysql.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {

    private final SecurityConfig securityConfig = new SecurityConfig();

    @Test
    void passwordEncoder_encodesPasswordsWithOneWayHash() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        String encoded = encoder.encode("Password1");

        assertNotEquals("Password1", encoded);
        assertTrue(encoder.matches("Password1", encoded));
        assertFalse(encoder.matches("WrongPassword1", encoded));
    }

    @Test
    void passwordEncoder_generatesDifferentHashesForSamePassword() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        String first = encoder.encode("Password1");
        String second = encoder.encode("Password1");

        assertNotEquals(first, second);
        assertTrue(encoder.matches("Password1", first));
        assertTrue(encoder.matches("Password1", second));
    }
}
