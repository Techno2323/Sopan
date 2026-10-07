package com.sopan.engine;

import com.sopan.exception.CyclicDependencyException;
import com.sopan.model.Concept;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ConceptGraphTest {

    private Concept createConcept(int id, String title, int order) {
        Concept c = new Concept();
        c.setConceptId(id);
        c.setCourseId(1);
        c.setTitle(title);
        c.setDisplayOrder(order);
        return c;
    }

    @Test
    @DisplayName("Should correctly construct DAG, compute topological order and levels")
    void testValidDag() throws Exception {
        // Graph structure:
        // C1 (Variables) -> C2 (Loops) -> C4 (Sorting)
        // C1 (Variables) -> C3 (Functions) -> C4 (Sorting)
        List<Concept> concepts = List.of(
                createConcept(1, "Variables", 1),
                createConcept(2, "Loops", 2),
                createConcept(3, "Functions", 3),
                createConcept(4, "Sorting", 4)
        );

        Map<Integer, Set<Integer>> prereqs = new HashMap<>();
        prereqs.put(1, Set.of());
        prereqs.put(2, Set.of(1));
        prereqs.put(3, Set.of(1));
        prereqs.put(4, Set.of(2, 3));

        ConceptGraph graph = ConceptGraph.of(concepts, prereqs);

        assertNotNull(graph);
        assertEquals(4, graph.allConcepts().size());

        // C1 must precede C2 and C3; C2 and C3 must precede C4
        List<Integer> topo = graph.topologicalOrder();
        assertTrue(topo.indexOf(1) < topo.indexOf(2));
        assertTrue(topo.indexOf(1) < topo.indexOf(3));
        assertTrue(topo.indexOf(2) < topo.indexOf(4));
        assertTrue(topo.indexOf(3) < topo.indexOf(4));

        // Longest path levels
        assertEquals(0, graph.levelOf(1));
        assertEquals(1, graph.levelOf(2));
        assertEquals(1, graph.levelOf(3));
        assertEquals(2, graph.levelOf(4));

        // Ancestors
        assertEquals(Set.of(), graph.ancestorsOf(1));
        assertEquals(Set.of(1), graph.ancestorsOf(2));
        assertEquals(Set.of(1, 2, 3), graph.ancestorsOf(4));

        // Descendants
        assertEquals(Set.of(2, 3, 4), graph.descendantsOf(1));
        assertEquals(Set.of(4), graph.descendantsOf(2));
        assertEquals(Set.of(), graph.descendantsOf(4));
    }

    @Test
    @DisplayName("Should detect direct cycle and throw CyclicDependencyException")
    void testDirectCycleDetection() {
        // C1 -> C2 -> C1
        List<Concept> concepts = List.of(
                createConcept(1, "A", 1),
                createConcept(2, "B", 2)
        );

        Map<Integer, Set<Integer>> prereqs = new HashMap<>();
        prereqs.put(1, Set.of(2));
        prereqs.put(2, Set.of(1));

        CyclicDependencyException ex = assertThrows(CyclicDependencyException.class, () ->
                ConceptGraph.of(concepts, prereqs)
        );
        assertNotNull(ex.getCyclePath());
        assertFalse(ex.getCyclePath().isEmpty());
    }

    @Test
    @DisplayName("Should detect multi-step transitive cycle and identify cycle path")
    void testTransitiveCycleDetection() {
        // C1 -> C2 -> C3 -> C1
        List<Concept> concepts = List.of(
                createConcept(1, "A", 1),
                createConcept(2, "B", 2),
                createConcept(3, "C", 3)
        );

        Map<Integer, Set<Integer>> prereqs = new HashMap<>();
        prereqs.put(2, Set.of(1)); // 2 depends on 1
        prereqs.put(3, Set.of(2)); // 3 depends on 2
        prereqs.put(1, Set.of(3)); // 1 depends on 3 -> cycle!

        assertThrows(CyclicDependencyException.class, () ->
                ConceptGraph.of(concepts, prereqs)
        );
    }
}
