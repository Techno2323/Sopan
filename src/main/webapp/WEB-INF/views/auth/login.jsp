<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Sign In" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div style="max-width: 440px; margin: 3rem auto;">
    <div class="card">
        <h2 style="margin-bottom: 0.3rem;">Welcome back</h2>
        <p class="subtitle" style="margin-bottom: 1.5rem;">Sign in to your Sopan learning account</p>

        <form action="${pageContext.request.contextPath}/login" method="post">
            <input type="hidden" name="csrfToken" value="${csrfToken}"/>

            <div class="form-group">
                <label for="email" class="form-label">Email address</label>
                <input type="email" id="email" name="email" class="form-control"
                       value="<c:out value="${param.email != null ? param.email : email}" />"
                       placeholder="you@sopan.edu" required autofocus/>
            </div>

            <div class="form-group">
                <label for="password" class="form-label">Password</label>
                <input type="password" id="password" name="password" class="form-control"
                       placeholder="••••••••" required/>
            </div>

            <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 0.5rem;">
                Sign In
            </button>
        </form>

        <div style="margin-top: 1.5rem; text-align: center; font-size: 0.9rem; color: var(--text-muted);">
            Don't have an account?
            <a href="${pageContext.request.contextPath}/register">Create one here</a>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
