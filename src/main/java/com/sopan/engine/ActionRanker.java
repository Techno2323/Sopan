package com.sopan.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Ranks candidate learning actions for a student to produce Today's 3 Moves.
 * Uses a PriorityQueue where Action natural ordering puts highest priority first.
 */
public class ActionRanker {

    /**
     * Scores the composite priority of an action according to:
     * 40 * deadlineUrgency + 30 * gapSeverity + 20 * unblockValue + 10 * refreshFlag
     */
    public static double computeScore(double deadlineUrgency, double gapSeverity,
                                      double unblockValue, boolean needsRefresh) {
        double urgency = Math.max(0.0, Math.min(1.0, deadlineUrgency));
        double gap = Math.max(0.0, Math.min(1.0, gapSeverity));
        double unblock = Math.max(0.0, Math.min(1.0, unblockValue));
        double refresh = needsRefresh ? 1.0 : 0.0;

        return (40.0 * urgency) + (30.0 * gap) + (20.0 * unblock) + (10.0 * refresh);
    }

    /**
     * Polls the top N actions from a collection of candidate actions.
     */
    public List<Action> rankTopMoves(Collection<? extends Action> candidates, int limit) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        PriorityQueue<Action> queue = new PriorityQueue<>(candidates);
        List<Action> top = new ArrayList<>(limit);

        while (!queue.isEmpty() && top.size() < limit) {
            top.add(queue.poll());
        }

        return top;
    }
}
