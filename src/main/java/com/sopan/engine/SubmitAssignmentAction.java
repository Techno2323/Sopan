package com.sopan.engine;

public class SubmitAssignmentAction extends Action {

    private final int assignmentId;
    private final String assignmentTitle;
    private final double priorityScore;
    private final String reason;

    public SubmitAssignmentAction(int assignmentId, String assignmentTitle, double priorityScore, String reason) {
        super("Submit " + assignmentTitle, "/learn/assignment?id=" + assignmentId);
        this.assignmentId = assignmentId;
        this.assignmentTitle = assignmentTitle;
        this.priorityScore = priorityScore;
        this.reason = reason;
    }

    public int getAssignmentId() {
        return assignmentId;
    }

    public String getAssignmentTitle() {
        return assignmentTitle;
    }

    @Override
    public double priority() {
        return priorityScore;
    }

    @Override
    public String describe() {
        return reason != null ? reason : "Complete and submit your assignment before the deadline";
    }
}
