<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="Gap Radar - Root Cause Diagnosis"/>
</jsp:include>

<jsp:include page="/WEB-INF/views/common/messages.jsp"/>

<div class="page-container">
    <div style="margin-bottom: 2rem;">
        <span class="badge" style="background: var(--bg-card-subtle); color: var(--accent-color); font-weight: 600; margin-bottom: 0.5rem; display: inline-block;">
            DIAGNOSTIC ENGINE
        </span>
        <h1 style="margin: 0.25rem 0 0.5rem 0; font-size: 2rem;">Gap Radar</h1>
        <p style="color: var(--text-muted); max-width: 700px; margin: 0; line-height: 1.6;">
            When you struggle with an advanced concept, the real problem is almost always an unmastered prerequisite deeper down in the graph. Gap Radar traces the ancestor dependencies to locate the true root cause.
        </p>
    </div>

    <!-- Course Selector -->
    <c:if test="${not empty enrolledCourses}">
        <div class="card" style="margin-bottom: 2rem; padding: 1rem 1.5rem;">
            <form action="${pageContext.request.contextPath}/learn/gaps" method="get" style="display: flex; align-items: center; gap: 1rem; flex-wrap: wrap;">
                <label for="courseSelect" style="font-weight: 600; font-size: 0.9rem;">Course:</label>
                <select id="courseSelect" name="course" onchange="this.form.submit()" style="padding: 0.5rem 1rem; border-radius: 6px; border: 1px solid var(--border-color); background: var(--bg-card); font-size: 0.9rem; min-width: 250px;">
                    <c:forEach var="c" items="${enrolledCourses}">
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
        <c:when test="${empty enrolledCourses}">
            <div class="card" style="text-align: center; padding: 3rem 1.5rem;">
                <p style="color: var(--text-muted); font-size: 1.1rem; margin-bottom: 1.5rem;">You are not currently enrolled in any courses.</p>
                <a href="${pageContext.request.contextPath}/catalog" class="btn btn-primary">Browse Course Catalog</a>
            </div>
        </c:when>

        <c:when test="${empty rootCauses}">
            <div class="card" style="text-align: center; padding: 3.5rem 1.5rem; border-color: var(--accent-mastered);">
                <div style="font-size: 3rem; margin-bottom: 0.5rem; color: var(--accent-mastered);">&#10003;</div>
                <h3 style="font-size: 1.4rem; margin-bottom: 0.5rem;">No Foundational Gaps Detected!</h3>
                <p style="color: var(--text-muted); max-width: 500px; margin: 0 auto 1.5rem auto; line-height: 1.6;">
                    Your prerequisite tree in <strong><c:out value="${selectedCourse.title}"/></strong> is healthy. All prerequisite ancestors for concepts you're currently working on meet the gate threshold.
                </p>
                <a href="${pageContext.request.contextPath}/learn/map?course=${selectedCourse.courseId}" class="btn btn-primary">
                    View Concept Map &rarr;
                </a>
            </div>
        </c:when>

        <c:otherwise>
            <div style="display: flex; flex-direction: column; gap: 1.5rem;">
                <c:forEach var="rc" items="${rootCauses}">
                    <div class="card" style="border-left: 5px solid var(--accent-weak); padding: 1.5rem;">
                        <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 1rem; margin-bottom: 1rem;">
                            <div>
                                <h3 style="margin: 0 0 0.5rem 0; font-size: 1.25rem;">
                                    <c:out value="${rc.message}"/>
                                </h3>
                                <p style="color: var(--text-muted); margin: 0; font-size: 0.9rem;">
                                    Attempting to practice <em><c:out value="${rc.shakyConceptTitle}"/></em> will be difficult until the root prerequisite is solidified.
                                </p>
                            </div>
                            <span class="badge badge-weak" style="font-size: 0.85rem; padding: 0.35rem 0.75rem;">
                                Root Cause Identified
                            </span>
                        </div>

                        <!-- Diagnosis Comparison Grid -->
                        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: 1rem; margin-top: 1rem; background: var(--bg-card-subtle); padding: 1.25rem; border-radius: 8px;">
                            <div style="border-right: 1px solid var(--border-color); padding-right: 1rem;">
                                <div style="font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted); margin-bottom: 0.35rem;">
                                    Downstream Struggling Concept
                                </div>
                                <div style="font-weight: 700; font-size: 1.05rem; margin-bottom: 0.25rem;">
                                    <c:out value="${rc.shakyConceptTitle}"/>
                                </div>
                                <div style="display: flex; align-items: center; gap: 0.5rem;">
                                    <span style="font-size: 0.85rem; color: var(--text-muted);">Current Mastery:</span>
                                    <span class="badge badge-weak" style="font-size: 0.8rem;"><c:out value="${rc.shakyMastery}"/>%</span>
                                </div>
                            </div>

                            <div>
                                <div style="font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--accent-weak); font-weight: 700; margin-bottom: 0.35rem;">
                                    Deepest Weak Ancestor (Fix This First)
                                </div>
                                <div style="font-weight: 700; font-size: 1.05rem; margin-bottom: 0.25rem; color: var(--accent-weak);">
                                    <c:out value="${rc.rootCauseConceptTitle}"/>
                                </div>
                                <div style="display: flex; align-items: center; gap: 0.5rem;">
                                    <span style="font-size: 0.85rem; color: var(--text-muted);">Root Mastery:</span>
                                    <span class="badge badge-weak" style="font-size: 0.8rem;"><c:out value="${rc.rootCauseMastery}"/>%</span>
                                </div>
                            </div>
                        </div>

                        <!-- Action Button -->
                        <div style="margin-top: 1.25rem; display: flex; gap: 1rem; justify-content: flex-end;">
                            <a href="${pageContext.request.contextPath}/learn/concept?id=${rc.rootCauseConceptId}" class="btn btn-primary" style="font-size: 0.9rem;">
                                Review Root Concept &rarr;
                            </a>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
