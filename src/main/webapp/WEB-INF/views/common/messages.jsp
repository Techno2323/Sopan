<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:if test="${not empty flashSuccess}">
    <div class="alert alert-success" role="alert">
        <span>✓</span>
        <div><c:out value="${flashSuccess}" /></div>
    </div>
</c:if>

<c:if test="${not empty flashError}">
    <div class="alert alert-error" role="alert">
        <span>⚠</span>
        <div><c:out value="${flashError}" /></div>
    </div>
</c:if>

<c:if test="${not empty flashInfo}">
    <div class="alert alert-info" role="alert">
        <span>ℹ</span>
        <div><c:out value="${flashInfo}" /></div>
    </div>
</c:if>
