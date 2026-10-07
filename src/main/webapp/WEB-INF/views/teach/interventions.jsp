<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="Interventions - ${selectedCourse.title}"/>
</jsp:include>

<jsp:include page="/WEB-INF/views/common/messages.jsp"/>

<div class="page-container">
    <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem;">
        <div>
            <div style="display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.5rem;">
                <a href="${pageContext.request.contextPath}/teach/cohort?course=${selectedCourse.courseId}" class="btn btn-outline" style="font-size: 0.8rem; padding: 0.25rem 0.5rem;">
                    &larr; Cohort Heatmap
                </a>
                <span class="badge" style="background: var(--bg-card-subtle); color: var(--accent-color); font-weight: 600;">
                    INTERVENTION TRACKER
                </span>
            </div>
            <h1 style="margin: 0 0 0.25rem 0; font-size: 1.85rem;">Targeted Interventions</h1>
            <p style="color: var(--text-muted); margin: 0; font-size: 0.95rem;">
                Log targeted pedagogical actions for struggling or stalled students. Sopan tracks before-and-after concept mastery to measure intervention effectiveness.
            </p>
        </div>
    </div>

    <!-- Course Selector -->
    <c:if test="${not empty courses}">
        <div class="card" style="margin-bottom: 2rem; padding: 1rem 1.5rem;">
            <form action="${pageContext.request.contextPath}/teach/interventions" method="get" style="display: flex; align-items: center; gap: 1rem; flex-wrap: wrap;">
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

    <c:choose>
        <c:when test="${empty selectedCourse}">
            <div class="card" style="text-align: center; padding: 3rem 1.5rem;">
                <p style="color: var(--text-muted);">Please select or create a course to log interventions.</p>
            </div>
        </c:when>

        <c:otherwise>
            <!-- Log New Intervention Card -->
            <div class="card" style="margin-bottom: 2.5rem;">
                <h3 style="font-size: 1.25rem; margin-top: 0; margin-bottom: 1.25rem;">+ Log Targeted Student Intervention</h3>
                <form action="${pageContext.request.contextPath}/teach/interventions" method="post">
                    <input type="hidden" name="csrfToken" value="${csrfToken}"/>
                    <input type="hidden" name="courseId" value="${selectedCourse.courseId}"/>

                    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1rem; margin-bottom: 1rem;">
                        <div>
                            <label for="studentId" style="display: block; font-weight: 600; font-size: 0.85rem; margin-bottom: 0.35rem;">Target Student:</label>
                            <select id="studentId" name="studentId" required style="width: 100%; padding: 0.5rem; border: 1px solid var(--border-color); border-radius: 6px; background: var(--bg-card);">
                                <c:forEach var="e" items="${enrolledStudents}">
                                    <option value="${e.studentId}">Student ID #${e.studentId}</option>
                                </c:forEach>
                            </select>
                        </div>

                        <div>
                            <label for="conceptId" style="display: block; font-weight: 600; font-size: 0.85rem; margin-bottom: 0.35rem;">Target Concept (DAG Node):</label>
                            <select id="conceptId" name="conceptId" required style="width: 100%; padding: 0.5rem; border: 1px solid var(--border-color); border-radius: 6px; background: var(--bg-card);">
                                <c:forEach var="c" items="${concepts}">
                                    <option value="${c.conceptId}">#${c.displayOrder} - <c:out value="${c.title}"/></option>
                                </c:forEach>
                            </select>
                        </div>

                        <div>
                            <label for="type" style="display: block; font-weight: 600; font-size: 0.85rem; margin-bottom: 0.35rem;">Intervention Type:</label>
                            <select id="type" name="type" style="width: 100%; padding: 0.5rem; border: 1px solid var(--border-color); border-radius: 6px; background: var(--bg-card);">
                                <option value="OFFICE_HOURS">1-on-1 Office Hours</option>
                                <option value="EXTRA_MATERIAL">Supplemental Material</option>
                                <option value="RETAKE">Quiz / Assignment Retake</option>
                                <option value="NOTE">Advisory Note</option>
                            </select>
                        </div>
                    </div>

                    <div style="margin-bottom: 1.25rem;">
                        <label for="note" style="display: block; font-weight: 600; font-size: 0.85rem; margin-bottom: 0.35rem;">Action Plan / Notes:</label>
                        <textarea id="note" name="note" rows="3" required placeholder="Describe the diagnosed gap and what remedial action was taken..." style="width: 100%; padding: 0.5rem; border: 1px solid var(--border-color); border-radius: 6px; background: var(--bg-card); font-family: inherit; font-size: 0.95rem;"></textarea>
                    </div>

                    <button type="submit" class="btn btn-primary">
                        Record Intervention & Capture Baseline
                    </button>
                </form>
            </div>

            <!-- Interventions Log Table with Before/After Mastery Tracking -->
            <div class="card">
                <h3 style="font-size: 1.25rem; margin-top: 0; margin-bottom: 1rem;">Historical Interventions Log (${interventions.size()})</h3>
                <c:choose>
                    <c:when test="${empty interventions}">
                        <p style="color: var(--text-muted); font-size: 0.95rem;">No interventions logged for this course yet.</p>
                    </c:when>
                    <c:otherwise>
                        <div class="table-container">
                            <table class="data-table">
                                <thead>
                                    <tr>
                                        <th>Date</th>
                                        <th>Student</th>
                                        <th>Concept</th>
                                        <th>Type</th>
                                        <th>Note</th>
                                        <th>Mastery Before</th>
                                        <th>Mastery After</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="inv" items="${interventions}">
                                        <tr>
                                            <td style="font-size: 0.8rem; color: var(--text-muted);"><c:out value="${inv.createdAt}"/></td>
                                            <td><strong><c:out value="${not empty inv.studentName ? inv.studentName : inv.studentId}"/></strong></td>
                                            <td><c:out value="${inv.conceptTitle}"/></td>
                                            <td><span class="badge" style="background: var(--bg-card-subtle);"><c:out value="${inv.type}"/></span></td>
                                            <td style="max-width: 280px; font-size: 0.85rem;"><c:out value="${inv.note}"/></td>
                                            <td>
                                                <span class="badge badge-weak">
                                                    <fmt:formatNumber value="${inv.masteryBefore}" maxFractionDigits="1"/>%
                                                </span>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty inv.masteryAfter}">
                                                        <span class="badge ${inv.masteryAfter >= 60 ? 'badge-mastered' : 'badge-practicing'}">
                                                            <fmt:formatNumber value="${inv.masteryAfter}" maxFractionDigits="1"/>%
                                                        </span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span style="color: var(--text-muted); font-size: 0.8rem;">In Progress</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
