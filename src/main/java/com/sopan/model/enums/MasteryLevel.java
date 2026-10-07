package com.sopan.model.enums;

/**
 * Discrete mastery levels derived from the continuous mastery score.
 * Each level has a display colour and bar count for the concept map.
 */
public enum MasteryLevel {

    UNSEEN   ("grey",   0, "Not yet attempted"),
    EARLY    ("slate",  0, "Too few data points"),
    SHAKY    ("coral",  1, "Below 50 — needs work"),
    DEVELOPING("amber", 2, "50–74 — on the way"),
    SOLID    ("teal",   3, "75+ — strong grasp");

    private final String colour;
    private final int bars;
    private final String label;

    MasteryLevel(String colour, int bars, String label) {
        this.colour = colour;
        this.bars   = bars;
        this.label  = label;
    }

    public String getColour() { return colour; }
    public int    getBars()   { return bars; }
    public String getLabel()  { return label; }
}
