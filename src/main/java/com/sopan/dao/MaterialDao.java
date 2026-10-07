package com.sopan.dao;

import com.sopan.model.Material;

import java.sql.Connection;
import java.util.List;

public interface MaterialDao extends Dao<Material, Integer> {

    List<Material> findByConceptId(int conceptId);
    List<Material> findByConceptId(Connection conn, int conceptId);
}
