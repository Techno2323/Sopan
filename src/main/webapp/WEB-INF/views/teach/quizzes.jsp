<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="Quizzes - Instructor"/>
</jsp:include>

<jsp:include page="/WEB-INF/views/common/messages.jsp"/>

<div class="page-container">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem;">
        <div>
            <h1 style="margin: 0 0 0.25rem 0; font-size: 2rem;">Course Quizzes</h1>
            <p style="color: var(--text-muted); margin: 0; font-size: 1rem;">
                Quizzes evaluate student understanding with weight 1.0 evidence per concept-tagged question.
            </p>
        </div>
    </div>

    <!-- Course Selector -->
    <c:if test="${not empty courses}">
        <div class="card" style="margin-bottom: 2rem; padding: 1rem 1.5rem;">
            <form action="${pageContext.request.contextPath}/teach/quizzes" method="get" style="display: flex; align-items: center; gap: 1rem; flex-wrap: wrap;">
                <label for="courseSelect" style="font-weight: 600; font-size: 0.9rem;">Course:</label>
                <select id="courseSelect" name="course" onchange="this.form.submit()" style="padding: 0.5rem 1rem; border-radius: 6px; border: 1px solid var(--border-color); background: var(--bg-card); font-size: 0.9rem; min-width: 250px;">
                    <c:forEach var="c" items="${courses}">
                        <option value="${c.courseId}" ${c.courseId == selectedCourse.courseId ? 'selected' : ''}>
                            <c:out value="${c.code}"/> - <c:out value="${c.title}"/>
                        </option>
                    </c:forEach>
                </select>
                <noscript><button type="submit" class="btn btn-outline" style="font-size: 0.85rem;">Select</button></noscript>
            </form>
        </div>
    </c:if>

    <c:choose>
        <c:when test="${empty courses}">
            <div class="card" style="text-align: center; padding: 3rem 1.5rem;">
                <p style="color: var(--text-muted); font-size: 1.05rem;">Please create a course before configuring quizzes.</p>
                <a href="${pageContext.request.contextPath}/teach/courses" class="btn btn-primary" style="margin-top: 1rem;">Create Course</a>
            </div>
        </c:when>

        <c:otherwise>
            <!-- Create Quiz Card -->
            <div class="card" style="margin-bottom: 2.5rem;">
                <h3 style="font-size: 1.25rem; margin-top: 0; margin-bottom: 1.25rem;">+ Create New Quiz for <c:out value="${selectedCourse.title}"/></h3>
                <form action="${pageContext.request.contextPath}/teach/quizzes" method="post">
                    <input type="hidden" name="csrfToken" value="${csrfToken}"/>
                    <input type="hidden" name="courseId" value="${selectedCourse.courseId}"/>
                    <input type="hidden" name="action" value="create"/>

                    <div style="display: grid; grid-template-columns: 3fr 1fr; gap: 1rem; margin-bottom: 1.25rem;">
                        <div>
                            <label for="title" style="display: block; font-weight: 600; font-size: 0.85rem; margin-bottom: 0.35rem;">Quiz Title:</label>
                            <input type="text" id="title" name="title" required placeholder="e.g. Unit 1 Checkpoint: Arrays & Search" style="width: 100%; padding: 0.5rem; border: 1px solid var(--border-color); border-radius: 6px; background: var(--bg-card);"/>
                        </div>
                        <div>
                            <label for="maxAttempts" style="display: block; font-weight: 600; font-size: 0.85rem; margin-bottom: 0.35rem;">Max Attempts:</label>
                            <input type="number" id="maxAttempts" name="maxAttempts" value="3" min="1" max="10" style="width: 100%; padding: 0.5rem; border: 1px solid var(--border-color); border-radius: 6px; background: var(--bg-card);"/>
                        </div>
                    </div>

                    <button type="submit" class="btn btn-primary">
                        Create Quiz & Add Questions &rarr;
                    </button>
                </form>
            </div>

            <!-- Existing Quizzes -->
            <div class="card">
                <h3 style="font-size: 1.25rem; margin-top: 0; margin-bottom: 1rem;">Course Quizzes (${quizzes.size()})</h3>
                <c:choose>
                    <c:when test="${empty quizzes}">
                        <p style="color: var(--text-muted); font-size: 0.95rem;">No quizzes created yet for this course.</p>
                    </c:when>
                    <c:otherwise>
                        <div class="table-container">
                            <table class="data-table">
                                <thead>
                                    <tr>
                                        <th>Title</th>
                                        <th>Status</th>
                                        <th>Max Attempts</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="q" items="${quizzes}">
                                        <tr>
                                            <td><strong><c:out value="${q.title}"/></strong></td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${q.published}">
                                                        <span class="badge badge-mastered">Published</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge badge-unlocked">Draft (Unpublished)</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td><c:out value="${q.maxAttempts}"/></td>
                                            <td>
                                                <a href="${pageContext.request.contextPath}/teach/quiz/edit?id=${q.quizId}" class="btn btn-outline" style="font-size: 0.8rem; padding: 0.3rem 0.65rem;">
                                                    Edit Questions & Rubric &rarr;
                                                </a>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
