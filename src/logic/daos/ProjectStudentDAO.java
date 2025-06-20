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

        String sql = "{CALL asignar_estudiante_seguro(?, ?, ?, ?)}";

        try (Connection conn = ConnectionDataBase.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, projectId);
            stmt.setInt(2, studentId);
            stmt.registerOutParameter(3, Types.BOOLEAN);
            stmt.registerOutParameter(4, Types.VARCHAR);

            stmt.execute();

            boolean exito = stmt.getBoolean(3);
            String mensaje = stmt.getString(4);

            if (!exito) {
                logger.warn("Fallo en asignación: {}", mensaje);
                throw new SQLException(mensaje);
            }

            logger.info("Asignación exitosa: {}", mensaje);
            return true;
        } catch (SQLException e) {
            logger.error("Error en asignación: {}", e.getMessage());
            throw e;
        }
    }

    public boolean removeStudentFromProyect(int proyectId, int studentId) throws SQLException {
        logger.debug("Eliminando estudiante {} del proyecto {}", studentId, proyectId);

        String deleteSql = "DELETE FROM proyecto_estudiante WHERE id_proyecto = ? AND id_estudiante = ?";
        String updateSql = "UPDATE proyecto SET estudiantes_actuales = estudiantes_actuales - 1 WHERE id_proyecto = ?";

        try (Connection connection = ConnectionDataBase.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, proyectId);
                deleteStmt.setInt(2, studentId);
                int affectedRows = deleteStmt.executeUpdate();

                if (affectedRows == 0) {
                    logger.warn("No se encontró la asignación estudiante-proyecto");
                    return false;
                }
            }

            try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                updateStmt.setInt(1, proyectId);
                updateStmt.executeUpdate();
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
        String sql = "SELECT id_estudiante FROM proyecto_estudiante WHERE id_proyecto = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setInt(1, proyectId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    studentIds.add(rs.getInt("id_estudiante"));
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

        String sql = "SELECT id_proyecto FROM proyecto_estudiante WHERE id_estudiante = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_proyecto");
                }
            }
        } catch (SQLException e) {
            logger.error("Error al obtener proyecto del estudiante", e);
            throw e;
        }

        return NO_PROJECT;
    }
}
