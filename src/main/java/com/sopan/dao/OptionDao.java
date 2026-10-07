package com.sopan.dao;

import com.sopan.model.Option;

import java.sql.Connection;
import java.util.List;

public interface OptionDao extends Dao<Option, Integer> {

    List<Option> findByQuestionId(int questionId);
    List<Option> findByQuestionId(Connection conn, int questionId);
}
