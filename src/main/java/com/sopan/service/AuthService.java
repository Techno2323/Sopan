package com.sopan.service;

import com.sopan.config.TransactionManager;
import com.sopan.dao.UserDao;
import com.sopan.exception.AuthException;
import com.sopan.exception.ValidationException;
import com.sopan.model.Instructor;
import com.sopan.model.Student;
import com.sopan.model.User;
import com.sopan.model.enums.AccountStatus;
import com.sopan.security.PasswordHasher;
import com.sopan.util.Validator;

import java.util.Optional;

public class AuthService {

    private final UserDao userDao;
    private final PasswordHasher passwordHasher;
    private final TransactionManager transactionManager;

    public AuthService(UserDao userDao, PasswordHasher passwordHasher, TransactionManager transactionManager) {
        this.userDao = userDao;
        this.passwordHasher = passwordHasher;
        this.transactionManager = transactionManager;
    }

    public User login(String email, String password) throws AuthException {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new AuthException("Invalid email or password.");
        }

        Optional<User> userOpt = userDao.findByEmail(email.trim());
        if (userOpt.isEmpty()) {
            // Generic message to prevent email enumeration
            throw new AuthException("Invalid email or password.");
        }

        User user = userOpt.get();
        if (user.getStatus() == AccountStatus.SUSPENDED) {
            throw new AuthException("Your account has been suspended. Please contact the administrator.");
        }

        boolean match = passwordHasher.verify(password, user.getPasswordSalt(), user.getPasswordHash());
        if (!match) {
            throw new AuthException("Invalid email or password.");
        }

        return user;
    }

    public Student registerStudent(String fullName, String email, String password,
                                   String rollNo, String program, int studyYear) throws ValidationException, AuthException {
        Validator.requireNotBlank(fullName, "Full name");
        Validator.validateEmail(email);
        Validator.validatePassword(password);
        Validator.requireNotBlank(rollNo, "Roll number");

        if (userDao.findByEmail(email.trim()).isPresent()) {
            throw new AuthException("An account with this email address already exists.");
        }

        PasswordHasher.HashResult hashResult = passwordHasher.hash(password);

        Student student = new Student();
        student.setFullName(fullName.trim());
        student.setEmail(email.trim().toLowerCase());
        student.setPasswordHash(hashResult.hash());
        student.setPasswordSalt(hashResult.salt());
        student.setStatus(AccountStatus.ACTIVE);
        student.setRollNo(rollNo.trim());
        student.setProgram(program != null ? program.trim() : "");
        student.setStudyYear(studyYear);

        return transactionManager.inTransaction(conn -> {
            int id = userDao.saveStudent(conn, student);
            student.setId(id);
            return student;
        });
    }

    public Instructor registerInstructor(String fullName, String email, String password,
                                         String department, String bio) throws ValidationException, AuthException {
        Validator.requireNotBlank(fullName, "Full name");
        Validator.validateEmail(email);
        Validator.validatePassword(password);

        if (userDao.findByEmail(email.trim()).isPresent()) {
            throw new AuthException("An account with this email address already exists.");
        }

        PasswordHasher.HashResult hashResult = passwordHasher.hash(password);

        Instructor instructor = new Instructor();
        instructor.setFullName(fullName.trim());
        instructor.setEmail(email.trim().toLowerCase());
        instructor.setPasswordHash(hashResult.hash());
        instructor.setPasswordSalt(hashResult.salt());
        instructor.setStatus(AccountStatus.ACTIVE);
        instructor.setDepartment(department != null ? department.trim() : "");
        instructor.setBio(bio != null ? bio.trim() : "");

        return transactionManager.inTransaction(conn -> {
            int id = userDao.saveInstructor(conn, instructor);
            instructor.setId(id);
            return instructor;
        });
    }

    public User verifyActiveSessionUser(int userId) throws AuthException {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new AuthException("User session is invalid."));
        if (user.getStatus() == AccountStatus.SUSPENDED) {
            throw new AuthException("Account is suspended.");
        }
        return user;
    }
}
