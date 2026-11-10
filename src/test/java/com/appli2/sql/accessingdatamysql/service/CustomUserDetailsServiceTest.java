package com.appli2.accessingdatamysql.service;

import com.appli2.accessingdatamysql.UserRepository;
import com.appli2.accessingdatamysql.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    private CustomUserDetailsService service;

    @BeforeEach
    void setUp() {
        service = new CustomUserDetailsService(userRepository);
    }

    @Test
    void loadUserByUsername_buildsUserWithDatabaseRole() {
        User user = user("ADMIN");
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("admin@test.com");

        assertEquals("admin@test.com", details.getUsername());
        assertEquals("encoded-password", details.getPassword());
        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void loadUserByUsername_defaultsNullRoleToUser() {
        User user = user(null);
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("admin@test.com");

        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void loadUserByUsername_throwsWhenUserDoesNotExist() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername("missing@test.com")
        );

        assertEquals("Utilisateur introuvable", exception.getMessage());
    }

    private User user(String role) {
        User user = new User();
        user.setId(1);
        user.setName("Admin");
        user.setEmail("admin@test.com");
        user.setPassword("encoded-password");
        user.setRole(role);
        return user;
    }
}
