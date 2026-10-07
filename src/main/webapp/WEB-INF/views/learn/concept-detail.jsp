<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="${concept.title} - Concept Details"/>
</jsp:include>

<jsp:include page="/WEB-INF/views/common/messages.jsp"/>

<div class="page-container">
    <div style="margin-bottom: 1.5rem;">
        <a href="${pageContext.request.contextPath}/learn/map?course=${course.courseId}" class="btn btn-outline" style="font-size: 0.85rem;">
            &larr; Back to Concept Map
        </a>
    </div>

    <!-- Concept Header Banner -->
    <div class="card" style="margin-bottom: 2rem;">
        <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 1rem;">
            <div>
                <span class="badge" style="background: var(--bg-card-subtle); color: var(--text-muted); margin-bottom: 0.5rem; display: inline-block;">
                    <c:out value="${course.code}"/> &bull; Concept #${concept.displayOrder}
                </span>
                <h1 style="margin: 0.25rem 0 0.5rem 0; font-size: 1.85rem;"><c:out value="${concept.title}"/></h1>
                <p style="color: var(--text-muted); max-width: 750px; margin: 0; line-height: 1.6;">
                    <c:out value="${concept.summary}"/>
                </p>
            </div>

            <div style="text-align: right; min-width: 180px;">
                <div style="font-size: 0.8rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted); margin-bottom: 0.25rem;">
                    Mastery Status
                </div>
                <c:choose>
                    <c:when test="${not statusView.unlocked}">
                        <span class="badge badge-locked" style="font-size: 0.95rem; padding: 0.35rem 0.75rem;">Locked</span>
                    </c:when>
                    <c:when test="${statusView.level == 'MASTERED'}">
                        <span class="badge badge-mastered" style="font-size: 0.95rem; padding: 0.35rem 0.75rem;">Mastered (100%)</span>
                    </c:when>
                    <c:when test="${statusView.level == 'PRACTICING'}">
                        <span class="badge badge-practicing" style="font-size: 0.95rem; padding: 0.35rem 0.75rem;">Practicing (${statusView.score}%)</span>
                    </c:when>
                    <c:when test="${statusView.level == 'WEAK'}">
                        <span class="badge badge-weak" style="font-size: 0.95rem; padding: 0.35rem 0.75rem;">Weak (${statusView.score}%)</span>
                    </c:when>
                    <c:otherwise>
                        <span class="badge badge-unlocked" style="font-size: 0.95rem; padding: 0.35rem 0.75rem;">Ready to Learn</span>
                    </c:otherwise>
                </c:choose>

                <c:if test="${statusView.needsRefresh}">
                    <div style="margin-top: 0.5rem;">
                        <span class="badge badge-refresh" style="font-size: 0.75rem;">Needs Refresh (21d+)</span>
                    </div>
                </c:if>
            </div>
        </div>
    </div>

    <!-- Prerequisites Check -->
    <c:if test="${not empty prerequisites}">
        <div class="card" style="margin-bottom: 2rem;">
            <h3 style="font-size: 1.15rem; margin-top: 0; margin-bottom: 1rem;">Prerequisite Concepts</h3>
            <div style="display: flex; gap: 0.75rem; flex-wrap: wrap;">
                <c:forEach var="prereq" items="${prerequisites}">
                    <a href="${pageContext.request.contextPath}/learn/concept?id=${prereq.conceptId}"
                       class="card" style="padding: 0.75rem 1rem; text-decoration: none; border-color: var(--border-color); display: inline-flex; align-items: center; gap: 0.5rem;">
                        <span style="font-size: 0.9rem; font-weight: 600; color: var(--text-color);">
                            <c:out value="${prereq.title}"/>
                        </span>
                        <span style="color: var(--text-muted); font-size: 0.8rem;">&rarr;</span>
                    </a>
                </c:forEach>
            </div>
            <c:if test="${not statusView.unlocked}">
                <div style="margin-top: 1rem; padding: 0.75rem 1rem; background: var(--bg-weak-subtle); border-left: 3px solid var(--accent-weak); border-radius: 4px; font-size: 0.85rem;">
                    <strong>Prerequisites Incomplete:</strong> You must reach at least <strong><c:out value="${course.gateThreshold}"/>%</strong> mastery in all prerequisite concepts to unlock this concept.
                </div>
            </c:if>
        </div>
    </c:if>

    <!-- Content Columns: Materials & Assessments -->
    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: 1.5rem; margin-bottom: 2rem;">

        <!-- Learning Materials -->
        <div class="card">
            <h3 style="font-size: 1.2rem; margin-top: 0; margin-bottom: 1rem; display: flex; align-items: center; gap: 0.5rem;">
                <span>Learning Materials</span>
                <span class="badge" style="background: var(--bg-card-subtle);">${materials.size()}</span>
            </h3>

            <c:choose>
                <c:when test="${empty materials}">
                    <p style="color: var(--text-muted); font-size: 0.9rem;">No learning materials have been posted for this concept yet.</p>
                </c:when>
                <c:otherwise>
                    <div style="display: flex; flex-direction: column; gap: 0.75rem;">
                        <c:forEach var="mat" items="${materials}">
                            <div style="padding: 0.75rem; border: 1px solid var(--border-color); border-radius: 6px; display: flex; justify-content: space-between; align-items: center;">
                                <div>
                                    <div style="font-weight: 600; font-size: 0.95rem;">
                                        <c:out value="${mat.title}"/>
                                    </div>
                                    <span class="badge" style="background: var(--bg-card-subtle); font-size: 0.75rem; margin-top: 0.25rem;">
                                        <c:out value="${mat.type}"/>
                                    </span>
                                </div>
                                <div>
                                    <c:choose>
                                        <c:when test="${mat.type == 'PDF'}">
                                            <a href="${pageContext.request.contextPath}/files?path=${mat.location}" target="_blank" class="btn btn-outline" style="font-size: 0.8rem; padding: 0.35rem 0.75rem;">
                                                View PDF
                                            </a>
                                        </c:when>
                                        <c:otherwise>
                                            <a href="<c:out value='${mat.location}'/>" target="_blank" rel="noopener noreferrer" class="btn btn-outline" style="font-size: 0.8rem; padding: 0.35rem 0.75rem;">
                                                Open Resource &rarr;
                                            </a>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <!-- Available Practice & Assessments -->
        <div class="card">
            <h3 style="font-size: 1.2rem; margin-top: 0; margin-bottom: 1rem;">Assessments & Practice</h3>

            <h4 style="font-size: 0.95rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted); margin-bottom: 0.5rem;">Quizzes</h4>
            <c:choose>
                <c:when test="${empty quizzes}">
                    <p style="color: var(--text-muted); font-size: 0.85rem; margin-bottom: 1.5rem;">No active quizzes for this course.</p>
                </c:when>
                <c:otherwise>
                    <div style="display: flex; flex-direction: column; gap: 0.5rem; margin-bottom: 1.5rem;">
                        <c:forEach var="quiz" items="${quizzes}">
                            <div style="padding: 0.75rem; border: 1px solid var(--border-color); border-radius: 6px; display: flex; justify-content: space-between; align-items: center;">
                                <div>
                                    <div style="font-weight: 600; font-size: 0.9rem;"><c:out value="${quiz.title}"/></div>
                                    <span style="color: var(--text-muted); font-size: 0.75rem;">Weight: 1.0 &bull; Max Attempts: ${quiz.maxAttempts}</span>
                                </div>
                                <c:choose>
                                    <c:when test="${statusView.unlocked}">
                                        <a href="${pageContext.request.contextPath}/learn/quiz?id=${quiz.quizId}" class="btn btn-primary" style="font-size: 0.8rem; padding: 0.35rem 0.75rem;">
                                            Take Quiz
                                        </a>
                                    </c:when>
                                    <c:otherwise>
                                        <button class="btn btn-outline" disabled style="font-size: 0.8rem; padding: 0.35rem 0.75rem; opacity: 0.6;">
                                            Locked
                                        </button>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>

            <h4 style="font-size: 0.95rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted); margin-bottom: 0.5rem;">Assignments</h4>
            <c:choose>
                <c:when test="${empty assignments}">
                    <p style="color: var(--text-muted); font-size: 0.85rem;">No assignments currently scheduled.</p>
                </c:when>
                <c:otherwise>
                    <div style="display: flex; flex-direction: column; gap: 0.5rem;">
                        <c:forEach var="asgn" items="${assignments}">
                            <div style="padding: 0.75rem; border: 1px solid var(--border-color); border-radius: 6px; display: flex; justify-content: space-between; align-items: center;">
                                <div>
                                    <div style="font-weight: 600; font-size: 0.9rem;"><c:out value="${asgn.title}"/></div>
                                    <span style="color: var(--text-muted); font-size: 0.75rem;">Weight: 3.0 &bull; Due: ${asgn.dueAt}</span>
                                </div>
                                <a href="${pageContext.request.contextPath}/learn/assignment?id=${asgn.assignmentId}" class="btn btn-outline" style="font-size: 0.8rem; padding: 0.35rem 0.75rem;">
                                    View Details
                                </a>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <!-- Mastery History Timeline -->
    <div class="card">
        <h3 style="font-size: 1.2rem; margin-top: 0; margin-bottom: 1rem;">Mastery Progress History</h3>
        <c:choose>
            <c:when test="${empty history}">
                <p style="color: var(--text-muted); font-size: 0.9rem;">
                    No evidence recorded yet. Complete quizzes and assignments tagged with this concept to build your mastery score!
                </p>
            </c:when>
            <c:otherwise>
                <div class="table-container">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Timestamp</th>
                                <th>Mastery Score</th>
                                <th>Mastery Level</th>
                                <th>Trigger Source</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="snap" items="${history}">
                                <tr>
                                    <td><c:out value="${snap.computedAt}"/></td>
                                    <td>
                                        <strong><c:out value="${snap.masteryScore}"/>%</strong>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${snap.level == 'MASTERED'}">
                                                <span class="badge badge-mastered">Mastered</span>
                                            </c:when>
                                            <c:when test="${snap.level == 'PRACTICING'}">
                                                <span class="badge badge-practicing">Practicing</span>
                                            </c:when>
                                            <c:when test="${snap.level == 'WEAK'}">
                                                <span class="badge badge-weak">Weak</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge badge-unlocked">Unlocked</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td style="color: var(--text-muted); font-size: 0.85rem;">
                                        <c:out value="${snap.triggerSource}"/>
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
