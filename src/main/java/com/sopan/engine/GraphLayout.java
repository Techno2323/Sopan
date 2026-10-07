package com.sopan.engine;

import com.sopan.dto.ConceptStatusView;
import com.sopan.model.Concept;

import java.util.*;

/**
 * Computes deterministic 2D grid coordinates for concept nodes and prerequisite edges
 * to render inline responsive SVG graphs without client-side charting libraries.
 */
public class GraphLayout {

    public static final int NODE_WIDTH = 200;
    public static final int NODE_HEIGHT = 76;
    public static final int COL_GAP = 90;
    public static final int ROW_GAP = 40;
    public static final int MARGIN_X = 50;
    public static final int MARGIN_Y = 50;

    public static class NodeLayout {
        private final int conceptId;
        private final Concept concept;
        private final ConceptStatusView status;
        private final int x;
        private final int y;
        private final int level;

        public NodeLayout(int conceptId, Concept concept, ConceptStatusView status, int x, int y, int level) {
            this.conceptId = conceptId;
            this.concept = concept;
            this.status = status;
            this.x = x;
            this.y = y;
            this.level = level;
        }

        public int getConceptId() { return conceptId; }
        public Concept getConcept() { return concept; }
        public ConceptStatusView getStatus() { return status; }
        public int getX() { return x; }
        public int getY() { return y; }
        public int getLevel() { return level; }
        public int getWidth() { return NODE_WIDTH; }
        public int getHeight() { return NODE_HEIGHT; }
        public int getCenterX() { return x + NODE_WIDTH / 2; }
        public int getCenterY() { return y + NODE_HEIGHT / 2; }
        public int getRightX() { return x + NODE_WIDTH; }
        public int getLeftX() { return x; }
    }

    public static class EdgeLayout {
        private final int fromConceptId;
        private final int toConceptId;
        private final int startX;
        private final int startY;
        private final int endX;
        private final int endY;

        public EdgeLayout(int fromConceptId, int toConceptId, int startX, int startY, int endX, int endY) {
            this.fromConceptId = fromConceptId;
            this.toConceptId = toConceptId;
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
        }

        public int getFromConceptId() { return fromConceptId; }
        public int getToConceptId() { return toConceptId; }
        public int getStartX() { return startX; }
        public int getStartY() { return startY; }
        public int getEndX() { return endX; }
        public int getEndY() { return endY; }

        /** Returns cubic bezier curve path string for smooth graph connectors */
        public String getPathData() {
            int dx = Math.abs(endX - startX) / 2;
            return String.format("M %d %d C %d %d, %d %d, %d %d",
                    startX, startY,
                    startX + dx, startY,
                    endX - dx, endY,
                    endX, endY);
        }
    }

    public static class LayoutResult {
        private final List<NodeLayout> nodes;
        private final List<EdgeLayout> edges;
        private final int totalWidth;
        private final int totalHeight;

        public LayoutResult(List<NodeLayout> nodes, List<EdgeLayout> edges, int totalWidth, int totalHeight) {
            this.nodes = nodes;
            this.edges = edges;
            this.totalWidth = totalWidth;
            this.totalHeight = totalHeight;
        }

        public List<NodeLayout> getNodes() { return nodes; }
        public List<EdgeLayout> getEdges() { return edges; }
        public int getTotalWidth() { return totalWidth; }
        public int getTotalHeight() { return totalHeight; }
    }

    public LayoutResult layout(ConceptGraph graph, Map<Integer, ConceptStatusView> statusMap) {
        if (graph == null) {
            return new LayoutResult(List.of(), List.of(), 600, 400);
        }

        // Group concepts by level
        Map<Integer, List<Concept>> byLevel = new TreeMap<>();
        for (Concept c : graph.allConcepts()) {
            int lvl = graph.levelOf(c.getConceptId());
            byLevel.computeIfAbsent(lvl, k -> new ArrayList<>()).add(c);
        }

        // Sort each column by display_order
        for (List<Concept> col : byLevel.values()) {
            col.sort(Comparator.comparingInt(Concept::getDisplayOrder));
        }

        Map<Integer, NodeLayout> nodeLayoutMap = new HashMap<>();
        List<NodeLayout> nodeList = new ArrayList<>();

        int maxRow = 0;
        int maxCol = byLevel.keySet().stream().max(Integer::compareTo).orElse(0);

        for (Map.Entry<Integer, List<Concept>> entry : byLevel.entrySet()) {
            int col = entry.getKey();
            List<Concept> conceptsInCol = entry.getValue();
            maxRow = Math.max(maxRow, conceptsInCol.size());

            for (int row = 0; row < conceptsInCol.size(); row++) {
                Concept c = conceptsInCol.get(row);
                int x = MARGIN_X + col * (NODE_WIDTH + COL_GAP);
                int y = MARGIN_Y + row * (NODE_HEIGHT + ROW_GAP);
                ConceptStatusView st = statusMap != null ? statusMap.get(c.getConceptId()) : null;

                NodeLayout nl = new NodeLayout(c.getConceptId(), c, st, x, y, col);
                nodeLayoutMap.put(c.getConceptId(), nl);
                nodeList.add(nl);
            }
        }

        // Generate edges: from prerequisite to dependent
        List<EdgeLayout> edgeList = new ArrayList<>();
        for (Concept c : graph.allConcepts()) {
            int dependentId = c.getConceptId();
            NodeLayout toNode = nodeLayoutMap.get(dependentId);
            if (toNode == null) continue;

            for (int prereqId : graph.prerequisitesOf(dependentId)) {
                NodeLayout fromNode = nodeLayoutMap.get(prereqId);
                if (fromNode != null) {
                    edgeList.add(new EdgeLayout(
                            prereqId, dependentId,
                            fromNode.getRightX(), fromNode.getCenterY(),
                            toNode.getLeftX(), toNode.getCenterY()
                    ));
                }
            }
        }

        int totalWidth = MARGIN_X * 2 + (maxCol + 1) * (NODE_WIDTH + COL_GAP);
        int totalHeight = MARGIN_Y * 2 + Math.max(1, maxRow) * (NODE_HEIGHT + ROW_GAP);

        return new LayoutResult(nodeList, edgeList, Math.max(700, totalWidth), Math.max(450, totalHeight));
    }
}
