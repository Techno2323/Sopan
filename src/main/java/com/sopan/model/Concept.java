package com.sopan.model;

/**
 * A single node in the course concept graph.
 * Prerequisites are modelled externally in ConceptGraph.
 */
public class Concept {

    private int conceptId;
    private int courseId;
    private String title;
    private String summary;
    private int displayOrder;

    public int getConceptId()             { return conceptId; }
    public void setConceptId(int id)      { this.conceptId = id; }

    public int getCourseId()              { return courseId; }
    public void setCourseId(int id)       { this.courseId = id; }

    public String getTitle()              { return title; }
    public void setTitle(String t)        { this.title = t; }

    public String getSummary()            { return summary; }
    public void setSummary(String s)      { this.summary = s; }

    public int getDisplayOrder()          { return displayOrder; }
    public void setDisplayOrder(int o)    { this.displayOrder = o; }
}
