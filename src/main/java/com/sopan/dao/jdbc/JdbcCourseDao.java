package com.sopan.dao.jdbc;

import com.sopan.config.ConnectionProvider;
import com.sopan.dao.CourseDao;
import com.sopan.exception.DaoException;
import com.sopan.model.Course;
import com.sopan.model.enums.CourseStatus;
import com.sopan.model.enums.Difficulty;
import com.sopan.util.Page;

import java.sql.*;
import java.util.*;

public class JdbcCourseDao extends BaseJdbcDao implements CourseDao {

    private static final Set<String> ALLOWED_SORT_COLUMNS = Set.of(
            "created_at", "title", "code", "difficulty", "gate_threshold", "category"
    );

    private static final String SELECT_BASE =
            "SELECT c.course_id, c.instructor_id, c.code, c.title, c.description, c.category, " +
            "       c.difficulty, c.max_students, c.gate_threshold, c.status, c.created_at, " +
            "       u.full_name AS instructor_name " +
            "FROM courses c " +
            "JOIN users u ON c.instructor_id = u.user_id ";

    public JdbcCourseDao(ConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public Optional<Course> findById(Integer id) {
        return execute(conn -> findById(conn, id), "Failed to find course by id");
    }

    @Override
    public Optional<Course> findById(Connection conn, Integer id) {
        String sql = SELECT_BASE + "WHERE c.course_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapCourse(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find course by id: " + id, e);
        }
    }

    @Override
    public List<Course> findAll() {
        return execute(this::findAll, "Failed to find all courses");
    }

    @Override
    public List<Course> findAll(Connection conn) {
        String sql = SELECT_BASE + "ORDER BY c.created_at DESC";
        List<Course> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapCourse(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DaoException("Failed to list all courses", e);
        }
    }

    @Override
    public List<Course> findByInstructorId(int instructorId) {
        return execute(conn -> findByInstructorId(conn, instructorId), "Failed to find instructor courses");
    }

    @Override
    public List<Course> findByInstructorId(Connection conn, int instructorId) {
        String sql = SELECT_BASE + "WHERE c.instructor_id = ? ORDER BY c.created_at DESC";
        List<Course> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, instructorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapCourse(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find courses for instructor: " + instructorId, e);
        }
    }

    @Override
    public List<Course> findByStatus(CourseStatus status) {
        return execute(conn -> findByStatus(conn, status), "Failed to find courses by status");
    }

    @Override
    public List<Course> findByStatus(Connection conn, CourseStatus status) {
        String sql = SELECT_BASE + "WHERE c.status = ? ORDER BY c.created_at DESC";
        List<Course> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapCourse(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find courses by status: " + status, e);
        }
    }

    @Override
    public Optional<Course> lockCourseForUpdate(Connection conn, int courseId) {
        String sql = SELECT_BASE + "WHERE c.course_id = ? FOR UPDATE";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapCourse(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to lock course row for update: " + courseId, e);
        }
    }

    @Override
    public Page<Course> searchPublished(String query, String category, String difficulty,
                                        String sortBy, String sortOrder, int pageNumber, int pageSize) {
        return execute(conn -> {
            StringBuilder whereClause = new StringBuilder("WHERE c.status = 'PUBLISHED' ");
            List<Object> params = new ArrayList<>();

            if (query != null && !query.isBlank()) {
                whereClause.append("AND (LOWER(c.title) LIKE ? OR LOWER(c.code) LIKE ? OR LOWER(c.description) LIKE ?) ");
                String wild = "%" + query.trim().toLowerCase() + "%";
                params.add(wild);
                params.add(wild);
                params.add(wild);
            }
            if (category != null && !category.isBlank()) {
                whereClause.append("AND LOWER(c.category) = LOWER(?) ");
                params.add(category.trim());
            }
            if (difficulty != null && !difficulty.isBlank()) {
                whereClause.append("AND c.difficulty = ? ");
                params.add(difficulty.trim().toUpperCase());
            }

            // Count query
            long total = 0;
            String countSql = "SELECT COUNT(*) FROM courses c " + whereClause;
            try (PreparedStatement stmt = conn.prepareStatement(countSql)) {
                for (int i = 0; i < params.size(); i++) {
                    stmt.setObject(i + 1, params.get(i));
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        total = rs.getLong(1);
                    }
                }
            }

            // Safe dynamic sort validation against strict allow-list
            String safeSort = "created_at";
            if (sortBy != null && ALLOWED_SORT_COLUMNS.contains(sortBy.toLowerCase().trim())) {
                safeSort = sortBy.toLowerCase().trim();
            }
            String safeOrder = "DESC";
            if ("asc".equalsIgnoreCase(sortOrder)) {
                safeOrder = "ASC";
            }

            int offset = Math.max(0, (pageNumber - 1) * pageSize);
            String dataSql = SELECT_BASE + whereClause + "ORDER BY c." + safeSort + " " + safeOrder + " LIMIT ? OFFSET ?";
            List<Course> courses = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(dataSql)) {
                int idx = 1;
                for (Object param : params) {
                    stmt.setObject(idx++, param);
                }
                stmt.setInt(idx++, pageSize);
                stmt.setInt(idx, offset);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        courses.add(mapCourse(rs));
                    }
                }
            }
            return new Page<>(courses, pageNumber, pageSize, total);
        }, "Failed to search published courses");
    }

    @Override
    public Integer save(Course course) {
        return execute(conn -> save(conn, course), "Failed to save course");
    }

    @Override
    public Integer save(Connection conn, Course course) {
        String sql = "INSERT INTO courses (instructor_id, code, title, description, category, difficulty, max_students, gate_threshold, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, course.getInstructorId());
            stmt.setString(2, course.getCode());
            stmt.setString(3, course.getTitle());
            stmt.setString(4, course.getDescription());
            stmt.setString(5, course.getCategory());
            stmt.setString(6, course.getDifficulty().name());
            stmt.setInt(7, course.getMaxStudents());
            stmt.setInt(8, course.getGateThreshold());
            stmt.setString(9, course.getStatus().name());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    course.setCourseId(id);
                    return id;
                }
                throw new DaoException("Failed to get generated course ID");
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to insert course: " + course.getCode(), e);
        }
    }

    @Override
    public void update(Course course) {
        executeVoid(conn -> update(conn, course), "Failed to update course");
    }

    @Override
    public void update(Connection conn, Course course) {
        String sql = "UPDATE courses SET title = ?, description = ?, category = ?, difficulty = ?, " +
                     "max_students = ?, gate_threshold = ?, status = ? WHERE course_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, course.getTitle());
            stmt.setString(2, course.getDescription());
            stmt.setString(3, course.getCategory());
            stmt.setString(4, course.getDifficulty().name());
            stmt.setInt(5, course.getMaxStudents());
            stmt.setInt(6, course.getGateThreshold());
            stmt.setString(7, course.getStatus().name());
            stmt.setInt(8, course.getCourseId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to update course: " + course.getCourseId(), e);
        }
    }

    @Override
    public void updateStatus(int courseId, CourseStatus status) {
        executeVoid(conn -> updateStatus(conn, courseId, status), "Failed to update course status");
    }

    @Override
    public void updateStatus(Connection conn, int courseId, CourseStatus status) {
        String sql = "UPDATE courses SET status = ? WHERE course_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, courseId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to update status for course: " + courseId, e);
        }
    }

    @Override
    public boolean hasEnrollments(int courseId) {
        return execute(conn -> hasEnrollments(conn, courseId), "Failed to check course enrollments");
    }

    @Override
    public boolean hasEnrollments(Connection conn, int courseId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE course_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to check enrollments for course: " + courseId, e);
        }
    }

    @Override
    public void delete(Integer id) {
        executeVoid(conn -> delete(conn, id), "Failed to delete course");
    }

    @Override
    public void delete(Connection conn, Integer id) {
        String sql = "DELETE FROM courses WHERE course_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to delete course: " + id, e);
        }
    }

    private Course mapCourse(ResultSet rs) throws SQLException {
        Course c = new Course();
        c.setCourseId(rs.getInt("course_id"));
        c.setInstructorId(rs.getInt("instructor_id"));
        c.setCode(rs.getString("code"));
        c.setTitle(rs.getString("title"));
        c.setDescription(rs.getString("description"));
        c.setCategory(rs.getString("category"));
        c.setDifficulty(Difficulty.valueOf(rs.getString("difficulty")));
        c.setMaxStudents(rs.getInt("max_students"));
        c.setGateThreshold(rs.getInt("gate_threshold"));
        c.setStatus(CourseStatus.valueOf(rs.getString("status")));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            c.setCreatedAt(ts.toLocalDateTime());
        }
        c.setInstructorName(rs.getString("instructor_name"));
        return c;
    }
}
