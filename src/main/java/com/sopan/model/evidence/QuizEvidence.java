package com.sopan.model.evidence;

import java.time.Instant;

/**
 * Evidence derived from an objective quiz question answer. Weight is 1.0.
 */
public class QuizEvidence extends Evidence {

    public QuizEvidence(int conceptId, double score, Instant recordedAt) {
        super(conceptId, score, recordedAt);
    }

    @Override
    public double weight() {
        return 1.0;
    }
}
