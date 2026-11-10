package com.appli2.accessingdatamysql.util;

import java.util.Set;
import java.util.regex.Pattern;

public final class ValidationUtils {

    private ValidationUtils() {
    }

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile(
                    "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
            );

    private static final Pattern PHONE_PATTERN =
            Pattern.compile(
                    "^[0-9+() .-]{6,25}$"
            );

    private static final Set<String>
            CONTACT_CATEGORIES =
            Set.of(
                    "Famille",
                    "Amis",
                    "Travail",
                    "Autre"
            );

    public static boolean validName(
            String name
    ) {

        return name != null
                && name.trim().length() >= 2
                && name.trim().length() <= 80;
    }

    public static boolean validEmail(
            String email
    ) {

        return email != null
                && email.length() <= 150
                && EMAIL_PATTERN
                .matcher(email.trim())
                .matches();
    }

    public static boolean validPassword(
            String password
    ) {

        if (
                password == null
                        || password.length() < 8
                        || password.length() > 100
        ) {
            return false;
        }

        boolean upper = false;
        boolean lower = false;
        boolean digit = false;

        for (
                char character :
                password.toCharArray()
        ) {

            if (
                    Character.isUpperCase(
                            character
                    )
            ) {
                upper = true;
            }

            if (
                    Character.isLowerCase(
                            character
                    )
            ) {
                lower = true;
            }

            if (
                    Character.isDigit(
                            character
                    )
            ) {
                digit = true;
            }
        }

        return upper
                && lower
                && digit;
    }

    public static boolean validPhone(
            String phone
    ) {

        return phone != null
                && PHONE_PATTERN
                .matcher(
                        phone.trim()
                )
                .matches();
    }

    public static boolean validCategory(
            String category
    ) {

        return category != null
                && CONTACT_CATEGORIES
                .contains(category);
    }

    public static boolean validLength(
            String value,
            int maxLength
    ) {

        return value == null
                || value.length() <= maxLength;
    }
}