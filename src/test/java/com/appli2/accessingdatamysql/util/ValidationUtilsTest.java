package com.appli2.accessingdatamysql.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

    @Test
    void validName_acceptsValidName() {
        assertTrue(ValidationUtils.validName("Mohamed"));
        assertTrue(ValidationUtils.validName("  Mohamed  "));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"A", "   "})
    void validName_rejectsNullEmptyAndTooShortNames(String name) {
        assertFalse(ValidationUtils.validName(name));
    }

    @Test
    void validName_rejectsNameLongerThan80Characters() {
        assertFalse(ValidationUtils.validName("a".repeat(81)));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "user@example.com",
            "john.doe+test@example.co.uk",
            "USER_123@test-domain.fr"
    })
    void validEmail_acceptsValidEmails(String email) {
        assertTrue(ValidationUtils.validEmail(email));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "not-an-email",
            "user@",
            "@example.com",
            "user@example",
            "user example@test.com"
    })
    void validEmail_rejectsInvalidEmails(String email) {
        assertFalse(ValidationUtils.validEmail(email));
    }

    @Test
    void validEmail_rejectsEmailLongerThan150Characters() {
        String email = "a".repeat(145) + "@x.com";
        assertTrue(email.length() > 150);
        assertFalse(ValidationUtils.validEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Password1",
            "Azerty123",
            "Abcdefg1"
    })
    void validPassword_acceptsStrongPasswords(String password) {
        assertTrue(ValidationUtils.validPassword(password));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "Short1",
            "password1",
            "PASSWORD1",
            "Password",
            "12345678"
    })
    void validPassword_rejectsInvalidPasswords(String password) {
        assertFalse(ValidationUtils.validPassword(password));
    }

    @Test
    void validPassword_rejectsPasswordLongerThan100Characters() {
        String password = "A1" + "a".repeat(99);
        assertTrue(password.length() > 100);
        assertFalse(ValidationUtils.validPassword(password));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "0612345678",
            "+33 6 12 34 56 78",
            "(+33) 6-12-34-56-78",
            "01.23.45.67.89"
    })
    void validPhone_acceptsValidPhoneNumbers(String phone) {
        assertTrue(ValidationUtils.validPhone(phone));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "123",
            "abc123456",
            "06/12/34/56/78",
            "12345678901234567890123456"
    })
    void validPhone_rejectsInvalidPhoneNumbers(String phone) {
        assertFalse(ValidationUtils.validPhone(phone));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Famille", "Amis", "Travail", "Autre"})
    void validCategory_acceptsKnownCategories(String category) {
        assertTrue(ValidationUtils.validCategory(category));
    }

    @Test
    void validCategory_rejectsUnknownOrNullCategory() {
        assertFalse(ValidationUtils.validCategory("Sport"));
        assertFalse(ValidationUtils.validCategory(null));
    }

    @Test
    void validLength_acceptsNullAndBoundaryValue() {
        assertTrue(ValidationUtils.validLength(null, 5));
        assertTrue(ValidationUtils.validLength("12345", 5));
    }

    @Test
    void validLength_rejectsValueOverLimit() {
        assertFalse(ValidationUtils.validLength("123456", 5));
    }
}
