package logic.interfaces;

import java.sql.SQLException;

public interface IAssignmentDocumentDAO {
    void saveAssignmentDocument(int projectId, int studentId, byte[] pdfContent) throws SQLException;
    byte[] getAssignmentDocument(int studentId) throws SQLException;
    void deleteAssignmentDocument(int studentId) throws SQLException;
}