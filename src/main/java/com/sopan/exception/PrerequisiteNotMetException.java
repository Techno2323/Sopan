package com.sopan.exception;

public class PrerequisiteNotMetException extends SopanException {

    private final String prerequisiteTitle;
    private final double currentMastery;
    private final double requiredThreshold;

    public PrerequisiteNotMetException(String prerequisiteTitle, double currentMastery, double requiredThreshold) {
        super(String.format("Prerequisite '%s' not met: mastery is %.1f%%, requires %.1f%%",
                prerequisiteTitle, currentMastery, requiredThreshold));
        this.prerequisiteTitle = prerequisiteTitle;
        this.currentMastery = currentMastery;
        this.requiredThreshold = requiredThreshold;
    }

    public String getPrerequisiteTitle() {
        return prerequisiteTitle;
    }

    public double getCurrentMastery() {
        return currentMastery;
    }

    public double getRequiredThreshold() {
        return requiredThreshold;
    }
}
