<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="404 - Page Not Found"/>
</jsp:include>

<div class="page-container" style="text-align: center; padding: 5rem 1rem;">
    <div style="font-size: 4rem; font-weight: 800; color: var(--accent-color); margin-bottom: 0.5rem;">
        404
    </div>
    <h1 style="font-size: 2rem; margin-bottom: 1rem;">Page Not Found</h1>
    <p style="color: var(--text-muted); max-width: 480px; margin: 0 auto 2rem auto; line-height: 1.6;">
        The requested resource, concept, or page does not exist or has been relocated.
    </p>
    <a href="${pageContext.request.contextPath}/" class="btn btn-primary">
        Return to Home &rarr;
    </a>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
