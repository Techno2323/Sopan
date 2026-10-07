<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="${course.title}" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 2rem;">
    <div>
        <div style="display: flex; gap: 0.5rem; align-items: center; margin-bottom: 0.5rem;">
            <span style="font-weight: 700; color: var(--accent-saffron); font-size: 1rem;"><c:out value="${course.code}" /></span>
            <span class="role-pill"><c:out value="${course.difficulty}" /></span>
            <span class="role-pill"><c:out value="${course.category}" /></span>
        </div>
        <h1><c:out value="${course.title}" /></h1>
        <p class="subtitle" style="margin-bottom: 0;">Instructor: <strong><c:out value="${course.instructorName}" /></strong> &bull; Gate Threshold: <strong><c:out value="${course.gateThreshold}" />%</strong></p>
    </div>

    <div>
        <c:choose>
            <c:when test="${isEnrolled}">
                <a href="${pageContext.request.contextPath}/learn/course/map?id=${course.courseId}" class="btn btn-primary" style="padding: 0.75rem 1.5rem;">
                    Go to Your Concept Map →
                </a>
            </c:when>
            <c:when test="${not empty sessionScope.CURRENT_USER && sessionScope.CURRENT_USER.role == 'STUDENT'}">
                <form action="${pageContext.request.contextPath}/learn/enroll" method="post">
                    <input type="hidden" name="csrfToken" value="${csrfToken}"/>
                    <input type="hidden" name="courseId" value="${course.courseId}"/>
                    <button type="submit" class="btn btn-primary" style="padding: 0.75rem 1.5rem;">
                        Enroll in Course
                    </button>
                </form>
            </c:when>
            <c:when test="${empty sessionScope.CURRENT_USER}">
                <a href="${pageContext.request.contextPath}/login" class="btn btn-primary" style="padding: 0.75rem 1.5rem;">
                    Sign In to Enroll
                </a>
            </c:when>
        </c:choose>
    </div>
</div>

<div class="card" style="margin-bottom: 2rem;">
    <h3 style="margin-bottom: 0.5rem;">About this course</h3>
    <p style="color: var(--text-muted); line-height: 1.6;"><c:out value="${course.description}" /></p>
</div>

<!-- Concept Graph Topology (Public Preview) -->
<div class="card">
    <div class="card-header">
        <h2 class="card-title">Concept Prerequisite Topology</h2>
        <span style="font-size: 0.85rem; color: var(--text-muted);">
            Arrows point from prerequisites to dependent concepts
        </span>
    </div>

    <div class="svg-map-container">
        <svg width="${layoutResult.totalWidth}" height="${layoutResult.totalHeight}" viewBox="0 0 ${layoutResult.totalWidth} ${layoutResult.totalHeight}">
            <defs>
                <marker id="arrow" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="6" markerHeight="6" orient="auto-start-reverse">
                    <path d="M 0 1 L 10 5 L 0 9 z" fill="#A8A29E" />
                </marker>
            </defs>

            <!-- Prerequisite Edges -->
            <c:forEach var="edge" items="${layoutResult.edges}">
                <path d="${edge.pathData}" fill="none" stroke="#D6D3D1" stroke-width="2" marker-end="url(#arrow)" />
            </c:forEach>

            <!-- Concept Nodes -->
            <c:forEach var="node" items="${layoutResult.nodes}">
                <g transform="translate(${node.x}, ${node.y})">
                    <rect width="${node.width}" height="${node.height}" rx="10" ry="10"
                          fill="#FFFFFF" stroke="#E2DBD0" stroke-width="1.5" />
                    <text x="16" y="28" font-size="13" font-weight="700" fill="#1C1917">
                        <c:out value="${node.concept.title}" />
                    </text>
                    <text x="16" y="48" font-size="11" fill="#78716C">
                        Level ${node.level + 1}
                    </text>
                </g>
            </c:forEach>
        </svg>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
