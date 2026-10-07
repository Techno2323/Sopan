package com.sopan.dao.jdbc;

import com.sopan.config.ConnectionProvider;
import com.sopan.dao.EnrollmentDao;
import com.sopan.exception.DaoException;
import com.sopan.model.Enrollment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcEnrollmentDao extends BaseJdbcDao implements EnrollmentDao {

    private static final String SELECT_BASE =
            "SELECT e.enrollment_id, e.course_id, e.student_id, e.status, e.enrolled_at, " +
            "       c.code AS course_code, c.title AS course_title, " +
            "       u.full_name AS student_name, sp.roll_no AS student_roll_no " +
            "FROM enrollments e " +
            "JOIN courses c ON e.course_id = c.course_id " +
            "JOIN users u ON e.student_id = u.user_id " +
            "LEFT JOIN student_profiles sp ON u.user_id = sp.user_id ";

    public JdbcEnrollmentDao(ConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public Optional<Enrollment> findById(Integer id) {
        return execute(conn -> findById(conn, id), "Failed to find enrollment by id");
    }

    @Override
    public Optional<Enrollment> findById(Connection conn, Integer id) {
        String sql = SELECT_BASE + "WHERE e.enrollment_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapEnrollment(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find enrollment by id: " + id, e);
        }
    }

    @Override
    public Optional<Enrollment> findByCourseAndStudent(int courseId, int studentId) {
        return execute(conn -> findByCourseAndStudent(conn, courseId, studentId), "Failed to find enrollment");
    }

    @Override
    public Optional<Enrollment> findByCourseAndStudent(Connection conn, int courseId, int studentId) {
        String sql = SELECT_BASE + "WHERE e.course_id = ? AND e.student_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            stmt.setInt(2, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapEnrollment(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find enrollment for course " + courseId + " and student " + studentId, e);
        }
    }

    @Override
    public List<Enrollment> findByStudentId(int studentId) {
        return execute(conn -> findByStudentId(conn, studentId), "Failed to find student enrollments");
    }

    @Override
    public List<Enrollment> findByStudentId(Connection conn, int studentId) {
        String sql = SELECT_BASE + "WHERE e.student_id = ? ORDER BY e.enrolled_at DESC";
        List<Enrollment> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapEnrollment(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find enrollments for student: " + studentId, e);
        }
    }

    @Override
    public List<Enrollment> findByCourseId(int courseId) {
        return execute(conn -> findByCourseId(conn, courseId), "Failed to find course enrollments");
    }

    @Override
    public List<Enrollment> findByCourseId(Connection conn, int courseId) {
        String sql = SELECT_BASE + "WHERE e.course_id = ? ORDER BY e.enrolled_at ASC";
        List<Enrollment> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapEnrollment(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find enrollments for course: " + courseId, e);
        }
    }

    @Override
    public int countActiveByCourseId(int courseId) {
        return execute(conn -> countActiveByCourseId(conn, courseId), "Failed to count active enrollments");
    }

    @Override
    public int countActiveByCourseId(Connection conn, int courseId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE course_id = ? AND status = 'ACTIVE'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to count active enrollments for course: " + courseId, e);
        }
    }

    @Override
    public List<Enrollment> findAll() {
        return execute(this::findAll, "Failed to find all enrollments");
    }

    @Override
    public List<Enrollment> findAll(Connection conn) {
        String sql = SELECT_BASE + "ORDER BY e.enrolled_at DESC";
        List<Enrollment> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapEnrollment(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DaoException("Failed to list enrollments", e);
        }
    }

    @Override
    public Integer save(Enrollment enrollment) {
        return execute(conn -> save(conn, enrollment), "Failed to save enrollment");
    }

    @Override
    public Integer save(Connection conn, Enrollment enrollment) {
        String sql = "INSERT INTO enrollments (course_id, student_id, status) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, enrollment.getCourseId());
            stmt.setInt(2, enrollment.getStudentId());
            stmt.setString(3, enrollment.getStatus() != null ? enrollment.getStatus().name() : Enrollment.Status.ACTIVE.name());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    enrollment.setEnrollmentId(id);
                    return id;
                }
                throw new DaoException("Failed to retrieve generated enrollment ID");
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to insert enrollment", e);
        }
    }

    @Override
    public void reactivate(Connection conn, int enrollmentId) {
        String sql = "UPDATE enrollments SET status = 'ACTIVE', enrolled_at = CURRENT_TIMESTAMP WHERE enrollment_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, enrollmentId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to reactivate enrollment: " + enrollmentId, e);
        }
    }

    @Override
    public void updateStatus(Connection conn, int enrollmentId, Enrollment.Status status) {
        String sql = "UPDATE enrollments SET status = ? WHERE enrollment_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, enrollmentId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to update status for enrollment: " + enrollmentId, e);
        }
    }

    @Override
    public void update(Enrollment enrollment) {
        executeVoid(conn -> update(conn, enrollment), "Failed to update enrollment");
    }

    @Override
    public void update(Connection conn, Enrollment enrollment) {
        updateStatus(conn, enrollment.getEnrollmentId(), enrollment.getStatus());
    }

    @Override
    public void delete(Integer id) {
        executeVoid(conn -> delete(conn, id), "Failed to delete enrollment");
    }

    @Override
    public void delete(Connection conn, Integer id) {
        String sql = "DELETE FROM enrollments WHERE enrollment_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to delete enrollment: " + id, e);
        }
    }

    private Enrollment mapEnrollment(ResultSet rs) throws SQLException {
        Enrollment e = new Enrollment();
        e.setEnrollmentId(rs.getInt("enrollment_id"));
        e.setCourseId(rs.getInt("course_id"));
        e.setStudentId(rs.getInt("student_id"));
        e.setStatus(Enrollment.Status.valueOf(rs.getString("status")));
        Timestamp ts = rs.getTimestamp("enrolled_at");
        if (ts != null) {
            e.setEnrolledAt(ts.toLocalDateTime());
        }
        e.setCourseCode(rs.getString("course_code"));
        e.setCourseTitle(rs.getString("course_title"));
        e.setStudentName(rs.getString("student_name"));
        e.setStudentRollNo(rs.getString("student_roll_no"));
        return e;
    }
}
