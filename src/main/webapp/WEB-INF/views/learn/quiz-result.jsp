<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="Quiz Results - ${result.quizTitle}"/>
</jsp:include>

<jsp:include page="/WEB-INF/views/common/messages.jsp"/>

<div class="page-container" style="max-width: 850px;">
    <!-- Results Summary Card -->
    <div class="card" style="margin-bottom: 2rem; text-align: center; padding: 2.5rem 1.5rem;">
        <span class="badge" style="background: var(--bg-card-subtle); color: var(--accent-color); font-weight: 600; margin-bottom: 0.5rem; display: inline-block;">
            ATTEMPT COMPLETED
        </span>
        <h1 style="margin: 0.25rem 0 1rem 0; font-size: 2rem;"><c:out value="${result.quizTitle}"/></h1>

        <div style="display: flex; justify-content: center; align-items: baseline; gap: 0.5rem; margin-bottom: 0.75rem;">
            <span style="font-size: 3.5rem; font-weight: 800; color: var(--text-color);">
                <fmt:formatNumber value="${result.percentage}" maxFractionDigits="1"/>%
            </span>
            <span style="font-size: 1.25rem; color: var(--text-muted);">
                (${result.score} / ${result.maxScore} marks)
            </span>
        </div>

        <p style="color: var(--text-muted); margin: 0 auto 1.5rem auto; max-width: 500px; font-size: 0.95rem;">
            Evidence from this attempt has been processed with recency weighting into your knowledge model.
        </p>

        <div style="display: flex; justify-content: center; gap: 1rem;">
            <a href="${pageContext.request.contextPath}/learn/home" class="btn btn-primary">
                Return to Dashboard
            </a>
            <a href="${pageContext.request.contextPath}/learn/gaps" class="btn btn-outline">
                Check Gap Radar &rarr;
            </a>
        </div>
    </div>

    <!-- Per-Question & Per-Concept Breakdown -->
    <h2 style="font-size: 1.35rem; margin-bottom: 1rem;">Question & Concept Mastery Breakdown</h2>
    <div style="display: flex; flex-direction: column; gap: 1.25rem; margin-bottom: 2.5rem;">
        <c:forEach var="qf" items="${result.questions}" varStatus="status">
            <div class="card" style="border-left: 4px solid ${qf.correct ? 'var(--accent-mastered)' : 'var(--accent-weak)'};">
                <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 1rem; margin-bottom: 0.75rem;">
                    <div>
                        <span style="font-size: 0.8rem; font-weight: 700; color: var(--text-muted); text-transform: uppercase;">
                            Question ${status.count}
                        </span>
                        <div style="font-size: 1.05rem; font-weight: 600; margin-top: 0.25rem;">
                            <c:out value="${qf.prompt}"/>
                        </div>
                    </div>
                    <span class="badge ${qf.correct ? 'badge-mastered' : 'badge-weak'}" style="font-size: 0.85rem; padding: 0.35rem 0.75rem;">
                        ${qf.correct ? 'Correct' : 'Incorrect'}
                    </span>
                </div>

                <div style="font-size: 0.9rem; margin-bottom: 1rem; background: var(--bg-card-subtle); padding: 0.75rem 1rem; border-radius: 6px;">
                    <div style="margin-bottom: 0.25rem;">
                        <strong>Your Answer:</strong> <c:out value="${qf.selectedOption}"/>
                    </div>
                    <c:if test="${not qf.correct}">
                        <div style="color: var(--accent-mastered);">
                            <strong>Correct Answer:</strong> <c:out value="${qf.correctOption}"/>
                        </div>
                    </c:if>
                </div>

                <!-- Updated Concept Mastery Pill -->
                <div style="display: flex; justify-content: space-between; align-items: center; border-top: 1px solid var(--border-color); padding-top: 0.75rem;">
                    <div>
                        <span style="color: var(--text-muted); font-size: 0.8rem;">Evaluated Concept:</span>
                        <a href="${pageContext.request.contextPath}/learn/concept?id=${qf.conceptId}" style="font-weight: 600; color: var(--accent-color); text-decoration: none; font-size: 0.9rem; margin-left: 0.25rem;">
                            <c:out value="${qf.conceptTitle}"/> &rarr;
                        </a>
                    </div>
                    <div style="display: flex; align-items: center; gap: 0.5rem;">
                        <span style="font-size: 0.8rem; color: var(--text-muted);">New Concept Mastery:</span>
                        <span class="badge ${qf.masteryLevel == 'MASTERED' ? 'badge-mastered' : (qf.masteryLevel == 'PRACTICING' ? 'badge-practicing' : 'badge-weak')}">
                            <fmt:formatNumber value="${qf.updatedMastery}" maxFractionDigits="1"/>% (${qf.masteryLevel})
                        </span>
                    </div>
                </div>
            </div>
        </c:forEach>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
