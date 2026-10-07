-- Sopan LMS — Full MySQL Schema
-- Run this script once to create the database and all tables.

CREATE DATABASE IF NOT EXISTS sopan CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sopan;

CREATE TABLE users (
  user_id INT AUTO_INCREMENT PRIMARY KEY,
  full_name VARCHAR(100) NOT NULL,
  email VARCHAR(150) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  password_salt VARCHAR(64) NOT NULL,
  role ENUM('STUDENT','INSTRUCTOR','ADMIN') NOT NULL,
  status ENUM('ACTIVE','SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE student_profiles (
  user_id INT PRIMARY KEY,
  roll_no VARCHAR(30) NOT NULL UNIQUE,
  program VARCHAR(80),
  study_year TINYINT,
  FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE instructor_profiles (
  user_id INT PRIMARY KEY,
  department VARCHAR(80),
  bio VARCHAR(500),
  FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE courses (
  course_id INT AUTO_INCREMENT PRIMARY KEY,
  instructor_id INT NOT NULL,
  code VARCHAR(20) NOT NULL UNIQUE,
  title VARCHAR(150) NOT NULL,
  description TEXT,
  category VARCHAR(60),
  difficulty ENUM('BEGINNER','INTERMEDIATE','ADVANCED') NOT NULL DEFAULT 'BEGINNER',
  max_students INT NOT NULL DEFAULT 60,
  gate_threshold TINYINT NOT NULL DEFAULT 60,
  status ENUM('DRAFT','PENDING_APPROVAL','PUBLISHED','ARCHIVED') NOT NULL DEFAULT 'DRAFT',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CHECK (max_students > 0),
  CHECK (gate_threshold BETWEEN 0 AND 100),
  FOREIGN KEY (instructor_id) REFERENCES users(user_id)
) ENGINE=InnoDB;

CREATE TABLE enrollments (
  enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
  course_id INT NOT NULL,
  student_id INT NOT NULL,
  status ENUM('ACTIVE','DROPPED','COMPLETED') NOT NULL DEFAULT 'ACTIVE',
  enrolled_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (course_id, student_id),
  FOREIGN KEY (course_id) REFERENCES courses(course_id),
  FOREIGN KEY (student_id) REFERENCES users(user_id)
) ENGINE=InnoDB;
CREATE INDEX idx_enroll_student ON enrollments(student_id);
CREATE INDEX idx_enroll_course_status ON enrollments(course_id, status);

CREATE TABLE concepts (
  concept_id INT AUTO_INCREMENT PRIMARY KEY,
  course_id INT NOT NULL,
  title VARCHAR(120) NOT NULL,
  summary VARCHAR(500),
  display_order INT NOT NULL DEFAULT 0,
  UNIQUE (course_id, title),
  FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE INDEX idx_concepts_course_order ON concepts(course_id, display_order);

CREATE TABLE concept_prerequisites (
  concept_id INT NOT NULL,
  prerequisite_id INT NOT NULL,
  PRIMARY KEY (concept_id, prerequisite_id),
  CHECK (concept_id <> prerequisite_id),
  FOREIGN KEY (concept_id) REFERENCES concepts(concept_id) ON DELETE CASCADE,
  FOREIGN KEY (prerequisite_id) REFERENCES concepts(concept_id) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE INDEX idx_prereq_reverse ON concept_prerequisites(prerequisite_id);

CREATE TABLE materials (
  material_id INT AUTO_INCREMENT PRIMARY KEY,
  concept_id INT NOT NULL,
  title VARCHAR(150) NOT NULL,
  type ENUM('PDF','LINK','NOTE') NOT NULL,
  location VARCHAR(500) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (concept_id) REFERENCES concepts(concept_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE quizzes (
  quiz_id INT AUTO_INCREMENT PRIMARY KEY,
  course_id INT NOT NULL,
  title VARCHAR(150) NOT NULL,
  max_attempts INT NOT NULL DEFAULT 3,
  status ENUM('DRAFT','PUBLISHED') NOT NULL DEFAULT 'DRAFT',
  FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE quiz_questions (
  question_id INT AUTO_INCREMENT PRIMARY KEY,
  quiz_id INT NOT NULL,
  concept_id INT NOT NULL,
  prompt VARCHAR(1000) NOT NULL,
  marks INT NOT NULL DEFAULT 1,
  FOREIGN KEY (quiz_id) REFERENCES quizzes(quiz_id) ON DELETE CASCADE,
  FOREIGN KEY (concept_id) REFERENCES concepts(concept_id)
) ENGINE=InnoDB;
CREATE INDEX idx_q_quiz ON quiz_questions(quiz_id);
CREATE INDEX idx_q_concept ON quiz_questions(concept_id);

CREATE TABLE question_options (
  option_id INT AUTO_INCREMENT PRIMARY KEY,
  question_id INT NOT NULL,
  label VARCHAR(500) NOT NULL,
  is_correct BOOLEAN NOT NULL DEFAULT FALSE,
  FOREIGN KEY (question_id) REFERENCES quiz_questions(question_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE quiz_attempts (
  attempt_id INT AUTO_INCREMENT PRIMARY KEY,
  quiz_id INT NOT NULL,
  student_id INT NOT NULL,
  attempt_no INT NOT NULL,
  score INT NOT NULL,
  max_score INT NOT NULL,
  submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (quiz_id, student_id, attempt_no),
  FOREIGN KEY (quiz_id) REFERENCES quizzes(quiz_id),
  FOREIGN KEY (student_id) REFERENCES users(user_id)
) ENGINE=InnoDB;
CREATE INDEX idx_attempt_student_quiz ON quiz_attempts(student_id, quiz_id);

CREATE TABLE attempt_answers (
  attempt_id INT NOT NULL,
  question_id INT NOT NULL,
  selected_option_id INT,
  is_correct BOOLEAN NOT NULL,
  PRIMARY KEY (attempt_id, question_id),
  FOREIGN KEY (attempt_id) REFERENCES quiz_attempts(attempt_id) ON DELETE CASCADE,
  FOREIGN KEY (question_id) REFERENCES quiz_questions(question_id),
  FOREIGN KEY (selected_option_id) REFERENCES question_options(option_id)
) ENGINE=InnoDB;

CREATE TABLE assignments (
  assignment_id INT AUTO_INCREMENT PRIMARY KEY,
  course_id INT NOT NULL,
  title VARCHAR(150) NOT NULL,
  instructions TEXT,
  due_at DATETIME NOT NULL,
  allow_late BOOLEAN NOT NULL DEFAULT FALSE,
  FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE INDEX idx_assign_course_due ON assignments(course_id, due_at);

CREATE TABLE rubric_criteria (
  criterion_id INT AUTO_INCREMENT PRIMARY KEY,
  assignment_id INT NOT NULL,
  concept_id INT NOT NULL,
  description VARCHAR(300) NOT NULL,
  max_points INT NOT NULL,
  CHECK (max_points > 0),
  FOREIGN KEY (assignment_id) REFERENCES assignments(assignment_id) ON DELETE CASCADE,
  FOREIGN KEY (concept_id) REFERENCES concepts(concept_id)
) ENGINE=InnoDB;

CREATE TABLE submissions (
  submission_id INT AUTO_INCREMENT PRIMARY KEY,
  assignment_id INT NOT NULL,
  student_id INT NOT NULL,
  body_text TEXT,
  file_path VARCHAR(300),
  submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_late BOOLEAN NOT NULL DEFAULT FALSE,
  status ENUM('SUBMITTED','GRADED') NOT NULL DEFAULT 'SUBMITTED',
  graded_at TIMESTAMP NULL,
  feedback TEXT,
  UNIQUE (assignment_id, student_id),
  FOREIGN KEY (assignment_id) REFERENCES assignments(assignment_id),
  FOREIGN KEY (student_id) REFERENCES users(user_id)
) ENGINE=InnoDB;

CREATE TABLE submission_scores (
  submission_id INT NOT NULL,
  criterion_id INT NOT NULL,
  points INT NOT NULL,
  PRIMARY KEY (submission_id, criterion_id),
  CHECK (points >= 0),
  FOREIGN KEY (submission_id) REFERENCES submissions(submission_id) ON DELETE CASCADE,
  FOREIGN KEY (criterion_id) REFERENCES rubric_criteria(criterion_id)
) ENGINE=InnoDB;

CREATE TABLE mastery_snapshots (
  snapshot_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  student_id INT NOT NULL,
  concept_id INT NOT NULL,
  mastery DECIMAL(5,2) NOT NULL,
  evidence_count INT NOT NULL,
  computed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (student_id) REFERENCES users(user_id),
  FOREIGN KEY (concept_id) REFERENCES concepts(concept_id)
) ENGINE=InnoDB;
CREATE INDEX idx_snap_latest ON mastery_snapshots(student_id, concept_id, computed_at);

CREATE TABLE interventions (
  intervention_id INT AUTO_INCREMENT PRIMARY KEY,
  course_id INT NOT NULL,
  concept_id INT NOT NULL,
  student_id INT NOT NULL,
  instructor_id INT NOT NULL,
  type ENUM('NOTE','EXTRA_MATERIAL','OFFICE_HOURS','RETAKE') NOT NULL,
  note VARCHAR(1000),
  mastery_before DECIMAL(5,2) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  resolved_at TIMESTAMP NULL,
  mastery_after DECIMAL(5,2) NULL,
  FOREIGN KEY (course_id) REFERENCES courses(course_id),
  FOREIGN KEY (concept_id) REFERENCES concepts(concept_id),
  FOREIGN KEY (student_id) REFERENCES users(user_id),
  FOREIGN KEY (instructor_id) REFERENCES users(user_id)
) ENGINE=InnoDB;
CREATE INDEX idx_interv_course_student ON interventions(course_id, student_id, resolved_at);

CREATE TABLE notifications (
  notification_id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  type VARCHAR(40) NOT NULL,
  message VARCHAR(300) NOT NULL,
  link VARCHAR(200),
  dedupe_key VARCHAR(120) NOT NULL,
  is_read BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (user_id, dedupe_key),
  FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE INDEX idx_notif_user ON notifications(user_id, is_read, created_at);

CREATE TABLE audit_log (
  log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NULL,
  action VARCHAR(60) NOT NULL,
  entity VARCHAR(60),
  entity_id INT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- Unified evidence view: quiz answers (weight 1) and rubric scores (weight 3)
CREATE VIEW v_evidence AS
SELECT qa.student_id, qq.concept_id,
       aa.is_correct AS score, 1 AS weight,
       qa.submitted_at AS recorded_at
FROM attempt_answers aa
JOIN quiz_attempts qa ON qa.attempt_id = aa.attempt_id
JOIN quiz_questions qq ON qq.question_id = aa.question_id
UNION ALL
SELECT s.student_id, rc.concept_id,
       ss.points / rc.max_points, 3,
       s.graded_at
FROM submission_scores ss
JOIN submissions s ON s.submission_id = ss.submission_id
JOIN rubric_criteria rc ON rc.criterion_id = ss.criterion_id;
