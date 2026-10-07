package com.sopan.dao;

import com.sopan.model.QuizAttempt;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface QuizAttemptDao extends Dao<QuizAttempt, Integer> {

    List<QuizAttempt> findByQuizAndStudent(int quizId, int studentId);
    List<QuizAttempt> findByQuizAndStudent(Connection conn, int quizId, int studentId);

    int findLatestAttemptNo(Connection conn, int quizId, int studentId);
    int findLatestAttemptNo(int quizId, int studentId);

    Optional<QuizAttempt> findByQuizStudentAndAttemptNo(Connection conn, int quizId, int studentId, int attemptNo);
}
