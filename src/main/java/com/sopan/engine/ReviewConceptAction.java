package com.sopan.engine;

public class ReviewConceptAction extends Action {

    private final int conceptId;
    private final String conceptTitle;
    private final double priorityScore;
    private final String reason;

    public ReviewConceptAction(int conceptId, String conceptTitle, double priorityScore, String reason) {
        super("Review " + conceptTitle, "/learn/concept?id=" + conceptId);
        this.conceptId = conceptId;
        this.conceptTitle = conceptTitle;
        this.priorityScore = priorityScore;
        this.reason = reason;
    }

    public int getConceptId() {
        return conceptId;
    }

    public String getConceptTitle() {
        return conceptTitle;
    }

    @Override
    public double priority() {
        return priorityScore;
    }

    @Override
    public String describe() {
        return reason != null ? reason : "Review learning materials to strengthen foundation in " + conceptTitle;
    }
}
