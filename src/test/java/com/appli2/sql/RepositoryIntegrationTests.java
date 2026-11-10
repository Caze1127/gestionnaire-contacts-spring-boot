package com.appli2.sql;

import com.appli2.accessingdatamysql.AccessingDataMysqlApplication;
import com.appli2.accessingdatamysql.ContactRepository;
import com.appli2.accessingdatamysql.PasswordResetTokenRepository;
import com.appli2.accessingdatamysql.UserRepository;
import com.appli2.accessingdatamysql.domain.Contact;
import com.appli2.accessingdatamysql.domain.PasswordResetToken;
import com.appli2.accessingdatamysql.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
@TestPropertySource(
        properties = "spring.jpa.hibernate.ddl-auto=create-drop"
)
@Import(TestcontainersConfiguration.class)
@SpringBootTest(classes = AccessingDataMysqlApplication.class)
@Transactional
class RepositoryIntegrationTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Test
    void userRepository_findsByEmailAndCountsRoles() {
        User user = new User();
        user.setName("Repository User");
        user.setEmail("repository@test.com");
        user.setPassword("hash");
        user.setRole("USER");

        userRepository.save(user);

        assertTrue(userRepository.findByEmail("repository@test.com").isPresent());
        assertTrue(userRepository.countByRole("USER") >= 1);
    }

    @Test
    void contactRepository_filtersByOwnerAndCountsCategoriesAndFavorites() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner-repository@test.com");
        owner.setPassword("hash");
        owner.setRole("USER");
        owner = userRepository.save(owner);

        Contact contact = new Contact();
        contact.setName("Alice");
        contact.setEmail("alice-repository@test.com");
        contact.setPhone("0612345678");
        contact.setCategory("Amis");
        contact.setFavorite(true);
        contact.setUser(owner);
        contact = contactRepository.save(contact);

        assertTrue(contactRepository.findByIdAndUser(contact.getId(), owner).isPresent());
        assertTrue(contactRepository.findByUser(owner).iterator().hasNext());
        assertTrue(contactRepository.countByFavoriteTrue() >= 1);
        assertTrue(contactRepository.countByCategory("Amis") >= 1);
    }

    @Test
    void passwordResetTokenRepository_findsTokenByTokenAndUser() {
        User user = new User();
        user.setName("Reset User");
        user.setEmail("reset-repository@test.com");
        user.setPassword("hash");
        user.setRole("USER");
        user = userRepository.save(user);

        PasswordResetToken token = new PasswordResetToken();
        token.setToken("integration-test-token");
        token.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        token.setUser(user);

        tokenRepository.save(token);

        assertTrue(tokenRepository.findByToken("integration-test-token").isPresent());
        assertTrue(tokenRepository.findByUser(user).isPresent());
    }
}
