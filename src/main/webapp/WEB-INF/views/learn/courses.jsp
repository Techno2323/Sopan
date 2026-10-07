<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="My Enrolled Courses" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div style="display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 2rem;">
    <div>
        <h1>My Courses</h1>
        <p class="subtitle" style="margin-bottom: 0;">Access your courses, concept maps, and assignments</p>
    </div>
    <a href="${pageContext.request.contextPath}/catalog" class="btn btn-primary">+ Browse Catalog</a>
</div>

<h2>Active Enrollments</h2>
<c:if test="${empty activeCourses}">
    <div class="card" style="text-align: center; padding: 2.5rem; margin-bottom: 2rem;">
        <p style="color: var(--text-muted); margin-bottom: 1rem;">You are not currently enrolled in any active courses.</p>
        <a href="${pageContext.request.contextPath}/catalog" class="btn btn-primary">Find a Course</a>
    </div>
</c:if>

<div class="grid-3" style="margin-bottom: 3rem;">
    <c:forEach var="course" items="${activeCourses}">
        <div class="card" style="display: flex; flex-direction: column; justify-content: space-between;">
            <div>
                <div style="font-weight: 700; color: var(--accent-saffron); font-size: 0.9rem; margin-bottom: 0.4rem;">
                    <c:out value="${course.code}" />
                </div>
                <h3><c:out value="${course.title}" /></h3>
                <p style="color: var(--text-muted); font-size: 0.9rem; margin: 0.5rem 0 1rem; line-height: 1.4;">
                    <c:out value="${course.description}" />
                </p>
            </div>
            <div>
                <a href="${pageContext.request.contextPath}/learn/course/map?id=${course.courseId}" class="btn btn-primary" style="width: 100%; margin-bottom: 0.5rem;">
                    View Concept Map →
                </a>
            </div>
        </div>
    </c:forEach>
</div>

<c:if test="${not empty droppedCourses}">
    <h2>Previous Enrollments</h2>
    <div class="grid-3">
        <c:forEach var="course" items="${droppedCourses}">
            <div class="card" style="opacity: 0.8;">
                <div style="color: var(--text-subtle); font-size: 0.85rem;"><c:out value="${course.code}" /></div>
                <h4><c:out value="${course.title}" /></h4>
                <div style="margin-top: 1rem;">
                    <form action="${pageContext.request.contextPath}/learn/enroll" method="post">
                        <input type="hidden" name="csrfToken" value="${csrfToken}"/>
                        <input type="hidden" name="courseId" value="${course.courseId}"/>
                        <button type="submit" class="btn btn-secondary btn-sm" style="width: 100%;">Re-enroll</button>
                    </form>
                </div>
            </div>
        </c:forEach>
    </div>
</c:if>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
