package com.sopan.dao;

import com.sopan.model.MasterySnapshot;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MasterySnapshotDao {

    long save(Connection conn, MasterySnapshot snapshot);

    Optional<MasterySnapshot> findLatest(int studentId, int conceptId);
    Optional<MasterySnapshot> findLatest(Connection conn, int studentId, int conceptId);

    Map<Integer, MasterySnapshot> findLatestForCourse(int studentId, int courseId);
    Map<Integer, MasterySnapshot> findLatestForCourse(Connection conn, int studentId, int courseId);

    List<MasterySnapshot> findHistory(int studentId, int conceptId);
    List<MasterySnapshot> findHistory(Connection conn, int studentId, int conceptId);
}
