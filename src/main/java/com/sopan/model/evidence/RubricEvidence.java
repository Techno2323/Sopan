package com.sopan.model.evidence;

import java.time.Instant;

/**
 * Evidence derived from an instructor rubric evaluation on an assignment. Weight is 3.0.
 */
public class RubricEvidence extends Evidence {

    public RubricEvidence(int conceptId, double score, Instant recordedAt) {
        super(conceptId, score, recordedAt);
    }

    @Override
    public double weight() {
        return 3.0;
    }
}
