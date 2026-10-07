package com.sopan.util;

import com.sopan.exception.ValidationException;

import java.util.regex.Pattern;

public final class Validator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private Validator() {}

    public static void requireNotBlank(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required and cannot be blank.");
        }
    }

    public static void validateEmail(String email) throws ValidationException {
        requireNotBlank(email, "Email");
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("Invalid email format.");
        }
    }

    public static void validatePassword(String password) throws ValidationException {
        if (password == null || password.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters long.");
        }
    }

    public static void validateRange(int value, int min, int max, String fieldName) throws ValidationException {
        if (value < min || value > max) {
            throw new ValidationException(fieldName + " must be between " + min + " and " + max + ".");
        }
    }

    public static int parsePositiveInt(String str, String fieldName) throws ValidationException {
        try {
            int val = Integer.parseInt(str);
            if (val <= 0) {
                throw new ValidationException(fieldName + " must be greater than zero.");
            }
            return val;
        } catch (NumberFormatException e) {
            throw new ValidationException(fieldName + " must be a valid number.");
        }
    }

    public static int parseIntOrDefault(String str, int defaultValue) {
        if (str == null || str.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
