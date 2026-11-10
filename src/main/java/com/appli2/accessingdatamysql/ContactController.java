package com.appli2.accessingdatamysql;

import com.appli2.accessingdatamysql.domain.Contact;
import com.appli2.accessingdatamysql.service.ContactService;
import com.appli2.accessingdatamysql.util.ValidationUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(
            ContactService contactService
    ) {
        this.contactService =
                contactService;
    }

    @GetMapping
    public Iterable<Contact> getContacts(
            Authentication authentication
    ) {

        return contactService
                .getContactsByUserEmail(
                        authentication
                                .getName()
                );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contact> getContact(
            @PathVariable Integer id,
            Authentication authentication
    ) {

        Contact contact =
                contactService.getContact(
                        id,
                        authentication
                                .getName()
                );

        if (contact == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(
                contact
        );
    }

    @PostMapping
    public ResponseEntity<?> addContact(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String category,

            @RequestParam(
                    required = false,
                    defaultValue = ""
            )
            String company,

            @RequestParam(
                    required = false,
                    defaultValue = ""
            )
            String address,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate birthday,

            @RequestParam(
                    required = false,
                    defaultValue = ""
            )
            String notes,

            Authentication authentication
    ) {

        String validation =
                validateContact(
                        name,
                        email,
                        phone,
                        category,
                        company,
                        address,
                        birthday,
                        notes
                );

        if (validation != null) {

            return ResponseEntity
                    .badRequest()
                    .body(validation);
        }

        Contact contact =
                contactService.addContact(
                        name.trim(),
                        email.trim()
                                .toLowerCase(),
                        phone.trim(),
                        category,
                        company.trim(),
                        address.trim(),
                        birthday,
                        notes.trim(),
                        authentication
                                .getName()
                );

        return ResponseEntity.ok(
                contact
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateContact(
            @PathVariable Integer id,
            @RequestBody Contact contact,
            Authentication authentication
    ) {

        String validation =
                validateContact(
                        contact.getName(),
                        contact.getEmail(),
                        contact.getPhone(),
                        contact.getCategory(),
                        contact.getCompany(),
                        contact.getAddress(),
                        contact.getBirthday(),
                        contact.getNotes()
                );

        if (validation != null) {

            return ResponseEntity
                    .badRequest()
                    .body(validation);
        }

        boolean updated =
                contactService.updateContact(
                        id,
                        contact.getName()
                                .trim(),
                        contact.getEmail()
                                .trim()
                                .toLowerCase(),
                        contact.getPhone()
                                .trim(),
                        contact.getCategory(),
                        safe(contact.getCompany()),
                        safe(contact.getAddress()),
                        contact.getBirthday(),
                        safe(contact.getNotes()),
                        authentication
                                .getName()
                );

        if (!updated) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(
                "Contact modifié"
        );
    }

    @PutMapping("/{id}/favorite")
    public ResponseEntity<String> toggleFavorite(
            @PathVariable Integer id,
            Authentication authentication
    ) {

        boolean updated =
                contactService
                        .toggleFavorite(
                                id,
                                authentication
                                        .getName()
                        );

        if (!updated) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(
                "Favori modifié"
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteContact(
            @PathVariable Integer id,
            Authentication authentication
    ) {

        boolean deleted =
                contactService
                        .deleteContact(
                                id,
                                authentication
                                        .getName()
                        );

        if (!deleted) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(
                "Contact supprimé"
        );
    }

    @GetMapping("/{id}/photo")
    public ResponseEntity<byte[]> getPhoto(
            @PathVariable Integer id,
            Authentication authentication
    ) {

        Contact contact =
                contactService.getContact(
                        id,
                        authentication
                                .getName()
                );

        if (
                contact == null
                        ||
                        contact.getPhoto() == null
        ) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        String contentType =
                contact.getPhotoContentType();


        if (contentType == null) {
            contentType =
                    "image/jpeg";
        }

        return ResponseEntity
                .ok()
                .contentType(
                        MediaType
                                .parseMediaType(
                                        contentType
                                )
                )
                .body(
                        contact.getPhoto()
                );
    }

    @PostMapping("/{id}/photo")
    public ResponseEntity<String> uploadPhoto(
            @PathVariable Integer id,

            @RequestParam("photo")
            MultipartFile photo,

            Authentication authentication
    ) throws IOException {

        if (photo.isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Aucune image sélectionnée"
                    );
        }

        String contentType =
                photo.getContentType();

        if (
                contentType == null
                        ||
                        !contentType
                                .startsWith(
                                        "image/"
                                )
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Le fichier doit être une image"
                    );
        }


        if (
                photo.getSize()
                        >
                        5L
                                * 1024
                                * 1024
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "L'image ne doit pas dépasser 5 Mo"
                    );
        }

        boolean updated =
                contactService
                        .updateContactPhoto(
                                id,
                                photo,
                                authentication
                                        .getName()
                        );

        if (!updated) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(
                "Photo enregistrée"
        );
    }

    @DeleteMapping("/{id}/photo")
    public ResponseEntity<String> deletePhoto(
            @PathVariable Integer id,
            Authentication authentication
    ) {

        boolean deleted =
                contactService
                        .deleteContactPhoto(
                                id,
                                authentication
                                        .getName()
                        );

        if (!deleted) {

            return ResponseEntity
                    .notFound()
                    .build();
        }


        return ResponseEntity.ok(
                "Photo supprimée"
        );
    }

    private String validateContact(
            String name,
            String email,
            String phone,
            String category,
            String company,
            String address,
            LocalDate birthday,
            String notes
    ) {

        if (
                !ValidationUtils
                        .validName(name)
        ) {

            return "Le nom du contact est invalide";
        }

        if (
                !ValidationUtils
                        .validEmail(email)
        ) {

            return "L'adresse e-mail du contact est invalide";
        }

        if (
                !ValidationUtils
                        .validPhone(phone)
        ) {

            return "Le numéro de téléphone est invalide";
        }

        if (
                !ValidationUtils
                        .validCategory(
                                category
                        )
        ) {

            return "Catégorie invalide";
        }

        if (
                !ValidationUtils
                        .validLength(
                                company,
                                120
                        )
        ) {

            return "Le nom de l'entreprise est trop long";
        }

        if (
                !ValidationUtils
                        .validLength(
                                address,
                                255
                        )
        ) {

            return "L'adresse est trop longue";
        }

        if (
                !ValidationUtils
                        .validLength(
                                notes,
                                2000
                        )
        ) {

            return "Les notes ne doivent pas dépasser 2000 caractères";
        }

        if (
                birthday != null
                        &&
                        birthday.isAfter(
                                LocalDate.now()
                        )
        ) {

            return "La date d'anniversaire ne peut pas être dans le futur";
        }

        return null;
    }

    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value.trim();
    }
}