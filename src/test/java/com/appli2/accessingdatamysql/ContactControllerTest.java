package com.appli2.accessingdatamysql;

import com.appli2.accessingdatamysql.domain.Contact;
import com.appli2.accessingdatamysql.service.ContactService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactControllerTest {

    @Mock
    private ContactService contactService;

    private ContactController controller;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        controller = new ContactController(contactService);
        authentication = new UsernamePasswordAuthenticationToken(
                "owner@test.com",
                null,
                List.of()
        );
    }

    @Test
    void getContacts_usesAuthenticatedUser() {
        List<Contact> contacts = List.of(contact());

        when(contactService.getContactsByUserEmail("owner@test.com"))
                .thenReturn(contacts);

        assertSame(contacts, controller.getContacts(authentication));
    }

    @Test
    void getContact_returnsNotFoundWhenMissing() {
        when(contactService.getContact(5, "owner@test.com")).thenReturn(null);

        ResponseEntity<Contact> response =
                controller.getContact(5, authentication);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getContact_returnsContactWhenFound() {
        Contact contact = contact();
        when(contactService.getContact(5, "owner@test.com")).thenReturn(contact);

        ResponseEntity<Contact> response =
                controller.getContact(5, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(contact, response.getBody());
    }

    @Test
    void addContact_rejectsInvalidName() {
        ResponseEntity<?> response = controller.addContact(
                "A",
                "contact@test.com",
                "0612345678",
                "Amis",
                "",
                "",
                null,
                "",
                authentication
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Le nom du contact est invalide", response.getBody());
        verifyNoInteractions(contactService);
    }

    @Test
    void addContact_rejectsInvalidEmail() {
        ResponseEntity<?> response = validAdd("invalid", "0612345678", "Amis", null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("L'adresse e-mail du contact est invalide", response.getBody());
    }

    @Test
    void addContact_rejectsInvalidPhone() {
        ResponseEntity<?> response = validAdd("contact@test.com", "abc", "Amis", null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Le numéro de téléphone est invalide", response.getBody());
    }

    @Test
    void addContact_rejectsInvalidCategory() {
        ResponseEntity<?> response = validAdd(
                "contact@test.com",
                "0612345678",
                "Sport",
                null
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Catégorie invalide", response.getBody());
    }

    @Test
    void addContact_rejectsCompanyOver120Characters() {
        ResponseEntity<?> response = controller.addContact(
                "Alice",
                "contact@test.com",
                "0612345678",
                "Amis",
                "a".repeat(121),
                "",
                null,
                "",
                authentication
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Le nom de l'entreprise est trop long", response.getBody());
    }

    @Test
    void addContact_rejectsAddressOver255Characters() {
        ResponseEntity<?> response = controller.addContact(
                "Alice",
                "contact@test.com",
                "0612345678",
                "Amis",
                "",
                "a".repeat(256),
                null,
                "",
                authentication
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("L'adresse est trop longue", response.getBody());
    }

    @Test
    void addContact_rejectsNotesOver2000Characters() {
        ResponseEntity<?> response = controller.addContact(
                "Alice",
                "contact@test.com",
                "0612345678",
                "Amis",
                "",
                "",
                null,
                "a".repeat(2001),
                authentication
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(
                "Les notes ne doivent pas dépasser 2000 caractères",
                response.getBody()
        );
    }

    @Test
    void addContact_rejectsFutureBirthday() {
        ResponseEntity<?> response = validAdd(
                "contact@test.com",
                "0612345678",
                "Amis",
                LocalDate.now().plusDays(1)
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(
                "La date d'anniversaire ne peut pas être dans le futur",
                response.getBody()
        );
    }

    @Test
    void addContact_normalizesDataAndReturnsSavedContact() {
        Contact saved = contact();

        when(contactService.addContact(
                "Alice",
                "alice@test.com",
                "0612345678",
                "Amis",
                "Company",
                "Paris",
                LocalDate.of(2000, 1, 1),
                "Notes",
                "owner@test.com"
        )).thenReturn(saved);

        ResponseEntity<?> response = controller.addContact(
                " Alice ",
                " ALICE@TEST.COM ",
                " 0612345678 ",
                "Amis",
                " Company ",
                " Paris ",
                LocalDate.of(2000, 1, 1),
                " Notes ",
                authentication
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(saved, response.getBody());
    }

    @Test
    void updateContact_returnsNotFoundWhenServiceReturnsFalse() {
        Contact contact = contact();

        when(contactService.updateContact(
                anyInt(), anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), any(), anyString(), anyString()
        )).thenReturn(false);

        ResponseEntity<String> response =
                controller.updateContact(1, contact, authentication);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void updateContact_convertsNullOptionalFieldsToEmptyStrings() {
        Contact contact = contact();
        contact.setCompany(null);
        contact.setAddress(null);
        contact.setNotes(null);

        when(contactService.updateContact(
                1,
                "Alice",
                "alice@test.com",
                "0612345678",
                "Amis",
                "",
                "",
                null,
                "",
                "owner@test.com"
        )).thenReturn(true);

        ResponseEntity<String> response =
                controller.updateContact(1, contact, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Contact modifié", response.getBody());
    }

    @Test
    void toggleFavorite_returnsNotFoundWhenContactMissing() {
        when(contactService.toggleFavorite(1, "owner@test.com")).thenReturn(false);

        assertEquals(
                HttpStatus.NOT_FOUND,
                controller.toggleFavorite(1, authentication).getStatusCode()
        );
    }

    @Test
    void toggleFavorite_returnsOkWhenUpdated() {
        when(contactService.toggleFavorite(1, "owner@test.com")).thenReturn(true);

        ResponseEntity<String> response =
                controller.toggleFavorite(1, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Favori modifié", response.getBody());
    }

    @Test
    void deleteContact_returnsNotFoundWhenMissing() {
        when(contactService.deleteContact(1, "owner@test.com")).thenReturn(false);

        assertEquals(
                HttpStatus.NOT_FOUND,
                controller.deleteContact(1, authentication).getStatusCode()
        );
    }

    @Test
    void deleteContact_returnsOkWhenDeleted() {
        when(contactService.deleteContact(1, "owner@test.com")).thenReturn(true);

        ResponseEntity<String> response =
                controller.deleteContact(1, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Contact supprimé", response.getBody());
    }

    @Test
    void getPhoto_returnsNotFoundWhenContactDoesNotExist() {
        when(contactService.getContact(1, "owner@test.com")).thenReturn(null);

        assertEquals(
                HttpStatus.NOT_FOUND,
                controller.getPhoto(1, authentication).getStatusCode()
        );
    }

    @Test
    void getPhoto_returnsNotFoundWhenContactHasNoPhoto() {
        Contact contact = contact();
        contact.setPhoto(null);

        when(contactService.getContact(1, "owner@test.com")).thenReturn(contact);

        assertEquals(
                HttpStatus.NOT_FOUND,
                controller.getPhoto(1, authentication).getStatusCode()
        );
    }

    @Test
    void getPhoto_usesStoredContentType() {
        Contact contact = contact();
        contact.setPhoto(new byte[]{1, 2, 3});
        contact.setPhotoContentType("image/png");

        when(contactService.getContact(1, "owner@test.com")).thenReturn(contact);

        ResponseEntity<byte[]> response =
                controller.getPhoto(1, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
        assertArrayEquals(new byte[]{1, 2, 3}, response.getBody());
    }

    @Test
    void getPhoto_defaultsToJpegWhenContentTypeIsNull() {
        Contact contact = contact();
        contact.setPhoto(new byte[]{1});
        contact.setPhotoContentType(null);

        when(contactService.getContact(1, "owner@test.com")).thenReturn(contact);

        ResponseEntity<byte[]> response =
                controller.getPhoto(1, authentication);

        assertEquals(MediaType.IMAGE_JPEG, response.getHeaders().getContentType());
    }

    @Test
    void uploadPhoto_rejectsEmptyFile() throws Exception {
        MultipartFile photo = mock(MultipartFile.class);
        when(photo.isEmpty()).thenReturn(true);

        ResponseEntity<String> response =
                controller.uploadPhoto(1, photo, authentication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Aucune image sélectionnée", response.getBody());
        verifyNoInteractions(contactService);
    }

    @Test
    void uploadPhoto_rejectsNonImageFile() throws Exception {
        MultipartFile photo = mock(MultipartFile.class);
        when(photo.isEmpty()).thenReturn(false);
        when(photo.getContentType()).thenReturn("application/pdf");

        ResponseEntity<String> response =
                controller.uploadPhoto(1, photo, authentication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Le fichier doit être une image", response.getBody());
    }

    @Test
    void uploadPhoto_rejectsFileLargerThan5Mb() throws Exception {
        MultipartFile photo = mock(MultipartFile.class);
        when(photo.isEmpty()).thenReturn(false);
        when(photo.getContentType()).thenReturn("image/png");
        when(photo.getSize()).thenReturn(5L * 1024 * 1024 + 1);

        ResponseEntity<String> response =
                controller.uploadPhoto(1, photo, authentication);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("L'image ne doit pas dépasser 5 Mo", response.getBody());
    }

    @Test
    void uploadPhoto_returnsNotFoundWhenContactMissing() throws Exception {
        MockMultipartFile photo = new MockMultipartFile(
                "photo", "photo.png", "image/png", new byte[]{1}
        );

        when(contactService.updateContactPhoto(1, photo, "owner@test.com"))
                .thenReturn(false);

        assertEquals(
                HttpStatus.NOT_FOUND,
                controller.uploadPhoto(1, photo, authentication).getStatusCode()
        );
    }

    @Test
    void uploadPhoto_returnsOkWhenSaved() throws Exception {
        MockMultipartFile photo = new MockMultipartFile(
                "photo", "photo.png", "image/png", new byte[]{1}
        );

        when(contactService.updateContactPhoto(1, photo, "owner@test.com"))
                .thenReturn(true);

        ResponseEntity<String> response =
                controller.uploadPhoto(1, photo, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Photo enregistrée", response.getBody());
    }

    @Test
    void deletePhoto_returnsNotFoundWhenContactMissing() {
        when(contactService.deleteContactPhoto(1, "owner@test.com"))
                .thenReturn(false);

        assertEquals(
                HttpStatus.NOT_FOUND,
                controller.deletePhoto(1, authentication).getStatusCode()
        );
    }

    @Test
    void deletePhoto_returnsOkWhenDeleted() {
        when(contactService.deleteContactPhoto(1, "owner@test.com"))
                .thenReturn(true);

        ResponseEntity<String> response =
                controller.deletePhoto(1, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Photo supprimée", response.getBody());
    }

    private ResponseEntity<?> validAdd(
            String email,
            String phone,
            String category,
            LocalDate birthday
    ) {
        return controller.addContact(
                "Alice",
                email,
                phone,
                category,
                "",
                "",
                birthday,
                "",
                authentication
        );
    }

    private Contact contact() {
        Contact contact = new Contact();
        contact.setId(1);
        contact.setName("Alice");
        contact.setEmail("alice@test.com");
        contact.setPhone("0612345678");
        contact.setCategory("Amis");
        return contact;
    }
}
