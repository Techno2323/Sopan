<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="Concept Graph Editor - ${course.title}"/>
</jsp:include>

<jsp:include page="/WEB-INF/views/common/messages.jsp"/>

<div class="page-container">
    <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem;">
        <div>
            <div style="display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.5rem;">
                <a href="${pageContext.request.contextPath}/teach/courses" class="btn btn-outline" style="font-size: 0.8rem; padding: 0.25rem 0.5rem;">
                    &larr; Courses
                </a>
                <span class="badge" style="background: var(--bg-card-subtle); color: var(--accent-color); font-weight: 600;">
                    DAG BUILDER & VALIDATOR
                </span>
            </div>
            <h1 style="margin: 0 0 0.25rem 0; font-size: 1.85rem;"><c:out value="${course.title}"/> (<c:out value="${course.code}"/>)</h1>
            <p style="color: var(--text-muted); margin: 0; font-size: 0.95rem;">
                Build your Directed Acyclic Graph of concepts. Circular dependencies are strictly rejected by Kahn's cycle detector algorithm.
            </p>
        </div>
        <div>
            <a href="${pageContext.request.contextPath}/teach/cohort?course=${course.courseId}" class="btn btn-outline">
                View Cohort Heatmap &rarr;
            </a>
        </div>
    </div>

    <!-- Visual SVG Concept DAG Canvas -->
    <div class="card" style="margin-bottom: 2.5rem; overflow: hidden; padding: 1rem;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; padding: 0 0.5rem;">
            <h3 style="margin: 0; font-size: 1.15rem;">Interactive Directed Graph Topology</h3>
            <span style="font-size: 0.8rem; color: var(--text-muted);">
                ${concepts.size()} Concepts &bull; Auto-Arranged by Longest Prerequisite Depth
            </span>
        </div>

        <c:choose>
            <c:when test="${empty concepts}">
                <div style="text-align: center; padding: 4rem 1.5rem; background: var(--bg-card-subtle); border-radius: 6px;">
                    <p style="color: var(--text-muted); margin-bottom: 1rem;">No concepts created yet for this course.</p>
                    <p style="font-size: 0.85rem; color: var(--text-muted);">Use the form below to add your first foundational concept!</p>
                </div>
            </c:when>
            <c:otherwise>
                <div style="overflow-x: auto; background: var(--bg-card-subtle); border-radius: 8px; border: 1px solid var(--border-color); padding: 1rem;">
                    <svg width="${layoutResult.totalWidth}" height="${layoutResult.totalHeight}" style="display: block; margin: 0 auto;">
                        <defs>
                            <marker id="arrowhead" markerWidth="10" markerHeight="7" refX="9" refY="3.5" orient="auto">
                                <polygon points="0 0, 10 3.5, 0 7" fill="var(--text-muted)"/>
                            </marker>
                        </defs>

                        <!-- Prerequisite Edges -->
                        <c:forEach var="edge" items="${layoutResult.edges}">
                            <path d="${edge.pathData}" fill="none" stroke="var(--border-color)" stroke-width="2.5" marker-end="url(#arrowhead)"/>
                        </c:forEach>

                        <!-- Concept Nodes -->
                        <c:forEach var="node" items="${layoutResult.nodes}">
                            <g transform="translate(${node.x}, ${node.y})">
                                <rect width="${node.width}" height="${node.height}" rx="8" ry="8"
                                      fill="var(--bg-card)" stroke="var(--border-color)" stroke-width="1.5"
                                      style="filter: drop-shadow(0 2px 4px rgba(0,0,0,0.04));"/>

                                <text x="12" y="24" font-size="10" font-weight="700" fill="var(--accent-color)" font-family="system-ui, sans-serif">
                                    CONCEPT #${node.concept.displayOrder}
                                </text>

                                <text x="12" y="44" font-size="13" font-weight="600" fill="var(--text-color)" font-family="system-ui, sans-serif">
                                    <c:choose>
                                        <c:when test="${node.concept.title.length() > 22}">
                                            <c:out value="${node.concept.title.substring(0, 20)}"/>...
                                        </c:when>
                                        <c:otherwise>
                                            <c:out value="${node.concept.title}"/>
                                        </c:otherwise>
                                    </c:choose>
                                </text>

                                <text x="12" y="62" font-size="11" fill="var(--text-muted)" font-family="system-ui, sans-serif">
                                    Prereq Depth: Level ${node.level}
                                </text>
                            </g>
                        </c:forEach>
                    </svg>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- Two Columns: Add Concept Form & Prerequisite Matrix -->
    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(360px, 1fr)); gap: 2rem;">

        <!-- Add Concept Form -->
        <div class="card">
            <h3 style="font-size: 1.25rem; margin-top: 0; margin-bottom: 1.25rem;">+ Add New Concept</h3>
            <form action="${pageContext.request.contextPath}/teach/course/graph" method="post">
                <input type="hidden" name="csrfToken" value="${csrfToken}"/>
                <input type="hidden" name="courseId" value="${course.courseId}"/>
                <input type="hidden" name="action" value="addConcept"/>

                <div style="margin-bottom: 1rem;">
                    <label for="title" style="display: block; font-weight: 600; font-size: 0.85rem; margin-bottom: 0.35rem;">Concept Title:</label>
                    <input type="text" id="title" name="title" required maxlength="128" placeholder="e.g. Recursion & Base Cases" style="width: 100%; padding: 0.5rem; border: 1px solid var(--border-color); border-radius: 6px; background: var(--bg-card);"/>
                </div>

                <div style="margin-bottom: 1rem;">
                    <label for="summary" style="display: block; font-weight: 600; font-size: 0.85rem; margin-bottom: 0.35rem;">Educational Summary:</label>
                    <textarea id="summary" name="summary" rows="3" placeholder="Briefly describe what this concept covers..." style="width: 100%; padding: 0.5rem; border: 1px solid var(--border-color); border-radius: 6px; background: var(--bg-card); font-family: inherit; font-size: 0.9rem;"></textarea>
                </div>

                <div style="margin-bottom: 1.25rem;">
                    <label for="displayOrder" style="display: block; font-weight: 600; font-size: 0.85rem; margin-bottom: 0.35rem;">Display Order (1, 2, 3...):</label>
                    <input type="number" id="displayOrder" name="displayOrder" value="${concepts.size() + 1}" min="1" style="width: 100%; padding: 0.5rem; border: 1px solid var(--border-color); border-radius: 6px; background: var(--bg-card);"/>
                </div>

                <button type="submit" class="btn btn-primary" style="width: 100%;">
                    Add Concept to Course
                </button>
            </form>
        </div>

        <!-- Prerequisite Dependency Manager -->
        <div class="card">
            <h3 style="font-size: 1.25rem; margin-top: 0; margin-bottom: 0.5rem;">Prerequisite Connections</h3>
            <p style="color: var(--text-muted); font-size: 0.85rem; margin-bottom: 1.25rem;">
                Check the prerequisite concepts required BEFORE a student can unlock each concept.
            </p>

            <c:choose>
                <c:when test="${concepts.size() < 2}">
                    <p style="color: var(--text-muted); font-size: 0.9rem;">
                        Add at least 2 concepts to define prerequisite dependencies between them.
                    </p>
                </c:when>
                <c:otherwise>
                    <form action="${pageContext.request.contextPath}/teach/course/graph" method="post">
                        <input type="hidden" name="csrfToken" value="${csrfToken}"/>
                        <input type="hidden" name="courseId" value="${course.courseId}"/>
                        <input type="hidden" name="action" value="savePrerequisites"/>

                        <div style="display: flex; flex-direction: column; gap: 1.25rem; margin-bottom: 1.5rem; max-height: 480px; overflow-y: auto; padding-right: 0.5rem;">
                            <c:forEach var="c" items="${concepts}">
                                <div style="border: 1px solid var(--border-color); border-radius: 6px; padding: 0.85rem; background: var(--bg-card);">
                                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem;">
                                        <strong><c:out value="${c.title}"/></strong>
                                        <div style="display: flex; gap: 0.5rem;">
                                            <a href="${pageContext.request.contextPath}/teach/materials?concept=${c.conceptId}" class="btn btn-outline" style="font-size: 0.75rem; padding: 0.2rem 0.5rem;">
                                                Materials
                                            </a>
                                            <button type="submit" form="del_${c.conceptId}" class="btn btn-outline" style="font-size: 0.75rem; padding: 0.2rem 0.5rem; color: var(--accent-weak); border-color: var(--accent-weak);">
                                                Delete
                                            </button>
                                        </div>
                                    </div>

                                    <div style="font-size: 0.8rem; color: var(--text-muted); margin-bottom: 0.35rem;">Requires Prerequisite(s):</div>
                                    <div style="display: flex; flex-direction: column; gap: 0.35rem;">
                                        <c:forEach var="other" items="${concepts}">
                                            <c:if test="${other.conceptId != c.conceptId}">
                                                <label style="display: flex; align-items: center; gap: 0.5rem; font-size: 0.85rem; cursor: pointer;">
                                                    <input type="checkbox" name="prereq_${c.conceptId}" value="${other.conceptId}"
                                                           ${graph.prerequisitesOf(c.conceptId).contains(other.conceptId) ? 'checked' : ''}/>
                                                    <span><c:out value="${other.title}"/></span>
                                                </label>
                                            </c:if>
                                        </c:forEach>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>

                        <button type="submit" class="btn btn-primary" style="width: 100%;">
                            Validate & Save Prerequisites
                        </button>
                    </form>

                    <!-- Hidden deletion forms to avoid nested form tags -->
                    <c:forEach var="c" items="${concepts}">
                        <form id="del_${c.conceptId}" action="${pageContext.request.contextPath}/teach/course/graph" method="post" style="display: none;" onsubmit="return confirm('Delete this concept and its relationships?');">
                            <input type="hidden" name="csrfToken" value="${csrfToken}"/>
                            <input type="hidden" name="courseId" value="${course.courseId}"/>
                            <input type="hidden" name="action" value="deleteConcept"/>
                            <input type="hidden" name="conceptId" value="${c.conceptId}"/>
                        </form>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
