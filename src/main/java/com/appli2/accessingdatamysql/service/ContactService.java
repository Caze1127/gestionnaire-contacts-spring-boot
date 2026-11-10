package com.appli2.accessingdatamysql.service;

import com.appli2.accessingdatamysql.ContactRepository;
import com.appli2.accessingdatamysql.UserRepository;
import com.appli2.accessingdatamysql.domain.Contact;
import com.appli2.accessingdatamysql.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;

@Service
public class ContactService {

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;


    public ContactService(
            ContactRepository contactRepository,
            UserRepository userRepository
    ) {
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
    }

    private User getUser(String email) {

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Utilisateur introuvable"
                        )
                );
    }

    public Iterable<Contact> getContactsByUserEmail(
            String email
    ) {

        User user = getUser(email);

        return contactRepository.findByUser(user);
    }

    public Contact getContact(
            Integer id,
            String userEmail
    ) {

        User user = getUser(userEmail);

        return contactRepository
                .findByIdAndUser(id, user)
                .orElse(null);
    }

    public Contact addContact(
            String name,
            String email,
            String phone,
            String category,
            String company,
            String address,
            LocalDate birthday,
            String notes,
            String userEmail
    ) {

        User user = getUser(userEmail);

        Contact contact = new Contact();

        contact.setName(name);
        contact.setEmail(email);
        contact.setPhone(phone);
        contact.setCategory(category);
        contact.setCompany(company);
        contact.setAddress(address);
        contact.setBirthday(birthday);
        contact.setNotes(notes);
        contact.setFavorite(false);
        contact.setUser(user);

        return contactRepository.save(contact);
    }

    public boolean updateContact(
            Integer id,
            String name,
            String email,
            String phone,
            String category,
            String company,
            String address,
            LocalDate birthday,
            String notes,
            String userEmail
    ) {

        User user = getUser(userEmail);

        var optionalContact =
                contactRepository.findByIdAndUser(
                        id,
                        user
                );

        if (optionalContact.isEmpty()) {
            return false;
        }

        Contact contact =
                optionalContact.get();

        contact.setName(name);
        contact.setEmail(email);
        contact.setPhone(phone);
        contact.setCategory(category);
        contact.setCompany(company);
        contact.setAddress(address);
        contact.setBirthday(birthday);
        contact.setNotes(notes);

        contactRepository.save(contact);

        return true;
    }

    public boolean updateContactPhoto(
            Integer id,
            MultipartFile file,
            String userEmail
    ) throws IOException {

        User user = getUser(userEmail);

        var optionalContact =
                contactRepository.findByIdAndUser(
                        id,
                        user
                );

        if (optionalContact.isEmpty()) {
            return false;
        }

        Contact contact =
                optionalContact.get();

        contact.setPhoto(
                file.getBytes()
        );

        contact.setPhotoContentType(
                file.getContentType()
        );

        contactRepository.save(contact);

        return true;
    }

    public boolean deleteContactPhoto(
            Integer id,
            String userEmail
    ) {

        User user = getUser(userEmail);

        var optionalContact =
                contactRepository.findByIdAndUser(
                        id,
                        user
                );

        if (optionalContact.isEmpty()) {
            return false;
        }

        Contact contact =
                optionalContact.get();

        contact.setPhoto(null);
        contact.setPhotoContentType(null);

        contactRepository.save(contact);

        return true;
    }

    public boolean toggleFavorite(
            Integer id,
            String userEmail
    ) {

        User user = getUser(userEmail);

        var optionalContact =
                contactRepository.findByIdAndUser(
                        id,
                        user
                );

        if (optionalContact.isEmpty()) {
            return false;
        }

        Contact contact =
                optionalContact.get();

        boolean favorite =
                Boolean.TRUE.equals(
                        contact.getFavorite()
                );

        contact.setFavorite(
                !favorite
        );

        contactRepository.save(contact);

        return true;
    }

    public boolean deleteContact(
            Integer id,
            String userEmail
    ) {

        User user = getUser(userEmail);

        var optionalContact =
                contactRepository.findByIdAndUser(
                        id,
                        user
                );

        if (optionalContact.isEmpty()) {
            return false;
        }

        contactRepository.delete(
                optionalContact.get()
        );

        return true;
    }
}