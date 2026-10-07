<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="Course Management"/>
</jsp:include>

<jsp:include page="/WEB-INF/views/common/messages.jsp"/>

<div class="page-container">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem;">
        <div>
            <h1 style="margin: 0 0 0.25rem 0; font-size: 2rem;">Course Management</h1>
            <p style="color: var(--text-muted); margin: 0; font-size: 1rem;">
                Create new courses, manage drafting, and submit to administration for catalog approval.
            </p>
        </div>
    </div>

    <!-- Create Course Form -->
    <div class="card" style="margin-bottom: 2.5rem;">
        <h3 style="font-size: 1.25rem; margin-top: 0; margin-bottom: 1.25rem;">Create a New Course</h3>
        <form action="${pageContext.request.contextPath}/teach/courses" method="post">
            <input type="hidden" name="csrfToken" value="${csrfToken}"/>
            <input type="hidden" name="action" value="create"/>

            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1rem; margin-bottom: 1rem;">
                <div>
                    <label for="code" style="display: block; font-weight: 600; font-size: 0.85rem; margin-bottom: 0.35rem;">Course Code (e.g. CS101):</label>
                    <input type="text" id="code" name="code" required maxlength="16" style="width: 100%; padding: 0.5rem; border: 1px solid var(--border-color); border-radius: 6px; background: var(--bg-card);"/>
                </div>
                <div>
                    <label for="title" style="display: block; font-weight: 600; font-size: 0.85rem; margin-bottom: 0.35rem;">Course Title:</label>
                    <input type="text" id="title" name="title" required maxlength="128" style="width: 100%; padding: 0.5rem; border: 1px solid var(--border-color); border-radius: 6px; background: var(--bg-card);"/>
                </div>
                <div>
                    <label for="category" style="display: block; font-weight: 600; font-size: 0.85rem; margin-bottom: 0.35rem;">Category:</label>
                    <input type="text" id="category" name="category" placeholder="e.g. Computer Science" style="width: 100%; padding: 0.5rem; border: 1px solid var(--border-color); border-radius: 6px; background: var(--bg-card);"/>
                </div>
            </div>

            <div style="margin-bottom: 1rem;">
                <label for="description" style="display: block; font-weight: 600; font-size: 0.85rem; margin-bottom: 0.35rem;">Course Description:</label>
                <textarea id="description" name="description" rows="3" style="width: 100%; padding: 0.5rem; border: 1px solid var(--border-color); border-radius: 6px; background: var(--bg-card); font-family: inherit; font-size: 0.9rem;"></textarea>
            </div>

            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem; margin-bottom: 1.5rem;">
                <div>
                    <label for="difficulty" style="display: block; font-weight: 600; font-size: 0.85rem; margin-bottom: 0.35rem;">Difficulty:</label>
                    <select id="difficulty" name="difficulty" style="width: 100%; padding: 0.5rem; border: 1px solid var(--border-color); border-radius: 6px; background: var(--bg-card);">
                        <option value="BEGINNER">BEGINNER</option>
                        <option value="INTERMEDIATE">INTERMEDIATE</option>
                        <option value="ADVANCED">ADVANCED</option>
                    </select>
                </div>
                <div>
                    <label for="maxStudents" style="display: block; font-weight: 600; font-size: 0.85rem; margin-bottom: 0.35rem;">Max Students Capacity:</label>
                    <input type="number" id="maxStudents" name="maxStudents" value="50" min="1" max="1000" style="width: 100%; padding: 0.5rem; border: 1px solid var(--border-color); border-radius: 6px; background: var(--bg-card);"/>
                </div>
                <div>
                    <label for="gateThreshold" style="display: block; font-weight: 600; font-size: 0.85rem; margin-bottom: 0.35rem;">Prerequisite Gate (%):</label>
                    <input type="number" id="gateThreshold" name="gateThreshold" value="60" min="1" max="100" style="width: 100%; padding: 0.5rem; border: 1px solid var(--border-color); border-radius: 6px; background: var(--bg-card);"/>
                </div>
            </div>

            <button type="submit" class="btn btn-primary" style="font-size: 0.95rem; padding: 0.55rem 1.5rem;">
                Create Course & Continue to Graph Editor
            </button>
        </form>
    </div>

    <!-- Instructor Course List -->
    <div class="card">
        <h3 style="font-size: 1.25rem; margin-top: 0; margin-bottom: 1rem;">Managed Courses</h3>
        <c:choose>
            <c:when test="${empty courses}">
                <p style="color: var(--text-muted); font-size: 0.95rem;">You do not have any courses created.</p>
            </c:when>
            <c:otherwise>
                <div class="table-container">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Code</th>
                                <th>Title</th>
                                <th>Status</th>
                                <th>Difficulty</th>
                                <th>Gate</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="c" items="${courses}">
                                <tr>
                                    <td><strong><c:out value="${c.code}"/></strong></td>
                                    <td><c:out value="${c.title}"/></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${c.status == 'APPROVED'}">
                                                <span class="badge badge-mastered">Approved</span>
                                            </c:when>
                                            <c:when test="${c.status == 'PENDING_APPROVAL'}">
                                                <span class="badge badge-practicing">Pending Approval</span>
                                            </c:when>
                                            <c:when test="${c.status == 'DRAFT'}">
                                                <span class="badge badge-unlocked">Draft</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge"><c:out value="${c.status}"/></span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td><c:out value="${c.difficulty}"/></td>
                                    <td><c:out value="${c.gateThreshold}"/>%</td>
                                    <td>
                                        <div style="display: flex; gap: 0.5rem; align-items: center; flex-wrap: wrap;">
                                            <a href="${pageContext.request.contextPath}/teach/course/graph?id=${c.courseId}" class="btn btn-outline" style="font-size: 0.8rem; padding: 0.3rem 0.65rem;">
                                                Graph Editor
                                            </a>
                                            <c:if test="${c.status == 'DRAFT'}">
                                                <form action="${pageContext.request.contextPath}/teach/courses" method="post" style="margin: 0; display: inline;">
                                                    <input type="hidden" name="csrfToken" value="${csrfToken}"/>
                                                    <input type="hidden" name="action" value="submitApproval"/>
                                                    <input type="hidden" name="courseId" value="${c.courseId}"/>
                                                    <button type="submit" class="btn btn-primary" style="font-size: 0.8rem; padding: 0.3rem 0.65rem;">
                                                        Submit for Approval
                                                    </button>
                                                </form>
                                            </c:if>
                                            <form action="${pageContext.request.contextPath}/teach/courses" method="post" style="margin: 0; display: inline;" onsubmit="return confirm('Are you sure you want to delete/archive this course?');">
                                                <input type="hidden" name="csrfToken" value="${csrfToken}"/>
                                                <input type="hidden" name="action" value="delete"/>
                                                <input type="hidden" name="courseId" value="${c.courseId}"/>
                                                <button type="submit" class="btn btn-outline" style="font-size: 0.8rem; padding: 0.3rem 0.65rem; color: var(--accent-weak); border-color: var(--accent-weak);">
                                                    Delete
                                                </button>
                                            </form>
                                        </div>
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
