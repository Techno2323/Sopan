package com.sopan.engine;

public class TakeQuizAction extends Action {

    private final int quizId;
    private final String quizTitle;
    private final double priorityScore;
    private final String reason;

    public TakeQuizAction(int quizId, String quizTitle, double priorityScore, String reason) {
        super("Take " + quizTitle, "/learn/quiz?id=" + quizId);
        this.quizId = quizId;
        this.quizTitle = quizTitle;
        this.priorityScore = priorityScore;
        this.reason = reason;
    }

    public int getQuizId() {
        return quizId;
    }

    public String getQuizTitle() {
        return quizTitle;
    }

    @Override
    public double priority() {
        return priorityScore;
    }

    @Override
    public String describe() {
        return reason != null ? reason : "Complete quiz to demonstrate concept mastery";
    }
}
