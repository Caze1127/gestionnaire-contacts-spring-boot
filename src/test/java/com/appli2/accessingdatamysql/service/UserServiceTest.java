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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(
                userRepository,
                passwordEncoder,
                passwordResetTokenRepository
        );
    }

    @Test
    void addUser_trimsNormalizesEncodesAndForcesUserRole() {
        when(passwordEncoder.encode("Password1")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.addUser(
                "  Mohamed  ",
                "  USER@EXAMPLE.COM  ",
                "Password1"
        );

        assertEquals("Mohamed", result.getName());
        assertEquals("user@example.com", result.getEmail());
        assertEquals("encoded-password", result.getPassword());
        assertEquals("USER", result.getRole());

        verify(passwordEncoder).encode("Password1");
        verify(userRepository).save(result);
    }

    @Test
    void getAllUsers_returnsRepositoryResult() {
        List<User> users = List.of(user(1, "A", "a@test.com"), user(2, "B", "b@test.com"));
        when(userRepository.findAll()).thenReturn(users);

        assertSame(users, userService.getAllUsers());
    }

    @Test
    void getUserByEmail_returnsUserWhenFound() {
        User user = user(1, "Mohamed", "m@test.com");
        when(userRepository.findByEmail("m@test.com")).thenReturn(Optional.of(user));

        assertSame(user, userService.getUserByEmail("m@test.com"));
    }

    @Test
    void getUserByEmail_throwsWhenUserDoesNotExist() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.getUserByEmail("missing@test.com")
        );

        assertEquals("Utilisateur introuvable", exception.getMessage());
    }

    @Test
    void updateProfile_updatesAndNormalizesUser() {
        User user = user(1, "Old", "old@test.com");

        when(userRepository.findByEmail("old@test.com")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateProfile(
                "old@test.com",
                "  New Name  ",
                "  NEW@TEST.COM  "
        );

        assertSame(user, result);
        assertEquals("New Name", user.getName());
        assertEquals("new@test.com", user.getEmail());
        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_allowsKeepingSameEmail() {
        User user = user(7, "Mohamed", "same@test.com");

        when(userRepository.findByEmail("same@test.com"))
                .thenReturn(Optional.of(user), Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        assertDoesNotThrow(() ->
                userService.updateProfile("same@test.com", "Mohamed", "same@test.com")
        );

        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_rejectsEmailAlreadyUsedByAnotherUser() {
        User current = user(1, "Current", "current@test.com");
        User other = user(2, "Other", "used@test.com");

        when(userRepository.findByEmail("current@test.com")).thenReturn(Optional.of(current));
        when(userRepository.findByEmail("used@test.com")).thenReturn(Optional.of(other));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateProfile(
                        "current@test.com",
                        "Current",
                        "used@test.com"
                )
        );

        assertEquals("Cette adresse e-mail est déjà utilisée", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_returnsFalseWhenCurrentPasswordIsWrong() {
        User user = user(1, "Mohamed", "m@test.com");
        user.setPassword("old-hash");

        when(userRepository.findByEmail("m@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "old-hash")).thenReturn(false);

        assertFalse(userService.changePassword("m@test.com", "wrong", "NewPassword1"));

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_encodesAndSavesNewPassword() {
        User user = user(1, "Mohamed", "m@test.com");
        user.setPassword("old-hash");

        when(userRepository.findByEmail("m@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPassword1", "old-hash")).thenReturn(true);
        when(passwordEncoder.encode("NewPassword1")).thenReturn("new-hash");

        assertTrue(userService.changePassword(
                "m@test.com",
                "OldPassword1",
                "NewPassword1"
        ));

        assertEquals("new-hash", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_returnsFalseWhenIdDoesNotExist() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertFalse(userService.updateUser(99, "Name", "name@test.com"));

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_updatesExistingUser() {
        User user = user(4, "Old", "old@test.com");

        when(userRepository.findById(4)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());

        assertTrue(userService.updateUser(
                4,
                "  New Name  ",
                "  NEW@TEST.COM "
        ));

        assertEquals("New Name", user.getName());
        assertEquals("new@test.com", user.getEmail());
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_rejectsEmailOwnedByAnotherUser() {
        User current = user(4, "Current", "current@test.com");
        User other = user(5, "Other", "used@test.com");

        when(userRepository.findById(4)).thenReturn(Optional.of(current));
        when(userRepository.findByEmail("used@test.com")).thenReturn(Optional.of(other));

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(4, "Current", "used@test.com")
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_returnsFalseWhenUserDoesNotExist() {
        when(userRepository.findById(15)).thenReturn(Optional.empty());

        assertFalse(userService.deleteUser(15));

        verify(userRepository, never()).delete(any());
        verifyNoInteractions(passwordResetTokenRepository);
    }

    @Test
    void deleteUser_deletesResetTokenBeforeDeletingUser() {
        User user = user(8, "User", "user@test.com");
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);

        when(userRepository.findById(8)).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.findByUser(user)).thenReturn(Optional.of(token));

        assertTrue(userService.deleteUser(8));

        verify(passwordResetTokenRepository).delete(token);
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_deletesUserEvenWithoutResetToken() {
        User user = user(9, "User", "user@test.com");

        when(userRepository.findById(9)).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.findByUser(user)).thenReturn(Optional.empty());

        assertTrue(userService.deleteUser(9));

        verify(passwordResetTokenRepository, never()).delete(any());
        verify(userRepository).delete(user);
    }

    private User user(Integer id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setPassword("hash");
        user.setRole("USER");
        return user;
    }
}
