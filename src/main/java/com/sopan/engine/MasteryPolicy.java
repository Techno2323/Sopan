package com.sopan.engine;

import com.sopan.model.enums.MasteryLevel;
import com.sopan.model.evidence.Evidence;

import java.time.Instant;
import java.util.List;

public interface MasteryPolicy {

    /**
     * Computes continuous mastery score [0, 100] from evidence items.
     */
    double mastery(List<Evidence> evidence, Instant now);

    /**
     * Determines discrete mastery level based on continuous score and evidence count.
     */
    MasteryLevel levelOf(double mastery, int evidenceCount);

    /**
     * Checks if a solid concept requires a refresh because of lack of recent activity (e.g. 21 days).
     */
    boolean needsRefresh(List<Evidence> evidence, MasteryLevel level, Instant now);
}
