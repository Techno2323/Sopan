package com.sopan.web.listener;

import com.sopan.config.AppConfig;
import com.sopan.config.ConnectionProvider;
import com.sopan.config.DriverManagerProvider;
import com.sopan.config.TransactionManager;
import com.sopan.concurrent.ConceptGraphCache;
import com.sopan.concurrent.DeadlineScanner;
import com.sopan.concurrent.NotificationDispatcher;
import com.sopan.dao.*;
import com.sopan.dao.jdbc.*;
import com.sopan.security.PasswordHasher;
import com.sopan.security.Pbkdf2PasswordHasher;
import com.sopan.service.*;
import com.sopan.util.FileStorage;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebListener
public class AppContextListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(AppContextListener.class.getName());

    private ScheduledExecutorService scheduler;
    private ScheduledExecutorService workerPool;
    private NotificationDispatcher notificationDispatcher;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        LOGGER.info("Starting Sopan LMS application context...");

        try {
            // 1. Config & Connection Management
            AppConfig config = AppConfig.load();
            ConnectionProvider connectionProvider = new DriverManagerProvider(config);
            TransactionManager transactionManager = new TransactionManager(connectionProvider);

            // 2. DAOs
            UserDao userDao = new JdbcUserDao(connectionProvider);
            CourseDao courseDao = new JdbcCourseDao(connectionProvider);
            EnrollmentDao enrollmentDao = new JdbcEnrollmentDao(connectionProvider);
            ConceptDao conceptDao = new JdbcConceptDao(connectionProvider);
            MaterialDao materialDao = new JdbcMaterialDao(connectionProvider);
            QuizDao quizDao = new JdbcQuizDao(connectionProvider);
            QuestionDao questionDao = new JdbcQuestionDao(connectionProvider);
            OptionDao optionDao = new JdbcOptionDao(connectionProvider);
            QuizAttemptDao quizAttemptDao = new JdbcQuizAttemptDao(connectionProvider);
            AttemptAnswerDao attemptAnswerDao = new JdbcAttemptAnswerDao(connectionProvider);
            AssignmentDao assignmentDao = new JdbcAssignmentDao(connectionProvider);
            RubricCriterionDao rubricCriterionDao = new JdbcRubricCriterionDao(connectionProvider);
            SubmissionDao submissionDao = new JdbcSubmissionDao(connectionProvider);
            SubmissionScoreDao submissionScoreDao = new JdbcSubmissionScoreDao(connectionProvider);
            MasterySnapshotDao masterySnapshotDao = new JdbcMasterySnapshotDao(connectionProvider);
            EvidenceDao evidenceDao = new JdbcEvidenceDao(connectionProvider);
            InterventionDao interventionDao = new JdbcInterventionDao(connectionProvider);
            NotificationDao notificationDao = new JdbcNotificationDao(connectionProvider);
            AuditLogDao auditLogDao = new JdbcAuditLogDao(connectionProvider);

            // 3. Security & Utilities
            PasswordHasher passwordHasher = new Pbkdf2PasswordHasher();
            String uploadsPath = ctx.getInitParameter("uploadsPath");
            FileStorage fileStorage = new FileStorage(uploadsPath);
            ConceptGraphCache graphCache = new ConceptGraphCache();

            // 4. Services
            AuthService authService = new AuthService(userDao, passwordHasher, transactionManager);
            CourseService courseService = new CourseService(courseDao, conceptDao, auditLogDao, transactionManager);
            EnrollmentService enrollmentService = new EnrollmentService(enrollmentDao, courseDao, notificationDao, auditLogDao, transactionManager);
            ConceptService conceptService = new ConceptService(conceptDao, courseDao, auditLogDao, transactionManager);
            MasteryService masteryService = new MasteryService(evidenceDao, masterySnapshotDao, conceptDao, courseDao);
            QuizService quizService = new QuizService(quizDao, questionDao, optionDao, quizAttemptDao, attemptAnswerDao,
                    courseDao, masteryService, interventionDao, notificationDao, auditLogDao, transactionManager);
            AssignmentService assignmentService = new AssignmentService(assignmentDao, rubricCriterionDao, submissionDao,
                    submissionScoreDao, courseDao, masteryService, interventionDao, notificationDao, auditLogDao, transactionManager);
            RecommendationService recommendationService = new RecommendationService(enrollmentDao, courseDao, conceptService,
                    masteryService, quizDao, assignmentDao, notificationDao);
            InterventionService interventionService = new InterventionService(interventionDao, courseDao, enrollmentDao,
                    conceptDao, masterySnapshotDao, auditLogDao, notificationDao, transactionManager);
            NotificationService notificationService = new NotificationService(notificationDao);
            AdminService adminService = new AdminService(userDao, courseDao, auditLogDao, notificationDao, transactionManager);

            // 5. Publish to ServletContext
            ctx.setAttribute("authService", authService);
            ctx.setAttribute("courseService", courseService);
            ctx.setAttribute("enrollmentService", enrollmentService);
            ctx.setAttribute("conceptService", conceptService);
            ctx.setAttribute("masteryService", masteryService);
            ctx.setAttribute("quizService", quizService);
            ctx.setAttribute("assignmentService", assignmentService);
            ctx.setAttribute("recommendationService", recommendationService);
            ctx.setAttribute("interventionService", interventionService);
            ctx.setAttribute("notificationService", notificationService);
            ctx.setAttribute("adminService", adminService);
            ctx.setAttribute("materialDao", materialDao);
            ctx.setAttribute("fileStorage", fileStorage);
            ctx.setAttribute("graphCache", graphCache);

            // 6. Concurrency: Notification Dispatcher worker
            this.notificationDispatcher = new NotificationDispatcher(notificationDao);
            this.workerPool = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "sopan-notification-dispatcher");
                t.setDaemon(true);
                return t;
            });
            this.workerPool.submit(notificationDispatcher);

            // 7. Concurrency: Deadline Scanner (runs every 15 minutes)
            DeadlineScanner scanner = new DeadlineScanner(assignmentDao, enrollmentDao, submissionDao, notificationDao);
            this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "sopan-deadline-scanner");
                t.setDaemon(true);
                return t;
            });
            this.scheduler.scheduleAtFixedRate(scanner, 1, 15, TimeUnit.MINUTES);

            LOGGER.info("Sopan LMS initialized successfully.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to start Sopan LMS application context", e);
            throw new RuntimeException("Application startup failure", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("Shutting down Sopan LMS application context...");

        if (notificationDispatcher != null) {
            notificationDispatcher.stop();
        }

        if (workerPool != null) {
            workerPool.shutdown();
            try {
                if (!workerPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    workerPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                workerPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        LOGGER.info("Sopan LMS shutdown complete.");
    }
}
