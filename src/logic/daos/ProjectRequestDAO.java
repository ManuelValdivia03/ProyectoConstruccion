package logic.daos;

import dataaccess.ConnectionDataBase;
import logic.logicclasses.ProjectRequest;
import logic.enums.RequestStatus;
import logic.interfaces.IProjectRequestDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProjectRequestDAO implements IProjectRequestDAO {
    private static final Logger logger = LogManager.getLogger(ProjectRequestDAO.class);

    public boolean createRequest(ProjectRequest request) throws SQLException {
        String sql = "INSERT INTO solicitud_proyecto (id_proyecto, id_estudiante) VALUES (?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, request.getProjectId());
            stmt.setInt(2, request.getStudentId());

            try {
                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            request.setRequestId(generatedKeys.getInt(1));
                        }
                    }
                    logger.info("Solicitud creada: {}", request);
                    return true;
                }
                return false;
            } catch (SQLException e) {
                // Verificar si es un error de duplicado (código 1062 en MySQL)
                if (e.getErrorCode() == 1062) {
                    logger.warn("Intento de solicitud duplicada para proyecto {} y estudiante {}", 
                        request.getProjectId(), request.getStudentId());
                    throw new SQLException("Ya existe una solicitud para este proyecto");
                }
                throw e;
            }
        }
    }

    public List<ProjectRequest> getPendingRequests() throws SQLException {
        String sql = "SELECT sp.id_solicitud, sp.id_proyecto, p.titulo AS proyecto_titulo, " +
                "sp.id_estudiante, e.matricula AS estudiante_matricula, " +
                "sp.fecha_solicitud, sp.estado " +
                "FROM solicitud_proyecto sp " +
                "JOIN proyecto p ON sp.id_proyecto = p.id_proyecto " +
                "JOIN estudiante e ON sp.id_estudiante = e.id_usuario " +
                "WHERE sp.estado = 'pendiente'";

        List<ProjectRequest> requests = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ProjectRequest request = new ProjectRequest(
                        rs.getInt("id_solicitud"),
                        rs.getInt("id_proyecto"),
                        rs.getInt("id_estudiante"),
                        rs.getTimestamp("fecha_solicitud"),
                        RequestStatus.PENDIENTE,
                        rs.getString("proyecto_titulo"),
                        rs.getString("estudiante_matricula")
                );
                requests.add(request);
            }
        } catch (SQLException e) {
            logger.error("Error al obtener solicitudes pendientes", e);
            throw e;
        }
        return requests;
    }

    public boolean approveRequest(int requestId, ProjectStudentDAO projectStudentDAO) throws SQLException {
        ProjectRequest request = getRequestById(requestId);
        if (request == null || !request.isPending()) {
            return false;
        }

        if (projectStudentDAO.assignStudentToProyect(request.getProjectId(), request.getStudentId())) {
            return updateRequestStatus(requestId, RequestStatus.APROBADA);
        }
        return false;
    }

    public boolean rejectRequest(int requestId) throws SQLException {
        return updateRequestStatus(requestId, RequestStatus.RECHAZADA);
    }

    private boolean updateRequestStatus(int requestId, RequestStatus status) throws SQLException {
        String sql = "UPDATE solicitud_proyecto SET estado = ? WHERE id_solicitud = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, status.name().toLowerCase());
            stmt.setInt(2, requestId);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Solicitud {} actualizada a estado: {}", requestId, status);
                return true;
            }
            return false;
        } catch (SQLException e) {
            logger.error("Error al actualizar estado de solicitud", e);
            throw e;
        }
    }

    public ProjectRequest getRequestById(int requestId) throws SQLException {
        String sql = "SELECT sp.id_solicitud, sp.id_proyecto, p.titulo AS proyecto_titulo, " +
                "sp.id_estudiante, e.matricula AS estudiante_matricula, " +
                "sp.fecha_solicitud, sp.estado " +
                "FROM solicitud_proyecto sp " +
                "JOIN proyecto p ON sp.id_proyecto = p.id_proyecto " +
                "JOIN estudiante e ON sp.id_estudiante = e.id_usuario " +
                "WHERE sp.id_solicitud = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, requestId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    RequestStatus status = RequestStatus.valueOf(rs.getString("estado").toUpperCase());
                    return new ProjectRequest(
                            rs.getInt("id_solicitud"),
                            rs.getInt("id_proyecto"),
                            rs.getInt("id_estudiante"),
                            rs.getTimestamp("fecha_solicitud"),
                            status,
                            rs.getString("proyecto_titulo"),
                            rs.getString("estudiante_matricula")
                    );
                }
            }
        } catch (SQLException e) {
            logger.error("Error al obtener solicitud por ID", e);
            throw e;
        }
        return null;
    }

    public boolean hasExistingRequest(int projectId, int userId) throws SQLException {
        String query = "SELECT COUNT(*) FROM solicitud_proyecto WHERE id_proyecto = ? AND id_estudiante = ?";
        try (Connection conn = ConnectionDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, projectId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public List<ProjectRequest> getRequestsByProject(int projectId) throws SQLException {
        String sql = "SELECT sp.id_solicitud, sp.id_proyecto, p.titulo AS proyecto_titulo, " +
                "sp.id_estudiante, e.matricula AS estudiante_matricula, " +
                "sp.fecha_solicitud, sp.estado " +
                "FROM solicitud_proyecto sp " +
                "JOIN proyecto p ON sp.id_proyecto = p.id_proyecto " +
                "JOIN estudiante e ON sp.id_estudiante = e.id_usuario " +
                "WHERE sp.id_proyecto = ?";

        List<ProjectRequest> requests = new ArrayList<>();

        try (Connection conn = ConnectionDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    requests.add(new ProjectRequest(
                            rs.getInt("id_solicitud"),
                            rs.getInt("id_proyecto"),
                            rs.getInt("id_estudiante"),
                            rs.getTimestamp("fecha_solicitud"),
                            RequestStatus.valueOf(rs.getString("estado").toUpperCase()),
                            rs.getString("proyecto_titulo"),
                            rs.getString("estudiante_matricula")
                    ));
                }
            }
        }
        return requests;
    }
}
