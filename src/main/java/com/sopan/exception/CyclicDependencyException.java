package com.sopan.exception;

import java.util.Collections;
import java.util.List;

public class CyclicDependencyException extends SopanException {

    private final List<Integer> cyclePath;

    public CyclicDependencyException(String message, List<Integer> cyclePath) {
        super(message);
        this.cyclePath = cyclePath != null ? List.copyOf(cyclePath) : Collections.emptyList();
    }

    public List<Integer> getCyclePath() {
        return cyclePath;
    }
}
