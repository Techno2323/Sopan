package com.sopan.dao;

import com.sopan.model.Quiz;

import java.sql.Connection;
import java.util.List;

public interface QuizDao extends Dao<Quiz, Integer> {

    List<Quiz> findByCourseId(int courseId);
    List<Quiz> findByCourseId(Connection conn, int courseId);

    List<Quiz> findPublishedByCourseId(int courseId);
    List<Quiz> findPublishedByCourseId(Connection conn, int courseId);
}
