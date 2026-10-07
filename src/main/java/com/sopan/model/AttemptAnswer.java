package com.sopan.model;

public class AttemptAnswer {

    private int attemptId;
    private int questionId;
    private Integer selectedOptionId;
    private boolean correct;

    public int getAttemptId() { return attemptId; }
    public void setAttemptId(int attemptId) { this.attemptId = attemptId; }

    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }

    public Integer getSelectedOptionId() { return selectedOptionId; }
    public void setSelectedOptionId(Integer selectedOptionId) { this.selectedOptionId = selectedOptionId; }

    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }
}
