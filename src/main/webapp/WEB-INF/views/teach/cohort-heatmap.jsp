<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="Cohort Heatmap - ${selectedCourse.title}"/>
</jsp:include>

<jsp:include page="/WEB-INF/views/common/messages.jsp"/>

<div class="page-container">
    <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem;">
        <div>
            <div style="display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.5rem;">
                <a href="${pageContext.request.contextPath}/teach/home" class="btn btn-outline" style="font-size: 0.8rem; padding: 0.25rem 0.5rem;">
                    &larr; Instructor Hub
                </a>
                <span class="badge" style="background: var(--bg-card-subtle); color: var(--accent-color); font-weight: 600;">
                    COHORT MASTERY MATRIX
                </span>
            </div>
            <h1 style="margin: 0 0 0.25rem 0; font-size: 1.85rem;">Cohort Heatmap</h1>
            <p style="color: var(--text-muted); margin: 0; font-size: 0.95rem;">
                Live cohort-wide concept mastery matrix. Identify learning bottlenecks and stalled students across the entire curriculum.
            </p>
        </div>

        <div>
            <a href="${pageContext.request.contextPath}/teach/interventions?course=${selectedCourse.courseId}" class="btn btn-primary">
                + Log Targeted Intervention
            </a>
        </div>
    </div>

    <!-- Course Selector -->
    <c:if test="${not empty courses}">
        <div class="card" style="margin-bottom: 2rem; padding: 1rem 1.5rem;">
            <form action="${pageContext.request.contextPath}/teach/cohort" method="get" style="display: flex; align-items: center; gap: 1rem; flex-wrap: wrap;">
                <label for="courseSelect" style="font-weight: 600; font-size: 0.9rem;">Course:</label>
                <select id="courseSelect" name="course" onchange="this.form.submit()" style="padding: 0.5rem 1rem; border-radius: 6px; border: 1px solid var(--border-color); background: var(--bg-card); font-size: 0.9rem; min-width: 250px;">
                    <c:forEach var="c" items="${courses}">
                        <option value="${c.courseId}" ${c.courseId == selectedCourse.courseId ? 'selected' : ''}>
                            <c:out value="${c.code}"/> - <c:out value="${c.title}"/>
                        </option>
                    </c:forEach>
                </select>
                <noscript><button type="submit" class="btn btn-outline" style="font-size: 0.85rem;">Select</button></noscript>
            </form>
        </div>
    </c:if>

    <!-- Legend -->
    <div class="card" style="margin-bottom: 1.5rem; padding: 0.75rem 1.25rem; display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 1rem;">
        <div style="display: flex; gap: 1rem; align-items: center; flex-wrap: wrap;">
            <span style="font-weight: 600; font-size: 0.85rem;">Legend:</span>
            <span class="badge badge-mastered" style="font-size: 0.8rem;">Mastered (100%)</span>
            <span class="badge badge-practicing" style="font-size: 0.8rem;">Practicing (60-99%)</span>
            <span class="badge badge-weak" style="font-size: 0.8rem;">Weak (&lt;60%)</span>
            <span class="badge badge-unlocked" style="font-size: 0.8rem;">Unseen / No Evidence</span>
        </div>
        <div style="font-size: 0.85rem; color: var(--accent-weak); font-weight: 600;">
            &#9888; = Stalled Student (&gt;7 days stuck on weak concept)
        </div>
    </div>

    <!-- Heatmap Table Matrix -->
    <div class="card" style="padding: 1rem;">
        <c:choose>
            <c:when test="${empty cohortRows}">
                <p style="color: var(--text-muted); text-align: center; padding: 2rem;">
                    No enrolled students in this course yet.
                </p>
            </c:when>
            <c:when test="${empty concepts}">
                <p style="color: var(--text-muted); text-align: center; padding: 2rem;">
                    No concepts defined for this course. Build the concept graph first.
                </p>
            </c:when>
            <c:otherwise>
                <div class="table-container" style="overflow-x: auto;">
                    <table class="data-table" style="font-size: 0.85rem;">
                        <thead>
                            <tr>
                                <th style="position: sticky; left: 0; background: var(--bg-card); z-index: 2; min-width: 180px;">
                                    Student
                                </th>
                                <th style="min-width: 80px; text-align: center;">Status</th>
                                <c:forEach var="c" items="${concepts}">
                                    <th style="min-width: 110px; text-align: center; padding: 0.5rem 0.25rem;" title="<c:out value='${c.title}'/>">
                                        <div style="font-weight: 700;">#${c.displayOrder}</div>
                                        <div style="font-size: 0.75rem; font-weight: normal; max-width: 100px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin: 0 auto;">
                                            <c:out value="${c.title}"/>
                                        </div>
                                    </th>
                                </c:forEach>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="row" items="${cohortRows}">
                                <tr>
                                    <td style="position: sticky; left: 0; background: var(--bg-card); z-index: 1; font-weight: 600;">
                                        <c:out value="${row.studentName}"/>
                                        <div style="font-size: 0.75rem; color: var(--text-muted); font-weight: normal;">
                                            Roll: <c:out value="${row.rollNo}"/>
                                        </div>
                                    </td>
                                    <td style="text-align: center;">
                                        <c:choose>
                                            <c:when test="${row.stalled}">
                                                <span title="Stalled: stuck on ${row.stalledConceptsCount} concepts" style="color: var(--accent-weak); font-size: 1.1rem; cursor: help;">
                                                    &#9888;
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span style="color: var(--accent-mastered);">&#10003;</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <c:forEach var="c" items="${concepts}">
                                        <c:set var="lvl" value="${row.conceptLevels[c.conceptId]}"/>
                                        <c:set var="m" value="${row.conceptMastery[c.conceptId]}"/>
                                        <td style="text-align: center; padding: 0.4rem 0.2rem;">
                                            <c:choose>
                                                <c:when test="${lvl == 'MASTERED'}">
                                                    <span class="badge badge-mastered" style="font-size: 0.75rem; width: 100%; display: block;">
                                                        100%
                                                    </span>
                                                </c:when>
                                                <c:when test="${lvl == 'PRACTICING'}">
                                                    <span class="badge badge-practicing" style="font-size: 0.75rem; width: 100%; display: block;">
                                                        <fmt:formatNumber value="${m}" maxFractionDigits="0"/>%
                                                    </span>
                                                </c:when>
                                                <c:when test="${lvl == 'WEAK'}">
                                                    <span class="badge badge-weak" style="font-size: 0.75rem; width: 100%; display: block;">
                                                        <fmt:formatNumber value="${m}" maxFractionDigits="0"/>%
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge badge-unlocked" style="font-size: 0.75rem; width: 100%; display: block; opacity: 0.6;">
                                                        -
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </c:forEach>
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
