package logic.interfaces;

import logic.logicclasses.ProjectRequest;
import logic.daos.ProjectStudentDAO;

import java.sql.SQLException;
import java.util.List;

public interface IProjectRequestDAO {
    boolean createRequest(ProjectRequest request) throws SQLException;
    List<ProjectRequest> getPendingRequests() throws SQLException;
    boolean approveRequest(int requestId, ProjectStudentDAO projectStudentDAO) throws SQLException;
    boolean rejectRequest(int requestId) throws SQLException;
    ProjectRequest getRequestById(int requestId) throws SQLException;
    List<ProjectRequest> getRequestsByProject(int projectId) throws SQLException;
}
