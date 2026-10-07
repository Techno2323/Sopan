package com.sopan.engine;

import com.sopan.model.enums.MasteryLevel;
import com.sopan.model.evidence.Evidence;
import com.sopan.model.evidence.QuizEvidence;
import com.sopan.model.evidence.RubricEvidence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RecencyWeightedPolicyTest {

    private final RecencyWeightedPolicy policy = new RecencyWeightedPolicy();

    @Test
    @DisplayName("Empty evidence should produce 0 mastery")
    void testEmptyEvidence() {
        assertEquals(0.0, policy.mastery(List.of(), Instant.now()));
        assertEquals(MasteryLevel.UNSEEN, policy.levelOf(0.0, 0));
        assertFalse(policy.needsRefresh(List.of(), MasteryLevel.UNSEEN, Instant.now()));
    }

    @Test
    @DisplayName("Recent evidence should outweigh older evidence with 30-day half-life decay")
    void testRecencyWeightingDecay() {
        Instant now = Instant.parse("2026-10-01T00:00:00Z");

        // Old evidence (30 days ago, half-life decay factor = 0.5) with score 0.0 (fail)
        Evidence oldFail = new QuizEvidence(101, 0.0, now.minus(Duration.ofDays(30)));

        // Recent evidence (today, decay factor = 1.0) with score 1.0 (pass)
        Evidence recentPass = new QuizEvidence(101, 1.0, now);

        double mastery = policy.mastery(List.of(oldFail, recentPass), now);

        // Expected calculation:
        // old weight = 1.0 * 0.5 = 0.5, score = 0 -> 0
        // recent weight = 1.0 * 1.0 = 1.0, score = 1.0 -> 1.0
        // sum(w * r * x) = 1.0; sum(w * r) = 1.5
        // mastery = 100 * (1.0 / 1.5) = 66.67%
        assertTrue(mastery > 65.0 && mastery < 68.0, "Mastery should reflect recency weighting: " + mastery);
    }

    @Test
    @DisplayName("Rubric evidence (weight 3.0) should have 3x impact over quiz evidence (weight 1.0)")
    void testRubricWeightImpact() {
        Instant now = Instant.now();

        // 1 failed quiz (score 0, weight 1)
        Evidence quizFail = new QuizEvidence(101, 0.0, now);

        // 1 perfect rubric score (score 1, weight 3)
        Evidence rubricPass = new RubricEvidence(101, 1.0, now);

        double mastery = policy.mastery(List.of(quizFail, rubricPass), now);

        // sum(w*x) = 0 + 3.0 = 3.0; sum(w) = 1 + 3 = 4.0; mastery = 100 * 3/4 = 75.0%
        assertEquals(75.0, mastery, 0.001);
    }

    @Test
    @DisplayName("Mastery levels should transition based on evidence count and score thresholds")
    void testMasteryLevels() {
        assertEquals(MasteryLevel.UNSEEN, policy.levelOf(100.0, 0));
        assertEquals(MasteryLevel.EARLY, policy.levelOf(100.0, 1));
        assertEquals(MasteryLevel.EARLY, policy.levelOf(100.0, 2));

        // 3+ evidence points
        assertEquals(MasteryLevel.SHAKY, policy.levelOf(45.0, 3));
        assertEquals(MasteryLevel.DEVELOPING, policy.levelOf(65.0, 3));
        assertEquals(MasteryLevel.SOLID, policy.levelOf(85.0, 3));
    }

    @Test
    @DisplayName("Concepts at SOLID level should flag needsRefresh after 21 days without evidence")
    void testNeedsRefresh() {
        Instant now = Instant.parse("2026-10-25T00:00:00Z");

        Evidence oldEvidence = new QuizEvidence(101, 1.0, now.minus(Duration.ofDays(22)));
        assertTrue(policy.needsRefresh(List.of(oldEvidence), MasteryLevel.SOLID, now));

        Evidence recentEvidence = new QuizEvidence(101, 1.0, now.minus(Duration.ofDays(10)));
        assertFalse(policy.needsRefresh(List.of(recentEvidence), MasteryLevel.SOLID, now));
    }
}
