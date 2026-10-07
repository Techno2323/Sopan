<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="Materials - ${concept.title}"/>
</jsp:include>

<jsp:include page="/WEB-INF/views/common/messages.jsp"/>

<div class="page-container" style="max-width: 900px;">
    <div style="margin-bottom: 2rem;">
        <div style="display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.5rem;">
            <a href="${pageContext.request.contextPath}/teach/course/graph?id=${course.courseId}" class="btn btn-outline" style="font-size: 0.8rem; padding: 0.25rem 0.5rem;">
                &larr; Concept Graph
            </a>
            <span class="badge" style="background: var(--bg-card-subtle); color: var(--text-muted);">
                <c:out value="${course.code}"/> &bull; Concept #${concept.displayOrder}
            </span>
        </div>
        <h1 style="margin: 0 0 0.25rem 0; font-size: 1.85rem;">Materials: <c:out value="${concept.title}"/></h1>
        <p style="color: var(--text-muted); margin: 0; font-size: 0.95rem;">
            Attach reading materials, lecture slides, video links, or documentation to help students master this concept.
        </p>
    </div>

    <!-- Add Material Card -->
    <div class="card" style="margin-bottom: 2rem;">
        <h3 style="font-size: 1.2rem; margin-top: 0; margin-bottom: 1rem;">+ Add Learning Material</h3>
        <form action="${pageContext.request.contextPath}/teach/materials" method="post">
            <input type="hidden" name="csrfToken" value="${csrfToken}"/>
            <input type="hidden" name="conceptId" value="${concept.conceptId}"/>
            <input type="hidden" name="action" value="add"/>

            <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 1rem; margin-bottom: 1rem;">
                <div>
                    <label for="title" style="display: block; font-weight: 600; font-size: 0.85rem; margin-bottom: 0.35rem;">Material Title:</label>
                    <input type="text" id="title" name="title" required placeholder="e.g. Chapter 4: Binary Search Trees PDF" style="width: 100%; padding: 0.5rem; border: 1px solid var(--border-color); border-radius: 6px; background: var(--bg-card);"/>
                </div>
                <div>
                    <label for="type" style="display: block; font-weight: 600; font-size: 0.85rem; margin-bottom: 0.35rem;">Type:</label>
                    <select id="type" name="type" style="width: 100%; padding: 0.5rem; border: 1px solid var(--border-color); border-radius: 6px; background: var(--bg-card);">
                        <option value="LINK">LINK (URL)</option>
                        <option value="VIDEO">VIDEO (YouTube / Embed)</option>
                        <option value="PDF">PDF (File Path / URL)</option>
                        <option value="TEXT">TEXT / ARTICLE</option>
                    </select>
                </div>
            </div>

            <div style="margin-bottom: 1.25rem;">
                <label for="location" style="display: block; font-weight: 600; font-size: 0.85rem; margin-bottom: 0.35rem;">URL or Location:</label>
                <input type="text" id="location" name="location" required placeholder="https://example.com/guide or relative path" style="width: 100%; padding: 0.5rem; border: 1px solid var(--border-color); border-radius: 6px; background: var(--bg-card);"/>
            </div>

            <button type="submit" class="btn btn-primary">
                Add Material
            </button>
        </form>
    </div>

    <!-- Existing Materials List -->
    <div class="card">
        <h3 style="font-size: 1.2rem; margin-top: 0; margin-bottom: 1rem;">Current Materials (${materials.size()})</h3>
        <c:choose>
            <c:when test="${empty materials}">
                <p style="color: var(--text-muted); font-size: 0.9rem;">No materials have been added for this concept yet.</p>
            </c:when>
            <c:otherwise>
                <div style="display: flex; flex-direction: column; gap: 0.75rem;">
                    <c:forEach var="m" items="${materials}">
                        <div style="padding: 0.85rem 1rem; border: 1px solid var(--border-color); border-radius: 6px; display: flex; justify-content: space-between; align-items: center; gap: 1rem;">
                            <div>
                                <div style="font-weight: 600; font-size: 0.95rem;"><c:out value="${m.title}"/></div>
                                <div style="display: flex; align-items: center; gap: 0.5rem; margin-top: 0.25rem;">
                                    <span class="badge" style="background: var(--bg-card-subtle); font-size: 0.75rem;"><c:out value="${m.type}"/></span>
                                    <span style="font-size: 0.8rem; color: var(--text-muted); overflow: hidden; text-overflow: ellipsis; max-width: 400px; white-space: nowrap;">
                                        <c:out value="${m.location}"/>
                                    </span>
                                </div>
                            </div>

                            <form action="${pageContext.request.contextPath}/teach/materials" method="post" style="margin: 0;" onsubmit="return confirm('Delete this material?');">
                                <input type="hidden" name="csrfToken" value="${csrfToken}"/>
                                <input type="hidden" name="conceptId" value="${concept.conceptId}"/>
                                <input type="hidden" name="action" value="delete"/>
                                <input type="hidden" name="materialId" value="${m.materialId}"/>
                                <button type="submit" class="btn btn-outline" style="font-size: 0.8rem; padding: 0.3rem 0.65rem; color: var(--accent-weak); border-color: var(--accent-weak);">
                                    Delete
                                </button>
                            </form>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
