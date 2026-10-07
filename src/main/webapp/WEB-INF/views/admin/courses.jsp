<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="Course Approvals - Administration"/>
</jsp:include>

<jsp:include page="/WEB-INF/views/common/messages.jsp"/>

<div class="page-container">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem;">
        <div>
            <div style="display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.5rem;">
                <a href="${pageContext.request.contextPath}/admin/home" class="btn btn-outline" style="font-size: 0.8rem; padding: 0.25rem 0.5rem;">
                    &larr; Admin Home
                </a>
            </div>
            <h1 style="margin: 0 0 0.25rem 0; font-size: 1.85rem;">Course Approvals Queue</h1>
            <p style="color: var(--text-muted); margin: 0; font-size: 0.95rem;">
                Review submitted courses from instructors before publishing them to the student catalog.
            </p>
        </div>
    </div>

    <div class="card">
        <h3 style="font-size: 1.25rem; margin-top: 0; margin-bottom: 1rem;">Courses Pending Approval (${pendingCourses.size()})</h3>
        <c:choose>
            <c:when test="${empty pendingCourses}">
                <div style="text-align: center; padding: 3rem 1.5rem;">
                    <div style="font-size: 2.5rem; color: var(--accent-mastered); margin-bottom: 0.5rem;">&#10003;</div>
                    <p style="color: var(--text-muted); font-size: 1.05rem; margin: 0;">The approval queue is empty. No pending course submissions!</p>
                </div>
            </c:when>
            <c:otherwise>
                <div style="display: flex; flex-direction: column; gap: 1.5rem;">
                    <c:forEach var="c" items="${pendingCourses}">
                        <div style="border: 1px solid var(--border-color); border-radius: 8px; padding: 1.5rem; background: var(--bg-card);">
                            <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 1rem; margin-bottom: 0.75rem;">
                                <div>
                                    <span class="badge" style="background: var(--bg-card-subtle); margin-bottom: 0.35rem; display: inline-block;">
                                        <c:out value="${c.code}"/> &bull; Difficulty: <c:out value="${c.difficulty}"/>
                                    </span>
                                    <h3 style="margin: 0 0 0.5rem 0; font-size: 1.35rem;"><c:out value="${c.title}"/></h3>
                                    <p style="color: var(--text-muted); margin: 0; font-size: 0.95rem; line-height: 1.5;">
                                        <c:out value="${c.description}"/>
                                    </p>
                                </div>
                                <span class="badge badge-practicing" style="font-size: 0.85rem; padding: 0.35rem 0.75rem;">
                                    Pending Review
                                </span>
                            </div>

                            <div style="display: flex; gap: 1.5rem; font-size: 0.85rem; color: var(--text-muted); margin-bottom: 1.25rem; padding-top: 0.5rem; border-top: 1px solid var(--border-color);">
                                <span>Instructor ID: #<c:out value="${c.instructorId}"/></span>
                                <span>Max Capacity: <c:out value="${c.maxStudents}"/> students</span>
                                <span>Prereq Gate: <c:out value="${c.gateThreshold}"/>%</span>
                            </div>

                            <div style="display: flex; gap: 1rem; align-items: center; justify-content: flex-end; flex-wrap: wrap;">
                                <a href="${pageContext.request.contextPath}/catalog/course?id=${c.courseId}" target="_blank" class="btn btn-outline" style="font-size: 0.85rem;">
                                    Preview Course Page &rarr;
                                </a>

                                <form action="${pageContext.request.contextPath}/admin/courses" method="post" style="margin: 0; display: inline;">
                                    <input type="hidden" name="csrfToken" value="${csrfToken}"/>
                                    <input type="hidden" name="courseId" value="${c.courseId}"/>
                                    <input type="hidden" name="action" value="approve"/>
                                    <button type="submit" class="btn btn-primary" style="font-size: 0.85rem;">
                                        Approve & Publish to Catalog
                                    </button>
                                </form>

                                <form action="${pageContext.request.contextPath}/admin/courses" method="post" style="margin: 0; display: flex; gap: 0.5rem; align-items: center;">
                                    <input type="hidden" name="csrfToken" value="${csrfToken}"/>
                                    <input type="hidden" name="courseId" value="${c.courseId}"/>
                                    <input type="hidden" name="action" value="reject"/>
                                    <input type="text" name="reason" placeholder="Feedback/reason for rejection..." style="padding: 0.4rem 0.6rem; font-size: 0.85rem; border: 1px solid var(--border-color); border-radius: 4px; background: var(--bg-card);"/>
                                    <button type="submit" class="btn btn-outline" style="font-size: 0.85rem; color: var(--accent-weak); border-color: var(--accent-weak);">
                                        Return as Draft
                                    </button>
                                </form>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
