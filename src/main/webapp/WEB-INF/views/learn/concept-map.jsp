<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="${course.title} — Concept Map" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1.5rem; flex-wrap: wrap; gap: 1rem;">
    <div>
        <div style="display: flex; gap: 0.5rem; align-items: center; margin-bottom: 0.4rem;">
            <span style="font-weight: 700; color: var(--accent-saffron);"><c:out value="${course.code}" /></span>
            <span class="role-pill">Gate Threshold: <c:out value="${course.gateThreshold}" />%</span>
        </div>
        <h1><c:out value="${course.title}" /> — Concept Map</h1>
        <p class="subtitle" style="margin-bottom: 0;">Interactive prerequisite graph coloured by your personalized mastery</p>
    </div>

    <div style="display: flex; gap: 0.75rem;">
        <a href="${pageContext.request.contextPath}/learn/gaps?course=${course.courseId}" class="btn btn-secondary">
            Gap Radar →
        </a>
    </div>
</div>

<!-- Mastery Legend -->
<div class="card" style="padding: 1rem 1.25rem; margin-bottom: 1.5rem;">
    <div style="display: flex; gap: 1.5rem; align-items: center; flex-wrap: wrap; font-size: 0.88rem;">
        <span style="font-weight: 700; color: var(--text-muted); text-transform: uppercase;">Legend:</span>
        <div style="display: flex; align-items: center; gap: 0.4rem;">
            <span class="mastery-badge badge-UNSEEN">UNSEEN</span>
            <span style="color: var(--text-subtle);">Hollow / 0 bars</span>
        </div>
        <div style="display: flex; align-items: center; gap: 0.4rem;">
            <span class="mastery-badge badge-SHAKY">
                <span class="bars bars-1"><span></span><span></span><span></span></span> SHAKY
            </span>
            <span style="color: var(--text-subtle);">&lt; 50% / 1 bar</span>
        </div>
        <div style="display: flex; align-items: center; gap: 0.4rem;">
            <span class="mastery-badge badge-DEVELOPING">
                <span class="bars bars-2"><span></span><span></span><span></span></span> DEVELOPING
            </span>
            <span style="color: var(--text-subtle);">50–74% / 2 bars</span>
        </div>
        <div style="display: flex; align-items: center; gap: 0.4rem;">
            <span class="mastery-badge badge-SOLID">
                <span class="bars bars-3"><span></span><span></span><span></span></span> SOLID
            </span>
            <span style="color: var(--text-subtle);">75%+ / 3 bars</span>
        </div>
        <div style="display: flex; align-items: center; gap: 0.4rem;">
            <span style="font-size: 1rem;">🔒</span>
            <span style="color: var(--text-subtle);">Gated (Prereq &lt; <c:out value="${course.gateThreshold}" />%)</span>
        </div>
    </div>
</div>

<!-- Inline Responsive SVG Concept Map -->
<div class="svg-map-container">
    <svg width="${layoutResult.totalWidth}" height="${layoutResult.totalHeight}" viewBox="0 0 ${layoutResult.totalWidth} ${layoutResult.totalHeight}">
        <defs>
            <marker id="map-arrow" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="6" markerHeight="6" orient="auto-start-reverse">
                <path d="M 0 1 L 10 5 L 0 9 z" fill="#A8A29E" />
            </marker>
        </defs>

        <!-- Connecting Prerequisite Edges -->
        <c:forEach var="edge" items="${layoutResult.edges}">
            <path d="${edge.pathData}" fill="none" stroke="#D6D3D1" stroke-width="2.5" marker-end="url(#map-arrow)" />
        </c:forEach>

        <!-- Concept Nodes -->
        <c:forEach var="node" items="${layoutResult.nodes}">
            <c:set var="status" value="${node.status}" />
            <c:set var="isLocked" value="${status != null && status.locked}" />
            <c:set var="level" value="${status != null ? status.level : 'UNSEEN'}" />

            <!-- Dynamic Color & Stroke per Mastery Level -->
            <c:choose>
                <c:when test="${isLocked}">
                    <c:set var="nodeFill" value="#F5F5F4"/>
                    <c:set var="nodeStroke" value="#D6D3D1"/>
                    <c:set var="strokeDash" value="none"/>
                    <c:set var="badgeColor" value="#78716C"/>
                </c:when>
                <c:when test="${level == 'SOLID'}">
                    <c:set var="nodeFill" value="#CCFBF1"/>
                    <c:set var="nodeStroke" value="#0D9488"/>
                    <c:set var="strokeDash" value="none"/>
                    <c:set var="badgeColor" value="#0F766E"/>
                </c:when>
                <c:when test="${level == 'DEVELOPING'}">
                    <c:set var="nodeFill" value="#FEF3C7"/>
                    <c:set var="nodeStroke" value="#D97706"/>
                    <c:set var="strokeDash" value="none"/>
                    <c:set var="badgeColor" value="#B45309"/>
                </c:when>
                <c:when test="${level == 'SHAKY'}">
                    <c:set var="nodeFill" value="#FFE4E6"/>
                    <c:set var="nodeStroke" value="#E11D48"/>
                    <c:set var="strokeDash" value="none"/>
                    <c:set var="badgeColor" value="#BE123C"/>
                </c:when>
                <c:when test="${level == 'EARLY'}">
                    <c:set var="nodeFill" value="#F1F5F9"/>
                    <c:set var="nodeStroke" value="#64748B"/>
                    <c:set var="strokeDash" value="none"/>
                    <c:set var="badgeColor" value="#475569"/>
                </c:when>
                <c:otherwise>
                    <!-- UNSEEN: Grey hollow with dashed border -->
                    <c:set var="nodeFill" value="#FFFFFF"/>
                    <c:set var="nodeStroke" value="#78716C"/>
                    <c:set var="strokeDash" value="5,4"/>
                    <c:set var="badgeColor" value="#78716C"/>
                </c:otherwise>
            </c:choose>

            <a href="${pageContext.request.contextPath}/learn/concept?id=${node.conceptId}" style="text-decoration: none;">
                <g transform="translate(${node.x}, ${node.y})" style="cursor: pointer;">
                    <!-- Node Background -->
                    <rect width="${node.width}" height="${node.height}" rx="12" ry="12"
                          fill="${nodeFill}" stroke="${nodeStroke}" stroke-width="2" stroke-dasharray="${strokeDash}" />

                    <!-- Concept Title -->
                    <text x="14" y="26" font-size="13" font-weight="700" fill="#1C1917">
                        <c:out value="${node.concept.title}" />
                    </text>

                    <!-- Lock Icon or Mastery Percentage -->
                    <c:choose>
                        <c:when test="${isLocked}">
                            <text x="14" y="52" font-size="12" fill="#78716C">
                                🔒 Locked
                            </text>
                            <text x="${node.width - 16}" y="52" text-anchor="end" font-size="10" fill="#78716C">
                                Prereq &lt; ${course.gateThreshold}%
                            </text>
                        </c:when>
                        <c:otherwise>
                            <!-- Mastery Status & Bars -->
                            <text x="14" y="52" font-size="12" font-weight="700" fill="${badgeColor}">
                                <c:choose>
                                    <c:when test="${status != null && status.mastery != null}">
                                        ${String.format('%.1f%%', status.mastery)}
                                    </c:when>
                                    <c:otherwise>
                                        Unseen
                                    </c:otherwise>
                                </c:choose>
                            </text>

                            <!-- Bar Shapes Visualizer in SVG -->
                            <g transform="translate(${node.width - 38}, 42)">
                                <c:choose>
                                    <c:when test="${level == 'SOLID'}">
                                        <rect x="0" y="0" width="4" height="12" rx="1" fill="${badgeColor}"/>
                                        <rect x="7" y="0" width="4" height="12" rx="1" fill="${badgeColor}"/>
                                        <rect x="14" y="0" width="4" height="12" rx="1" fill="${badgeColor}"/>
                                    </c:when>
                                    <c:when test="${level == 'DEVELOPING'}">
                                        <rect x="0" y="0" width="4" height="12" rx="1" fill="${badgeColor}"/>
                                        <rect x="7" y="0" width="4" height="12" rx="1" fill="${badgeColor}"/>
                                        <rect x="14" y="6" width="4" height="6" rx="1" fill="${badgeColor}" opacity="0.3"/>
                                    </c:when>
                                    <c:when test="${level == 'SHAKY'}">
                                        <rect x="0" y="0" width="4" height="12" rx="1" fill="${badgeColor}"/>
                                        <rect x="7" y="6" width="4" height="6" rx="1" fill="${badgeColor}" opacity="0.3"/>
                                        <rect x="14" y="6" width="4" height="6" rx="1" fill="${badgeColor}" opacity="0.3"/>
                                    </c:when>
                                </c:choose>
                            </g>
                        </c:otherwise>
                    </c:choose>
                </g>
            </a>
        </c:forEach>
    </svg>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
