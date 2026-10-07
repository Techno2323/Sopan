package com.sopan.engine;

import com.sopan.exception.CyclicDependencyException;
import com.sopan.model.Concept;

import java.util.*;

/**
 * Immutable concept graph for a course.
 * Manages prerequisite relationships, cycle detection via Kahn's algorithm,
 * topological sorting, ancestor/descendant traversal, and level calculation.
 */
public final class ConceptGraph {

    private final Map<Integer, Concept> conceptsById;
    private final Map<Integer, Set<Integer>> prerequisites; // conceptId -> Set of prerequisite IDs it depends on
    private final Map<Integer, Set<Integer>> dependents;    // prerequisiteId -> Set of concept IDs that depend on it
    private final List<Integer> topologicalOrder;
    private final Map<Integer, Integer> levels;

    private ConceptGraph(Map<Integer, Concept> conceptsById,
                         Map<Integer, Set<Integer>> prerequisites,
                         Map<Integer, Set<Integer>> dependents,
                         List<Integer> topologicalOrder,
                         Map<Integer, Integer> levels) {
        this.conceptsById = Collections.unmodifiableMap(new LinkedHashMap<>(conceptsById));
        this.prerequisites = Collections.unmodifiableMap(copyMap(prerequisites));
        this.dependents = Collections.unmodifiableMap(copyMap(dependents));
        this.topologicalOrder = Collections.unmodifiableList(new ArrayList<>(topologicalOrder));
        this.levels = Collections.unmodifiableMap(new LinkedHashMap<>(levels));
    }

    /**
     * Factory constructor. Validates the graph and builds an immutable ConceptGraph.
     *
     * @param concepts list of concepts in the course
     * @param prereqMap map of conceptId -> set of prerequisite conceptIds
     * @throws CyclicDependencyException if the graph contains any cycles
     */
    public static ConceptGraph of(List<Concept> concepts, Map<Integer, Set<Integer>> prereqMap)
            throws CyclicDependencyException {

        Map<Integer, Concept> byId = new LinkedHashMap<>();
        if (concepts != null) {
            for (Concept c : concepts) {
                byId.put(c.getConceptId(), c);
            }
        }

        Map<Integer, Set<Integer>> prereqs = new HashMap<>();
        Map<Integer, Set<Integer>> deps = new HashMap<>();

        for (Integer id : byId.keySet()) {
            prereqs.put(id, new HashSet<>());
            deps.put(id, new HashSet<>());
        }

        if (prereqMap != null) {
            for (Map.Entry<Integer, Set<Integer>> entry : prereqMap.entrySet()) {
                int cId = entry.getKey();
                if (byId.containsKey(cId) && entry.getValue() != null) {
                    for (int pId : entry.getValue()) {
                        if (byId.containsKey(pId) && cId != pId) {
                            prereqs.get(cId).add(pId);
                            deps.get(pId).add(cId);
                        }
                    }
                }
            }
        }

        // Kahn's algorithm for topological sorting and cycle detection
        Map<Integer, Integer> inDegree = new HashMap<>();
        for (Integer id : byId.keySet()) {
            inDegree.put(id, prereqs.get(id).size());
        }

        ArrayDeque<Integer> queue = new ArrayDeque<>();
        for (Map.Entry<Integer, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        List<Integer> topo = new ArrayList<>();
        while (!queue.isEmpty()) {
            int u = queue.poll();
            topo.add(u);
            for (int v : deps.get(u)) {
                int deg = inDegree.get(v) - 1;
                inDegree.put(v, deg);
                if (deg == 0) {
                    queue.add(v);
                }
            }
        }

        if (topo.size() != byId.size()) {
            // Find cycle path for diagnostic reporting
            List<Integer> cycle = findCycle(byId.keySet(), prereqs);
            throw new CyclicDependencyException("Cycle detected in concept prerequisites", cycle);
        }

        // Compute level of each concept: longest path from any root
        Map<Integer, Integer> lvl = new LinkedHashMap<>();
        for (int node : topo) {
            Set<Integer> pSet = prereqs.get(node);
            if (pSet.isEmpty()) {
                lvl.put(node, 0);
            } else {
                int maxParentLevel = 0;
                for (int p : pSet) {
                    maxParentLevel = Math.max(maxParentLevel, lvl.getOrDefault(p, 0));
                }
                lvl.put(node, maxParentLevel + 1);
            }
        }

        return new ConceptGraph(byId, prereqs, deps, topo, lvl);
    }

    public List<Integer> topologicalOrder() {
        return topologicalOrder;
    }

    public Set<Integer> prerequisitesOf(int conceptId) {
        return prerequisites.getOrDefault(conceptId, Collections.emptySet());
    }

    public Set<Integer> dependentsOf(int conceptId) {
        return dependents.getOrDefault(conceptId, Collections.emptySet());
    }

    public Concept getConcept(int conceptId) {
        return conceptsById.get(conceptId);
    }

    public Collection<Concept> allConcepts() {
        return conceptsById.values();
    }

    public Map<Integer, Integer> levels() {
        return levels;
    }

    public int levelOf(int conceptId) {
        return levels.getOrDefault(conceptId, 0);
    }

    /**
     * Finds all ancestor concept IDs (all direct and transitive prerequisites) using DFS.
     */
    public Set<Integer> ancestorsOf(int conceptId) {
        Set<Integer> ancestors = new HashSet<>();
        dfsAncestors(conceptId, ancestors);
        return Collections.unmodifiableSet(ancestors);
    }

    private void dfsAncestors(int current, Set<Integer> visited) {
        for (int p : prerequisitesOf(current)) {
            if (visited.add(p)) {
                dfsAncestors(p, visited);
            }
        }
    }

    /**
     * Finds all descendant concept IDs (concepts that directly or transitively depend on this one) using DFS.
     */
    public Set<Integer> descendantsOf(int conceptId) {
        Set<Integer> descendants = new HashSet<>();
        dfsDescendants(conceptId, descendants);
        return Collections.unmodifiableSet(descendants);
    }

    private void dfsDescendants(int current, Set<Integer> visited) {
        for (int d : dependentsOf(current)) {
            if (visited.add(d)) {
                dfsDescendants(d, visited);
            }
        }
    }

    private static List<Integer> findCycle(Set<Integer> allNodes, Map<Integer, Set<Integer>> prereqs) {
        Map<Integer, Integer> state = new HashMap<>(); // 0: unvisited, 1: visiting, 2: visited
        List<Integer> path = new ArrayList<>();
        for (int node : allNodes) {
            if (findCycleDfs(node, prereqs, state, path)) {
                return path;
            }
        }
        return Collections.emptyList();
    }

    private static boolean findCycleDfs(int current, Map<Integer, Set<Integer>> prereqs,
                                        Map<Integer, Integer> state, List<Integer> path) {
        state.put(current, 1);
        path.add(current);

        for (int next : prereqs.getOrDefault(current, Collections.emptySet())) {
            int st = state.getOrDefault(next, 0);
            if (st == 1) {
                path.add(next);
                return true;
            }
            if (st == 0) {
                if (findCycleDfs(next, prereqs, state, path)) {
                    return true;
                }
            }
        }

        path.remove(path.size() - 1);
        state.put(current, 2);
        return false;
    }

    private static Map<Integer, Set<Integer>> copyMap(Map<Integer, Set<Integer>> source) {
        Map<Integer, Set<Integer>> copy = new HashMap<>();
        for (Map.Entry<Integer, Set<Integer>> entry : source.entrySet()) {
            copy.put(entry.getKey(), Collections.unmodifiableSet(new HashSet<>(entry.getValue())));
        }
        return copy;
    }
}
