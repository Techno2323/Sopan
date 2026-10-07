package com.sopan.engine;

import com.sopan.dto.RootCauseView;
import com.sopan.model.Concept;

import java.util.*;

/**
 * Identifies root-cause concept weaknesses for shaky concepts.
 * Finds the deepest ancestors whose mastery falls below the gate threshold
 * that have no weak ancestors of their own.
 */
public class RootCauseAnalyzer {

    public List<RootCauseView> analyzeGaps(ConceptGraph graph, Map<Integer, Double> masteryMap, double gateThreshold) {
        List<RootCauseView> results = new ArrayList<>();
        if (graph == null || masteryMap == null) {
            return results;
        }

        for (Concept concept : graph.allConcepts()) {
            int cId = concept.getConceptId();
            double mastery = masteryMap.getOrDefault(cId, 0.0);

            // Only analyze shaky concepts (mastery below 50)
            if (mastery < 50.0) {
                Set<Integer> ancestors = graph.ancestorsOf(cId);
                Set<Integer> weakAncestors = new HashSet<>();

                for (int ancId : ancestors) {
                    double ancMastery = masteryMap.getOrDefault(ancId, 0.0);
                    if (ancMastery < gateThreshold) {
                        weakAncestors.add(ancId);
                    }
                }

                // The root causes are weak ancestors that have no weak ancestors themselves
                for (int weakId : weakAncestors) {
                    Set<Integer> higherAncestors = graph.ancestorsOf(weakId);
                    boolean hasHigherWeakAncestor = false;
                    for (int higherId : higherAncestors) {
                        if (weakAncestors.contains(higherId)) {
                            hasHigherWeakAncestor = true;
                            break;
                        }
                    }

                    if (!hasHigherWeakAncestor) {
                        Concept weakConcept = graph.getConcept(weakId);
                        String weakTitle = weakConcept != null ? weakConcept.getTitle() : ("Concept #" + weakId);
                        double weakMastery = masteryMap.getOrDefault(weakId, 0.0);
                        String msg = String.format("%s is shaky because of %s (mastery %.0f%% vs gate %.0f%%)",
                                concept.getTitle(), weakTitle, weakMastery, gateThreshold);

                        results.add(new RootCauseView(
                                cId, concept.getTitle(), mastery,
                                weakId, weakTitle, weakMastery, msg
                        ));
                    }
                }
            }
        }

        return results;
    }
}
