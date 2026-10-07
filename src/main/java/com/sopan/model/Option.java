package com.sopan.model;

public class Option {

    private int optionId;
    private int questionId;
    private String label;
    private boolean correct;

    public int getOptionId()              { return optionId; }
    public void setOptionId(int id)       { this.optionId = id; }

    public int getQuestionId()            { return questionId; }
    public void setQuestionId(int id)     { this.questionId = id; }

    public String getLabel()              { return label; }
    public void setLabel(String l)        { this.label = l; }

    public boolean isCorrect()            { return correct; }
    public void setCorrect(boolean c)     { this.correct = c; }
}
