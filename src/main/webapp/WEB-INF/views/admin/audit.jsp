<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="Audit Log Trail - Administration"/>
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
            <h1 style="margin: 0 0 0.25rem 0; font-size: 1.85rem;">System Audit Trail</h1>
            <p style="color: var(--text-muted); margin: 0; font-size: 0.95rem;">
                Immutable event stream documenting administrative and state mutation actions.
            </p>
        </div>
    </div>

    <div class="card">
        <h3 style="font-size: 1.25rem; margin-top: 0; margin-bottom: 1rem;">Recent Audit Events (${logs.size()})</h3>
        <c:choose>
            <c:when test="${empty logs}">
                <p style="color: var(--text-muted);">No audit logs recorded yet.</p>
            </c:when>
            <c:otherwise>
                <div class="table-container">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Timestamp</th>
                                <th>User ID</th>
                                <th>Action Performed</th>
                                <th>Details</th>
                                <th>IP Address</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="log" items="${logs}">
                                <tr>
                                    <td style="font-size: 0.8rem; color: var(--text-muted); white-space: nowrap;">
                                        <c:out value="${log.createdAt}"/>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${log.userId != null}">
                                                User #<c:out value="${log.userId}"/>
                                            </c:when>
                                            <c:otherwise>
                                                <span style="color: var(--text-muted);">System</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <span class="badge" style="background: var(--bg-card-subtle); font-family: monospace;">
                                            <c:out value="${log.action}"/>
                                        </span>
                                    </td>
                                    <td style="font-size: 0.85rem; max-width: 450px;">
                                        <c:out value="${log.details}"/>
                                    </td>
                                    <td style="font-size: 0.8rem; color: var(--text-muted); font-family: monospace;">
                                        <c:out value="${log.ipAddress}"/>
                                    </td>
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
