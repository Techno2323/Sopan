<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="Quiz Editor - ${quiz.title}"/>
</jsp:include>

<jsp:include page="/WEB-INF/views/common/messages.jsp"/>

<div class="page-container">
    <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem;">
        <div>
            <div style="display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.5rem;">
                <a href="${pageContext.request.contextPath}/teach/quizzes?course=${course.courseId}" class="btn btn-outline" style="font-size: 0.8rem; padding: 0.25rem 0.5rem;">
                    &larr; Course Quizzes
                </a>
                <span class="badge" style="background: var(--bg-card-subtle); color: var(--text-muted);">
                    <c:out value="${course.code}"/>
                </span>
            </div>
            <h1 style="margin: 0 0 0.25rem 0; font-size: 1.85rem;"><c:out value="${quiz.title}"/></h1>
            <p style="color: var(--text-muted); margin: 0; font-size: 0.95rem;">
                Tag every question to exactly one concept in the DAG to measure student mastery with weight 1.0.
            </p>
        </div>

        <c:if test="${not quiz.published}">
            <form action="${pageContext.request.contextPath}/teach/quiz/edit" method="post" style="margin: 0;">
                <input type="hidden" name="csrfToken" value="${csrfToken}"/>
                <input type="hidden" name="quizId" value="${quiz.quizId}"/>
                <input type="hidden" name="action" value="publish"/>
                <button type="submit" class="btn btn-primary" ${empty questions ? 'disabled' : ''}>
                    Publish Quiz to Students
                </button>
            </form>
        </c:if>
    </div>

    <!-- Add Question Form -->
    <div class="card" style="margin-bottom: 2.5rem;">
        <h3 style="font-size: 1.25rem; margin-top: 0; margin-bottom: 1.25rem;">+ Add Concept-Tagged Question</h3>
        <form action="${pageContext.request.contextPath}/teach/quiz/edit" method="post">
            <input type="hidden" name="csrfToken" value="${csrfToken}"/>
            <input type="hidden" name="quizId" value="${quiz.quizId}"/>
            <input type="hidden" name="action" value="addQuestion"/>

            <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 1rem; margin-bottom: 1rem;">
                <div>
                    <label for="conceptId" style="display: block; font-weight: 600; font-size: 0.85rem; margin-bottom: 0.35rem;">
                        Target Concept (from DAG):
                    </label>
                    <select id="conceptId" name="conceptId" required style="width: 100%; padding: 0.5rem; border: 1px solid var(--border-color); border-radius: 6px; background: var(--bg-card);">
                        <c:forEach var="c" items="${concepts}">
                            <option value="${c.conceptId}">
                                Concept #${c.displayOrder}: <c:out value="${c.title}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div>
                    <label for="marks" style="display: block; font-weight: 600; font-size: 0.85rem; margin-bottom: 0.35rem;">Marks / Weight:</label>
                    <input type="number" id="marks" name="marks" value="1" min="1" max="100" style="width: 100%; padding: 0.5rem; border: 1px solid var(--border-color); border-radius: 6px; background: var(--bg-card);"/>
                </div>
            </div>

            <div style="margin-bottom: 1.25rem;">
                <label for="prompt" style="display: block; font-weight: 600; font-size: 0.85rem; margin-bottom: 0.35rem;">Question Prompt:</label>
                <textarea id="prompt" name="prompt" rows="3" required placeholder="Type the question text..." style="width: 100%; padding: 0.5rem; border: 1px solid var(--border-color); border-radius: 6px; background: var(--bg-card); font-family: inherit; font-size: 0.95rem;"></textarea>
            </div>

            <h4 style="font-size: 0.95rem; margin-top: 0; margin-bottom: 0.5rem;">Answer Options (select the radio button for the correct choice):</h4>
            <div style="display: flex; flex-direction: column; gap: 0.75rem; margin-bottom: 1.5rem;">
                <c:forEach var="idx" begin="0" end="3">
                    <div style="display: flex; align-items: center; gap: 0.75rem;">
                        <input type="radio" name="correctOptionIndex" value="${idx}" ${idx == 0 ? 'checked' : ''} title="Mark as correct option" style="cursor: pointer;"/>
                        <input type="text" name="optionLabel" placeholder="Option ${idx + 1} text..." style="flex: 1; padding: 0.5rem; border: 1px solid var(--border-color); border-radius: 6px; background: var(--bg-card);" ${idx < 2 ? 'required' : ''}/>
                    </div>
                </c:forEach>
            </div>

            <button type="submit" class="btn btn-primary">
                Save Question to Quiz
            </button>
        </form>
    </div>

    <!-- Current Questions List -->
    <div class="card">
        <h3 style="font-size: 1.25rem; margin-top: 0; margin-bottom: 1rem;">Existing Questions (${questions.size()})</h3>
        <c:choose>
            <c:when test="${empty questions}">
                <p style="color: var(--text-muted); font-size: 0.95rem;">No questions added yet. Use the form above to add questions.</p>
            </c:when>
            <c:otherwise>
                <div style="display: flex; flex-direction: column; gap: 1.25rem;">
                    <c:forEach var="q" items="${questions}" varStatus="status">
                        <div style="border: 1px solid var(--border-color); border-radius: 8px; padding: 1.25rem; background: var(--bg-card);">
                            <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.5rem;">
                                <div>
                                    <span style="font-size: 0.8rem; font-weight: 700; color: var(--accent-color);">
                                        Q${status.count} (${q.marks} mark)
                                    </span>
                                    <div style="font-size: 1.05rem; font-weight: 600; margin-top: 0.25rem;">
                                        <c:out value="${q.prompt}"/>
                                    </div>
                                </div>
                                <span class="badge" style="background: var(--bg-card-subtle);">
                                    Concept ID #${q.conceptId}
                                </span>
                            </div>

                            <div style="display: flex; flex-direction: column; gap: 0.35rem; margin-top: 0.75rem;">
                                <c:forEach var="opt" items="${optionsMap[q.questionId]}">
                                    <div style="font-size: 0.9rem; padding: 0.35rem 0.65rem; border-radius: 4px; ${opt.correct ? 'background: var(--bg-mastered-subtle); color: var(--accent-mastered); font-weight: 600;' : 'color: var(--text-muted);'}">
                                        &bull; <c:out value="${opt.label}"/> ${opt.correct ? '&#10003; (Correct)' : ''}
                                    </div>
                                </c:forEach>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
