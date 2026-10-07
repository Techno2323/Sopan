package com.sopan.model;

public class Question {

    private int questionId;
    private int quizId;
    private int conceptId;
    private String prompt;
    private int marks;

    // Transient: concept title for display
    private String conceptTitle;

    public int getQuestionId()              { return questionId; }
    public void setQuestionId(int id)       { this.questionId = id; }

    public int getQuizId()                  { return quizId; }
    public void setQuizId(int id)           { this.quizId = id; }

    public int getConceptId()               { return conceptId; }
    public void setConceptId(int id)        { this.conceptId = id; }

    public String getPrompt()               { return prompt; }
    public void setPrompt(String p)         { this.prompt = p; }

    public int getMarks()                   { return marks; }
    public void setMarks(int m)             { this.marks = m; }

    public String getConceptTitle()         { return conceptTitle; }
    public void setConceptTitle(String t)   { this.conceptTitle = t; }
}
