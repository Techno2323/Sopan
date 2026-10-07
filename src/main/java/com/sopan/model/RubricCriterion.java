package com.sopan.model;

public class RubricCriterion {

    private int criterionId;
    private int assignmentId;
    private int conceptId;
    private String description;
    private int maxPoints;

    // Transient: concept title for display
    private String conceptTitle;

    public int getCriterionId()              { return criterionId; }
    public void setCriterionId(int id)       { this.criterionId = id; }

    public int getAssignmentId()             { return assignmentId; }
    public void setAssignmentId(int id)      { this.assignmentId = id; }

    public int getConceptId()                { return conceptId; }
    public void setConceptId(int id)         { this.conceptId = id; }

    public String getDescription()           { return description; }
    public void setDescription(String d)      { this.description = d; }

    public int getMaxPoints()                { return maxPoints; }
    public void setMaxPoints(int m)          { this.maxPoints = m; }

    public String getConceptTitle()          { return conceptTitle; }
    public void setConceptTitle(String t)    { this.conceptTitle = t; }
}
