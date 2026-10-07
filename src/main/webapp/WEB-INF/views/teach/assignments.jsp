<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="Assignments - Instructor"/>
</jsp:include>

<jsp:include page="/WEB-INF/views/common/messages.jsp"/>

<div class="page-container">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem;">
        <div>
            <h1 style="margin: 0 0 0.25rem 0; font-size: 2rem;">Course Assignments</h1>
            <p style="color: var(--text-muted); margin: 0; font-size: 1rem;">
                Assignments attach multidimensional rubrics. Each criterion feeds concept mastery with high weight (3.0).
            </p>
        </div>
    </div>

    <!-- Course Selector -->
    <c:if test="${not empty courses}">
        <div class="card" style="margin-bottom: 2rem; padding: 1rem 1.5rem;">
            <form action="${pageContext.request.contextPath}/teach/assignments" method="get" style="display: flex; align-items: center; gap: 1rem; flex-wrap: wrap;">
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
                <p style="color: var(--text-muted); font-size: 1.05rem;">Please create a course before configuring assignments.</p>
                <a href="${pageContext.request.contextPath}/teach/courses" class="btn btn-primary" style="margin-top: 1rem;">Create Course</a>
            </div>
        </c:when>

        <c:otherwise>
            <!-- Create Assignment Card with Dynamic Rubric Row Builder -->
            <div class="card" style="margin-bottom: 2.5rem;">
                <h3 style="font-size: 1.25rem; margin-top: 0; margin-bottom: 1.25rem;">+ Create New Assignment for <c:out value="${selectedCourse.title}"/></h3>
                <form action="${pageContext.request.contextPath}/teach/assignments" method="post">
                    <input type="hidden" name="csrfToken" value="${csrfToken}"/>
                    <input type="hidden" name="courseId" value="${selectedCourse.courseId}"/>

                    <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 1rem; margin-bottom: 1rem;">
                        <div>
                            <label for="title" style="display: block; font-weight: 600; font-size: 0.85rem; margin-bottom: 0.35rem;">Assignment Title:</label>
                            <input type="text" id="title" name="title" required placeholder="e.g. Lab 3: Graph Traversal Implementation" style="width: 100%; padding: 0.5rem; border: 1px solid var(--border-color); border-radius: 6px; background: var(--bg-card);"/>
                        </div>
                        <div>
                            <label for="dueAt" style="display: block; font-weight: 600; font-size: 0.85rem; margin-bottom: 0.35rem;">Due Date & Time:</label>
                            <input type="datetime-local" id="dueAt" name="dueAt" required style="width: 100%; padding: 0.5rem; border: 1px solid var(--border-color); border-radius: 6px; background: var(--bg-card);"/>
                        </div>
                    </div>

                    <div style="margin-bottom: 1rem;">
                        <label for="instructions" style="display: block; font-weight: 600; font-size: 0.85rem; margin-bottom: 0.35rem;">Detailed Instructions:</label>
                        <textarea id="instructions" name="instructions" rows="4" required placeholder="Write instructions for the student..." style="width: 100%; padding: 0.5rem; border: 1px solid var(--border-color); border-radius: 6px; background: var(--bg-card); font-family: inherit; font-size: 0.95rem;"></textarea>
                    </div>

                    <div style="margin-bottom: 1.5rem;">
                        <label style="display: flex; align-items: center; gap: 0.5rem; cursor: pointer; font-size: 0.9rem;">
                            <input type="checkbox" name="allowLate" value="true" checked/>
                            <span>Allow late submissions after deadline</span>
                        </label>
                    </div>

                    <!-- Rubric Criteria Config -->
                    <div style="border-top: 1px solid var(--border-color); padding-top: 1.25rem; margin-bottom: 1.5rem;">
                        <h4 style="margin: 0 0 0.5rem 0; font-size: 1.05rem;">Rubric Criteria & Concept Linkages</h4>
                        <p style="color: var(--text-muted); font-size: 0.85rem; margin-top: 0; margin-bottom: 1rem;">
                            Specify at least one criterion and associate it with a specific concept from your graph.
                        </p>

                        <div id="criteriaContainer" style="display: flex; flex-direction: column; gap: 0.75rem;">
                            <!-- Default Row 1 -->
                            <div class="rubric-row" style="display: grid; grid-template-columns: 2fr 1fr 1fr; gap: 0.75rem; align-items: center; background: var(--bg-card-subtle); padding: 0.75rem; border-radius: 6px;">
                                <div>
                                    <input type="text" name="criterionDescription" required placeholder="Criterion description (e.g. Correctness)" style="width: 100%; padding: 0.45rem; border: 1px solid var(--border-color); border-radius: 4px; background: var(--bg-card); font-size: 0.85rem;"/>
                                </div>
                                <div>
                                    <input type="number" name="criterionMaxPoints" value="10" min="1" max="100" required placeholder="Max points" style="width: 100%; padding: 0.45rem; border: 1px solid var(--border-color); border-radius: 4px; background: var(--bg-card); font-size: 0.85rem;"/>
                                </div>
                                <div>
                                    <select name="criterionConceptId" required style="width: 100%; padding: 0.45rem; border: 1px solid var(--border-color); border-radius: 4px; background: var(--bg-card); font-size: 0.85rem;">
                                        <c:forEach var="c" items="${concepts}">
                                            <option value="${c.conceptId}"><c:out value="${c.title}"/></option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                        </div>

                        <div style="margin-top: 0.75rem;">
                            <button type="button" class="btn btn-outline" style="font-size: 0.8rem; padding: 0.35rem 0.75rem;" onclick="addRubricRow()">
                                + Add Another Criterion
                            </button>
                        </div>
                    </div>

                    <button type="submit" class="btn btn-primary">
                        Create Assignment & Rubric
                    </button>
                </form>
            </div>

            <!-- Existing Assignments List -->
            <div class="card">
                <h3 style="font-size: 1.25rem; margin-top: 0; margin-bottom: 1rem;">Course Assignments (${assignments.size()})</h3>
                <c:choose>
                    <c:when test="${empty assignments}">
                        <p style="color: var(--text-muted); font-size: 0.95rem;">No assignments currently configured.</p>
                    </c:when>
                    <c:otherwise>
                        <div class="table-container">
                            <table class="data-table">
                                <thead>
                                    <tr>
                                        <th>Title</th>
                                        <th>Due Date</th>
                                        <th>Late Allowed</th>
                                        <th>Link</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="asgn" items="${assignments}">
                                        <tr>
                                            <td><strong><c:out value="${asgn.title}"/></strong></td>
                                            <td><c:out value="${asgn.dueAt}"/></td>
                                            <td><c:out value="${asgn.allowLate ? 'Yes' : 'No'}"/></td>
                                            <td>
                                                <a href="${pageContext.request.contextPath}/learn/assignment?id=${asgn.assignmentId}" class="btn btn-outline" style="font-size: 0.8rem; padding: 0.25rem 0.5rem;">
                                                    Preview Student View &rarr;
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

<script>
function addRubricRow() {
    const container = document.getElementById('criteriaContainer');
    const firstRow = container.querySelector('.rubric-row');
    if (firstRow) {
        const clone = firstRow.cloneNode(true);
        clone.querySelectorAll('input').forEach(input => {
            if (input.type === 'text') input.value = '';
            if (input.type === 'number') input.value = '10';
        });
        container.appendChild(clone);
    }
}
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
