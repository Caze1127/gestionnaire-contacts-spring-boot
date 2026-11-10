package com.appli2.accessingdatamysql.service;

import com.appli2.accessingdatamysql.PasswordResetTokenRepository;
import com.appli2.accessingdatamysql.UserRepository;
import com.appli2.accessingdatamysql.domain.PasswordResetToken;
import com.appli2.accessingdatamysql.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private PasswordResetService service;

    @BeforeEach
    void setUp() {
        service = new PasswordResetService(
                userRepository,
                tokenRepository,
                passwordEncoder
        );
    }

    @Test
    void createResetToken_doesNothingForUnknownEmail() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        service.createResetToken("missing@test.com");

        verifyNoInteractions(tokenRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void createResetToken_replacesExistingTokenAndCreates30MinuteToken() {
        User user = user();
        PasswordResetToken oldToken = new PasswordResetToken();
        oldToken.setToken("old-token");
        oldToken.setUser(user);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findByUser(user)).thenReturn(Optional.of(oldToken));

        LocalDateTime before = LocalDateTime.now();

        service.createResetToken("user@test.com");

        verify(tokenRepository).delete(oldToken);

        ArgumentCaptor<PasswordResetToken> captor =
                ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(captor.capture());

        PasswordResetToken saved = captor.getValue();

        assertSame(user, saved.getUser());
        assertDoesNotThrow(() -> UUID.fromString(saved.getToken()));
        assertTrue(saved.getExpiresAt().isAfter(before.plusMinutes(29)));
        assertTrue(saved.getExpiresAt().isBefore(LocalDateTime.now().plusMinutes(31)));
    }

    @Test
    void resetPassword_returnsFalseForUnknownToken() {
        when(tokenRepository.findByToken("unknown")).thenReturn(Optional.empty());

        assertFalse(service.resetPassword("unknown", "NewPassword1"));

        verifyNoInteractions(passwordEncoder);
        verify(userRepository, never()).save(any());
        verify(tokenRepository, never()).delete(any());
    }

    @Test
    void resetPassword_deletesExpiredTokenAndReturnsFalse() {
        PasswordResetToken token = token(LocalDateTime.now().minusMinutes(1));

        when(tokenRepository.findByToken("expired")).thenReturn(Optional.of(token));

        assertFalse(service.resetPassword("expired", "NewPassword1"));

        verify(tokenRepository).delete(token);
        verifyNoInteractions(passwordEncoder);
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_encodesPasswordSavesUserAndConsumesToken() {
        User user = user();
        PasswordResetToken token = token(LocalDateTime.now().plusMinutes(10));
        token.setUser(user);

        when(tokenRepository.findByToken("valid")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("NewPassword1")).thenReturn("new-hash");

        assertTrue(service.resetPassword("valid", "NewPassword1"));

        assertEquals("new-hash", user.getPassword());
        verify(userRepository).save(user);
        verify(tokenRepository).delete(token);
    }

    private User user() {
        User user = new User();
        user.setId(1);
        user.setName("User");
        user.setEmail("user@test.com");
        user.setPassword("old-hash");
        user.setRole("USER");
        return user;
    }

    private PasswordResetToken token(LocalDateTime expiration) {
        PasswordResetToken token = new PasswordResetToken();
        token.setToken("token");
        token.setExpiresAt(expiration);
        token.setUser(user());
        return token;
    }
}
