package com.sopan.model;

public class SubmissionScore {

    private int submissionId;
    private int criterionId;
    private int points;

    // Transient helpers
    private int conceptId;
    private String criterionDescription;
    private int maxPoints;

    public int getSubmissionId() { return submissionId; }
    public void setSubmissionId(int submissionId) { this.submissionId = submissionId; }

    public int getCriterionId() { return criterionId; }
    public void setCriterionId(int criterionId) { this.criterionId = criterionId; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public int getConceptId() { return conceptId; }
    public void setConceptId(int conceptId) { this.conceptId = conceptId; }

    public String getCriterionDescription() { return criterionDescription; }
    public void setCriterionDescription(String criterionDescription) { this.criterionDescription = criterionDescription; }

    public int getMaxPoints() { return maxPoints; }
    public void setMaxPoints(int maxPoints) { this.maxPoints = maxPoints; }
}
