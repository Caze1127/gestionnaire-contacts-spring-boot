package com.appli2.accessingdatamysql;

import com.appli2.accessingdatamysql.domain.User;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MainControllerTest {

    @Mock
    private UserService userService;

    private MainController controller;

    @BeforeEach
    void setUp() {
        controller = new MainController(userService);
    }

    @Test
    void addUser_rejectsInvalidName() {
        ResponseEntity<String> response =
                controller.addUser("A", "user@test.com", "Password1");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Nom invalide", response.getBody());
        verifyNoInteractions(userService);
    }

    @Test
    void addUser_rejectsInvalidEmail() {
        ResponseEntity<String> response =
                controller.addUser("User", "invalid", "Password1");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Adresse e-mail invalide", response.getBody());
    }

    @Test
    void addUser_rejectsWeakPassword() {
        ResponseEntity<String> response =
                controller.addUser("User", "user@test.com", "weak");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void addUser_returnsOkWhenCreated() {
        ResponseEntity<String> response =
                controller.addUser("User", "user@test.com", "Password1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Utilisateur ajouté", response.getBody());
        verify(userService).addUser("User", "user@test.com", "Password1");
    }

    @Test
    void addUser_returnsBadRequestWhenServiceFails() {
        when(userService.addUser("User", "user@test.com", "Password1"))
                .thenThrow(new RuntimeException("database error"));

        ResponseEntity<String> response =
                controller.addUser("User", "user@test.com", "Password1");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Impossible d'ajouter cet utilisateur", response.getBody());
    }

    @Test
    void getAllUsers_returnsServiceResult() {
        List<User> users = List.of(user(1), user(2));
        when(userService.getAllUsers()).thenReturn(users);

        assertSame(users, controller.getAllUsers());
    }

    @Test
    void updateUser_rejectsInvalidName() {
        User user = user(1);
        user.setName("A");

        ResponseEntity<String> response = controller.updateUser(1, user);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(userService);
    }

    @Test
    void updateUser_rejectsInvalidEmail() {
        User user = user(1);
        user.setEmail("invalid");

        ResponseEntity<String> response = controller.updateUser(1, user);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(userService);
    }

    @Test
    void updateUser_returnsNotFoundWhenServiceReturnsFalse() {
        User user = user(1);
        when(userService.updateUser(1, user.getName(), user.getEmail()))
                .thenReturn(false);

        assertEquals(
                HttpStatus.NOT_FOUND,
                controller.updateUser(1, user).getStatusCode()
        );
    }

    @Test
    void updateUser_returnsDuplicateEmailError() {
        User user = user(1);

        when(userService.updateUser(1, user.getName(), user.getEmail()))
                .thenThrow(new IllegalArgumentException("Cette adresse e-mail est déjà utilisée"));

        ResponseEntity<String> response = controller.updateUser(1, user);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Cette adresse e-mail est déjà utilisée", response.getBody());
    }

    @Test
    void updateUser_returnsOkWhenUpdated() {
        User user = user(1);
        when(userService.updateUser(1, user.getName(), user.getEmail()))
                .thenReturn(true);

        ResponseEntity<String> response = controller.updateUser(1, user);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Utilisateur modifié", response.getBody());
    }

    @Test
    void deleteUser_refusesDeletingOwnAdminAccount() {
        User current = user(1);
        when(userService.getUserByEmail("admin@test.com")).thenReturn(current);

        ResponseEntity<String> response =
                controller.deleteUser(1, auth("admin@test.com"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(
                "Vous ne pouvez pas supprimer votre propre compte administrateur",
                response.getBody()
        );
        verify(userService, never()).deleteUser(anyInt());
    }

    @Test
    void deleteUser_returnsNotFoundWhenTargetDoesNotExist() {
        User current = user(1);
        when(userService.getUserByEmail("admin@test.com")).thenReturn(current);
        when(userService.deleteUser(2)).thenReturn(false);

        assertEquals(
                HttpStatus.NOT_FOUND,
                controller.deleteUser(2, auth("admin@test.com")).getStatusCode()
        );
    }

    @Test
    void deleteUser_returnsOkWhenDeleted() {
        User current = user(1);
        when(userService.getUserByEmail("admin@test.com")).thenReturn(current);
        when(userService.deleteUser(2)).thenReturn(true);

        ResponseEntity<String> response =
                controller.deleteUser(2, auth("admin@test.com"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Utilisateur supprimé", response.getBody());
    }

    private Authentication auth(String email) {
        return new UsernamePasswordAuthenticationToken(email, null, List.of());
    }

    private User user(Integer id) {
        User user = new User();
        user.setId(id);
        user.setName("User " + id);
        user.setEmail("user" + id + "@test.com");
        user.setPassword("hash");
        user.setRole("USER");
        return user;
    }
}
