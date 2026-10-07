<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Today's Moves" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div style="margin-bottom: 2rem;">
    <h1>Welcome back, <c:out value="${sessionScope.CURRENT_USER.fullName}" /></h1>
    <p class="subtitle" style="margin-bottom: 0;">Your personalized adaptive learning dashboard</p>
</div>

<!-- Mastery Status Summary -->
<div class="grid-3" style="margin-bottom: 2rem;">
    <div class="card" style="margin-bottom: 0; border-left: 4px solid var(--mastery-solid);">
        <div style="font-size: 0.85rem; font-weight: 700; color: var(--text-muted); text-transform: uppercase;">Solid Foundations</div>
        <div style="font-size: 2rem; font-weight: 700; color: var(--mastery-solid); margin-top: 0.25rem;">
            ${dashboard.totalSolidConcepts}
        </div>
        <div style="font-size: 0.85rem; color: var(--text-subtle);">Mastery 75%+</div>
    </div>
    <div class="card" style="margin-bottom: 0; border-left: 4px solid var(--mastery-shaky);">
        <div style="font-size: 0.85rem; font-weight: 700; color: var(--text-muted); text-transform: uppercase;">Shaky Gaps</div>
        <div style="font-size: 2rem; font-weight: 700; color: var(--mastery-shaky); margin-top: 0.25rem;">
            ${dashboard.totalShakyConcepts}
        </div>
        <div style="font-size: 0.85rem; color: var(--text-subtle);"><a href="${pageContext.request.contextPath}/learn/gaps">Inspect in Gap Radar →</a></div>
    </div>
    <div class="card" style="margin-bottom: 0; border-left: 4px solid var(--accent-saffron);">
        <div style="font-size: 0.85rem; font-weight: 700; color: var(--text-muted); text-transform: uppercase;">Active Courses</div>
        <div style="font-size: 2rem; font-weight: 700; color: var(--accent-saffron); margin-top: 0.25rem;">
            ${dashboard.enrolledCourses.size()}
        </div>
        <div style="font-size: 0.85rem; color: var(--text-subtle);"><a href="${pageContext.request.contextPath}/learn/courses">View all enrolled →</a></div>
    </div>
</div>

<div class="grid-2">
    <!-- Today's 3 Moves -->
    <div>
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
            <h2>Today's 3 Moves</h2>
            <span style="font-size: 0.85rem; color: var(--accent-saffron); font-weight: 600;">Ranked by Priority</span>
        </div>

        <c:if test="${empty dashboard.topMoves}">
            <div class="card" style="text-align: center; padding: 2.5rem;">
                <p style="color: var(--text-muted);">You're all caught up! Browse your course concept maps to start new topics.</p>
            </div>
        </c:if>

        <c:forEach var="move" items="${dashboard.topMoves}" varStatus="loop">
            <div class="move-card">
                <div style="flex: 1; padding-right: 1rem;">
                    <div class="move-priority">Move #${loop.index + 1} &bull; Priority Score: <c:out value="${String.format('%.1f', move.priority())}" /></div>
                    <h3 style="margin-bottom: 0.35rem;"><c:out value="${move.title}" /></h3>
                    <p style="color: var(--text-muted); font-size: 0.9rem;"><c:out value="${move.describe()}" /></p>
                </div>
                <div>
                    <a href="${pageContext.request.contextPath}${move.link}" class="btn btn-primary btn-sm">
                        Start Move →
                    </a>
                </div>
            </div>
        </c:forEach>
    </div>

    <!-- Upcoming Deadlines & Quick Courses -->
    <div>
        <h2 style="margin-bottom: 1rem;">Upcoming Deadlines</h2>

        <c:if test="${empty dashboard.upcomingDeadlines}">
            <div class="card" style="text-align: center; padding: 2rem;">
                <p style="color: var(--text-muted);">No impending deadlines in the next 7 days.</p>
            </div>
        </c:if>

        <c:forEach var="assign" items="${dashboard.upcomingDeadlines}">
            <div class="card" style="margin-bottom: 1rem; padding: 1.15rem; display: flex; justify-content: space-between; align-items: center;">
                <div>
                    <h4 style="margin-bottom: 0.2rem;"><c:out value="${assign.title}" /></h4>
                    <span style="font-size: 0.85rem; color: ${assign.overdue ? 'var(--color-error)' : 'var(--text-muted)'}; font-weight: 500;">
                        Due: <c:out value="${assign.dueAt}" />
                    </span>
                </div>
                <a href="${pageContext.request.contextPath}/learn/assignment?id=${assign.assignmentId}" class="btn btn-secondary btn-sm">
                    Submit →
                </a>
            </div>
        </c:forEach>

        <h2 style="margin-top: 2rem; margin-bottom: 1rem;">Enrolled Concept Maps</h2>
        <div class="grid-2">
            <c:forEach var="course" items="${dashboard.enrolledCourses}">
                <div class="card" style="margin-bottom: 0; padding: 1.15rem;">
                    <div style="font-size: 0.8rem; font-weight: 700; color: var(--accent-saffron);"><c:out value="${course.code}" /></div>
                    <h4 style="margin: 0.25rem 0 0.75rem;"><c:out value="${course.title}" /></h4>
                    <a href="${pageContext.request.contextPath}/learn/course/map?id=${course.courseId}" class="btn btn-secondary btn-sm" style="width: 100%;">
                        Open Map
                    </a>
                </div>
            </c:forEach>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
