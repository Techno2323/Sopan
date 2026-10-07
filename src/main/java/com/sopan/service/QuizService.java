package com.sopan.service;

import com.sopan.config.TransactionManager;
import com.sopan.dao.*;
import com.sopan.dto.QuizResultView;
import com.sopan.exception.AccessDeniedException;
import com.sopan.exception.DaoException;
import com.sopan.exception.DuplicateAttemptException;
import com.sopan.exception.PrerequisiteNotMetException;
import com.sopan.exception.SopanException;
import com.sopan.exception.ValidationException;
import com.sopan.model.*;
import com.sopan.model.enums.MasteryLevel;
import com.sopan.util.Validator;

import java.math.BigDecimal;
import java.util.*;

public class QuizService {

    private final QuizDao quizDao;
    private final QuestionDao questionDao;
    private final OptionDao optionDao;
    private final QuizAttemptDao quizAttemptDao;
    private final AttemptAnswerDao attemptAnswerDao;
    private final CourseDao courseDao;
    private final MasteryService masteryService;
    private final InterventionDao interventionDao;
    private final NotificationDao notificationDao;
    private final AuditLogDao auditLogDao;
    private final TransactionManager transactionManager;

    public QuizService(QuizDao quizDao, QuestionDao questionDao, OptionDao optionDao,
                       QuizAttemptDao quizAttemptDao, AttemptAnswerDao attemptAnswerDao,
                       CourseDao courseDao, MasteryService masteryService,
                       InterventionDao interventionDao, NotificationDao notificationDao,
                       AuditLogDao auditLogDao, TransactionManager transactionManager) {
        this.quizDao = quizDao;
        this.questionDao = questionDao;
        this.optionDao = optionDao;
        this.quizAttemptDao = quizAttemptDao;
        this.attemptAnswerDao = attemptAnswerDao;
        this.courseDao = courseDao;
        this.masteryService = masteryService;
        this.interventionDao = interventionDao;
        this.notificationDao = notificationDao;
        this.auditLogDao = auditLogDao;
        this.transactionManager = transactionManager;
    }

    public Quiz getQuiz(int quizId) {
        return quizDao.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found: " + quizId));
    }

    public List<Quiz> getCourseQuizzes(int courseId) {
        return quizDao.findByCourseId(courseId);
    }

    public List<Quiz> getPublishedCourseQuizzes(int courseId) {
        return quizDao.findPublishedByCourseId(courseId);
    }

    public List<Question> getQuizQuestions(int quizId) {
        return questionDao.findByQuizId(quizId);
    }

    public List<Option> getQuestionOptions(int questionId) {
        return optionDao.findByQuestionId(questionId);
    }

    public int createQuiz(int courseId, int instructorId, String title, int maxAttempts)
            throws ValidationException, AccessDeniedException {

        Course course = courseDao.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
        if (course.getInstructorId() != instructorId) {
            throw new AccessDeniedException("You do not own this course.");
        }

        Validator.requireNotBlank(title, "Quiz title");
        if (maxAttempts <= 0) {
            throw new ValidationException("Maximum attempts must be at least 1.");
        }

        Quiz quiz = new Quiz();
        quiz.setCourseId(courseId);
        quiz.setTitle(title.trim());
        quiz.setMaxAttempts(maxAttempts);
        quiz.setStatus("DRAFT");

        return transactionManager.inTransaction(conn -> {
            int id = quizDao.save(conn, quiz);
            auditLogDao.log(conn, instructorId, "QUIZ_CREATED", "QUIZ", id);
            return id;
        });
    }

    public void updateQuiz(int quizId, int instructorId, String title, int maxAttempts)
            throws ValidationException, AccessDeniedException {

        Quiz quiz = getQuiz(quizId);
        Course course = courseDao.findById(quiz.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        if (course.getInstructorId() != instructorId) {
            throw new AccessDeniedException("You do not own this course.");
        }

        Validator.requireNotBlank(title, "Quiz title");
        if (maxAttempts <= 0) {
            throw new ValidationException("Maximum attempts must be at least 1.");
        }

        quiz.setTitle(title.trim());
        quiz.setMaxAttempts(maxAttempts);

        transactionManager.inTransactionVoid(conn -> {
            quizDao.update(conn, quiz);
            auditLogDao.log(conn, instructorId, "QUIZ_UPDATED", "QUIZ", quizId);
        });
    }

    /**
     * Publishes quiz after verifying:
     * 1. At least one question exists.
     * 2. Every question has a valid concept tag.
     * 3. Every question has at least 2 options and EXACTLY one correct option.
     */
    public void publishQuiz(int quizId, int instructorId) throws AccessDeniedException, ValidationException {
        Quiz quiz = getQuiz(quizId);
        Course course = courseDao.findById(quiz.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        if (course.getInstructorId() != instructorId) {
            throw new AccessDeniedException("You do not own this course.");
        }

        List<Question> questions = questionDao.findByQuizId(quizId);
        if (questions.isEmpty()) {
            throw new ValidationException("Cannot publish a quiz with zero questions.");
        }

        for (Question q : questions) {
            if (q.getConceptId() <= 0) {
                throw new ValidationException("Question '" + q.getPrompt() + "' is not tagged to any concept.");
            }
            List<Option> options = optionDao.findByQuestionId(q.getQuestionId());
            if (options.size() < 2) {
                throw new ValidationException("Question '" + q.getPrompt() + "' must have at least two choices.");
            }
            long correctCount = options.stream().filter(Option::isCorrect).count();
            if (correctCount != 1) {
                throw new ValidationException("Question '" + q.getPrompt() + "' must have exactly one correct option (found " + correctCount + ").");
            }
        }

        quiz.setStatus("PUBLISHED");
        transactionManager.inTransactionVoid(conn -> {
            quizDao.update(conn, quiz);
            auditLogDao.log(conn, instructorId, "QUIZ_PUBLISHED", "QUIZ", quizId);
        });
    }

    public int addQuestion(int quizId, int instructorId, int conceptId, String prompt, int marks,
                           List<Option> options) throws AccessDeniedException, ValidationException {

        Quiz quiz = getQuiz(quizId);
        Course course = courseDao.findById(quiz.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        if (course.getInstructorId() != instructorId) {
            throw new AccessDeniedException("You do not own this course.");
        }

        Validator.requireNotBlank(prompt, "Question prompt");
        if (marks <= 0) throw new ValidationException("Marks must be positive.");
        if (options == null || options.size() < 2) {
            throw new ValidationException("Question must have at least two options.");
        }

        long correctCount = options.stream().filter(Option::isCorrect).count();
        if (correctCount != 1) {
            throw new ValidationException("Question must have exactly one correct answer.");
        }

        return transactionManager.inTransaction(conn -> {
            Question q = new Question();
            q.setQuizId(quizId);
            q.setConceptId(conceptId);
            q.setPrompt(prompt.trim());
            q.setMarks(marks);
            int qId = questionDao.save(conn, q);

            for (Option opt : options) {
                opt.setQuestionId(qId);
                optionDao.save(conn, opt);
            }

            auditLogDao.log(conn, instructorId, "QUESTION_ADDED", "QUESTION", qId);
            return qId;
        });
    }

    /**
     * Submits a quiz attempt, grades answers, updates mastery snapshots for affected concepts,
     * resolves any pending interventions, and records an audit log all in ONE TRANSACTION.
     */
    public int submitQuizAttempt(int studentId, int quizId, Map<Integer, Integer> submittedAnswers)
            throws PrerequisiteNotMetException, ValidationException, DuplicateAttemptException, SopanException {

        Quiz quiz = getQuiz(quizId);
        int courseId = quiz.getCourseId();

        List<Question> questions = questionDao.findByQuizId(quizId);
        if (questions.isEmpty()) {
            throw new ValidationException("This quiz has no questions.");
        }

        // Gating Check: Ensure no question belongs to a locked concept
        Set<Integer> affectedConcepts = new HashSet<>();
        for (Question q : questions) {
            affectedConcepts.add(q.getConceptId());
            masteryService.checkPrerequisitesMet(studentId, courseId, q.getConceptId());
        }

        return transactionManager.inTransaction(conn -> {
            int latestAttemptNo = quizAttemptDao.findLatestAttemptNo(conn, quizId, studentId);
            if (latestAttemptNo >= quiz.getMaxAttempts()) {
                throw new ValidationException("Maximum attempt limit (" + quiz.getMaxAttempts() + ") reached.");
            }

            int nextAttemptNo = latestAttemptNo + 1;

            // Compute score
            int totalScore = 0;
            int maxScore = 0;
            List<AttemptAnswer> answers = new ArrayList<>();

            for (Question q : questions) {
                maxScore += q.getMarks();
                Integer selectedOptId = submittedAnswers.get(q.getQuestionId());
                boolean isCorrect = false;

                if (selectedOptId != null) {
                    List<Option> opts = optionDao.findByQuestionId(conn, q.getQuestionId());
                    for (Option o : opts) {
                        if (o.getOptionId() == selectedOptId && o.isCorrect()) {
                            isCorrect = true;
                            totalScore += q.getMarks();
                            break;
                        }
                    }
                }

                AttemptAnswer ans = new AttemptAnswer();
                ans.setQuestionId(q.getQuestionId());
                ans.setSelectedOptionId(selectedOptId);
                ans.setCorrect(isCorrect);
                answers.add(ans);
            }

            // Save Attempt
            QuizAttempt attempt = new QuizAttempt();
            attempt.setQuizId(quizId);
            attempt.setStudentId(studentId);
            attempt.setAttemptNo(nextAttemptNo);
            attempt.setScore(totalScore);
            attempt.setMaxScore(maxScore);

            int attemptId;
            try {
                attemptId = quizAttemptDao.save(conn, attempt);
            } catch (DaoException e) {
                // Duplicate submit (e.g. rapid double click)
                Optional<QuizAttempt> existing = quizAttemptDao.findByQuizStudentAndAttemptNo(conn, quizId, studentId, nextAttemptNo);
                if (existing.isPresent()) {
                    throw new DuplicateAttemptException("Duplicate quiz submission detected", existing.get().getAttemptId());
                }
                throw e;
            }

            // Link answers and save
            for (AttemptAnswer a : answers) {
                a.setAttemptId(attemptId);
            }
            attemptAnswerDao.saveAll(conn, answers);

            // Recompute mastery snapshots for all affected concepts
            masteryService.recomputeForConcepts(conn, studentId, affectedConcepts);

            // Check and resolve any pending instructor interventions
            for (int conceptId : affectedConcepts) {
                List<Intervention> pending = interventionDao.findUnresolvedByStudentAndConcept(conn, studentId, conceptId);
                if (!pending.isEmpty()) {
                    var latestSnap = masteryService.recomputeAndSaveSnapshot(conn, studentId, conceptId);
                    BigDecimal newMastery = latestSnap.getMastery();
                    for (Intervention inv : pending) {
                        interventionDao.resolve(conn, inv.getInterventionId(), newMastery);
                    }
                }
            }

            // Audit & Notification
            auditLogDao.log(conn, studentId, "QUIZ_SUBMITTED", "QUIZ_ATTEMPT", attemptId);

            Notification notif = new Notification();
            notif.setUserId(studentId);
            notif.setType("QUIZ_SCORED");
            notif.setMessage(String.format("You scored %d/%d (%.0f%%) on %s.",
                    totalScore, maxScore, (maxScore > 0 ? (totalScore * 100.0 / maxScore) : 0), quiz.getTitle()));
            notif.setLink("/learn/quiz/result?attempt=" + attemptId);
            notif.setDedupeKey("quiz_res_" + attemptId);
            notificationDao.saveIfNotExists(conn, notif);

            return attemptId;
        });
    }

    public QuizResultView getAttemptResultBreakdown(int attemptId, int requestingUserId, boolean isInstructor)
            throws AccessDeniedException {

        QuizAttempt attempt = quizAttemptDao.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found: " + attemptId));

        if (!isInstructor && attempt.getStudentId() != requestingUserId) {
            throw new AccessDeniedException("You are not authorized to view this quiz result.");
        }

        Quiz quiz = getQuiz(attempt.getQuizId());
        List<Question> questions = questionDao.findByQuizId(quiz.getQuizId());
        List<AttemptAnswer> answers = attemptAnswerDao.findByAttemptId(attemptId);

        Map<Integer, AttemptAnswer> answerMap = new HashMap<>();
        for (AttemptAnswer a : answers) {
            answerMap.put(a.getQuestionId(), a);
        }

        List<QuizResultView.QuestionFeedback> feedbackList = new ArrayList<>();
        for (Question q : questions) {
            AttemptAnswer ans = answerMap.get(q.getQuestionId());
            boolean isCorrect = ans != null && ans.isCorrect();

            List<Option> options = optionDao.findByQuestionId(q.getQuestionId());
            String selectedLabel = "None";
            String correctLabel = "Unknown";

            for (Option opt : options) {
                if (ans != null && ans.getSelectedOptionId() != null && opt.getOptionId() == ans.getSelectedOptionId()) {
                    selectedLabel = opt.getLabel();
                }
                if (opt.isCorrect()) {
                    correctLabel = opt.getLabel();
                }
            }

            var statusView = masteryService.getConceptStatus(attempt.getStudentId(), quiz.getCourseId(), q.getConceptId());
            double updatedMastery = statusView != null && statusView.getMastery() != null ? statusView.getMastery() : 0.0;
            MasteryLevel level = statusView != null ? statusView.getLevel() : MasteryLevel.UNSEEN;

            feedbackList.add(new QuizResultView.QuestionFeedback(
                    q.getPrompt(), q.getConceptTitle(), q.getConceptId(),
                    selectedLabel, correctLabel, isCorrect, updatedMastery, level
            ));
        }

        return new QuizResultView(attempt, quiz.getTitle(), feedbackList);
    }
}
