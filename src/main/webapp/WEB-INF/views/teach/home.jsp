<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="Instructor Dashboard"/>
</jsp:include>

<jsp:include page="/WEB-INF/views/common/messages.jsp"/>

<div class="page-container">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem;">
        <div>
            <h1 style="margin: 0 0 0.25rem 0; font-size: 2rem;">Instructor Hub</h1>
            <p style="color: var(--text-muted); margin: 0; font-size: 1rem;">
                Manage concept DAG courses, review cohort mastery heatmaps, and target interventions.
            </p>
        </div>
        <a href="${pageContext.request.contextPath}/teach/courses" class="btn btn-primary">
            + Create New Course
        </a>
    </div>

    <!-- Metrics Cards -->
    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 1.5rem; margin-bottom: 2.5rem;">
        <div class="card">
            <div style="font-size: 0.8rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted); margin-bottom: 0.5rem;">
                Active Courses
            </div>
            <div style="font-size: 2.5rem; font-weight: 800; color: var(--text-color);">
                ${courses.size()}
            </div>
        </div>

        <div class="card">
            <div style="font-size: 0.8rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted); margin-bottom: 0.5rem;">
                Pending Submissions to Grade
            </div>
            <div style="font-size: 2.5rem; font-weight: 800; color: ${pendingGrading.size() > 0 ? 'var(--accent-color)' : 'var(--text-color)'};">
                ${pendingGrading.size()}
            </div>
        </div>

        <div class="card">
            <div style="font-size: 0.8rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted); margin-bottom: 0.5rem;">
                Stalled Students Detected
            </div>
            <div style="font-size: 2.5rem; font-weight: 800; color: ${totalStalled > 0 ? 'var(--accent-weak)' : 'var(--text-color)'};">
                ${totalStalled}
            </div>
            <div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 0.25rem;">
                (Stuck on concept for 7+ days)
            </div>
        </div>
    </div>

    <!-- Pending Grading Queue -->
    <c:if test="${not empty pendingGrading}">
        <div class="card" style="margin-bottom: 2.5rem; border-left: 4px solid var(--accent-color);">
            <h3 style="font-size: 1.25rem; margin-top: 0; margin-bottom: 1rem;">Submissions Requiring Rubric Grading</h3>
            <div class="table-container">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Submission ID</th>
                            <th>Student ID</th>
                            <th>Submitted At</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="sub" items="${pendingGrading}">
                            <tr>
                                <td>#<c:out value="${sub.submissionId}"/></td>
                                <td>Student #<c:out value="${sub.studentId}"/></td>
                                <td><c:out value="${sub.submittedAt}"/></td>
                                <td>
                                    <a href="${pageContext.request.contextPath}/teach/grade?submission=${sub.submissionId}" class="btn btn-primary" style="font-size: 0.8rem; padding: 0.35rem 0.75rem;">
                                        Grade Rubric &rarr;
                                    </a>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </c:if>

    <!-- My Courses Table -->
    <div class="card">
        <h3 style="font-size: 1.25rem; margin-top: 0; margin-bottom: 1rem;">Your Courses</h3>
        <c:choose>
            <c:when test="${empty courses}">
                <p style="color: var(--text-muted); font-size: 0.95rem;">You haven't created any courses yet.</p>
            </c:when>
            <c:otherwise>
                <div class="table-container">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Code</th>
                                <th>Title</th>
                                <th>Status</th>
                                <th>Gate Threshold</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="c" items="${courses}">
                                <tr>
                                    <td><strong><c:out value="${c.code}"/></strong></td>
                                    <td><c:out value="${c.title}"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${c.status == 'APPROVED'}">
                                                <span class="badge badge-mastered">Approved</span>
                                            </c:when>
                                            <c:when test="${c.status == 'PENDING_APPROVAL'}">
                                                <span class="badge badge-practicing">Pending Approval</span>
                                            </c:when>
                                            <c:when test="${c.status == 'DRAFT'}">
                                                <span class="badge badge-unlocked">Draft</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge" style="background: var(--bg-card-subtle);"><c:out value="${c.status}"/></span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td><c:out value="${c.gateThreshold}"/>%</td>
                                    <td>
                                        <div style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
                                            <a href="${pageContext.request.contextPath}/teach/course/graph?id=${c.courseId}" class="btn btn-outline" style="font-size: 0.8rem; padding: 0.3rem 0.65rem;">
                                                Graph Editor
                                            </a>
                                            <a href="${pageContext.request.contextPath}/teach/cohort?course=${c.courseId}" class="btn btn-outline" style="font-size: 0.8rem; padding: 0.3rem 0.65rem;">
                                                Cohort Heatmap
                                            </a>
                                            <a href="${pageContext.request.contextPath}/teach/quizzes?course=${c.courseId}" class="btn btn-outline" style="font-size: 0.8rem; padding: 0.3rem 0.65rem;">
                                                Quizzes
                                            </a>
                                            <a href="${pageContext.request.contextPath}/teach/assignments?course=${c.courseId}" class="btn btn-outline" style="font-size: 0.8rem; padding: 0.3rem 0.65rem;">
                                                Assignments
                                            </a>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
