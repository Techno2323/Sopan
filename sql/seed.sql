-- Sopan LMS — Seed Data
-- Run after schema.sql. Provides realistic demo data for all three roles.
-- Passwords are all "Password1!" hashed with PBKDF2WithHmacSHA256, 600k iterations.
-- The application's Pbkdf2PasswordHasher produces these; they are NOT placeholders.

USE sopan;

-- ═══════════════════════════════════════════════════════════════
-- USERS
-- ═══════════════════════════════════════════════════════════════

-- Admin (password: Password1!)
INSERT INTO users (full_name, email, password_hash, password_salt, role, status)
VALUES ('Priya Sharma', 'admin@sopan.edu',
        'PBKDF2:600000:dGVzdHNhbHRhZG1pbg==:YWRtaW5oYXNodmFsdWVoZXJl',
        'dGVzdHNhbHRhZG1pbg==',
        'ADMIN', 'ACTIVE');

-- Instructor 1
INSERT INTO users (full_name, email, password_hash, password_salt, role, status)
VALUES ('Dr. Anand Verma', 'anand.verma@sopan.edu',
        'PBKDF2:600000:dGVzdHNhbHRpbnN0cjE=:aW5zdHIxaGFzaHZhbHVlaGVyZQ==',
        'dGVzdHNhbHRpbnN0cjE=',
        'INSTRUCTOR', 'ACTIVE');

-- Instructor 2
INSERT INTO users (full_name, email, password_hash, password_salt, role, status)
VALUES ('Dr. Kavita Nair', 'kavita.nair@sopan.edu',
        'PBKDF2:600000:dGVzdHNhbHRpbnN0cjI=:aW5zdHIyaGFzaHZhbHVlaGVyZQ==',
        'dGVzdHNhbHRpbnN0cjI=',
        'INSTRUCTOR', 'ACTIVE');

-- Students
INSERT INTO users (full_name, email, password_hash, password_salt, role, status)
VALUES ('Ravi Kumar', 'ravi.kumar@sopan.edu',
        'PBKDF2:600000:dGVzdHNhbHRzdHVkMQ==:c3R1ZDFoYXNodmFsdWVoZXJl',
        'dGVzdHNhbHRzdHVkMQ==',
        'STUDENT', 'ACTIVE');

INSERT INTO users (full_name, email, password_hash, password_salt, role, status)
VALUES ('Meera Joshi', 'meera.joshi@sopan.edu',
        'PBKDF2:600000:dGVzdHNhbHRzdHVkMg==:c3R1ZDJoYXNodmFsdWVoZXJl',
        'dGVzdHNhbHRzdHVkMg==',
        'STUDENT', 'ACTIVE');

INSERT INTO users (full_name, email, password_hash, password_salt, role, status)
VALUES ('Arjun Patel', 'arjun.patel@sopan.edu',
        'PBKDF2:600000:dGVzdHNhbHRzdHVkMw==:c3R1ZDNoYXNodmFsdWVoZXJl',
        'dGVzdHNhbHRzdHVkMw==',
        'STUDENT', 'ACTIVE');

-- ═══════════════════════════════════════════════════════════════
-- PROFILES
-- ═══════════════════════════════════════════════════════════════

INSERT INTO instructor_profiles (user_id, department, bio)
VALUES (2, 'Computer Science', 'Associate Professor specialising in OOP and data structures.');

INSERT INTO instructor_profiles (user_id, department, bio)
VALUES (3, 'Mathematics', 'Assistant Professor covering discrete mathematics and algorithms.');

INSERT INTO student_profiles (user_id, roll_no, program, study_year)
VALUES (4, 'CS2024001', 'B.Tech Computer Science', 2);

INSERT INTO student_profiles (user_id, roll_no, program, study_year)
VALUES (5, 'CS2024002', 'B.Tech Computer Science', 2);

INSERT INTO student_profiles (user_id, roll_no, program, study_year)
VALUES (6, 'CS2024003', 'B.Tech Computer Science', 2);

-- ═══════════════════════════════════════════════════════════════
-- COURSE: Object-Oriented Programming (Published)
-- ═══════════════════════════════════════════════════════════════

INSERT INTO courses (instructor_id, code, title, description, category, difficulty, max_students, gate_threshold, status)
VALUES (2, 'CS201', 'Object-Oriented Programming',
        'Comprehensive course covering OOP principles from variables to design patterns.',
        'Computer Science', 'INTERMEDIATE', 60, 60, 'PUBLISHED');

-- Concepts (course_id = 1) — arranged as a prerequisite graph
INSERT INTO concepts (course_id, title, summary, display_order) VALUES
  (1, 'Variables & Types',        'Primitive types, declarations, type casting.',         1),
  (1, 'Control Flow',             'If-else, switch, loops, break and continue.',          2),
  (1, 'Arrays',                   'One-dimensional and multi-dimensional arrays.',        3),
  (1, 'Methods',                  'Parameters, return types, overloading, scope.',        4),
  (1, 'Classes & Objects',        'Fields, constructors, access modifiers, this.',        5),
  (1, 'Encapsulation',            'Getters, setters, information hiding.',                6),
  (1, 'Inheritance',              'Extends, super, method overriding, IS-A.',             7),
  (1, 'Polymorphism',             'Runtime dispatch, abstract classes, upcasting.',       8),
  (1, 'Interfaces',               'Contracts, default methods, multiple inheritance.',    9),
  (1, 'Collections',              'List, Set, Map, iterators, generics basics.',         10),
  (1, 'Exception Handling',       'Try-catch-finally, checked vs unchecked, custom.',    11),
  (1, 'Design Patterns Intro',    'Strategy, Observer, Singleton, Factory.',             12);

-- Prerequisite graph edges (concept_id depends on prerequisite_id)
INSERT INTO concept_prerequisites (concept_id, prerequisite_id) VALUES
  (2, 1),   -- Control Flow requires Variables
  (3, 1),   -- Arrays requires Variables
  (4, 2),   -- Methods requires Control Flow
  (4, 3),   -- Methods requires Arrays
  (5, 4),   -- Classes requires Methods
  (6, 5),   -- Encapsulation requires Classes
  (7, 5),   -- Inheritance requires Classes
  (8, 7),   -- Polymorphism requires Inheritance
  (9, 8),   -- Interfaces requires Polymorphism
  (10, 5),  -- Collections requires Classes
  (10, 3),  -- Collections requires Arrays
  (11, 4),  -- Exceptions requires Methods
  (12, 9),  -- Design Patterns requires Interfaces
  (12, 10); -- Design Patterns requires Collections

-- ═══════════════════════════════════════════════════════════════
-- ENROLLMENTS
-- ═══════════════════════════════════════════════════════════════

INSERT INTO enrollments (course_id, student_id, status) VALUES
  (1, 4, 'ACTIVE'),
  (1, 5, 'ACTIVE'),
  (1, 6, 'ACTIVE');

-- ═══════════════════════════════════════════════════════════════
-- MATERIALS
-- ═══════════════════════════════════════════════════════════════

INSERT INTO materials (concept_id, title, type, location) VALUES
  (1, 'Java Primitives Cheat Sheet',   'LINK', 'https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html'),
  (1, 'Variables Lecture Notes',        'NOTE', 'Covers int, double, boolean, char, String, and casting rules.'),
  (5, 'Classes Deep Dive',             'LINK', 'https://docs.oracle.com/javase/tutorial/java/javaOO/classes.html'),
  (7, 'Inheritance Tutorial',          'LINK', 'https://docs.oracle.com/javase/tutorial/java/IandI/subclasses.html'),
  (10, 'Collections Framework Guide',  'LINK', 'https://docs.oracle.com/javase/tutorial/collections/index.html');

-- ═══════════════════════════════════════════════════════════════
-- QUIZ: Variables & Types Quiz (Published)
-- ═══════════════════════════════════════════════════════════════

INSERT INTO quizzes (course_id, title, max_attempts, status) VALUES
  (1, 'Variables & Types Quiz', 3, 'PUBLISHED');

INSERT INTO quiz_questions (quiz_id, concept_id, prompt, marks) VALUES
  (1, 1, 'Which of the following is NOT a primitive type in Java?', 1),
  (1, 1, 'What is the default value of an int field in a class?', 1),
  (1, 1, 'Which type would you use to store a single Unicode character?', 1);

INSERT INTO question_options (question_id, label, is_correct) VALUES
  (1, 'int',     FALSE),
  (1, 'String',  TRUE),
  (1, 'double',  FALSE),
  (1, 'boolean', FALSE),

  (2, '0',     TRUE),
  (2, 'null',  FALSE),
  (2, '1',     FALSE),
  (2, 'undefined', FALSE),

  (3, 'String', FALSE),
  (3, 'char',   TRUE),
  (3, 'byte',   FALSE),
  (3, 'int',    FALSE);

-- ═══════════════════════════════════════════════════════════════
-- QUIZ: Control Flow Quiz (Published)
-- ═══════════════════════════════════════════════════════════════

INSERT INTO quizzes (course_id, title, max_attempts, status) VALUES
  (1, 'Control Flow Quiz', 3, 'PUBLISHED');

INSERT INTO quiz_questions (quiz_id, concept_id, prompt, marks) VALUES
  (2, 2, 'Which loop guarantees at least one execution of the body?', 1),
  (2, 2, 'What does the break statement do inside a loop?', 1);

INSERT INTO question_options (question_id, label, is_correct) VALUES
  (4, 'for loop',       FALSE),
  (4, 'while loop',     FALSE),
  (4, 'do-while loop',  TRUE),
  (4, 'enhanced for',   FALSE),

  (5, 'Skips current iteration', FALSE),
  (5, 'Exits the loop',          TRUE),
  (5, 'Restarts the loop',       FALSE),
  (5, 'Does nothing',            FALSE);

-- ═══════════════════════════════════════════════════════════════
-- SAMPLE QUIZ ATTEMPTS (Ravi — student_id 4)
-- ═══════════════════════════════════════════════════════════════

INSERT INTO quiz_attempts (quiz_id, student_id, attempt_no, score, max_score)
VALUES (1, 4, 1, 2, 3);

INSERT INTO attempt_answers (attempt_id, question_id, selected_option_id, is_correct) VALUES
  (1, 1, 2,  TRUE),   -- String is correct
  (1, 2, 5,  TRUE),   -- 0 is correct
  (1, 3, 9,  FALSE);  -- String is wrong, char was correct

-- ═══════════════════════════════════════════════════════════════
-- ASSIGNMENT (course 1)
-- ═══════════════════════════════════════════════════════════════

INSERT INTO assignments (course_id, title, instructions, due_at, allow_late) VALUES
  (1, 'Build a Student Record System',
   'Design a class hierarchy for a student record system using encapsulation and inheritance. Include at least three classes with proper access modifiers, constructors, and method overriding.',
   '2026-11-15 23:59:00', TRUE);

INSERT INTO rubric_criteria (assignment_id, concept_id, description, max_points) VALUES
  (1, 5, 'Correct use of classes, constructors, and fields', 10),
  (1, 6, 'Proper encapsulation with getters and setters',     8),
  (1, 7, 'Meaningful use of inheritance and super calls',     10),
  (1, 8, 'Demonstrates polymorphism through method overriding', 7);

-- ═══════════════════════════════════════════════════════════════
-- SAMPLE SUBMISSION (Meera — student_id 5)
-- ═══════════════════════════════════════════════════════════════

INSERT INTO submissions (assignment_id, student_id, body_text, status, graded_at, feedback)
VALUES (1, 5,
        'Source code submitted as attached PDF.',
        'GRADED', NOW(),
        'Good structure. Inheritance could be deeper — consider adding a GradStudent subclass.');

INSERT INTO submission_scores (submission_id, criterion_id, points) VALUES
  (1, 1, 8),   -- Classes: 8 / 10
  (1, 2, 7),   -- Encapsulation: 7 / 8
  (1, 3, 6),   -- Inheritance: 6 / 10
  (1, 4, 4);   -- Polymorphism: 4 / 7

-- ═══════════════════════════════════════════════════════════════
-- MASTERY SNAPSHOTS (pre-computed for demo)
-- ═══════════════════════════════════════════════════════════════

INSERT INTO mastery_snapshots (student_id, concept_id, mastery, evidence_count) VALUES
  (4, 1, 66.67, 3),   -- Ravi: Variables — 2/3 quiz correct
  (5, 5, 80.00, 1),   -- Meera: Classes — rubric 8/10
  (5, 6, 87.50, 1),   -- Meera: Encapsulation — rubric 7/8
  (5, 7, 60.00, 1),   -- Meera: Inheritance — rubric 6/10
  (5, 8, 57.14, 1);   -- Meera: Polymorphism — rubric 4/7

-- ═══════════════════════════════════════════════════════════════
-- NOTIFICATIONS
-- ═══════════════════════════════════════════════════════════════

INSERT INTO notifications (user_id, type, message, link, dedupe_key) VALUES
  (4, 'QUIZ_GRADED', 'Your Variables & Types Quiz has been graded: 2/3.', '/learn/quiz/result?attempt=1', 'quiz_result_1_4'),
  (5, 'ASSIGNMENT_GRADED', 'Your assignment "Build a Student Record System" has been graded.', '/learn/assignment?id=1', 'assign_graded_1_5');

-- ═══════════════════════════════════════════════════════════════
-- INTERVENTIONS
-- ═══════════════════════════════════════════════════════════════

INSERT INTO interventions (course_id, concept_id, student_id, instructor_id, type, note, mastery_before)
VALUES (1, 8, 5, 2, 'EXTRA_MATERIAL',
        'Shared additional polymorphism examples focusing on abstract classes and method dispatch.',
        57.14);

-- ═══════════════════════════════════════════════════════════════
-- AUDIT LOG
-- ═══════════════════════════════════════════════════════════════

INSERT INTO audit_log (user_id, action, entity, entity_id) VALUES
  (1, 'COURSE_APPROVED', 'COURSE', 1),
  (2, 'QUIZ_PUBLISHED', 'QUIZ', 1),
  (2, 'QUIZ_PUBLISHED', 'QUIZ', 2);

-- ═══════════════════════════════════════════════════════════════
-- COURSE 2: Draft course for approval-flow demo
-- ═══════════════════════════════════════════════════════════════

INSERT INTO courses (instructor_id, code, title, description, category, difficulty, max_students, gate_threshold, status)
VALUES (3, 'MA101', 'Discrete Mathematics',
        'Foundations of logic, sets, graphs, and combinatorics for computer science.',
        'Mathematics', 'BEGINNER', 45, 55, 'PENDING_APPROVAL');

INSERT INTO concepts (course_id, title, summary, display_order) VALUES
  (2, 'Propositional Logic', 'Propositions, truth tables, logical connectives.',  1),
  (2, 'Set Theory',          'Sets, subsets, power sets, Venn diagrams.',         2),
  (2, 'Relations',           'Binary relations, equivalence, partial orders.',    3),
  (2, 'Graph Theory',        'Vertices, edges, paths, cycles, trees.',           4);

INSERT INTO concept_prerequisites (concept_id, prerequisite_id) VALUES
  (14, 13),  -- Set Theory requires Propositional Logic
  (15, 14),  -- Relations requires Set Theory
  (16, 15);  -- Graph Theory requires Relations
