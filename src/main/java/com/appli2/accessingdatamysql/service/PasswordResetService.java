package com.appli2.accessingdatamysql.service;

import com.appli2.accessingdatamysql.PasswordResetTokenRepository;
import com.appli2.accessingdatamysql.UserRepository;
import com.appli2.accessingdatamysql.domain.PasswordResetToken;
import com.appli2.accessingdatamysql.domain.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;

    private final PasswordResetTokenRepository
            tokenRepository;

    private final PasswordEncoder passwordEncoder;


    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void createResetToken(
            String email
    ) {

        var optionalUser =
                userRepository.findByEmail(
                        email
                );

        if (optionalUser.isEmpty()) {
            return;
        }

        User user =
                optionalUser.get();

        tokenRepository
                .findByUser(user)
                .ifPresent(
                        tokenRepository::delete
                );

        PasswordResetToken resetToken =
                new PasswordResetToken();

        String token =
                UUID.randomUUID()
                        .toString();

        resetToken.setToken(
                token
        );

        resetToken.setUser(
                user
        );

        resetToken.setExpiresAt(
                LocalDateTime
                        .now()
                        .plusMinutes(30)
        );

        tokenRepository.save(
                resetToken
        );

        System.out.println();
        System.out.println(
                "===================================="
        );
        System.out.println(
                "LIEN DE RÉINITIALISATION"
        );
        System.out.println(
                "http://localhost:8080/reset-password.html?token="
                        + token
        );
        System.out.println(
                "Valable pendant 30 minutes."
        );
        System.out.println(
                "===================================="
        );
        System.out.println();
    }

    @Transactional
    public boolean resetPassword(
            String token,
            String newPassword
    ) {

        var optionalToken =
                tokenRepository
                        .findByToken(
                                token
                        );

        if (optionalToken.isEmpty()) {
            return false;
        }


        PasswordResetToken resetToken =
                optionalToken.get();

        if (
                resetToken
                        .getExpiresAt()
                        .isBefore(
                                LocalDateTime.now()
                        )
        ) {

            tokenRepository.delete(
                    resetToken
            );

            return false;
        }

        User user =
                resetToken.getUser();

        user.setPassword(
                passwordEncoder.encode(
                        newPassword
                )
        );

        userRepository.save(
                user
        );

        tokenRepository.delete(
                resetToken
        );

        return true;
    }
}