package com.appli2.accessingdatamysql.service;

import com.appli2.accessingdatamysql.PasswordResetTokenRepository;
import com.appli2.accessingdatamysql.UserRepository;
import com.appli2.accessingdatamysql.domain.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final PasswordResetTokenRepository
            passwordResetTokenRepository;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            PasswordResetTokenRepository passwordResetTokenRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetTokenRepository =
                passwordResetTokenRepository;
    }

    public User addUser(
            String name,
            String email,
            String password
    ) {

        User user =
                new User();

        user.setName(
                name.trim()
        );

        user.setEmail(
                email.trim().toLowerCase()
        );

        user.setPassword(
                passwordEncoder.encode(
                        password
                )
        );

        user.setRole(
                "USER"
        );


        return userRepository.save(
                user
        );
    }

    public Iterable<User> getAllUsers() {

        return userRepository.findAll();
    }


    public User getUserByEmail(
            String email
    ) {

        return userRepository
                .findByEmail(email)
                .orElseThrow(
                        () ->
                                new RuntimeException(
                                        "Utilisateur introuvable"
                                )
                );
    }

    public User updateProfile(
            String currentEmail,
            String name,
            String newEmail
    ) {

        User user =
                getUserByEmail(
                        currentEmail
                );


        String normalizedEmail =
                newEmail
                        .trim()
                        .toLowerCase();


        var existing =
                userRepository
                        .findByEmail(
                                normalizedEmail
                        );


        if (
                existing.isPresent()
                        &&
                        !existing
                                .get()
                                .getId()
                                .equals(
                                        user.getId()
                                )
        ) {

            throw new IllegalArgumentException(
                    "Cette adresse e-mail est déjà utilisée"
            );
        }

        user.setName(
                name.trim()
        );

        user.setEmail(
                normalizedEmail
        );


        return userRepository.save(
                user
        );
    }

    public boolean changePassword(
            String email,
            String currentPassword,
            String newPassword
    ) {

        User user =
                getUserByEmail(
                        email
                );

        if (
                !passwordEncoder
                        .matches(
                                currentPassword,
                                user.getPassword()
                        )
        ) {

            return false;
        }

        user.setPassword(
                passwordEncoder.encode(
                        newPassword
                )
        );

        userRepository.save(
                user
        );

        return true;
    }

    public boolean updateUser(
            Integer id,
            String name,
            String email
    ) {

        var optionalUser =
                userRepository
                        .findById(id);

        if (optionalUser.isEmpty()) {
            return false;
        }

        User user =
                optionalUser.get();

        String normalizedEmail =
                email
                        .trim()
                        .toLowerCase();

        var existing =
                userRepository
                        .findByEmail(
                                normalizedEmail
                        );

        if (
                existing.isPresent()
                        &&
                        !existing
                                .get()
                                .getId()
                                .equals(id)
        ) {

            throw new IllegalArgumentException(
                    "Cette adresse e-mail est déjà utilisée"
            );
        }

        user.setName(
                name.trim()
        );

        user.setEmail(
                normalizedEmail
        );

        userRepository.save(
                user
        );

        return true;
    }

    public boolean deleteUser(
            Integer id
    ) {

        var optionalUser =
                userRepository.findById(id);

        if (optionalUser.isEmpty()) {
            return false;
        }

        User user =
                optionalUser.get();

        passwordResetTokenRepository
                .findByUser(user)
                .ifPresent(
                        passwordResetTokenRepository::delete
                );

        userRepository.delete(
                user
        );

        return true;
    }
}