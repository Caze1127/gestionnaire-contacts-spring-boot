package com.appli2.accessingdatamysql;

import com.appli2.accessingdatamysql.domain.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository
        extends CrudRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    long countByRole(String role);
}