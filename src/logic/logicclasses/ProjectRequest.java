package logic.logicclasses;

import logic.enums.RequestStatus;

import java.sql.Timestamp;

public class ProjectRequest {
    private int requestId;
    private int projectId;
    private int studentId;
    private Timestamp requestDate;
    private RequestStatus status;
    private String projectTitle;
    private String studentName;

    public ProjectRequest(int requestId, int projectId, int studentId,
                          Timestamp requestDate, RequestStatus status,
                          String projectTitle, String studentName) {
        this.requestId = requestId;
        this.projectId = projectId;
        this.studentId = studentId;
        this.requestDate = requestDate;
        this.status = status;
        this.projectTitle = projectTitle;
        this.studentName = studentName;
    }

    public ProjectRequest(int projectId, int studentId) {
        this(0, projectId, studentId, new Timestamp(System.currentTimeMillis()),
                RequestStatus.PENDIENTE, "", "");
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public Timestamp getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Timestamp requestDate) {
        this.requestDate = requestDate;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public boolean isPending() {
        return status == RequestStatus.PENDIENTE;
    }

    public boolean isApproved() {
        return status == RequestStatus.APROBADA;
    }

    public boolean isRejected() {
        return status == RequestStatus.RECHAZADA;
    }

    @Override
    public String toString() {
        return "ProjectRequest{" +
                "requestId=" + requestId +
                ", projectId=" + projectId +
                ", projectTitle='" + projectTitle + '\'' +
                ", studentId=" + studentId +
                ", studentName='" + studentName + '\'' +
                ", requestDate=" + requestDate +
                ", status=" + status.getDisplayName() +
                '}';
    }
}
