<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${pageTitle != null ? pageTitle : 'Sopan LMS'}" /> — Concept-Graph Learning</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
</head>
<body>
<header class="site-header">
    <div class="header-container">
        <a href="${pageContext.request.contextPath}/" class="brand">
            <span class="brand-dot"></span>
            <span>Sopan</span>
        </a>

        <nav>
            <ul class="nav-links">
                <li><a href="${pageContext.request.contextPath}/catalog" class="nav-link">Catalog</a></li>

                <c:if test="${empty sessionScope.CURRENT_USER}">
                    <li><a href="${pageContext.request.contextPath}/login" class="nav-link">Log In</a></li>
                    <li><a href="${pageContext.request.contextPath}/register" class="btn btn-primary btn-sm">Sign Up</a></li>
                </c:if>

                <c:if test="${not empty sessionScope.CURRENT_USER}">
                    <c:choose>
                        <c:when test="${sessionScope.CURRENT_USER.role == 'STUDENT'}">
                            <li><a href="${pageContext.request.contextPath}/learn/home" class="nav-link">Today's Moves</a></li>
                            <li><a href="${pageContext.request.contextPath}/learn/courses" class="nav-link">My Courses</a></li>
                            <li><a href="${pageContext.request.contextPath}/learn/gaps" class="nav-link">Gap Radar</a></li>
                            <li><a href="${pageContext.request.contextPath}/learn/notifications" class="nav-link">Notifications</a></li>
                        </c:when>
                        <c:when test="${sessionScope.CURRENT_USER.role == 'INSTRUCTOR'}">
                            <li><a href="${pageContext.request.contextPath}/teach/home" class="nav-link">Dashboard</a></li>
                            <li><a href="${pageContext.request.contextPath}/teach/courses" class="nav-link">My Courses</a></li>
                            <li><a href="${pageContext.request.contextPath}/teach/quizzes" class="nav-link">Quizzes</a></li>
                            <li><a href="${pageContext.request.contextPath}/teach/assignments" class="nav-link">Assignments</a></li>
                            <li><a href="${pageContext.request.contextPath}/teach/cohort" class="nav-link">Cohort Heatmap</a></li>
                        </c:when>
                        <c:when test="${sessionScope.CURRENT_USER.role == 'ADMIN'}">
                            <li><a href="${pageContext.request.contextPath}/admin/home" class="nav-link">Overview</a></li>
                            <li><a href="${pageContext.request.contextPath}/admin/courses" class="nav-link">Course Approvals</a></li>
                            <li><a href="${pageContext.request.contextPath}/admin/users" class="nav-link">User Directory</a></li>
                            <li><a href="${pageContext.request.contextPath}/admin/audit" class="nav-link">Audit Log</a></li>
                        </c:when>
                    </c:choose>

                    <li class="user-badge">
                        <span><c:out value="${sessionScope.CURRENT_USER.fullName}" /></span>
                        <span class="role-pill"><c:out value="${sessionScope.CURRENT_USER.role}" /></span>
                    </li>
                    <li><a href="${pageContext.request.contextPath}/logout" class="nav-link">Log Out</a></li>
                </c:if>
            </ul>
        </nav>
    </div>
</header>
<main class="main-content">
<jsp:include page="/WEB-INF/views/common/messages.jsp" />
