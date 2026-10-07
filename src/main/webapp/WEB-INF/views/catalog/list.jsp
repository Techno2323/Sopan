<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Course Catalog" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div style="display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 2rem;">
    <div>
        <h1>Course Catalog</h1>
        <p class="subtitle" style="margin-bottom: 0;">Explore concept graphs and enroll in published courses</p>
    </div>
</div>

<!-- Search & Filter Card -->
<div class="card" style="padding: 1.25rem; margin-bottom: 2rem;">
    <form action="${pageContext.request.contextPath}/catalog" method="get" style="display: flex; gap: 1rem; flex-wrap: wrap;">
        <div style="flex: 2; min-width: 220px;">
            <input type="text" name="q" class="form-control" placeholder="Search by title, code or keyword..."
                   value="<c:out value="${query}" />"/>
        </div>
        <div style="flex: 1; min-width: 140px;">
            <select name="difficulty" class="form-control">
                <option value="">All Difficulties</option>
                <option value="BEGINNER" ${difficulty == 'BEGINNER' ? 'selected' : ''}>Beginner</option>
                <option value="INTERMEDIATE" ${difficulty == 'INTERMEDIATE' ? 'selected' : ''}>Intermediate</option>
                <option value="ADVANCED" ${difficulty == 'ADVANCED' ? 'selected' : ''}>Advanced</option>
            </select>
        </div>
        <div style="flex: 1; min-width: 140px;">
            <select name="sortBy" class="form-control">
                <option value="created_at" ${sortBy == 'created_at' ? 'selected' : ''}>Newest First</option>
                <option value="title" ${sortBy == 'title' ? 'selected' : ''}>Title (A-Z)</option>
                <option value="difficulty" ${sortBy == 'difficulty' ? 'selected' : ''}>Difficulty</option>
            </select>
        </div>
        <div>
            <button type="submit" class="btn btn-primary">Filter</button>
            <a href="${pageContext.request.contextPath}/catalog" class="btn btn-secondary">Reset</a>
        </div>
    </form>
</div>

<!-- Courses Grid -->
<c:if test="${empty page.items}">
    <div class="card" style="text-align: center; padding: 3rem;">
        <h3>No courses found</h3>
        <p style="color: var(--text-muted); margin-top: 0.5rem;">Try adjusting your search criteria or filters.</p>
    </div>
</c:if>

<div class="grid-3">
    <c:forEach var="course" items="${page.items}">
        <div class="card" style="display: flex; flex-direction: column; justify-content: space-between;">
            <div>
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem;">
                    <span style="font-weight: 700; color: var(--accent-saffron); font-size: 0.9rem;">
                        <c:out value="${course.code}" />
                    </span>
                    <span class="role-pill">
                        <c:out value="${course.difficulty}" />
                    </span>
                </div>
                <h3 style="margin-bottom: 0.5rem;">
                    <a href="${pageContext.request.contextPath}/catalog/course?id=${course.courseId}">
                        <c:out value="${course.title}" />
                    </a>
                </h3>
                <p style="color: var(--text-muted); font-size: 0.92rem; margin-bottom: 1rem; line-height: 1.4;">
                    <c:out value="${course.description}" />
                </p>
            </div>

            <div style="border-top: 1px solid var(--border-color); padding-top: 0.85rem; margin-top: 1rem; display: flex; justify-content: space-between; align-items: center; font-size: 0.88rem; color: var(--text-subtle);">
                <div>Instructor: <strong><c:out value="${course.instructorName}" /></strong></div>
                <div>Gate: <strong><c:out value="${course.gateThreshold}" />%</strong></div>
            </div>
        </div>
    </c:forEach>
</div>

<!-- Pagination -->
<c:if test="${page.totalPages > 1}">
    <div style="display: flex; justify-content: center; gap: 0.5rem; align-items: center; margin-top: 2.5rem;">
        <c:if test="${page.hasPrevious()}">
            <a href="${pageContext.request.contextPath}/catalog?q=<c:out value="${query}" />&difficulty=<c:out value="${difficulty}" />&sortBy=<c:out value="${sortBy}" />&page=${page.pageNumber - 1}" class="btn btn-secondary btn-sm">← Previous</a>
        </c:if>
        <span style="color: var(--text-muted); font-size: 0.9rem; margin: 0 0.5rem;">
            Page ${page.pageNumber} of ${page.totalPages}
        </span>
        <c:if test="${page.hasNext()}">
            <a href="${pageContext.request.contextPath}/catalog?q=<c:out value="${query}" />&difficulty=<c:out value="${difficulty}" />&sortBy=<c:out value="${sortBy}" />&page=${page.pageNumber + 1}" class="btn btn-secondary btn-sm">Next →</a>
        </c:if>
    </div>
</c:if>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
