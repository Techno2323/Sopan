package com.sopan.engine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ActionRankerTest {

    private final ActionRanker ranker = new ActionRanker();

    @Test
    @DisplayName("Should accurately calculate composite priority score")
    void testComputeScore() {
        // formula: 40 * deadlineUrgency + 30 * gapSeverity + 20 * unblockValue + 10 * refreshFlag
        // All max (1.0 each)
        double maxScore = ActionRanker.computeScore(1.0, 1.0, 1.0, true);
        assertEquals(100.0, maxScore, 0.001);

        // Half deadline, full gap, no unblock, no refresh
        // 40*0.5 + 30*1.0 + 20*0.0 + 10*0.0 = 20 + 30 = 50.0
        double partialScore = ActionRanker.computeScore(0.5, 1.0, 0.0, false);
        assertEquals(50.0, partialScore, 0.001);

        // Clamping check (>1.0 or <0.0)
        double clamped = ActionRanker.computeScore(1.5, -0.5, 0.5, true);
        // 40*1.0 + 30*0.0 + 20*0.5 + 10*1.0 = 40 + 0 + 10 + 10 = 60.0
        assertEquals(60.0, clamped, 0.001);
    }

    @Test
    @DisplayName("Should rank candidate moves by priority in descending order and limit to top N")
    void testRankTopMoves() {
        Action highUrgency = new ReviewConceptAction(1, "Variables", 95.0, "Root cause");
        Action medUrgency = new ReviewConceptAction(2, "Loops", 70.0, "Practice needed");
        Action lowUrgency = new ReviewConceptAction(3, "OOP", 30.0, "Optional review");
        Action criticalUrgency = new ReviewConceptAction(4, "Syntax", 99.0, "Immediate blocker");

        List<Action> candidates = List.of(medUrgency, highUrgency, lowUrgency, criticalUrgency);

        List<Action> top3 = ranker.rankTopMoves(candidates, 3);
        assertEquals(3, top3.size());
        assertEquals("Syntax", ((ReviewConceptAction) top3.get(0)).getConceptTitle());
        assertEquals("Variables", ((ReviewConceptAction) top3.get(1)).getConceptTitle());
        assertEquals("Loops", ((ReviewConceptAction) top3.get(2)).getConceptTitle());
    }

    @Test
    @DisplayName("Empty candidates should return empty list")
    void testEmptyCandidates() {
        List<Action> res = ranker.rankTopMoves(List.of(), 3);
        assertNotNull(res);
        assertTrue(res.isEmpty());
    }
}
