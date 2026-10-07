package com.sopan.engine;

import com.sopan.model.enums.MasteryLevel;
import com.sopan.model.evidence.Evidence;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Mastery calculation using recency weighting with a 30-day half-life.
 * M = 100 * sum(w_i * r_i * x_i) / sum(w_i * r_i), where r_i = 0.5^(age_days_i / 30).
 */
public class RecencyWeightedPolicy implements MasteryPolicy {

    private static final double HALF_LIFE_DAYS = 30.0;
    private static final long REFRESH_THRESHOLD_DAYS = 21;

    @Override
    public double mastery(List<Evidence> evidence, Instant now) {
        if (evidence == null || evidence.isEmpty()) {
            return 0.0;
        }

        double weightedScoreSum = 0.0;
        double weightSum = 0.0;

        for (Evidence ev : evidence) {
            long seconds = Math.max(0, Duration.between(ev.recordedAt(), now).getSeconds());
            double ageDays = seconds / 86400.0;
            double recencyFactor = Math.pow(0.5, ageDays / HALF_LIFE_DAYS);

            double w = ev.weight();
            double x = ev.score();

            weightedScoreSum += (w * recencyFactor * x);
            weightSum += (w * recencyFactor);
        }

        if (weightSum <= 0.0) {
            return 0.0;
        }

        double score = 100.0 * (weightedScoreSum / weightSum);
        return Math.max(0.0, Math.min(100.0, score));
    }

    @Override
    public MasteryLevel levelOf(double mastery, int evidenceCount) {
        if (evidenceCount == 0) {
            return MasteryLevel.UNSEEN;
        }
        if (evidenceCount < 3) {
            return MasteryLevel.EARLY;
        }
        if (mastery < 50.0) {
            return MasteryLevel.SHAKY;
        }
        if (mastery < 75.0) {
            return MasteryLevel.DEVELOPING;
        }
        return MasteryLevel.SOLID;
    }

    @Override
    public boolean needsRefresh(List<Evidence> evidence, MasteryLevel level, Instant now) {
        if (level != MasteryLevel.SOLID || evidence == null || evidence.isEmpty()) {
            return false;
        }

        Instant latest = Instant.MIN;
        for (Evidence ev : evidence) {
            if (ev.recordedAt().isAfter(latest)) {
                latest = ev.recordedAt();
            }
        }

        long daysSinceLastEvidence = Duration.between(latest, now).toDays();
        return daysSinceLastEvidence >= REFRESH_THRESHOLD_DAYS;
    }
}
