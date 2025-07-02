package logic.daos;

import logic.interfaces.IAssignmentDocumentDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import dataaccess.ConnectionDataBase;

public class AssignmentDocumentDAO implements IAssignmentDocumentDAO {
    private static final byte[] EMPTY_DOCUMENT = new byte[0];

    @Override
    public void saveAssignmentDocument(int projectId, int studentId, byte[] pdfContent) throws SQLException {
        String query = "INSERT INTO documentos_asignacion (id_proyecto, id_estudiante, contenido) VALUES (?, ?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, projectId);
            statement.setInt(2, studentId);
            statement.setBytes(3, pdfContent);
            statement.executeUpdate();
        }
    }

    @Override
    public byte[] getAssignmentDocument(int studentId) throws SQLException {
        String query = "SELECT contenido FROM documentos_asignacion WHERE id_estudiante = ? ORDER BY fecha_creacion DESC LIMIT 1";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, studentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    byte[] content = resultSet.getBytes("contenido");
                    return content != null ? content : EMPTY_DOCUMENT;
                }
                return EMPTY_DOCUMENT;
            }
        }
    }

    public void deleteAssignmentDocument(int studentId) throws SQLException {
        String query = "DELETE FROM documentos_asignacion WHERE id_estudiante = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, studentId);
            statement.executeUpdate();
        }
    }
}
