package com.sopan.dto;

import com.sopan.model.Concept;
import com.sopan.model.enums.MasteryLevel;

public class ConceptStatusView {

    private final Concept concept;
    private final Double mastery; // null if UNSEEN
    private final int evidenceCount;
    private final MasteryLevel level;
    private final boolean locked;
    private final String blockingPrerequisiteTitle;
    private final boolean needsRefresh;

    public ConceptStatusView(Concept concept, Double mastery, int evidenceCount, MasteryLevel level,
                             boolean locked, String blockingPrerequisiteTitle, boolean needsRefresh) {
        this.concept = concept;
        this.mastery = mastery;
        this.evidenceCount = evidenceCount;
        this.level = level != null ? level : MasteryLevel.UNSEEN;
        this.locked = locked;
        this.blockingPrerequisiteTitle = blockingPrerequisiteTitle;
        this.needsRefresh = needsRefresh;
    }

    public Concept getConcept() { return concept; }
    public int getConceptId() { return concept.getConceptId(); }
    public String getTitle() { return concept.getTitle(); }
    public String getSummary() { return concept.getSummary(); }
    public int getDisplayOrder() { return concept.getDisplayOrder(); }
    public Double getMastery() { return mastery; }
    public int getEvidenceCount() { return evidenceCount; }
    public MasteryLevel getLevel() { return level; }
    public boolean isLocked() { return locked; }
    public String getBlockingPrerequisiteTitle() { return blockingPrerequisiteTitle; }
    public boolean isNeedsRefresh() { return needsRefresh; }

    public String getMasteryFormatted() {
        return mastery != null ? String.format("%.1f%%", mastery) : "Unseen";
    }
}
