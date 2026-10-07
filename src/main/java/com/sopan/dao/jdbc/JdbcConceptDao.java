package com.sopan.dao.jdbc;

import com.sopan.config.ConnectionProvider;
import com.sopan.dao.ConceptDao;
import com.sopan.exception.DaoException;
import com.sopan.model.Concept;

import java.sql.*;
import java.util.*;

public class JdbcConceptDao extends BaseJdbcDao implements ConceptDao {

    public JdbcConceptDao(ConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public Optional<Concept> findById(Integer id) {
        return execute(conn -> findById(conn, id), "Failed to find concept by id");
    }

    @Override
    public Optional<Concept> findById(Connection conn, Integer id) {
        String sql = "SELECT concept_id, course_id, title, summary, display_order FROM concepts WHERE concept_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapConcept(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find concept: " + id, e);
        }
    }

    @Override
    public List<Concept> findByCourseId(int courseId) {
        return execute(conn -> findByCourseId(conn, courseId), "Failed to find course concepts");
    }

    @Override
    public List<Concept> findByCourseId(Connection conn, int courseId) {
        String sql = "SELECT concept_id, course_id, title, summary, display_order FROM concepts WHERE course_id = ? ORDER BY display_order ASC";
        List<Concept> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapConcept(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find concepts for course: " + courseId, e);
        }
    }

    @Override
    public Map<Integer, Set<Integer>> findPrerequisitesMap(int courseId) {
        return execute(conn -> findPrerequisitesMap(conn, courseId), "Failed to find prerequisites map");
    }

    @Override
    public Map<Integer, Set<Integer>> findPrerequisitesMap(Connection conn, int courseId) {
        String sql = "SELECT cp.concept_id, cp.prerequisite_id " +
                     "FROM concept_prerequisites cp " +
                     "JOIN concepts c ON cp.concept_id = c.concept_id " +
                     "WHERE c.course_id = ?";
        Map<Integer, Set<Integer>> map = new HashMap<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int cId = rs.getInt("concept_id");
                    int pId = rs.getInt("prerequisite_id");
                    map.computeIfAbsent(cId, k -> new HashSet<>()).add(pId);
                }
                return map;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find prerequisites for course: " + courseId, e);
        }
    }

    @Override
    public Set<Integer> findPrerequisitesForConcept(int conceptId) {
        return execute(conn -> findPrerequisitesForConcept(conn, conceptId), "Failed to find concept prerequisites");
    }

    @Override
    public Set<Integer> findPrerequisitesForConcept(Connection conn, int conceptId) {
        String sql = "SELECT prerequisite_id FROM concept_prerequisites WHERE concept_id = ?";
        Set<Integer> set = new HashSet<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, conceptId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    set.add(rs.getInt(1));
                }
                return set;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find prerequisites for concept: " + conceptId, e);
        }
    }

    @Override
    public void savePrerequisites(Connection conn, int courseId, Map<Integer, Set<Integer>> prerequisites) {
        // Delete existing prerequisites for all concepts belonging to this course
        String deleteSql = "DELETE cp FROM concept_prerequisites cp " +
                           "JOIN concepts c ON cp.concept_id = c.concept_id " +
                           "WHERE c.course_id = ?";
        try (PreparedStatement delStmt = conn.prepareStatement(deleteSql)) {
            delStmt.setInt(1, courseId);
            delStmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to clear prerequisites for course: " + courseId, e);
        }

        // Insert new prerequisites
        if (prerequisites != null && !prerequisites.isEmpty()) {
            String insertSql = "INSERT INTO concept_prerequisites (concept_id, prerequisite_id) VALUES (?, ?)";
            try (PreparedStatement insStmt = conn.prepareStatement(insertSql)) {
                for (Map.Entry<Integer, Set<Integer>> entry : prerequisites.entrySet()) {
                    int cId = entry.getKey();
                    for (int pId : entry.getValue()) {
                        insStmt.setInt(1, cId);
                        insStmt.setInt(2, pId);
                        insStmt.addBatch();
                    }
                }
                insStmt.executeBatch();
            } catch (SQLException e) {
                throw new DaoException("Failed to save prerequisites for course: " + courseId, e);
            }
        }
    }

    @Override
    public int countEvidenceForConcept(int conceptId) {
        return execute(conn -> countEvidenceForConcept(conn, conceptId), "Failed to count concept evidence");
    }

    @Override
    public int countEvidenceForConcept(Connection conn, int conceptId) {
        String sql = "SELECT COUNT(*) FROM v_evidence WHERE concept_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, conceptId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to count evidence for concept: " + conceptId, e);
        }
    }

    @Override
    public List<Concept> findAll() {
        return execute(this::findAll, "Failed to find all concepts");
    }

    @Override
    public List<Concept> findAll(Connection conn) {
        String sql = "SELECT concept_id, course_id, title, summary, display_order FROM concepts ORDER BY course_id, display_order";
        List<Concept> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapConcept(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DaoException("Failed to list concepts", e);
        }
    }

    @Override
    public Integer save(Concept concept) {
        return execute(conn -> save(conn, concept), "Failed to save concept");
    }

    @Override
    public Integer save(Connection conn, Concept concept) {
        String sql = "INSERT INTO concepts (course_id, title, summary, display_order) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, concept.getCourseId());
            stmt.setString(2, concept.getTitle());
            stmt.setString(3, concept.getSummary());
            stmt.setInt(4, concept.getDisplayOrder());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    concept.setConceptId(id);
                    return id;
                }
                throw new DaoException("Failed to obtain concept ID");
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to insert concept: " + concept.getTitle(), e);
        }
    }

    @Override
    public void update(Concept concept) {
        executeVoid(conn -> update(conn, concept), "Failed to update concept");
    }

    @Override
    public void update(Connection conn, Concept concept) {
        String sql = "UPDATE concepts SET title = ?, summary = ?, display_order = ? WHERE concept_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, concept.getTitle());
            stmt.setString(2, concept.getSummary());
            stmt.setInt(3, concept.getDisplayOrder());
            stmt.setInt(4, concept.getConceptId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to update concept: " + concept.getConceptId(), e);
        }
    }

    @Override
    public void delete(Integer id) {
        executeVoid(conn -> delete(conn, id), "Failed to delete concept");
    }

    @Override
    public void delete(Connection conn, Integer id) {
        String sql = "DELETE FROM concepts WHERE concept_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to delete concept: " + id, e);
        }
    }

    private Concept mapConcept(ResultSet rs) throws SQLException {
        Concept c = new Concept();
        c.setConceptId(rs.getInt("concept_id"));
        c.setCourseId(rs.getInt("course_id"));
        c.setTitle(rs.getString("title"));
        c.setSummary(rs.getString("summary"));
        c.setDisplayOrder(rs.getInt("display_order"));
        return c;
    }
}
