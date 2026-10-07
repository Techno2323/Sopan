<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="${quiz.title} - Quiz"/>
</jsp:include>

<jsp:include page="/WEB-INF/views/common/messages.jsp"/>

<div class="page-container" style="max-width: 800px;">
    <!-- Quiz Header -->
    <div class="card" style="margin-bottom: 2rem;">
        <span class="badge" style="background: var(--bg-card-subtle); color: var(--accent-color); font-weight: 600; margin-bottom: 0.5rem; display: inline-block;">
            ASSESSMENT (WEIGHT 1.0)
        </span>
        <h1 style="margin: 0.25rem 0 0.5rem 0; font-size: 1.85rem;"><c:out value="${quiz.title}"/></h1>
        <p style="color: var(--text-muted); margin: 0; font-size: 0.95rem;">
            Answer each question below. Each question produces concept-tagged evidence that immediately updates your mastery scores.
        </p>
    </div>

    <!-- Quiz Form -->
    <form action="${pageContext.request.contextPath}/learn/quiz" method="post" id="quizForm">
        <input type="hidden" name="csrfToken" value="${csrfToken}"/>
        <input type="hidden" name="quizId" value="${quiz.quizId}"/>

        <div style="display: flex; flex-direction: column; gap: 1.5rem; margin-bottom: 2rem;">
            <c:forEach var="q" items="${questions}" varStatus="status">
                <div class="card" style="padding: 1.5rem;">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; border-bottom: 1px solid var(--border-color); padding-bottom: 0.5rem;">
                        <span style="font-weight: 700; color: var(--accent-color); font-size: 0.9rem;">
                            Question ${status.count} of ${questions.size()}
                        </span>
                        <span style="font-size: 0.8rem; color: var(--text-muted);">
                            <c:out value="${q.marks}"/> <c:out value="${q.marks == 1 ? 'mark' : 'marks'}"/>
                        </span>
                    </div>

                    <p style="font-size: 1.05rem; font-weight: 500; line-height: 1.5; margin: 0 0 1.25rem 0;">
                        <c:out value="${q.prompt}"/>
                    </p>

                    <!-- Options -->
                    <div style="display: flex; flex-direction: column; gap: 0.75rem;">
                        <c:forEach var="opt" items="${optionsMap[q.questionId]}">
                            <label style="display: flex; align-items: flex-start; gap: 0.75rem; padding: 0.75rem 1rem; border: 1px solid var(--border-color); border-radius: 6px; cursor: pointer; transition: background 0.15s, border-color 0.15s; background: var(--bg-card);">
                                <input type="radio" name="q_${q.questionId}" value="${opt.optionId}" required style="margin-top: 0.2rem; cursor: pointer;"/>
                                <span style="font-size: 0.95rem; line-height: 1.4;"><c:out value="${opt.label}"/></span>
                            </label>
                        </c:forEach>
                    </div>
                </div>
            </c:forEach>
        </div>

        <div class="card" style="display: flex; justify-content: space-between; align-items: center; padding: 1rem 1.5rem;">
            <a href="${pageContext.request.contextPath}/learn/home" class="btn btn-outline">
                Cancel
            </a>
            <button type="submit" class="btn btn-primary" style="font-size: 1rem; padding: 0.6rem 1.75rem;">
                Submit Quiz Answers
            </button>
        </div>
    </form>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
