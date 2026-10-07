package com.sopan.exception;

/**
 * Base checked exception for Sopan LMS business and system failures.
 */
public class SopanException extends Exception {

    public SopanException(String message) {
        super(message);
    }

    public SopanException(String message, Throwable cause) {
        super(message, cause);
    }
}
