package com.appli2.accessingdatamysql.service;

import com.appli2.accessingdatamysql.ContactRepository;
import com.appli2.accessingdatamysql.UserRepository;
import com.appli2.accessingdatamysql.domain.Contact;
import com.appli2.accessingdatamysql.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private UserRepository userRepository;

    private ContactService contactService;

    private User owner;

    @BeforeEach
    void setUp() {
        contactService = new ContactService(contactRepository, userRepository);

        owner = new User();
        owner.setId(1);
        owner.setName("Owner");
        owner.setEmail("owner@test.com");
        owner.setRole("USER");
    }

    @Test
    void getContactsByUserEmail_returnsOnlyRepositoryContactsForOwner() {
        List<Contact> contacts = List.of(contact(1), contact(2));

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(contactRepository.findByUser(owner)).thenReturn(contacts);

        assertSame(contacts, contactService.getContactsByUserEmail("owner@test.com"));
        verify(contactRepository).findByUser(owner);
    }

    @Test
    void anyOperation_throwsWhenOwnerDoesNotExist() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> contactService.getContactsByUserEmail("missing@test.com")
        );

        assertEquals("Utilisateur introuvable", exception.getMessage());
    }

    @Test
    void getContact_returnsOwnedContact() {
        Contact contact = contact(10);

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(contactRepository.findByIdAndUser(10, owner)).thenReturn(Optional.of(contact));

        assertSame(contact, contactService.getContact(10, "owner@test.com"));
    }

    @Test
    void getContact_returnsNullWhenContactIsNotOwnedOrMissing() {
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(contactRepository.findByIdAndUser(10, owner)).thenReturn(Optional.empty());

        assertNull(contactService.getContact(10, "owner@test.com"));
    }

    @Test
    void addContact_setsAllFieldsAndOwner() {
        LocalDate birthday = LocalDate.of(2000, 1, 2);

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(contactRepository.save(any(Contact.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Contact result = contactService.addContact(
                "Alice",
                "alice@test.com",
                "0612345678",
                "Amis",
                "Company",
                "Paris",
                birthday,
                "Notes",
                "owner@test.com"
        );

        assertEquals("Alice", result.getName());
        assertEquals("alice@test.com", result.getEmail());
        assertEquals("0612345678", result.getPhone());
        assertEquals("Amis", result.getCategory());
        assertEquals("Company", result.getCompany());
        assertEquals("Paris", result.getAddress());
        assertEquals(birthday, result.getBirthday());
        assertEquals("Notes", result.getNotes());
        assertFalse(result.getFavorite());
        assertSame(owner, result.getUser());

        verify(contactRepository).save(result);
    }

    @Test
    void updateContact_returnsFalseWhenContactDoesNotBelongToOwner() {
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(contactRepository.findByIdAndUser(10, owner)).thenReturn(Optional.empty());

        assertFalse(contactService.updateContact(
                10, "Name", "mail@test.com", "0612345678", "Autre",
                "", "", null, "", "owner@test.com"
        ));

        verify(contactRepository, never()).save(any());
    }

    @Test
    void updateContact_updatesExistingContact() {
        Contact contact = contact(10);
        LocalDate birthday = LocalDate.of(1999, 5, 4);

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(contactRepository.findByIdAndUser(10, owner)).thenReturn(Optional.of(contact));

        assertTrue(contactService.updateContact(
                10,
                "Updated",
                "updated@test.com",
                "0600000000",
                "Travail",
                "OpenAI",
                "Paris",
                birthday,
                "Updated notes",
                "owner@test.com"
        ));

        assertEquals("Updated", contact.getName());
        assertEquals("updated@test.com", contact.getEmail());
        assertEquals("0600000000", contact.getPhone());
        assertEquals("Travail", contact.getCategory());
        assertEquals("OpenAI", contact.getCompany());
        assertEquals("Paris", contact.getAddress());
        assertEquals(birthday, contact.getBirthday());
        assertEquals("Updated notes", contact.getNotes());
        verify(contactRepository).save(contact);
    }

    @Test
    void updateContactPhoto_storesBytesAndContentType() throws Exception {
        Contact contact = contact(10);
        MockMultipartFile file = new MockMultipartFile(
                "photo",
                "photo.png",
                "image/png",
                new byte[]{1, 2, 3}
        );

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(contactRepository.findByIdAndUser(10, owner)).thenReturn(Optional.of(contact));

        assertTrue(contactService.updateContactPhoto(10, file, "owner@test.com"));

        assertArrayEquals(new byte[]{1, 2, 3}, contact.getPhoto());
        assertEquals("image/png", contact.getPhotoContentType());
        verify(contactRepository).save(contact);
    }

    @Test
    void updateContactPhoto_returnsFalseWhenContactMissing() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "photo", "photo.png", "image/png", new byte[]{1}
        );

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(contactRepository.findByIdAndUser(10, owner)).thenReturn(Optional.empty());

        assertFalse(contactService.updateContactPhoto(10, file, "owner@test.com"));
        verify(contactRepository, never()).save(any());
    }

    @Test
    void deleteContactPhoto_removesPhotoAndContentType() {
        Contact contact = contact(10);
        contact.setPhoto(new byte[]{1, 2});
        contact.setPhotoContentType("image/jpeg");

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(contactRepository.findByIdAndUser(10, owner)).thenReturn(Optional.of(contact));

        assertTrue(contactService.deleteContactPhoto(10, "owner@test.com"));

        assertNull(contact.getPhoto());
        assertNull(contact.getPhotoContentType());
        verify(contactRepository).save(contact);
    }

    @Test
    void deleteContactPhoto_returnsFalseWhenContactMissing() {
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(contactRepository.findByIdAndUser(10, owner)).thenReturn(Optional.empty());

        assertFalse(contactService.deleteContactPhoto(10, "owner@test.com"));
    }

    @Test
    void toggleFavorite_changesFalseToTrue() {
        Contact contact = contact(10);
        contact.setFavorite(false);

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(contactRepository.findByIdAndUser(10, owner)).thenReturn(Optional.of(contact));

        assertTrue(contactService.toggleFavorite(10, "owner@test.com"));
        assertTrue(contact.getFavorite());
        verify(contactRepository).save(contact);
    }

    @Test
    void toggleFavorite_changesTrueToFalse() {
        Contact contact = contact(10);
        contact.setFavorite(true);

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(contactRepository.findByIdAndUser(10, owner)).thenReturn(Optional.of(contact));

        assertTrue(contactService.toggleFavorite(10, "owner@test.com"));
        assertFalse(contact.getFavorite());
    }

    @Test
    void toggleFavorite_treatsNullAsFalse() {
        Contact contact = contact(10);
        contact.setFavorite(null);

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(contactRepository.findByIdAndUser(10, owner)).thenReturn(Optional.of(contact));

        assertTrue(contactService.toggleFavorite(10, "owner@test.com"));
        assertTrue(contact.getFavorite());
    }

    @Test
    void toggleFavorite_returnsFalseWhenContactMissing() {
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(contactRepository.findByIdAndUser(10, owner)).thenReturn(Optional.empty());

        assertFalse(contactService.toggleFavorite(10, "owner@test.com"));
    }

    @Test
    void deleteContact_deletesOwnedContact() {
        Contact contact = contact(10);

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(contactRepository.findByIdAndUser(10, owner)).thenReturn(Optional.of(contact));

        assertTrue(contactService.deleteContact(10, "owner@test.com"));

        verify(contactRepository).delete(contact);
    }

    @Test
    void deleteContact_returnsFalseWhenContactMissing() {
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(contactRepository.findByIdAndUser(10, owner)).thenReturn(Optional.empty());

        assertFalse(contactService.deleteContact(10, "owner@test.com"));
        verify(contactRepository, never()).delete(any());
    }

    private Contact contact(Integer id) {
        Contact contact = new Contact();
        contact.setId(id);
        contact.setName("Contact");
        contact.setEmail("contact@test.com");
        contact.setPhone("0612345678");
        contact.setCategory("Autre");
        contact.setUser(owner);
        return contact;
    }
}
