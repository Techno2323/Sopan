package com.sopan.exception;

/**
 * Unchecked exception thrown by the DAO layer wrapping SQLExceptions.
 */
public class DaoException extends RuntimeException {

    public DaoException(String message) {
        super(message);
    }

    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }
}
