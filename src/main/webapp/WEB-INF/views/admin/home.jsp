<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="Administration"/>
</jsp:include>

<jsp:include page="/WEB-INF/views/common/messages.jsp"/>

<div class="page-container">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem;">
        <div>
            <h1 style="margin: 0 0 0.25rem 0; font-size: 2rem;">System Administration</h1>
            <p style="color: var(--text-muted); margin: 0; font-size: 1rem;">
                Manage platform users, approve course publications, and audit system events.
            </p>
        </div>
        <div style="display: flex; gap: 0.75rem;">
            <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-outline">
                Manage Users
            </a>
            <a href="${pageContext.request.contextPath}/admin/courses" class="btn btn-outline">
                Course Approvals
            </a>
            <a href="${pageContext.request.contextPath}/admin/audit" class="btn btn-outline">
                Audit Logs
            </a>
        </div>
    </div>

    <!-- Metrics Cards -->
    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1.5rem; margin-bottom: 2.5rem;">
        <div class="card">
            <div style="font-size: 0.8rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted); margin-bottom: 0.5rem;">
                Pending Course Approvals
            </div>
            <div style="font-size: 2.5rem; font-weight: 800; color: ${pendingCourses.size() > 0 ? 'var(--accent-color)' : 'var(--text-color)'};">
                ${pendingCourses.size()}
            </div>
            <div style="margin-top: 0.5rem;">
                <a href="${pageContext.request.contextPath}/admin/courses" style="font-size: 0.85rem; color: var(--accent-color); font-weight: 600; text-decoration: none;">
                    Review queue &rarr;
                </a>
            </div>
        </div>

        <div class="card">
            <div style="font-size: 0.8rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted); margin-bottom: 0.5rem;">
                Total Registered Users
            </div>
            <div style="font-size: 2.5rem; font-weight: 800; color: var(--text-color);">
                ${totalUsers}
            </div>
            <div style="margin-top: 0.5rem;">
                <a href="${pageContext.request.contextPath}/admin/users" style="font-size: 0.85rem; color: var(--accent-color); font-weight: 600; text-decoration: none;">
                    View directory &rarr;
                </a>
            </div>
        </div>

        <div class="card">
            <div style="font-size: 0.8rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted); margin-bottom: 0.5rem;">
                Suspended Accounts
            </div>
            <div style="font-size: 2.5rem; font-weight: 800; color: ${suspendedCount > 0 ? 'var(--accent-weak)' : 'var(--text-color)'};">
                ${suspendedCount}
            </div>
            <div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 0.5rem;">
                Locked or flagged users
            </div>
        </div>
    </div>

    <!-- Pending Course Approvals -->
    <c:if test="${not empty pendingCourses}">
        <div class="card" style="margin-bottom: 2.5rem; border-left: 4px solid var(--accent-color);">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
                <h3 style="font-size: 1.25rem; margin: 0;">Courses Pending Approval</h3>
                <a href="${pageContext.request.contextPath}/admin/courses" class="btn btn-outline" style="font-size: 0.8rem; padding: 0.25rem 0.5rem;">
                    Full Queue &rarr;
                </a>
            </div>
            <div class="table-container">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Code</th>
                            <th>Title</th>
                            <th>Difficulty</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="pc" items="${pendingCourses}">
                            <tr>
                                <td><strong><c:out value="${pc.code}"/></strong></td>
                                <td><c:out value="${pc.title}"/></td>
                                <td><c:out value="${pc.difficulty}"/></td>
                                <td>
                                    <div style="display: flex; gap: 0.5rem;">
                                        <form action="${pageContext.request.contextPath}/admin/courses" method="post" style="margin: 0;">
                                            <input type="hidden" name="csrfToken" value="${csrfToken}"/>
                                            <input type="hidden" name="courseId" value="${pc.courseId}"/>
                                            <input type="hidden" name="action" value="approve"/>
                                            <button type="submit" class="btn btn-primary" style="font-size: 0.8rem; padding: 0.3rem 0.65rem;">
                                                Approve
                                            </button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </c:if>

    <!-- Recent Audit Logs -->
    <div class="card">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
            <h3 style="font-size: 1.25rem; margin: 0;">Recent Audit Trail</h3>
            <a href="${pageContext.request.contextPath}/admin/audit" class="btn btn-outline" style="font-size: 0.8rem; padding: 0.25rem 0.5rem;">
                View All Logs &rarr;
            </a>
        </div>
        <div class="table-container">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Timestamp</th>
                        <th>User ID</th>
                        <th>Action</th>
                        <th>Details</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="log" items="${recentLogs}">
                        <tr>
                            <td style="font-size: 0.8rem; color: var(--text-muted);"><c:out value="${log.createdAt}"/></td>
                            <td>User #<c:out value="${log.userId}"/></td>
                            <td><strong><c:out value="${log.action}"/></strong></td>
                            <td style="font-size: 0.85rem;"><c:out value="${log.details}"/></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
