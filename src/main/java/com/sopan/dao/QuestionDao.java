package com.sopan.dao;

import com.sopan.model.Question;

import java.sql.Connection;
import java.util.List;

public interface QuestionDao extends Dao<Question, Integer> {

    List<Question> findByQuizId(int quizId);
    List<Question> findByQuizId(Connection conn, int quizId);
}
