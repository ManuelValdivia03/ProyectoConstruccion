package logic.daos;

import dataaccess.ConnectionDataBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import logic.interfaces.IProjectStudentDAO;

public class ProjectStudentDAO implements IProjectStudentDAO {
    private static final Logger logger = LogManager.getLogger(ProjectStudentDAO.class);

    public boolean assignStudentToProyect(int proyectId, int studentId) throws SQLException {
        logger.debug("Asignando estudiante {} al proyecto {}", studentId, proyectId);

        String checkCapacitySql = "SELECT estudiantes_actuales, cupo_maximo FROM proyecto WHERE id_proyecto = ?";
        String insertSql = "INSERT INTO proyecto_estudiante (id_proyecto, id_estudiante) VALUES (?, ?)";
        String updateSql = "UPDATE proyecto SET estudiantes_actuales = estudiantes_actuales + 1 WHERE id_proyecto = ?";

        try (Connection connection = ConnectionDataBase.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement checkStmt = connection.prepareStatement(checkCapacitySql)) {
                checkStmt.setInt(1, proyectId);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    int current = rs.getInt("estudiantes_actuales");
                    int max = rs.getInt("cupo_maximo");

                    if (current >= max) {
                        logger.warn("No hay cupo disponible en el proyecto {}", proyectId);
                        return false;
                    }
                } else {
                    logger.warn("Proyecto no encontrado: {}", proyectId);
                    return false;
                }
            }

            try (PreparedStatement checkStudentStmt = connection.prepareStatement(
                    "SELECT 1 FROM proyecto_estudiante WHERE id_estudiante = ?")) {
                checkStudentStmt.setInt(1, studentId);
                if (checkStudentStmt.executeQuery().next()) {
                    logger.warn("El estudiante {} ya está asignado a un proyecto", studentId);
                    return false;
                }
            }

            try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                insertStmt.setInt(1, proyectId);
                insertStmt.setInt(2, studentId);
                insertStmt.executeUpdate();
            }

            try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                updateStmt.setInt(1, proyectId);
                updateStmt.executeUpdate();
            }

            connection.commit();
            logger.info("Estudiante {} asignado exitosamente al proyecto {}", studentId, proyectId);
            return true;
        } catch (SQLException e) {
            logger.error("Error al asignar estudiante al proyecto", e);
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

        List<Integer> studentIds = new ArrayList<>();
        String sql = "SELECT id_estudiante FROM proyecto_estudiante WHERE id_proyecto = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, proyectId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                studentIds.add(rs.getInt("id_estudiante"));
            }
        } catch (SQLException e) {
            logger.error("Error al obtener estudiantes del proyecto", e);
            throw e;
        }

        return studentIds;
    }

    public Integer getProyectByStudent(int studentId) throws SQLException {
        logger.debug("Obteniendo proyecto del estudiante {}", studentId);

        String sql = "SELECT id_proyecto FROM proyecto_estudiante WHERE id_estudiante = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id_proyecto");
            }
            return null;
        } catch (SQLException e) {
            logger.error("Error al obtener proyecto del estudiante", e);
            throw e;
        }
    }
}
