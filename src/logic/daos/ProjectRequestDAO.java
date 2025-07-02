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
import java.sql.Timestamp;

public class ProjectRequestDAO implements IProjectRequestDAO {

    private static final Logger logger = LogManager.getLogger(ProjectRequestDAO.class);
    private static final ProjectRequest EMPTY_REQUEST = new ProjectRequest(
        -1, -1, -1, new Timestamp(0), RequestStatus.RECHAZADA, "", ""
    );

    public boolean createRequest(ProjectRequest request) throws SQLException, IllegalArgumentException {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud no puede ser nula");
        }

        String sql = "INSERT INTO solicitud_proyecto (id_proyecto, id_estudiante) VALUES (?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setInt(1, request.getProjectId());
            preparedStatement.setInt(2, request.getStudentId());

            try {
                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            request.setRequestId(generatedKeys.getInt(1));
                        }
                    }
                    logger.info("Solicitud creada: {}", request);
                    return true;
                }
                return false;
            } catch (SQLException e) {
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
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                ProjectRequest request = new ProjectRequest(
                        resultSet.getInt("id_solicitud"),
                        resultSet.getInt("id_proyecto"),
                        resultSet.getInt("id_estudiante"),
                        resultSet.getTimestamp("fecha_solicitud"),
                        RequestStatus.PENDIENTE,
                        resultSet.getString("proyecto_titulo"),
                        resultSet.getString("estudiante_matricula")
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
        if (request.getRequestId() == -1 || !request.isPending()) {
            return false;
        }

        if (projectStudentDAO.assignStudentToProject(request.getProjectId(), request.getStudentId())) {
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
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status.getDisplayName());
            statement.setInt(2, requestId);

            int affectedRows = statement.executeUpdate();
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
        if (requestId <= 0) {
            return EMPTY_REQUEST;
        }

        String sql = "SELECT sp.id_solicitud, sp.id_proyecto, p.titulo AS proyecto_titulo, " +
                "sp.id_estudiante, e.matricula AS estudiante_matricula, " +
                "sp.fecha_solicitud, sp.estado " +
                "FROM solicitud_proyecto sp " +
                "JOIN proyecto p ON sp.id_proyecto = p.id_proyecto " +
                "JOIN estudiante e ON sp.id_estudiante = e.id_usuario " +
                "WHERE sp.id_solicitud = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, requestId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    RequestStatus status = RequestStatus.valueOf(resultSet.getString("estado").toUpperCase());
                    return new ProjectRequest(
                            resultSet.getInt("id_solicitud"),
                            resultSet.getInt("id_proyecto"),
                            resultSet.getInt("id_estudiante"),
                            resultSet.getTimestamp("fecha_solicitud"),
                            status,
                            resultSet.getString("proyecto_titulo"),
                            resultSet.getString("estudiante_matricula")
                    );
                }
            }
        } catch (SQLException e) {
            logger.error("Error al obtener solicitud por ID", e);
            throw e;
        }
        return EMPTY_REQUEST;
    }

    public boolean hasExistingRequest(int projectId, int userId) throws SQLException {
        if (projectId <= 0 || userId <= 0) {
            return false;
        }

        String query = "SELECT COUNT(*) FROM solicitud_proyecto WHERE id_proyecto = ? AND id_estudiante = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, projectId);
            preparedStatement.setInt(2, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        }
        return false;
    }

    public List<ProjectRequest> getRequestsByProject(int projectId) throws SQLException {
        if (projectId <= 0) {
            return new ArrayList<>();
        }

        String sql = "SELECT sp.id_solicitud, sp.id_proyecto, p.titulo AS proyecto_titulo, " +
                "sp.id_estudiante, e.matricula AS estudiante_matricula, " +
                "sp.fecha_solicitud, sp.estado " +
                "FROM solicitud_proyecto sp " +
                "JOIN proyecto p ON sp.id_proyecto = p.id_proyecto " +
                "JOIN estudiante e ON sp.id_estudiante = e.id_usuario " +
                "WHERE sp.id_proyecto = ?";

        List<ProjectRequest> requests = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, projectId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    requests.add(new ProjectRequest(
                            resultSet.getInt("id_solicitud"),
                            resultSet.getInt("id_proyecto"),
                            resultSet.getInt("id_estudiante"),
                            resultSet.getTimestamp("fecha_solicitud"),
                            RequestStatus.fromDisplayName(resultSet.getString("estado")),
                            resultSet.getString("proyecto_titulo"),
                            resultSet.getString("estudiante_matricula")
                    ));
                }
            }
        }
        return requests;
    }
}
