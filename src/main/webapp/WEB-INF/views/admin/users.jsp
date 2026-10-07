<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="User Directory - Administration"/>
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
            <h1 style="margin: 0 0 0.25rem 0; font-size: 1.85rem;">User Directory</h1>
            <p style="color: var(--text-muted); margin: 0; font-size: 0.95rem;">
                Manage registered user accounts, roles, and suspension states.
            </p>
        </div>
    </div>

    <div class="card">
        <h3 style="font-size: 1.25rem; margin-top: 0; margin-bottom: 1rem;">All Accounts (${users.size()})</h3>
        <div class="table-container">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Role</th>
                        <th>Status</th>
                        <th>Joined</th>
                        <th>Change Status</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="u" items="${users}">
                        <tr>
                            <td>#<c:out value="${u.id}"/></td>
                            <td><strong><c:out value="${u.fullName}"/></strong></td>
                            <td><c:out value="${u.email}"/></td>
                            <td>
                                <span class="badge" style="background: var(--bg-card-subtle);">
                                    <c:out value="${u.role}"/>
                                </span>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${u.status == 'ACTIVE'}">
                                        <span class="badge badge-mastered">Active</span>
                                    </c:when>
                                    <c:when test="${u.status == 'SUSPENDED'}">
                                        <span class="badge badge-weak">Suspended</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge badge-unlocked"><c:out value="${u.status}"/></span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td style="font-size: 0.8rem; color: var(--text-muted);"><c:out value="${u.createdAt}"/></td>
                            <td>
                                <form action="${pageContext.request.contextPath}/admin/users" method="post" style="display: flex; gap: 0.5rem; align-items: center; margin: 0;">
                                    <input type="hidden" name="csrfToken" value="${csrfToken}"/>
                                    <input type="hidden" name="userId" value="${u.id}"/>
                                    <select name="status" style="padding: 0.3rem 0.5rem; font-size: 0.8rem; border-radius: 4px; border: 1px solid var(--border-color); background: var(--bg-card);">
                                        <option value="ACTIVE" ${u.status == 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
                                        <option value="SUSPENDED" ${u.status == 'SUSPENDED' ? 'selected' : ''}>SUSPENDED</option>
                                        <option value="PENDING_APPROVAL" ${u.status == 'PENDING_APPROVAL' ? 'selected' : ''}>PENDING</option>
                                    </select>
                                    <button type="submit" class="btn btn-outline" style="font-size: 0.8rem; padding: 0.25rem 0.5rem;">
                                        Save
                                    </button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
