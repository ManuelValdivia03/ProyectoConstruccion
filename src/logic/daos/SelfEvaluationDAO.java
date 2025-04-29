package logic.daos;

import dataaccess.ConnectionDataBase;
import logic.logicclasses.SelfEvaluation;
import logic.logicclasses.Student;
import logic.interfaces.ISelfEvaluationDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SelfEvaluationDAO implements ISelfEvaluationDAO {
    private static final Logger logger = LogManager.getLogger(SelfEvaluationDAO.class);
    private final StudentDAO studentDAO;

    public SelfEvaluationDAO() {
        this.studentDAO = new StudentDAO();
    }

    public boolean addSelfEvaluation(SelfEvaluation selfEvaluation) throws SQLException {
        if (selfEvaluation == null ||
                selfEvaluation.getFeedBack() == null ||
                selfEvaluation.getStudent() == null) {
            logger.warn("Intento de agregar autoevaluación con datos nulos");
            throw new SQLException("Datos de autoevaluación incompletos");
        }

        logger.debug("Agregando nueva autoevaluación para estudiante ID: {}",
                selfEvaluation.getStudent().getIdUser());

        String sql = "INSERT INTO autoevaluacion (calificacion, comentarios, id_usuario) VALUES (?, ?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setFloat(1, selfEvaluation.getCalification());
            statement.setString(2, selfEvaluation.getFeedBack());
            statement.setInt(3, selfEvaluation.getStudent().getIdUser());

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        selfEvaluation.setIdSelfEvaluation(generatedId);
                        logger.info("Autoevaluación agregada exitosamente - ID: {}, Estudiante ID: {}",
                                generatedId, selfEvaluation.getStudent().getIdUser());
                        return true;
                    }
                }
            }
            logger.warn("No se pudo agregar la autoevaluación para estudiante ID: {}",
                    selfEvaluation.getStudent().getIdUser());
            return false;
        } catch (SQLException e) {
            logger.error("Error al agregar autoevaluación para estudiante ID: {}",
                    selfEvaluation.getStudent().getIdUser(), e);
            throw e;
        }
    }

    public SelfEvaluation getSelfEvaluationById(int idSelfEvaluation) throws SQLException {
        if (idSelfEvaluation <= 0) {
            logger.warn("Intento de buscar autoevaluación con ID inválido: {}", idSelfEvaluation);
            return null;
        }

        logger.debug("Buscando autoevaluación por ID: {}", idSelfEvaluation);

        String sql = "SELECT * FROM autoevaluacion WHERE id_autoevaluacion = ?";
        SelfEvaluation selfEvaluation = null;

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idSelfEvaluation);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    selfEvaluation = new SelfEvaluation();
                    selfEvaluation.setIdSelfEvaluation(resultSet.getInt("id_autoevaluacion"));
                    selfEvaluation.setFeedBack(resultSet.getString("comentarios"));
                    selfEvaluation.setCalification(resultSet.getFloat("calificacion"));

                    selfEvaluation.setStudent(studentDAO.getStudentById(resultSet.getInt("id_usuario")));
                    logger.debug("Autoevaluación encontrada con ID: {}", idSelfEvaluation);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al obtener autoevaluación con ID: {}", idSelfEvaluation, e);
            throw e;
        }

        if (selfEvaluation == null) {
            logger.info("No se encontró autoevaluación con ID: {}", idSelfEvaluation);
        }
        return selfEvaluation;
    }

    public List<SelfEvaluation> getAllSelfEvaluations() throws SQLException {
        logger.info("Obteniendo todas las autoevaluaciones");

        String sql = "SELECT * FROM autoevaluacion";
        List<SelfEvaluation> selfEvaluations = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                SelfEvaluation selfEvaluation = new SelfEvaluation();
                selfEvaluation.setIdSelfEvaluation(resultSet.getInt("id_autoevaluacion"));
                selfEvaluation.setFeedBack(resultSet.getString("comentarios"));
                selfEvaluation.setCalification(resultSet.getFloat("calificacion"));

                selfEvaluation.setStudent(studentDAO.getStudentById(resultSet.getInt("id_usuario")));
                selfEvaluations.add(selfEvaluation);
            }
            logger.debug("Se encontraron {} autoevaluaciones", selfEvaluations.size());
        } catch (SQLException e) {
            logger.error("Error al obtener todas las autoevaluaciones", e);
            throw e;
        }
        return selfEvaluations;
    }

    public List<SelfEvaluation> getSelfEvaluationsByStudent(int studentId) throws SQLException {
        if (studentId <= 0) {
            logger.warn("Intento de buscar autoevaluaciones con ID de estudiante inválido: {}", studentId);
            return new ArrayList<>();
        }

        logger.debug("Buscando autoevaluaciones por estudiante ID: {}", studentId);

        String sql = "SELECT * FROM autoevaluacion WHERE id_usuario = ?";
        List<SelfEvaluation> selfEvaluations = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, studentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    SelfEvaluation selfEvaluation = new SelfEvaluation();
                    selfEvaluation.setIdSelfEvaluation(resultSet.getInt("id_autoevaluacion"));
                    selfEvaluation.setFeedBack(resultSet.getString("comentarios"));
                    selfEvaluation.setCalification(resultSet.getFloat("calificacion"));

                    selfEvaluation.setStudent(studentDAO.getStudentById(studentId));
                    selfEvaluations.add(selfEvaluation);
                }
            }
            logger.debug("Se encontraron {} autoevaluaciones para estudiante ID: {}",
                    selfEvaluations.size(), studentId);
        } catch (SQLException e) {
            logger.error("Error al obtener autoevaluaciones por estudiante ID: {}", studentId, e);
            throw e;
        }
        return selfEvaluations;
    }

    public boolean updateSelfEvaluation(SelfEvaluation selfEvaluation) throws SQLException {
        if (selfEvaluation == null ||
                selfEvaluation.getIdSelfEvaluation() <= 0 ||
                selfEvaluation.getStudent() == null) {
            logger.warn("Intento de actualizar autoevaluación con datos inválidos");
            throw new SQLException("Datos de autoevaluación incompletos");
        }

        logger.debug("Actualizando autoevaluación ID: {}", selfEvaluation.getIdSelfEvaluation());

        String sql = "UPDATE autoevaluacion SET calificacion = ?, comentarios = ?, id_usuario = ? WHERE id_autoevaluacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setFloat(1, selfEvaluation.getCalification());
            statement.setString(2, selfEvaluation.getFeedBack());
            statement.setInt(3, selfEvaluation.getStudent().getIdUser());
            statement.setInt(4, selfEvaluation.getIdSelfEvaluation());

            boolean result = statement.executeUpdate() > 0;
            if (result) {
                logger.info("Autoevaluación actualizada exitosamente - ID: {}",
                        selfEvaluation.getIdSelfEvaluation());
            } else {
                logger.warn("No se encontró autoevaluación con ID: {} para actualizar",
                        selfEvaluation.getIdSelfEvaluation());
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al actualizar autoevaluación ID: {}",
                    selfEvaluation.getIdSelfEvaluation(), e);
            throw e;
        }
    }

    public boolean deleteSelfEvaluation(int idSelfEvaluation) throws SQLException {
        if (idSelfEvaluation <= 0) {
            logger.warn("Intento de eliminar autoevaluación con ID inválido: {}", idSelfEvaluation);
            return false;
        }

        logger.debug("Eliminando autoevaluación ID: {}", idSelfEvaluation);

        String sql = "DELETE FROM autoevaluacion WHERE id_autoevaluacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idSelfEvaluation);
            boolean result = statement.executeUpdate() > 0;
            if (result) {
                logger.info("Autoevaluación eliminada exitosamente - ID: {}", idSelfEvaluation);
            } else {
                logger.warn("No se encontró autoevaluación con ID: {} para eliminar", idSelfEvaluation);
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al eliminar autoevaluación ID: {}", idSelfEvaluation, e);
            throw e;
        }
    }

    public boolean selfEvaluationExists(int idSelfEvaluation) throws SQLException {
        if (idSelfEvaluation <= 0) {
            logger.warn("Intento de verificar existencia de autoevaluación con ID inválido: {}", idSelfEvaluation);
            return false;
        }

        logger.debug("Verificando existencia de autoevaluación ID: {}", idSelfEvaluation);

        String sql = "SELECT 1 FROM autoevaluacion WHERE id_autoevaluacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idSelfEvaluation);
            try (ResultSet resultSet = statement.executeQuery()) {
                boolean exists = resultSet.next();
                logger.debug("¿Autoevaluación ID {} existe?: {}", idSelfEvaluation, exists);
                return exists;
            }
        } catch (SQLException e) {
            logger.error("Error al verificar existencia de autoevaluación ID: {}", idSelfEvaluation, e);
            throw e;
        }
    }

    public int countSelfEvaluations() throws SQLException {
        logger.debug("Contando autoevaluaciones");

        String sql = "SELECT COUNT(*) FROM autoevaluacion";

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            int count = resultSet.next() ? resultSet.getInt(1) : 0;
            logger.info("Total de autoevaluaciones: {}", count);
            return count;
        } catch (SQLException e) {
            logger.error("Error al contar autoevaluaciones", e);
            throw e;
        }
    }
}