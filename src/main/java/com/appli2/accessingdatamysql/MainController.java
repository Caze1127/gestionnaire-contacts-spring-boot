package com.appli2.accessingdatamysql;

import com.appli2.accessingdatamysql.domain.User;
import com.appli2.accessingdatamysql.service.UserService;
import com.appli2.accessingdatamysql.util.ValidationUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/demo")
public class MainController {

    private final UserService userService;


    public MainController(
            UserService userService
    ) {
        this.userService = userService;
    }

    @PostMapping("/add")
    public ResponseEntity<String> addUser(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password
    ) {

        if (
                !ValidationUtils.validName(name)
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Nom invalide"
                    );
        }

        if (
                !ValidationUtils.validEmail(email)
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Adresse e-mail invalide"
                    );
        }

        if (
                !ValidationUtils
                        .validPassword(password)
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Le mot de passe doit contenir au moins 8 caractères, une majuscule, une minuscule et un chiffre"
                    );
        }

        try {

            userService.addUser(
                    name,
                    email,
                    password
            );

            return ResponseEntity.ok(
                    "Utilisateur ajouté"
            );

        } catch (Exception exception) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Impossible d'ajouter cet utilisateur"
                    );
        }
    }

    @GetMapping("/all")
    public Iterable<User> getAllUsers() {

        return userService
                .getAllUsers();
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateUser(
            @PathVariable Integer id,
            @RequestBody User user
    ) {

        if (
                !ValidationUtils
                        .validName(
                                user.getName()
                        )
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Nom invalide"
                    );
        }

        if (
                !ValidationUtils
                        .validEmail(
                                user.getEmail()
                        )
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Adresse e-mail invalide"
                    );
        }

        try {

            boolean updated =
                    userService.updateUser(
                            id,
                            user.getName(),
                            user.getEmail()
                    );

            if (!updated) {

                return ResponseEntity
                        .notFound()
                        .build();
            }

            return ResponseEntity.ok(
                    "Utilisateur modifié"
            );

        } catch (
                IllegalArgumentException exception
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            exception.getMessage()
                    );
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUser(
            @PathVariable Integer id,
            Authentication authentication
    ) {

        User currentUser =
                userService
                        .getUserByEmail(
                                authentication
                                        .getName()
                        );

        if (
                currentUser
                        .getId()
                        .equals(id)
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Vous ne pouvez pas supprimer votre propre compte administrateur"
                    );
        }

        boolean deleted =
                userService.deleteUser(id);

        if (!deleted) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(
                "Utilisateur supprimé"
        );
    }
}