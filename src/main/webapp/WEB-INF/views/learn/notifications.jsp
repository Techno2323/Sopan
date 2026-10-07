<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="Notifications"/>
</jsp:include>

<jsp:include page="/WEB-INF/views/common/messages.jsp"/>

<div class="page-container" style="max-width: 800px;">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem;">
        <div>
            <h1 style="margin: 0 0 0.25rem 0; font-size: 1.85rem;">Notifications</h1>
            <p style="color: var(--text-muted); margin: 0; font-size: 0.95rem;">
                Stay updated on newly unlocked concepts, assignment feedback, and deadlines.
            </p>
        </div>
        <c:if test="${not empty notifications}">
            <form action="${pageContext.request.contextPath}/learn/notifications" method="post">
                <input type="hidden" name="csrfToken" value="${csrfToken}"/>
                <input type="hidden" name="action" value="markAll"/>
                <button type="submit" class="btn btn-outline" style="font-size: 0.85rem;">
                    Mark All as Read
                </button>
            </form>
        </c:if>
    </div>

    <c:choose>
        <c:when test="${empty notifications}">
            <div class="card" style="text-align: center; padding: 3rem 1.5rem;">
                <p style="color: var(--text-muted); font-size: 1.05rem; margin: 0;">You have no notifications at this time.</p>
            </div>
        </c:when>

        <c:otherwise>
            <div style="display: flex; flex-direction: column; gap: 0.75rem;">
                <c:forEach var="n" items="${notifications}">
                    <div class="card" style="padding: 1rem 1.25rem; display: flex; justify-content: space-between; align-items: center; gap: 1rem; ${n.read ? 'opacity: 0.75; background: var(--bg-card-subtle);' : 'border-left: 4px solid var(--accent-color);'}">
                        <div>
                            <div style="font-size: 0.95rem; font-weight: ${n.read ? 'normal' : '600'}; margin-bottom: 0.25rem;">
                                <c:out value="${n.message}"/>
                            </div>
                            <span style="font-size: 0.75rem; color: var(--text-muted);">
                                <c:out value="${n.createdAt}"/>
                            </span>
                        </div>

                        <div style="display: flex; align-items: center; gap: 0.75rem; white-space: nowrap;">
                            <c:if test="${not empty n.link}">
                                <a href="<c:out value='${n.link}'/>" class="btn btn-outline" style="font-size: 0.8rem; padding: 0.3rem 0.65rem;">
                                    View &rarr;
                                </a>
                            </c:if>
                            <c:if test="${not n.read}">
                                <form action="${pageContext.request.contextPath}/learn/notifications" method="post" style="margin: 0;">
                                    <input type="hidden" name="csrfToken" value="${csrfToken}"/>
                                    <input type="hidden" name="id" value="${n.notificationId}"/>
                                    <button type="submit" class="btn btn-outline" style="font-size: 0.8rem; padding: 0.3rem 0.65rem;">
                                        Mark Read
                                    </button>
                                </form>
                            </c:if>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
