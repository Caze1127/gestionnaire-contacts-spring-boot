package com.appli2.accessingdatamysql;

import com.appli2.accessingdatamysql.domain.Contact;
import com.appli2.accessingdatamysql.domain.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ContactRepository
        extends CrudRepository<Contact, Integer> {

    Iterable<Contact> findByUser(User user);

    Optional<Contact> findByIdAndUser(
            Integer id,
            User user
    );

    long countByFavoriteTrue();

    long countByCategory(String category);
}