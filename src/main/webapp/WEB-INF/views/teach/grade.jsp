<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="Grade Submission - #${submission.submissionId}"/>
</jsp:include>

<jsp:include page="/WEB-INF/views/common/messages.jsp"/>

<div class="page-container" style="max-width: 900px;">
    <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem;">
        <div>
            <div style="display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.5rem;">
                <a href="${pageContext.request.contextPath}/teach/home" class="btn btn-outline" style="font-size: 0.8rem; padding: 0.25rem 0.5rem;">
                    &larr; Instructor Hub
                </a>
                <span class="badge" style="background: var(--bg-card-subtle); color: var(--accent-color); font-weight: 600;">
                    CRITERION-LEVEL RUBRIC SCORING
                </span>
            </div>
            <h1 style="margin: 0 0 0.25rem 0; font-size: 1.85rem;">Grading: <c:out value="${assignment.title}"/></h1>
            <p style="color: var(--text-muted); margin: 0; font-size: 0.95rem;">
                Student ID: <strong>#<c:out value="${submission.studentId}"/></strong> &bull; Submitted: <strong><c:out value="${submission.submittedAt}"/></strong>
            </p>
        </div>
    </div>

    <!-- Student Submission Content -->
    <div class="card" style="margin-bottom: 2rem;">
        <h3 style="font-size: 1.2rem; margin-top: 0; margin-bottom: 1rem;">Student Submission</h3>

        <c:choose>
            <c:when test="${not empty submission.bodyText}">
                <div style="background: var(--bg-card-subtle); padding: 1.25rem; border-radius: 6px; white-space: pre-wrap; font-family: inherit; font-size: 0.95rem; line-height: 1.6; margin-bottom: 1rem;">
                    <c:out value="${submission.bodyText}"/>
                </div>
            </c:when>
            <c:otherwise>
                <p style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 1rem;"><em>No written text provided.</em></p>
            </c:otherwise>
        </c:choose>

        <c:if test="${not empty submission.filePath}">
            <div>
                <a href="${pageContext.request.contextPath}/files?path=${submission.filePath}" target="_blank" class="btn btn-outline" style="font-size: 0.9rem;">
                    View Uploaded PDF Document &rarr;
                </a>
            </div>
        </c:if>
    </div>

    <!-- Rubric Scoring Form -->
    <div class="card">
        <h3 style="font-size: 1.2rem; margin-top: 0; margin-bottom: 0.5rem;">Rubric Assessment</h3>
        <p style="color: var(--text-muted); font-size: 0.85rem; margin-top: 0; margin-bottom: 1.25rem;">
            Score each criterion according to the student's demonstrated mastery. Saving immediately triggers a new mastery snapshot for all tagged concepts.
        </p>

        <form action="${pageContext.request.contextPath}/teach/grade" method="post">
            <input type="hidden" name="csrfToken" value="${csrfToken}"/>
            <input type="hidden" name="submissionId" value="${submission.submissionId}"/>

            <div style="display: flex; flex-direction: column; gap: 1rem; margin-bottom: 1.5rem;">
                <c:forEach var="crit" items="${criteria}">
                    <div style="display: grid; grid-template-columns: 3fr 1fr; gap: 1rem; align-items: center; padding: 1rem; border: 1px solid var(--border-color); border-radius: 6px; background: var(--bg-card);">
                        <div>
                            <div style="font-weight: 600; font-size: 0.95rem;">
                                <c:out value="${crit.description}"/>
                            </div>
                            <span class="badge" style="background: var(--bg-card-subtle); font-size: 0.75rem; margin-top: 0.35rem;">
                                Concept ID #${crit.conceptId} &bull; Max: ${crit.maxPoints} pts
                            </span>
                        </div>
                        <div>
                            <label for="points_${crit.criterionId}" style="display: block; font-size: 0.75rem; color: var(--text-muted); margin-bottom: 0.25rem;">
                                Points Awarded:
                            </label>
                            <input type="number" id="points_${crit.criterionId}" name="points_${crit.criterionId}"
                                   value="${not empty scoreMap[crit.criterionId] ? scoreMap[crit.criterionId] : crit.maxPoints}"
                                   min="0" max="${crit.maxPoints}" required
                                   style="width: 100%; padding: 0.5rem; border: 1px solid var(--border-color); border-radius: 4px; background: var(--bg-card); font-size: 0.95rem; font-weight: 700;"/>
                        </div>
                    </div>
                </c:forEach>
            </div>

            <div style="margin-bottom: 1.5rem;">
                <label for="feedback" style="display: block; font-weight: 600; font-size: 0.9rem; margin-bottom: 0.5rem;">
                    Qualitative Feedback for Student:
                </label>
                <textarea id="feedback" name="feedback" rows="4" style="width: 100%; padding: 0.75rem; border: 1px solid var(--border-color); border-radius: 6px; background: var(--bg-card); font-family: inherit; font-size: 0.95rem;"><c:out value="${submission.feedback}"/></textarea>
            </div>

            <button type="submit" class="btn btn-primary" style="font-size: 1rem; padding: 0.6rem 2rem;">
                Finalize Grade & Update Mastery Engine
            </button>
        </form>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
