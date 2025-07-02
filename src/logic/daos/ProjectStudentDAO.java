package logic.daos;

import dataaccess.ConnectionDataBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import logic.interfaces.IProjectStudentDAO;

public class ProjectStudentDAO implements IProjectStudentDAO {
    private static final Logger logger = LogManager.getLogger(ProjectStudentDAO.class);
    private static final int NO_PROJECT = -1;

    public boolean assignStudentToProject(int projectId, int studentId) throws SQLException {
        logger.debug("Asignando estudiante {} al proyecto {}", studentId, projectId);

        String query = "{CALL asignar_estudiante_seguro(?, ?, ?, ?)}";

        try (Connection connection = ConnectionDataBase.getConnection();
             CallableStatement statement = connection.prepareCall(query)) {

            statement.setInt(1, projectId);
            statement.setInt(2, studentId);
            statement.registerOutParameter(3, Types.BOOLEAN);
            statement.registerOutParameter(4, Types.VARCHAR);

            statement.execute();

            boolean success = statement.getBoolean(3);
            String message = statement.getString(4);

            if (!success) {
                logger.warn("Fallo en asignación: {}", message);
                throw new SQLException(message);
            }

            logger.info("Asignación exitosa: {}", message);
            return true;
        } catch (SQLException e) {
            logger.error("Error en asignación: {}", e.getMessage());
            throw e;
        }
    }

    public boolean removeStudentFromProyect(int proyectId, int studentId) throws SQLException {
        logger.debug("Eliminando estudiante {} del proyecto {}", studentId, proyectId);

        String deleteQuery = "DELETE FROM proyecto_estudiante WHERE id_proyecto = ? AND id_estudiante = ?";
        String updateQuery = "UPDATE proyecto SET estudiantes_actuales = estudiantes_actuales - 1 WHERE id_proyecto = ?";

        try (Connection connection = ConnectionDataBase.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery)) {
                deleteStatement.setInt(1, proyectId);
                deleteStatement.setInt(2, studentId);
                int affectedRows = deleteStatement.executeUpdate();

                if (affectedRows == 0) {
                    logger.warn("No se encontró la asignación estudiante-proyecto");
                    return false;
                }
            }

            try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                updateStatement.setInt(1, proyectId);
                updateStatement.executeUpdate();
            }

            connection.commit();
            logger.info("Estudiante {} eliminado del proyecto {}", studentId, proyectId);
            return true;
        } catch (SQLException e) {
            logger.error("Error al eliminar estudiante del proyecto", e);
            throw e;
        }
    }

    public List<Integer> getStudentsByProyect(int proyectId) throws SQLException {
        logger.debug("Obteniendo estudiantes del proyecto {}", proyectId);

        if (proyectId <= 0) {
            return new ArrayList<>();
        }

        List<Integer> studentIds = new ArrayList<>();
        String query = "SELECT id_estudiante FROM proyecto_estudiante WHERE id_proyecto = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            
            preparedStatement.setInt(1, proyectId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    studentIds.add(resultSet.getInt("id_estudiante"));
                }
            }
        } catch (SQLException e) {
            logger.error("Error al obtener estudiantes del proyecto", e);
            throw e;
        }

        return studentIds;
    }

    public Integer getProyectByStudent(int studentId) throws SQLException {
        logger.debug("Obteniendo proyecto del estudiante {}", studentId);

        if (studentId <= 0) {
            return NO_PROJECT;
        }

        String query = "SELECT id_proyecto FROM proyecto_estudiante WHERE id_estudiante = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, studentId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id_proyecto");
                }
            }
        } catch (SQLException e) {
            logger.error("Error al obtener proyecto del estudiante", e);
            throw e;
        }

        return NO_PROJECT;
    }
}
