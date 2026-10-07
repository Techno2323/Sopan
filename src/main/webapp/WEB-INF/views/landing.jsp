<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Concept-Graph Learning Management System" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div style="max-width: 900px; margin: 2rem auto; text-align: center;">
    <h1 style="font-size: 2.75rem; line-height: 1.15; margin-bottom: 1.25rem;">
        Courses aren't lists of modules.<br/>
        They're <span style="color: var(--accent-saffron);">graphs of concepts</span>.
    </h1>
    <p class="subtitle" style="font-size: 1.2rem; max-width: 720px; margin: 0 auto 2.5rem;">
        Every quiz answer and rubric score is evidence. Sopan computes continuous concept mastery,
        diagnoses root-cause weaknesses when you struggle, and calculates your <strong>Today's 3 Moves</strong>.
    </p>

    <div style="display: flex; gap: 1rem; justify-content: center; margin-bottom: 3.5rem;">
        <a href="${pageContext.request.contextPath}/catalog" class="btn btn-primary" style="padding: 0.8rem 1.8rem; font-size: 1.05rem;">Browse Course Catalog</a>
        <a href="${pageContext.request.contextPath}/login" class="btn btn-secondary" style="padding: 0.8rem 1.8rem; font-size: 1.05rem;">Sign In</a>
    </div>

    <div class="grid-3" style="text-align: left; margin-bottom: 3rem;">
        <div class="card">
            <h3 style="color: var(--accent-saffron); margin-bottom: 0.5rem;">Directed Concept Graph</h3>
            <p style="color: var(--text-muted); font-size: 0.95rem;">
                Prerequisite gating ensures you build solid foundations before advanced topics unlock. No more getting stuck because of unaddressed prerequisites.
            </p>
        </div>
        <div class="card">
            <h3 style="color: var(--mastery-shaky); margin-bottom: 0.5rem;">Root Cause Gap Radar</h3>
            <p style="color: var(--text-muted); font-size: 0.95rem;">
                When you're shaky on Inheritance, our graph traverser traces back to identify the deepest weak ancestor: <em>"Inheritance is shaky because of Classes."</em>
            </p>
        </div>
        <div class="card">
            <h3 style="color: var(--mastery-solid); margin-bottom: 0.5rem;">Recency-Weighted Mastery</h3>
            <p style="color: var(--text-muted); font-size: 0.95rem;">
                Evidence scores decay with a 30-day half-life. Mastered concepts require periodic refresh drills to ensure permanent knowledge retention.
            </p>
        </div>
    </div>

    <div class="card" style="text-align: left; background-color: var(--bg-surface-alt);">
        <h3 style="margin-bottom: 0.5rem;">Demo Credentials</h3>
        <p style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 1rem;">
            Pre-seeded accounts ready to test:
        </p>
        <div class="grid-3" style="font-size: 0.88rem;">
            <div>
                <strong>Student</strong><br/>
                <code>ravi.kumar@sopan.edu</code><br/>
                Password: <code>Password1!</code>
            </div>
            <div>
                <strong>Instructor</strong><br/>
                <code>anand.verma@sopan.edu</code><br/>
                Password: <code>Password1!</code>
            </div>
            <div>
                <strong>Administrator</strong><br/>
                <code>admin@sopan.edu</code><br/>
                Password: <code>Password1!</code>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
