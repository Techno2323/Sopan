package com.sopan.exception;

public class DuplicateAttemptException extends SopanException {

    private final int existingAttemptId;

    public DuplicateAttemptException(String message, int existingAttemptId) {
        super(message);
        this.existingAttemptId = existingAttemptId;
    }

    public int getExistingAttemptId() {
        return existingAttemptId;
    }
}
