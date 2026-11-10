package com.appli2.accessingdatamysql;

import com.appli2.accessingdatamysql.domain.User;
import com.appli2.accessingdatamysql.service.PasswordResetService;
import com.appli2.accessingdatamysql.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetService passwordResetService;

    private AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController(
                userService,
                userRepository,
                passwordResetService
        );
    }

    @Test
    void register_rejectsInvalidName() {
        ResponseEntity<String> response =
                controller.register("A", "user@test.com", "Password1");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Le nom doit contenir entre 2 et 80 caractères", response.getBody());
        verifyNoInteractions(userService, userRepository);
    }

    @Test
    void register_rejectsInvalidEmail() {
        ResponseEntity<String> response =
                controller.register("User", "invalid", "Password1");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Adresse e-mail invalide", response.getBody());
        verifyNoInteractions(userService, userRepository);
    }

    @Test
    void register_rejectsWeakPassword() {
        ResponseEntity<String> response =
                controller.register("User", "user@test.com", "password");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(userService, userRepository);
    }

    @Test
    void register_rejectsDuplicateEmail() {
        when(userRepository.findByEmail("user@test.com"))
                .thenReturn(Optional.of(user(1, "USER")));

        ResponseEntity<String> response =
                controller.register("User", " USER@TEST.COM ", "Password1");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Cette adresse e-mail est déjà utilisée", response.getBody());
        verify(userService, never()).addUser(anyString(), anyString(), anyString());
    }

    @Test
    void register_normalizesEmailAndCreatesAccount() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.empty());

        ResponseEntity<String> response =
                controller.register("User", " USER@TEST.COM ", "Password1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Compte créé avec succès", response.getBody());
        verify(userService).addUser("User", "user@test.com", "Password1");
    }

    @Test
    void profile_returnsUserInformationAndAdminFlag() {
        User user = user(1, "ADMIN");
        when(userService.getUserByEmail("admin@test.com")).thenReturn(user);

        Authentication authentication = auth(
                "admin@test.com",
                "ROLE_ADMIN"
        );

        Map<String, Object> profile = controller.profile(authentication);

        assertEquals(1, profile.get("id"));
        assertEquals("User", profile.get("name"));
        assertEquals("admin@test.com", profile.get("email"));
        assertEquals("ADMIN", profile.get("role"));
        assertEquals(true, profile.get("admin"));
    }

    @Test
    void profile_defaultsNullRoleToUser() {
        User user = user(2, null);
        user.setEmail("user@test.com");
        when(userService.getUserByEmail("user@test.com")).thenReturn(user);

        Map<String, Object> profile =
                controller.profile(auth("user@test.com", "ROLE_USER"));

        assertEquals("USER", profile.get("role"));
        assertEquals(false, profile.get("admin"));
    }

    @Test
    void updateProfile_rejectsInvalidName() {
        ResponseEntity<String> response =
                controller.updateProfile("A", "user@test.com", auth("old@test.com", "ROLE_USER"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(userService);
    }

    @Test
    void updateProfile_rejectsInvalidEmail() {
        ResponseEntity<String> response =
                controller.updateProfile("User", "invalid", auth("old@test.com", "ROLE_USER"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(userService);
    }

    @Test
    void updateProfile_returnsServiceError() {
        doThrow(new IllegalArgumentException("Cette adresse e-mail est déjà utilisée"))
                .when(userService)
                .updateProfile("old@test.com", "User", "new@test.com");

        ResponseEntity<String> response =
                controller.updateProfile(
                        "User",
                        "new@test.com",
                        auth("old@test.com", "ROLE_USER")
                );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Cette adresse e-mail est déjà utilisée", response.getBody());
    }

    @Test
    void updateProfile_returnsOkWhenUpdated() {
        ResponseEntity<String> response =
                controller.updateProfile(
                        "User",
                        "new@test.com",
                        auth("old@test.com", "ROLE_USER")
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).updateProfile("old@test.com", "User", "new@test.com");
    }

    @Test
    void changePassword_rejectsWeakNewPassword() {
        ResponseEntity<String> response =
                controller.changePassword(
                        "OldPassword1",
                        "weak",
                        auth("user@test.com", "ROLE_USER")
                );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(userService);
    }

    @Test
    void changePassword_rejectsWrongCurrentPassword() {
        when(userService.changePassword(
                "user@test.com",
                "WrongPassword1",
                "NewPassword1"
        )).thenReturn(false);

        ResponseEntity<String> response =
                controller.changePassword(
                        "WrongPassword1",
                        "NewPassword1",
                        auth("user@test.com", "ROLE_USER")
                );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Mot de passe actuel incorrect", response.getBody());
    }

    @Test
    void changePassword_returnsOkWhenChanged() {
        when(userService.changePassword(
                "user@test.com",
                "OldPassword1",
                "NewPassword1"
        )).thenReturn(true);

        ResponseEntity<String> response =
                controller.changePassword(
                        "OldPassword1",
                        "NewPassword1",
                        auth("user@test.com", "ROLE_USER")
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Mot de passe modifié", response.getBody());
    }

    @Test
    void forgotPassword_doesNotRevealInvalidOrUnknownEmail() {
        ResponseEntity<String> response =
                controller.forgotPassword("invalid");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(
                "Si cette adresse existe, un lien de réinitialisation a été généré",
                response.getBody()
        );
        verifyNoInteractions(passwordResetService);
    }

    @Test
    void forgotPassword_normalizesValidEmailAndCreatesToken() {
        ResponseEntity<String> response =
                controller.forgotPassword(" USER@TEST.COM ");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(passwordResetService).createResetToken("user@test.com");
    }

    @Test
    void resetPassword_rejectsBlankToken() {
        ResponseEntity<String> response =
                controller.resetPassword("   ", "Password1");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Token invalide", response.getBody());
        verifyNoInteractions(passwordResetService);
    }

    @Test
    void resetPassword_rejectsWeakPassword() {
        ResponseEntity<String> response =
                controller.resetPassword("token", "weak");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(passwordResetService);
    }

    @Test
    void resetPassword_rejectsInvalidOrExpiredToken() {
        when(passwordResetService.resetPassword("token", "Password1"))
                .thenReturn(false);

        ResponseEntity<String> response =
                controller.resetPassword("token", "Password1");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Lien invalide ou expiré", response.getBody());
    }

    @Test
    void resetPassword_returnsOkOnSuccess() {
        when(passwordResetService.resetPassword("token", "Password1"))
                .thenReturn(true);

        ResponseEntity<String> response =
                controller.resetPassword("token", "Password1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Mot de passe réinitialisé", response.getBody());
    }

    private User user(Integer id, String role) {
        User user = new User();
        user.setId(id);
        user.setName("User");
        user.setEmail("admin@test.com");
        user.setPassword("hash");
        user.setRole(role);
        return user;
    }

    private Authentication auth(String email, String authority) {
        return new UsernamePasswordAuthenticationToken(
                email,
                null,
                List.of(new SimpleGrantedAuthority(authority))
        );
    }
}
