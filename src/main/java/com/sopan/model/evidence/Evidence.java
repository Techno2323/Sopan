package com.sopan.model.evidence;

import java.time.Instant;

/**
 * Base evidence unit contributing to concept mastery calculation.
 * Each evidence item has a normalized score [0, 1], a weight, a timestamp, and a conceptId.
 */
public abstract class Evidence {

    private final int conceptId;
    private final double score;
    private final Instant recordedAt;

    protected Evidence(int conceptId, double score, Instant recordedAt) {
        this.conceptId = conceptId;
        this.score = Math.max(0.0, Math.min(1.0, score));
        this.recordedAt = recordedAt;
    }

    public int conceptId() {
        return conceptId;
    }

    public double score() {
        return score;
    }

    public Instant recordedAt() {
        return recordedAt;
    }

    public abstract double weight();
}
