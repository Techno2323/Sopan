package com.sopan.dao;

import com.sopan.model.Admin;
import com.sopan.model.Instructor;
import com.sopan.model.Student;
import com.sopan.model.User;
import com.sopan.model.enums.AccountStatus;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public interface UserDao extends Dao<User, Integer> {

    Optional<User> findByEmail(String email);
    Optional<User> findByEmail(Connection conn, String email);

    int saveStudent(Connection conn, Student student);
    int saveInstructor(Connection conn, Instructor instructor);
    int saveAdmin(Connection conn, Admin admin);

    void updateStatus(Connection conn, int userId, AccountStatus status);
    void updateStatus(int userId, AccountStatus status);

    List<User> findAllUsers();
    List<User> findAllUsers(Connection conn);
}
