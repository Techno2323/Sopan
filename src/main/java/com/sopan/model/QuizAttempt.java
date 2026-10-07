package com.sopan.model;

import java.time.LocalDateTime;

public class QuizAttempt {

    private int attemptId;
    private int quizId;
    private int studentId;
    private int attemptNo;
    private int score;
    private int maxScore;
    private LocalDateTime submittedAt;

    public int getAttemptId()                { return attemptId; }
    public void setAttemptId(int id)         { this.attemptId = id; }

    public int getQuizId()                   { return quizId; }
    public void setQuizId(int id)            { this.quizId = id; }

    public int getStudentId()                { return studentId; }
    public void setStudentId(int id)         { this.studentId = id; }

    public int getAttemptNo()                { return attemptNo; }
    public void setAttemptNo(int n)          { this.attemptNo = n; }

    public int getScore()                    { return score; }
    public void setScore(int s)              { this.score = s; }

    public int getMaxScore()                 { return maxScore; }
    public void setMaxScore(int m)           { this.maxScore = m; }

    public LocalDateTime getSubmittedAt()    { return submittedAt; }
    public void setSubmittedAt(LocalDateTime t) { this.submittedAt = t; }

    /** Percentage score for display. */
    public double getPercentage() {
        return maxScore == 0 ? 0 : (score * 100.0) / maxScore;
    }
}
