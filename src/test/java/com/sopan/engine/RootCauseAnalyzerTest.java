package com.sopan.engine;

import com.sopan.dto.RootCauseView;
import com.sopan.model.Concept;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class RootCauseAnalyzerTest {

    private final RootCauseAnalyzer analyzer = new RootCauseAnalyzer();

    private Concept makeConcept(int id, String title) {
        Concept c = new Concept();
        c.setConceptId(id);
        c.setCourseId(1);
        c.setTitle(title);
        c.setDisplayOrder(id);
        return c;
    }

    @Test
    @DisplayName("Should identify the deepest weak ancestor when downstream concept is shaky")
    void testDeepestWeakAncestorRootCause() throws Exception {
        // Linear chain: C1 (Variables) -> C2 (Conditionals) -> C3 (Loops)
        List<Concept> concepts = List.of(
                makeConcept(1, "Variables"),
                makeConcept(2, "Conditionals"),
                makeConcept(3, "Loops")
        );

        Map<Integer, Set<Integer>> prereqs = new HashMap<>();
        prereqs.put(1, Set.of());
        prereqs.put(2, Set.of(1));
        prereqs.put(3, Set.of(2));

        ConceptGraph graph = ConceptGraph.of(concepts, prereqs);

        // Student is weak in Loops (30%) and Conditionals (40%), and Variables (35%)
        // Gate threshold is 60%.
        // The root cause for Loops (C3) must be Variables (C1), because C1 has NO weak ancestors of its own!
        Map<Integer, Double> masteries = Map.of(
                1, 35.0,
                2, 40.0,
                3, 30.0
        );

        List<RootCauseView> gaps = analyzer.analyzeGaps(graph, masteries, 60.0);
        assertNotNull(gaps);
        assertFalse(gaps.isEmpty());

        // For C3, the diagnosed root cause should be C1
        Optional<RootCauseView> c3Gap = gaps.stream().filter(g -> g.getShakyConceptId() == 3).findFirst();
        assertTrue(c3Gap.isPresent());
        assertEquals(1, c3Gap.get().getRootCauseConceptId());
        assertEquals("Variables", c3Gap.get().getRootCauseConceptTitle());
        assertTrue(c3Gap.get().getMessage().contains("Loops is shaky because of Variables"));
    }

    @Test
    @DisplayName("Should return no gaps when all ancestors meet the gate threshold")
    void testNoGapsWhenAncestorsAreStrong() throws Exception {
        // C1 (Variables) -> C2 (Loops)
        List<Concept> concepts = List.of(
                makeConcept(1, "Variables"),
                makeConcept(2, "Loops")
        );

        Map<Integer, Set<Integer>> prereqs = new HashMap<>();
        prereqs.put(1, Set.of());
        prereqs.put(2, Set.of(1));

        ConceptGraph graph = ConceptGraph.of(concepts, prereqs);

        // Variables is mastered (90% >= 60%), Loops is struggling (40% < 50%)
        Map<Integer, Double> masteries = Map.of(
                1, 90.0,
                2, 40.0
        );

        List<RootCauseView> gaps = analyzer.analyzeGaps(graph, masteries, 60.0);
        // Loops is weak, but has no weak ancestors! So no root cause in the prerequisite tree.
        assertTrue(gaps.isEmpty());
    }
}
