<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="${assignment.title} - Assignment"/>
</jsp:include>

<jsp:include page="/WEB-INF/views/common/messages.jsp"/>

<div class="page-container" style="max-width: 900px;">
    <!-- Assignment Header -->
    <div class="card" style="margin-bottom: 2rem;">
        <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 1rem;">
            <div>
                <span class="badge" style="background: var(--bg-card-subtle); color: var(--accent-color); font-weight: 600; margin-bottom: 0.5rem; display: inline-block;">
                    ASSIGNMENT (RUBRIC WEIGHT 3.0)
                </span>
                <h1 style="margin: 0.25rem 0 0.5rem 0; font-size: 1.85rem;"><c:out value="${assignment.title}"/></h1>
                <p style="color: var(--text-muted); margin: 0; font-size: 0.95rem;">
                    Due: <strong><c:out value="${assignment.dueAt}"/></strong> &bull;
                    Late submissions: <strong><c:out value="${assignment.allowLate ? 'Allowed' : 'Not Allowed'}"/></strong>
                </p>
            </div>
            <div>
                <c:choose>
                    <c:when test="${not empty submission and submission.status == 'GRADED'}">
                        <span class="badge badge-mastered" style="font-size: 0.95rem; padding: 0.4rem 0.8rem;">
                            Graded: <fmt:formatNumber value="${submission.totalScore}" maxFractionDigits="1"/> pts
                        </span>
                    </c:when>
                    <c:when test="${not empty submission}">
                        <span class="badge badge-practicing" style="font-size: 0.95rem; padding: 0.4rem 0.8rem;">
                            Submitted &bull; Pending Review
                        </span>
                    </c:when>
                    <c:otherwise>
                        <span class="badge badge-unlocked" style="font-size: 0.95rem; padding: 0.4rem 0.8rem;">
                            Not Submitted
                        </span>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <hr style="border: 0; border-top: 1px solid var(--border-color); margin: 1.5rem 0;"/>

        <h3 style="font-size: 1.1rem; margin-top: 0; margin-bottom: 0.5rem;">Instructions</h3>
        <div style="line-height: 1.6; color: var(--text-color); font-size: 0.95rem;">
            <c:out value="${assignment.instructions}"/>
        </div>
    </div>

    <!-- Rubric Criteria & Concept Linkages -->
    <div class="card" style="margin-bottom: 2rem;">
        <h3 style="font-size: 1.2rem; margin-top: 0; margin-bottom: 1rem;">Assessment Rubric</h3>
        <p style="color: var(--text-muted); font-size: 0.85rem; margin-top: -0.5rem; margin-bottom: 1rem;">
            Each rubric criterion directly impacts your concept mastery score with high evidence weighting (weight 3.0).
        </p>
        <div class="table-container">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Criterion Description</th>
                        <th>Max Points</th>
                        <c:if test="${not empty submission and submission.status == 'GRADED'}">
                            <th>Score Awarded</th>
                        </c:if>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="crit" items="${criteria}">
                        <tr>
                            <td>
                                <strong><c:out value="${crit.description}"/></strong>
                            </td>
                            <td><c:out value="${crit.maxPoints}"/> pts</td>
                            <c:if test="${not empty submission and submission.status == 'GRADED'}">
                                <td>
                                    <c:set var="earned" value="0"/>
                                    <c:forEach var="sc" items="${scores}">
                                        <c:if test="${sc.criterionId == crit.criterionId}">
                                            <c:set var="earned" value="${sc.points}"/>
                                        </c:if>
                                    </c:forEach>
                                    <strong><c:out value="${earned}"/> / <c:out value="${crit.maxPoints}"/></strong>
                                </td>
                            </c:if>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

    <!-- Submission Details or Form -->
    <div class="card">
        <c:choose>
            <c:when test="${not empty submission and submission.status == 'GRADED'}">
                <h3 style="font-size: 1.2rem; margin-top: 0; margin-bottom: 1rem;">Instructor Evaluation & Feedback</h3>
                <div style="background: var(--bg-card-subtle); padding: 1.25rem; border-radius: 8px; margin-bottom: 1.5rem; border-left: 4px solid var(--accent-color);">
                    <div style="font-weight: 700; font-size: 0.9rem; margin-bottom: 0.5rem;">Written Feedback:</div>
                    <p style="margin: 0; line-height: 1.6; font-size: 0.95rem;">
                        <c:choose>
                            <c:when test="${not empty submission.feedback}">
                                <c:out value="${submission.feedback}"/>
                            </c:when>
                            <c:otherwise>
                                <em>No written commentary provided.</em>
                            </c:otherwise>
                        </c:choose>
                    </p>
                </div>

                <div style="font-size: 0.9rem; color: var(--text-muted);">
                    Submitted at: <c:out value="${submission.submittedAt}"/><br/>
                    <c:if test="${not empty submission.filePath}">
                        Attachment: <a href="${pageContext.request.contextPath}/files?path=${submission.filePath}" target="_blank" class="btn btn-outline" style="font-size: 0.8rem; margin-top: 0.5rem;">Download Submitted PDF</a>
                    </c:if>
                </div>
            </c:when>

            <c:when test="${not empty submission}">
                <h3 style="font-size: 1.2rem; margin-top: 0; margin-bottom: 1rem;">Your Submitted Work</h3>
                <p style="color: var(--text-muted); font-size: 0.9rem;">
                    You submitted your work on <strong><c:out value="${submission.submittedAt}"/></strong>. Your instructor will grade it using the rubric above.
                </p>

                <c:if test="${not empty submission.bodyText}">
                    <div style="background: var(--bg-card-subtle); padding: 1rem; border-radius: 6px; margin-bottom: 1rem; white-space: pre-wrap; font-size: 0.95rem;">
                        <c:out value="${submission.bodyText}"/>
                    </div>
                </c:if>

                <c:if test="${not empty submission.filePath}">
                    <div>
                        <a href="${pageContext.request.contextPath}/files?path=${submission.filePath}" target="_blank" class="btn btn-outline" style="font-size: 0.85rem;">
                            View Submitted PDF Attachment &rarr;
                        </a>
                    </div>
                </c:if>
            </c:when>

            <c:otherwise>
                <h3 style="font-size: 1.2rem; margin-top: 0; margin-bottom: 1rem;">Submit Your Solution</h3>
                <form action="${pageContext.request.contextPath}/learn/assignment" method="post" enctype="multipart/form-data">
                    <input type="hidden" name="csrfToken" value="${csrfToken}"/>
                    <input type="hidden" name="assignmentId" value="${assignment.assignmentId}"/>

                    <div style="margin-bottom: 1.25rem;">
                        <label for="bodyText" style="display: block; font-weight: 600; font-size: 0.9rem; margin-bottom: 0.5rem;">
                            Solution Text / Notes / Links:
                        </label>
                        <textarea id="bodyText" name="bodyText" rows="6" style="width: 100%; padding: 0.75rem; border: 1px solid var(--border-color); border-radius: 6px; background: var(--bg-card); font-family: inherit; font-size: 0.95rem; resize: vertical;" placeholder="Write or paste your submission here..."></textarea>
                    </div>

                    <div style="margin-bottom: 1.5rem;">
                        <label for="file" style="display: block; font-weight: 600; font-size: 0.9rem; margin-bottom: 0.5rem;">
                            PDF Document Attachment (Optional, max 5MB):
                        </label>
                        <input type="file" id="file" name="file" accept="application/pdf" style="font-size: 0.9rem;"/>
                    </div>

                    <button type="submit" class="btn btn-primary" style="font-size: 1rem; padding: 0.6rem 1.75rem;">
                        Submit Assignment
                    </button>
                </form>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
