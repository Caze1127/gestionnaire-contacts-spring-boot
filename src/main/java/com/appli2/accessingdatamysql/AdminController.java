package com.appli2.accessingdatamysql;

import com.appli2.accessingdatamysql.domain.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final ContactRepository contactRepository;

    public AdminController(
            UserRepository userRepository,
            ContactRepository contactRepository
    ) {
        this.userRepository = userRepository;
        this.contactRepository = contactRepository;
    }

    @GetMapping("/stats")
    public Map<String, Long> getStats() {

        long totalUsers =
                userRepository.count();

        long admins =
                userRepository.countByRole("ADMIN");

        long users =
                userRepository.countByRole("USER");

        long totalContacts =
                contactRepository.count();

        long favorites =
                contactRepository.countByFavoriteTrue();

        long family =
                contactRepository.countByCategory("Famille");

        long friends =
                contactRepository.countByCategory("Amis");

        long work =
                contactRepository.countByCategory("Travail");

        long other =
                contactRepository.countByCategory("Autre");

        return Map.of(
                "totalUsers", totalUsers,
                "admins", admins,
                "users", users,
                "totalContacts", totalContacts,
                "favorites", favorites,
                "family", family,
                "friends", friends,
                "work", work,
                "other", other
        );
    }

    @PutMapping("/users/{id}/promote")
    public ResponseEntity<String> promoteUser(
            @PathVariable Integer id
    ) {

        var optionalUser =
                userRepository.findById(id);

        if (optionalUser.isEmpty()) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        User user =
                optionalUser.get();

        if ("ADMIN".equals(user.getRole())) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Cet utilisateur est déjà administrateur"
                    );
        }

        user.setRole("ADMIN");

        userRepository.save(user);

        return ResponseEntity.ok(
                "Utilisateur promu administrateur"
        );
    }
}