package com.sopan.dao.jdbc;

import com.sopan.config.ConnectionProvider;
import com.sopan.dao.UserDao;
import com.sopan.exception.DaoException;
import com.sopan.model.Admin;
import com.sopan.model.Instructor;
import com.sopan.model.Student;
import com.sopan.model.User;
import com.sopan.model.enums.AccountStatus;
import com.sopan.model.enums.Role;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcUserDao extends BaseJdbcDao implements UserDao {

    private static final String SELECT_BASE =
            "SELECT u.user_id, u.full_name, u.email, u.password_hash, u.password_salt, u.role, u.status, u.created_at, " +
            "       sp.roll_no, sp.program, sp.study_year, " +
            "       ip.department, ip.bio " +
            "FROM users u " +
            "LEFT JOIN student_profiles sp ON u.user_id = sp.user_id " +
            "LEFT JOIN instructor_profiles ip ON u.user_id = ip.user_id ";

    public JdbcUserDao(ConnectionProvider connectionProvider) {
        super(connectionProvider);
    }

    @Override
    public Optional<User> findById(Integer id) {
        return execute(conn -> findById(conn, id), "Failed to find user by id");
    }

    @Override
    public Optional<User> findById(Connection conn, Integer id) {
        String sql = SELECT_BASE + "WHERE u.user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find user by id: " + id, e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return execute(conn -> findByEmail(conn, email), "Failed to find user by email");
    }

    @Override
    public Optional<User> findByEmail(Connection conn, String email) {
        String sql = SELECT_BASE + "WHERE LOWER(u.email) = LOWER(?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to find user by email: " + email, e);
        }
    }

    @Override
    public List<User> findAll() {
        return execute(this::findAll, "Failed to find all users");
    }

    @Override
    public List<User> findAll(Connection conn) {
        return findAllUsers(conn);
    }

    @Override
    public List<User> findAllUsers() {
        return execute(this::findAllUsers, "Failed to find all users");
    }

    @Override
    public List<User> findAllUsers(Connection conn) {
        String sql = SELECT_BASE + "ORDER BY u.created_at DESC";
        List<User> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapUser(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DaoException("Failed to list users", e);
        }
    }

    @Override
    public Integer save(User user) {
        return execute(conn -> save(conn, user), "Failed to save user");
    }

    @Override
    public Integer save(Connection conn, User user) {
        if (user instanceof Student s) {
            return saveStudent(conn, s);
        } else if (user instanceof Instructor i) {
            return saveInstructor(conn, i);
        } else if (user instanceof Admin a) {
            return saveAdmin(conn, a);
        }
        throw new IllegalArgumentException("Unknown user subclass: " + user.getClass());
    }

    @Override
    public int saveStudent(Connection conn, Student student) {
        int userId = insertBaseUser(conn, student);
        student.setId(userId);
        String sql = "INSERT INTO student_profiles (user_id, roll_no, program, study_year) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, student.getRollNo());
            stmt.setString(3, student.getProgram());
            stmt.setInt(4, student.getStudyYear());
            stmt.executeUpdate();
            return userId;
        } catch (SQLException e) {
            throw new DaoException("Failed to save student profile for user: " + userId, e);
        }
    }

    @Override
    public int saveInstructor(Connection conn, Instructor instructor) {
        int userId = insertBaseUser(conn, instructor);
        instructor.setId(userId);
        String sql = "INSERT INTO instructor_profiles (user_id, department, bio) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, instructor.getDepartment());
            stmt.setString(3, instructor.getBio());
            stmt.executeUpdate();
            return userId;
        } catch (SQLException e) {
            throw new DaoException("Failed to save instructor profile for user: " + userId, e);
        }
    }

    @Override
    public int saveAdmin(Connection conn, Admin admin) {
        int userId = insertBaseUser(conn, admin);
        admin.setId(userId);
        return userId;
    }

    private int insertBaseUser(Connection conn, User user) {
        String sql = "INSERT INTO users (full_name, email, password_hash, password_salt, role, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPasswordHash());
            stmt.setString(4, user.getPasswordSalt());
            stmt.setString(5, user.getRole().name());
            stmt.setString(6, user.getStatus() != null ? user.getStatus().name() : AccountStatus.ACTIVE.name());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
                throw new DaoException("Creating user failed, no ID obtained.");
            }
        } catch (SQLException e) {
            throw new DaoException("Failed to insert base user: " + user.getEmail(), e);
        }
    }

    @Override
    public void update(User user) {
        executeVoid(conn -> update(conn, user), "Failed to update user");
    }

    @Override
    public void update(Connection conn, User user) {
        String sql = "UPDATE users SET full_name = ?, email = ?, status = ? WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getStatus().name());
            stmt.setInt(4, user.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to update user: " + user.getId(), e);
        }
    }

    @Override
    public void updateStatus(int userId, AccountStatus status) {
        executeVoid(conn -> updateStatus(conn, userId, status), "Failed to update user status");
    }

    @Override
    public void updateStatus(Connection conn, int userId, AccountStatus status) {
        String sql = "UPDATE users SET status = ? WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to update user status: " + userId, e);
        }
    }

    @Override
    public void delete(Integer id) {
        executeVoid(conn -> delete(conn, id), "Failed to delete user");
    }

    @Override
    public void delete(Connection conn, Integer id) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Failed to delete user: " + id, e);
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        Role role = Role.valueOf(rs.getString("role"));
        User user;
        switch (role) {
            case STUDENT -> {
                Student s = new Student();
                s.setRollNo(rs.getString("roll_no"));
                s.setProgram(rs.getString("program"));
                s.setStudyYear(rs.getInt("study_year"));
                user = s;
            }
            case INSTRUCTOR -> {
                Instructor i = new Instructor();
                i.setDepartment(rs.getString("department"));
                i.setBio(rs.getString("bio"));
                user = i;
            }
            case ADMIN -> user = new Admin();
            default -> throw new IllegalStateException("Unexpected role: " + role);
        }

        user.setId(rs.getInt("user_id"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setPasswordSalt(rs.getString("password_salt"));
        user.setRole(role);
        user.setStatus(AccountStatus.valueOf(rs.getString("status")));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            user.setCreatedAt(ts.toLocalDateTime());
        }
        return user;
    }
}
