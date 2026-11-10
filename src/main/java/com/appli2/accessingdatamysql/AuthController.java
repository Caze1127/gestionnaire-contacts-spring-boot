package com.appli2.accessingdatamysql;

import com.appli2.accessingdatamysql.domain.User;
import com.appli2.accessingdatamysql.service.PasswordResetService;
import com.appli2.accessingdatamysql.service.UserService;
import com.appli2.accessingdatamysql.util.ValidationUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class AuthController {

    private final UserService userService;

    private final UserRepository userRepository;

    private final PasswordResetService
            passwordResetService;

    public AuthController(
            UserService userService,
            UserRepository userRepository,
            PasswordResetService passwordResetService
    ) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordResetService =
                passwordResetService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password
    ) {

        if (
                !ValidationUtils
                        .validName(name)
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Le nom doit contenir entre 2 et 80 caractères"
                    );
        }

        if (
                !ValidationUtils
                        .validEmail(email)
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

        String normalizedEmail =
                email
                        .trim()
                        .toLowerCase();


        if (
                userRepository
                        .findByEmail(
                                normalizedEmail
                        )
                        .isPresent()
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Cette adresse e-mail est déjà utilisée"
                    );
        }

        userService.addUser(
                name,
                normalizedEmail,
                password
        );

        return ResponseEntity.ok(
                "Compte créé avec succès"
        );
    }

    @GetMapping("/profile")
    public Map<String, Object> profile(
            Authentication authentication
    ) {

        User user =
                userService
                        .getUserByEmail(
                                authentication
                                        .getName()
                        );

        boolean admin =
                authentication
                        .getAuthorities()
                        .stream()
                        .anyMatch(
                                authority ->
                                        authority
                                                .getAuthority()
                                                .equals(
                                                        "ROLE_ADMIN"
                                                )
                        );

        String role =
                user.getRole() == null
                        ? "USER"
                        : user.getRole();

        return Map.of(
                "id",
                user.getId(),

                "name",
                user.getName(),

                "email",
                user.getEmail(),

                "role",
                role,

                "admin",
                admin
        );
    }

    @PutMapping("/account/profile")
    public ResponseEntity<String> updateProfile(
            @RequestParam String name,
            @RequestParam String email,
            Authentication authentication
    ) {

        if (
                !ValidationUtils
                        .validName(name)
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Nom invalide"
                    );
        }


        if (
                !ValidationUtils
                        .validEmail(email)
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Adresse e-mail invalide"
                    );
        }


        try {

            userService.updateProfile(
                    authentication
                            .getName(),
                    name,
                    email
            );


            return ResponseEntity.ok(
                    "Profil modifié"
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

    @PutMapping("/account/password")
    public ResponseEntity<String> changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            Authentication authentication
    ) {

        if (
                !ValidationUtils
                        .validPassword(
                                newPassword
                        )
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Le nouveau mot de passe doit contenir au moins 8 caractères, une majuscule, une minuscule et un chiffre"
                    );
        }

        boolean changed =
                userService
                        .changePassword(
                                authentication
                                        .getName(),
                                currentPassword,
                                newPassword
                        );

        if (!changed) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Mot de passe actuel incorrect"
                    );
        }

        return ResponseEntity.ok(
                "Mot de passe modifié"
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @RequestParam String email
    ) {

        if (
                ValidationUtils
                        .validEmail(email)
        ) {

            passwordResetService
                    .createResetToken(
                            email
                                    .trim()
                                    .toLowerCase()
                    );
        }

        return ResponseEntity.ok(
                "Si cette adresse existe, un lien de réinitialisation a été généré"
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword
    ) {

        if (
                token == null
                        || token.isBlank()
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Token invalide"
                    );
        }

        if (
                !ValidationUtils
                        .validPassword(
                                newPassword
                        )
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Le mot de passe doit contenir au moins 8 caractères, une majuscule, une minuscule et un chiffre"
                    );
        }

        boolean success =
                passwordResetService
                        .resetPassword(
                                token,
                                newPassword
                        );

        if (!success) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Lien invalide ou expiré"
                    );
        }

        return ResponseEntity.ok(
                "Mot de passe réinitialisé"
        );
    }
}