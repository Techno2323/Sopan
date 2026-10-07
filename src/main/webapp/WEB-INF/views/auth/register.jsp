<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Create Account" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div style="max-width: 520px; margin: 2rem auto;">
    <div class="card">
        <h2 style="margin-bottom: 0.3rem;">Create your account</h2>
        <p class="subtitle" style="margin-bottom: 1.5rem;">Join Sopan LMS as a student or course instructor</p>

        <form action="${pageContext.request.contextPath}/register" method="post" id="regForm">
            <input type="hidden" name="csrfToken" value="${csrfToken}"/>

            <div class="form-group">
                <label class="form-label">I am joining as:</label>
                <div style="display: flex; gap: 1rem;">
                    <label style="display: flex; align-items: center; gap: 0.4rem; font-weight: 500; cursor: pointer;">
                        <input type="radio" name="role" value="STUDENT" ${empty param.role || param.role == 'STUDENT' ? 'checked' : ''} onchange="toggleRoleFields()"/> Student
                    </label>
                    <label style="display: flex; align-items: center; gap: 0.4rem; font-weight: 500; cursor: pointer;">
                        <input type="radio" name="role" value="INSTRUCTOR" ${param.role == 'INSTRUCTOR' ? 'checked' : ''} onchange="toggleRoleFields()"/> Instructor
                    </label>
                </div>
            </div>

            <div class="form-group">
                <label for="fullName" class="form-label">Full Name</label>
                <input type="text" id="fullName" name="fullName" class="form-control"
                       value="<c:out value="${fullName}" />" required/>
            </div>

            <div class="form-group">
                <label for="email" class="form-label">Email Address</label>
                <input type="email" id="email" name="email" class="form-control"
                       value="<c:out value="${email}" />" required/>
            </div>

            <div class="form-group">
                <label for="password" class="form-label">Password (min 8 characters)</label>
                <input type="password" id="password" name="password" class="form-control" required minlength="8"/>
            </div>

            <!-- Student Fields -->
            <div id="studentFields">
                <div class="form-group">
                    <label for="rollNo" class="form-label">Roll Number / Student ID</label>
                    <input type="text" id="rollNo" name="rollNo" class="form-control" value="<c:out value="${param.rollNo}" />"/>
                </div>

                <div class="grid-2">
                    <div class="form-group">
                        <label for="program" class="form-label">Program</label>
                        <input type="text" id="program" name="program" class="form-control" placeholder="B.Tech CS" value="<c:out value="${param.program}" />"/>
                    </div>
                    <div class="form-group">
                        <label for="studyYear" class="form-label">Year of Study</label>
                        <select id="studyYear" name="studyYear" class="form-control">
                            <option value="1">1st Year</option>
                            <option value="2">2nd Year</option>
                            <option value="3">3rd Year</option>
                            <option value="4">4th Year</option>
                        </select>
                    </div>
                </div>
            </div>

            <!-- Instructor Fields -->
            <div id="instructorFields" style="display: none;">
                <div class="form-group">
                    <label for="department" class="form-label">Academic Department</label>
                    <input type="text" id="department" name="department" class="form-control" placeholder="Computer Science" value="<c:out value="${param.department}" />"/>
                </div>
                <div class="form-group">
                    <label for="bio" class="form-label">Brief Bio</label>
                    <textarea id="bio" name="bio" class="form-control" placeholder="Teaching focus and research areas..."><c:out value="${param.bio}" /></textarea>
                </div>
            </div>

            <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 1rem;">
                Create Account
            </button>
        </form>

        <div style="margin-top: 1.5rem; text-align: center; font-size: 0.9rem; color: var(--text-muted);">
            Already have an account?
            <a href="${pageContext.request.contextPath}/login">Sign in here</a>
        </div>
    </div>
</div>

<script>
function toggleRoleFields() {
    const isInstructor = document.querySelector('input[name="role"]:checked').value === 'INSTRUCTOR';
    document.getElementById('studentFields').style.display = isInstructor ? 'none' : 'block';
    document.getElementById('instructorFields').style.display = isInstructor ? 'block' : 'none';
}
window.addEventListener('DOMContentLoaded', toggleRoleFields);
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
