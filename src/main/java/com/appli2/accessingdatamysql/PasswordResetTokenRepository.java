package com.appli2.accessingdatamysql;

import com.appli2.accessingdatamysql.domain.PasswordResetToken;
import com.appli2.accessingdatamysql.domain.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository
        extends CrudRepository<PasswordResetToken, Integer> {

    Optional<PasswordResetToken> findByToken(
            String token
    );

    Optional<PasswordResetToken> findByUser(
            User user
    );
}