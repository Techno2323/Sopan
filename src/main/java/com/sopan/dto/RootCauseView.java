package com.sopan.dto;

public class RootCauseView {

    private final int shakyConceptId;
    private final String shakyConceptTitle;
    private final double shakyMastery;
    private final int rootCauseConceptId;
    private final String rootCauseConceptTitle;
    private final double rootCauseMastery;
    private final String message; // e.g. "Inheritance is shaky because of Classes"

    public RootCauseView(int shakyConceptId, String shakyConceptTitle, double shakyMastery,
                         int rootCauseConceptId, String rootCauseConceptTitle, double rootCauseMastery,
                         String message) {
        this.shakyConceptId = shakyConceptId;
        this.shakyConceptTitle = shakyConceptTitle;
        this.shakyMastery = shakyMastery;
        this.rootCauseConceptId = rootCauseConceptId;
        this.rootCauseConceptTitle = rootCauseConceptTitle;
        this.rootCauseMastery = rootCauseMastery;
        this.message = message;
    }

    public int getShakyConceptId() { return shakyConceptId; }
    public String getShakyConceptTitle() { return shakyConceptTitle; }
    public double getShakyMastery() { return shakyMastery; }
    public int getRootCauseConceptId() { return rootCauseConceptId; }
    public String getRootCauseConceptTitle() { return rootCauseConceptTitle; }
    public double getRootCauseMastery() { return rootCauseMastery; }
    public String getMessage() { return message; }
}
