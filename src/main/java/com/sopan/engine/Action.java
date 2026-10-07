package com.sopan.engine;

/**
 * An actionable recommendation for a student's learning journey.
 * Natural ordering ranks highest priority actions first.
 */
public abstract class Action implements Comparable<Action> {

    private final String title;
    private final String link;

    protected Action(String title, String link) {
        this.title = title;
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public abstract double priority();

    public abstract String describe();

    @Override
    public int compareTo(Action other) {
        // Higher priority first
        return Double.compare(other.priority(), this.priority());
    }
}
