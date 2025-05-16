package logic.daos;

import dataaccess.ConnectionDataBase;
import logic.logicclasses.Evaluation;
import logic.logicclasses.Academic;
import logic.logicclasses.Presentation;
import logic.logicclasses.Student;
import logic.enums.PresentationType;
import logic.interfaces.IEvaluationDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvaluationDAO implements IEvaluationDAO {
    private static final Logger logger = LogManager.getLogger(EvaluationDAO.class);
    private final AcademicDAO academicDAO;
    private final PresentationDAO presentationDAO;

    public EvaluationDAO() {
        this.academicDAO = new AcademicDAO();
        this.presentationDAO = new PresentationDAO();
    }

    @Override
    public boolean addEvaluation(Evaluation evaluation) throws SQLException {
        if (evaluation == null || evaluation.getAcademic() == null || evaluation.getPresentation() == null) {
            logger.warn("Intento de agregar evaluación con datos nulos");
            throw new SQLException("Evaluation, Academic y Presentation no pueden ser nulos");
        }

        logger.debug("Agregando nueva evaluación para presentación ID: {} por académico ID: {}",
                evaluation.getPresentation().getIdPresentation(), evaluation.getAcademic().getIdUser());

        String sql = "INSERT INTO evaluacion (calificacion, comentarios, fecha, id_academicoevaluador, id_presentacion) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, evaluation.getCalification());
            ps.setString(2, evaluation.getDescription());
            ps.setTimestamp(3, evaluation.getEvaluationDate());
            ps.setInt(4, evaluation.getAcademic().getIdUser());
            ps.setInt(5, evaluation.getPresentation().getIdPresentation());

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                logger.warn("No se pudo agregar la evaluación, ninguna fila afectada");
                return false;
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    evaluation.setIdEvaluation(generatedId);
                    logger.info("Evaluación agregada exitosamente con ID: {}", generatedId);
                    return true;
                } else {
                    logger.error("No se pudo obtener el ID generado para la evaluación");
                    throw new SQLException("No se pudo obtener el ID generado");
                }
            }
        } catch (SQLException e) {
            logger.error("Error al agregar evaluación", e);
            throw e;
        }
    }

    @Override
    public Evaluation getEvaluationById(int idEvaluation) throws SQLException {
        if (idEvaluation <= 0) {
            logger.warn("Intento de buscar evaluación con ID inválido: {}", idEvaluation);
            return null;
        }

        logger.debug("Buscando evaluación por ID: {}", idEvaluation);

        String sql = "SELECT e.*, a.numero_personal, p.id_presentacion, p.fecha as p_fecha, p.tipo, p.id_estudiante " +
                "FROM evaluacion e " +
                "JOIN academico a ON e.id_academicoevaluador = a.id_usuario " +
                "JOIN presentacion p ON e.id_presentacion = p.id_presentacion " +
                "WHERE e.id_evaluacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, idEvaluation);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    logger.debug("Evaluación encontrada con ID: {}", idEvaluation);

                    Academic academic = new Academic();
                    academic.setIdUser(rs.getInt("id_academicoevaluador"));
                    academic.setStaffNumber(rs.getString("numero_personal"));

                    Student student = new Student();
                    student.setIdUser(rs.getInt("id_estudiante"));

                    Presentation presentation = new Presentation();
                    presentation.setIdPresentation(rs.getInt("id_presentacion"));
                    presentation.setPresentationDate(rs.getTimestamp("p_fecha"));
                    presentation.setPresentationType(PresentationType.valueOf(rs.getString("tipo")));
                    presentation.setStudent(student);

                    return new Evaluation(
                            rs.getInt("id_evaluacion"),
                            rs.getInt("calificacion"),
                            rs.getString("comentarios"),
                            rs.getTimestamp("fecha"),
                            academic,
                            presentation
                    );
                }
            }
        } catch (SQLException e) {
            logger.error("Error al obtener evaluación con ID: {}", idEvaluation, e);
            throw e;
        }
        logger.info("No se encontró evaluación con ID: {}", idEvaluation);
        return null;
    }

    @Override
    public List<Evaluation> getAllEvaluations() throws SQLException {
        logger.info("Obteniendo todas las evaluaciones");

        String sql = "SELECT e.*, a.numero_personal, p.fecha, p.tipo, p.id_estudiante " +
                "FROM evaluacion e " +
                "JOIN academico a ON e.id_academicoevaluador = a.id_usuario " +
                "JOIN presentacion p ON e.id_presentacion = p.id_presentacion";

        List<Evaluation> evaluations = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Evaluation evaluation = new Evaluation();
                evaluation.setIdEvaluation(rs.getInt("id_evaluacion"));
                evaluation.setCalification(rs.getInt("calificacion"));
                evaluation.setDescription(rs.getString("comentarios"));
                evaluation.setEvaluationDate(rs.getTimestamp("fecha"));

                Academic academic = new Academic();
                academic.setIdUser(rs.getInt("id_academicoevaluador"));
                academic.setStaffNumber(rs.getString("numero_personal"));
                evaluation.setAcademic(academic);

                Presentation presentation = new Presentation();
                presentation.setIdPresentation(rs.getInt("id_presentacion"));
                presentation.setPresentationDate(rs.getTimestamp("fecha"));
                presentation.setPresentationType(PresentationType.valueOf(rs.getString("tipo")));

                Student student = new Student();
                student.setIdUser(rs.getInt("id_estudiante"));
                presentation.setStudent(student);

                evaluations.add(evaluation);
            }
            logger.debug("Se encontraron {} evaluaciones", evaluations.size());
        } catch (SQLException e) {
            logger.error("Error al obtener todas las evaluaciones", e);
            throw e;
        }
        return evaluations;
    }

    @Override
    public List<Evaluation> getEvaluationsByAcademic(int academicId) throws SQLException {
        logger.debug("Buscando evaluaciones por académico ID: {}", academicId);

        String sql = "SELECT e.*, a.numero_personal, p.fecha, p.tipo, p.id_estudiante " +
                "FROM evaluacion e " +
                "JOIN academico a ON e.id_academicoevaluador = a.id_usuario " +
                "JOIN presentacion p ON e.id_presentacion = p.id_presentacion " +
                "WHERE e.id_academicoevaluador = ?";

        List<Evaluation> evaluations = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, academicId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Evaluation evaluation = new Evaluation();
                    evaluation.setIdEvaluation(rs.getInt("id_evaluacion"));
                    evaluation.setCalification(rs.getInt("calificacion"));
                    evaluation.setDescription(rs.getString("comentarios"));
                    evaluation.setEvaluationDate(rs.getTimestamp("fecha"));

                    Academic academic = new Academic();
                    academic.setIdUser(rs.getInt("id_academicoevaluador"));
                    academic.setStaffNumber(rs.getString("numero_personal"));
                    evaluation.setAcademic(academic);

                    Presentation presentation = new Presentation();
                    presentation.setIdPresentation(rs.getInt("id_presentacion"));
                    presentation.setPresentationDate(rs.getTimestamp("fecha"));
                    presentation.setPresentationType(PresentationType.valueOf(rs.getString("tipo")));

                    Student student = new Student();
                    student.setIdUser(rs.getInt("id_estudiante"));
                    presentation.setStudent(student);

                    evaluations.add(evaluation);
                }
            }
            logger.debug("Se encontraron {} evaluaciones para académico ID: {}", evaluations.size(), academicId);
        } catch (SQLException e) {
            logger.error("Error al obtener evaluaciones por académico ID: {}", academicId, e);
            throw e;
        }
        return evaluations;
    }

    @Override
    public List<Evaluation> getEvaluationsByPresentation(int presentationId) throws SQLException {
        logger.debug("Buscando evaluaciones por presentación ID: {}", presentationId);

        String sql = "SELECT e.*, a.numero_personal, p.fecha, p.tipo, p.id_estudiante " +
                "FROM evaluacion e " +
                "JOIN academico a ON e.id_academicoevaluador = a.id_usuario " +
                "JOIN presentacion p ON e.id_presentacion = p.id_presentacion " +
                "WHERE e.id_presentacion = ?";

        List<Evaluation> evaluations = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, presentationId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Evaluation evaluation = new Evaluation();
                    evaluation.setIdEvaluation(rs.getInt("id_evaluacion"));
                    evaluation.setCalification(rs.getInt("calificacion"));
                    evaluation.setDescription(rs.getString("comentarios"));
                    evaluation.setEvaluationDate(rs.getTimestamp("fecha"));

                    Academic academic = new Academic();
                    academic.setIdUser(rs.getInt("id_academicoevaluador"));
                    academic.setStaffNumber(rs.getString("numero_personal"));
                    evaluation.setAcademic(academic);

                    Presentation presentation = new Presentation();
                    presentation.setIdPresentation(rs.getInt("id_presentacion"));
                    presentation.setPresentationDate(rs.getTimestamp("fecha"));
                    presentation.setPresentationType(PresentationType.valueOf(rs.getString("tipo")));

                    Student student = new Student();
                    student.setIdUser(rs.getInt("id_estudiante"));
                    presentation.setStudent(student);

                    evaluation.setPresentation(presentation);
                    evaluations.add(evaluation);
                }
            }
            logger.debug("Se encontraron {} evaluaciones para presentación ID: {}", evaluations.size(), presentationId);
        } catch (SQLException e) {
            logger.error("Error al obtener evaluaciones por presentación ID: {}", presentationId, e);
            throw e;
        }
        return evaluations;
    }

    @Override
    public boolean updateEvaluation(Evaluation evaluation) throws SQLException {
        if (evaluation == null || evaluation.getIdEvaluation() <= 0 ||
                evaluation.getAcademic() == null || evaluation.getPresentation() == null) {
            logger.warn("Intento de actualizar evaluación con datos inválidos");
            return false;
        }

        logger.debug("Actualizando evaluación ID: {}", evaluation.getIdEvaluation());

        String sql = "UPDATE evaluacion SET calificacion = ?, comentarios = ?, fecha = ?, " +
                "id_academicoevaluador = ?, id_presentacion = ? WHERE id_evaluacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, evaluation.getCalification());
            ps.setString(2, evaluation.getDescription());
            ps.setTimestamp(3, evaluation.getEvaluationDate());
            ps.setInt(4, evaluation.getAcademic().getIdUser());
            ps.setInt(5, evaluation.getPresentation().getIdPresentation());
            ps.setInt(6, evaluation.getIdEvaluation());

            boolean result = ps.executeUpdate() > 0;
            if (result) {
                logger.info("Evaluación ID {} actualizada exitosamente", evaluation.getIdEvaluation());
            } else {
                logger.warn("No se pudo actualizar la evaluación ID {}", evaluation.getIdEvaluation());
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al actualizar evaluación ID {}", evaluation.getIdEvaluation(), e);
            throw e;
        }
    }

    @Override
    public boolean deleteEvaluation(int idEvaluation) throws SQLException {
        if (idEvaluation <= 0) {
            logger.warn("Intento de eliminar evaluación con ID inválido: {}", idEvaluation);
            return false;
        }

        logger.debug("Eliminando evaluación ID: {}", idEvaluation);

        String sql = "DELETE FROM evaluacion WHERE id_evaluacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, idEvaluation);
            boolean result = ps.executeUpdate() > 0;
            if (result) {
                logger.info("Evaluación ID {} eliminada exitosamente", idEvaluation);
            } else {
                logger.warn("No se encontró evaluación ID {} para eliminar", idEvaluation);
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al eliminar evaluación ID {}", idEvaluation, e);
            throw e;
        }
    }

    @Override
    public boolean evaluationExists(int idEvaluation) throws SQLException {
        if (idEvaluation <= 0) {
            logger.warn("Intento de verificar existencia de evaluación con ID inválido: {}", idEvaluation);
            return false;
        }

        logger.debug("Verificando existencia de evaluación ID: {}", idEvaluation);

        String sql = "SELECT 1 FROM evaluacion WHERE id_evaluacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, idEvaluation);
            try (ResultSet rs = ps.executeQuery()) {
                boolean exists = rs.next();
                logger.debug("¿Evaluación ID {} existe?: {}", idEvaluation, exists);
                return exists;
            }
        } catch (SQLException e) {
            logger.error("Error al verificar existencia de evaluación ID {}", idEvaluation, e);
            throw e;
        }
    }

    @Override
    public int countEvaluations() throws SQLException {
        logger.debug("Contando evaluaciones");

        String sql = "SELECT COUNT(*) FROM evaluacion";

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int count = rs.next() ? rs.getInt(1) : 0;
            logger.info("Total de evaluaciones: {}", count);
            return count;
        } catch (SQLException e) {
            logger.error("Error al contar evaluaciones", e);
            throw e;
        }
    }
}