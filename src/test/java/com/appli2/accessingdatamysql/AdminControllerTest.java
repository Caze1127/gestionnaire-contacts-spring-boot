package com.appli2.accessingdatamysql;

import com.appli2.accessingdatamysql.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ContactRepository contactRepository;

    private AdminController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminController(userRepository, contactRepository);
    }

    @Test
    void getStats_returnsAllDashboardCounters() {
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.countByRole("ADMIN")).thenReturn(2L);
        when(userRepository.countByRole("USER")).thenReturn(8L);
        when(contactRepository.count()).thenReturn(30L);
        when(contactRepository.countByFavoriteTrue()).thenReturn(7L);
        when(contactRepository.countByCategory("Famille")).thenReturn(5L);
        when(contactRepository.countByCategory("Amis")).thenReturn(6L);
        when(contactRepository.countByCategory("Travail")).thenReturn(12L);
        when(contactRepository.countByCategory("Autre")).thenReturn(7L);

        Map<String, Long> stats = controller.getStats();

        assertEquals(10L, stats.get("totalUsers"));
        assertEquals(2L, stats.get("admins"));
        assertEquals(8L, stats.get("users"));
        assertEquals(30L, stats.get("totalContacts"));
        assertEquals(7L, stats.get("favorites"));
        assertEquals(5L, stats.get("family"));
        assertEquals(6L, stats.get("friends"));
        assertEquals(12L, stats.get("work"));
        assertEquals(7L, stats.get("other"));
    }

    @Test
    void promoteUser_returnsNotFoundWhenUserDoesNotExist() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertEquals(
                HttpStatus.NOT_FOUND,
                controller.promoteUser(99).getStatusCode()
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void promoteUser_rejectsAlreadyAdminUser() {
        User user = user("ADMIN");
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        ResponseEntity<String> response = controller.promoteUser(1);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Cet utilisateur est déjà administrateur", response.getBody());
        verify(userRepository, never()).save(any());
    }

    @Test
    void promoteUser_changesRoleToAdminAndSaves() {
        User user = user("USER");
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        ResponseEntity<String> response = controller.promoteUser(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ADMIN", user.getRole());
        assertEquals("Utilisateur promu administrateur", response.getBody());
        verify(userRepository).save(user);
    }

    private User user(String role) {
        User user = new User();
        user.setId(1);
        user.setName("User");
        user.setEmail("user@test.com");
        user.setPassword("hash");
        user.setRole(role);
        return user;
    }
}
