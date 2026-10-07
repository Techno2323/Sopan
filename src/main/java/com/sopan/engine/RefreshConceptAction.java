package com.sopan.engine;

public class RefreshConceptAction extends Action {

    private final int conceptId;
    private final String conceptTitle;
    private final double priorityScore;
    private final String reason;

    public RefreshConceptAction(int conceptId, String conceptTitle, double priorityScore, String reason) {
        super("Refresh " + conceptTitle, "/learn/concept?id=" + conceptId);
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
        return reason != null ? reason : "Solid concept with no recent activity in over 21 days - quick review recommended";
    }
}
