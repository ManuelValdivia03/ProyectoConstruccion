package logic.daos;

import dataaccess.ConnectionDataBase;
import logic.logicclasses.Evaluation;
import logic.logicclasses.Academic;
import logic.logicclasses.Presentation;
import logic.logicclasses.Student;
import logic.enums.PresentationType;
import logic.interfaces.IEvaluationDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EvaluationDAO implements IEvaluationDAO {
    private static final Evaluation EMPTY_EVALUATION = new Evaluation();
    private final AcademicDAO academicDAO;
    private final PresentationDAO presentationDAO;

    public EvaluationDAO() {
        this.academicDAO = new AcademicDAO();
        this.presentationDAO = new PresentationDAO();
    }

    @Override
    public boolean addEvaluation(Evaluation evaluation) throws SQLException, IllegalArgumentException {
        if (evaluation == null || evaluation.getAcademic() == null || evaluation.getPresentation() == null) {
            throw new IllegalArgumentException("Evaluacion, Academico y Presentacion no pueden ser nulos");
        }

        String query = "INSERT INTO evaluacion (calificacion, comentarios, fecha, id_academicoevaluador, id_presentacion) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setInt(1, evaluation.getCalification());
            preparedStatement.setString(2, evaluation.getDescription());
            preparedStatement.setTimestamp(3, evaluation.getEvaluationDate());
            preparedStatement.setInt(4, evaluation.getAcademic().getIdUser());
            preparedStatement.setInt(5, evaluation.getPresentation().getIdPresentation());

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        evaluation.setIdEvaluation(generatedId);
                        return true;
                    }
                }
            }
            return false;
        }
    }

    @Override
    public Evaluation getEvaluationById(int idEvaluation) throws SQLException {
        if (idEvaluation <= 0) {
            return EMPTY_EVALUATION;
        }

        String query = "SELECT e.*, a.numero_personal, p.id_presentacion, p.fecha as p_fecha, p.tipo, p.id_estudiante " +
                "FROM evaluacion e " +
                "JOIN academico a ON e.id_academicoevaluador = a.id_usuario " +
                "JOIN presentacion p ON e.id_presentacion = p.id_presentacion " +
                "WHERE e.id_evaluacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, idEvaluation);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    Academic academic = new Academic();
                    academic.setIdUser(resultSet.getInt("id_academicoevaluador"));
                    academic.setStaffNumber(resultSet.getString("numero_personal"));

                    Student student = new Student();
                    student.setIdUser(resultSet.getInt("id_estudiante"));

                    Presentation presentation = new Presentation();
                    presentation.setIdPresentation(resultSet.getInt("id_presentacion"));
                    presentation.setPresentationDate(resultSet.getTimestamp("p_fecha"));
                    presentation.setPresentationType(PresentationType.valueOf(resultSet.getString("tipo")));
                    presentation.setStudent(student);

                    return new Evaluation(
                            resultSet.getInt("id_evaluacion"),
                            resultSet.getInt("calificacion"),
                            resultSet.getString("comentarios"),
                            resultSet.getTimestamp("fecha"),
                            academic,
                            presentation
                    );
                }
            }
        }
        return EMPTY_EVALUATION;
    }

    @Override
    public List<Evaluation> getAllEvaluations() throws SQLException {
        String query = "SELECT e.*, a.numero_personal, p.fecha, p.tipo, p.id_estudiante " +
                "FROM evaluacion e " +
                "JOIN academico a ON e.id_academicoevaluador = a.id_usuario " +
                "JOIN presentacion p ON e.id_presentacion = p.id_presentacion";

        List<Evaluation> evaluations = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                Evaluation evaluation = new Evaluation();
                evaluation.setIdEvaluation(resultSet.getInt("id_evaluacion"));
                evaluation.setCalification(resultSet.getInt("calificacion"));
                evaluation.setDescription(resultSet.getString("comentarios"));
                evaluation.setEvaluationDate(resultSet.getTimestamp("fecha"));

                Academic academic = new Academic();
                academic.setIdUser(resultSet.getInt("id_academicoevaluador"));
                academic.setStaffNumber(resultSet.getString("numero_personal"));
                evaluation.setAcademic(academic);

                Presentation presentation = new Presentation();
                presentation.setIdPresentation(resultSet.getInt("id_presentacion"));
                presentation.setPresentationDate(resultSet.getTimestamp("fecha"));
                presentation.setPresentationType(PresentationType.valueOf(resultSet.getString("tipo")));

                Student student = new Student();
                student.setIdUser(resultSet.getInt("id_estudiante"));
                presentation.setStudent(student);

                evaluation.setPresentation(presentation);
                evaluations.add(evaluation);
            }
        }
        return evaluations;
    }

    @Override
    public List<Evaluation> getEvaluationsByAcademic(int academicId) throws SQLException {
        if (academicId <= 0) {
            return Collections.emptyList();
        }

        String query = "SELECT e.*, a.numero_personal, p.fecha, p.tipo, p.id_estudiante " +
                "FROM evaluacion e " +
                "JOIN academico a ON e.id_academicoevaluador = a.id_usuario " +
                "JOIN presentacion p ON e.id_presentacion = p.id_presentacion " +
                "WHERE e.id_academicoevaluador = ?";

        List<Evaluation> evaluations = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, academicId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Evaluation evaluation = new Evaluation();
                    evaluation.setIdEvaluation(resultSet.getInt("id_evaluacion"));
                    evaluation.setCalification(resultSet.getInt("calificacion"));
                    evaluation.setDescription(resultSet.getString("comentarios"));
                    evaluation.setEvaluationDate(resultSet.getTimestamp("fecha"));

                    Academic academic = new Academic();
                    academic.setIdUser(resultSet.getInt("id_academicoevaluador"));
                    academic.setStaffNumber(resultSet.getString("numero_personal"));
                    evaluation.setAcademic(academic);

                    Presentation presentation = new Presentation();
                    presentation.setIdPresentation(resultSet.getInt("id_presentacion"));
                    presentation.setPresentationDate(resultSet.getTimestamp("fecha"));
                    presentation.setPresentationType(PresentationType.valueOf(resultSet.getString("tipo")));

                    Student student = new Student();
                    student.setIdUser(resultSet.getInt("id_estudiante"));
                    presentation.setStudent(student);

                    evaluation.setPresentation(presentation);
                    evaluations.add(evaluation);
                }
            }
        }
        return evaluations;
    }

    @Override
    public List<Evaluation> getEvaluationsByPresentation(int presentationId) throws SQLException {
        if (presentationId <= 0) {
            return Collections.emptyList();
        }

        String query = "SELECT e.*, a.numero_personal, p.fecha, p.tipo, p.id_estudiante " +
                "FROM evaluacion e " +
                "JOIN academico a ON e.id_academicoevaluador = a.id_usuario " +
                "JOIN presentacion p ON e.id_presentacion = p.id_presentacion " +
                "WHERE e.id_presentacion = ?";

        List<Evaluation> evaluations = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, presentationId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Evaluation evaluation = new Evaluation();
                    evaluation.setIdEvaluation(resultSet.getInt("id_evaluacion"));
                    evaluation.setCalification(resultSet.getInt("calificacion"));
                    evaluation.setDescription(resultSet.getString("comentarios"));
                    evaluation.setEvaluationDate(resultSet.getTimestamp("fecha"));

                    Academic academic = new Academic();
                    academic.setIdUser(resultSet.getInt("id_academicoevaluador"));
                    academic.setStaffNumber(resultSet.getString("numero_personal"));
                    evaluation.setAcademic(academic);

                    Presentation presentation = new Presentation();
                    presentation.setIdPresentation(resultSet.getInt("id_presentacion"));
                    presentation.setPresentationDate(resultSet.getTimestamp("fecha"));
                    presentation.setPresentationType(PresentationType.valueOf(resultSet.getString("tipo")));

                    Student student = new Student();
                    student.setIdUser(resultSet.getInt("id_estudiante"));
                    presentation.setStudent(student);

                    evaluation.setPresentation(presentation);
                    evaluations.add(evaluation);
                }
            }
        }
        return evaluations;
    }

    public List<Evaluation> getEvaluationsByStudent(int studentId) throws SQLException {
        if (studentId <= 0) {
            return Collections.emptyList();
        }

        String query = "SELECT e.*, a.numero_personal, p.fecha, p.tipo, p.id_estudiante " +
                "FROM evaluacion e " +
                "JOIN academico a ON e.id_academicoevaluador = a.id_usuario " +
                "JOIN presentacion p ON e.id_presentacion = p.id_presentacion " +
                "WHERE p.id_estudiante = ?";

        List<Evaluation> evaluations = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, studentId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Evaluation evaluation = new Evaluation();
                    evaluation.setIdEvaluation(resultSet.getInt("id_evaluacion"));
                    evaluation.setCalification(resultSet.getInt("calificacion"));
                    evaluation.setDescription(resultSet.getString("comentarios"));
                    evaluation.setEvaluationDate(resultSet.getTimestamp("fecha"));

                    Academic academic = new Academic();
                    academic.setIdUser(resultSet.getInt("id_academicoevaluador"));
                    academic.setStaffNumber(resultSet.getString("numero_personal"));
                    evaluation.setAcademic(academic);

                    Presentation presentation = new Presentation();
                    presentation.setIdPresentation(resultSet.getInt("id_presentacion"));
                    presentation.setPresentationDate(resultSet.getTimestamp("fecha"));
                    presentation.setPresentationType(PresentationType.valueOf(resultSet.getString("tipo")));

                    Student student = new Student();
                    student.setIdUser(resultSet.getInt("id_estudiante"));
                    presentation.setStudent(student);

                    evaluation.setPresentation(presentation);
                    evaluations.add(evaluation);
                }
            }
        }
        return evaluations;
    }

    @Override
    public boolean updateEvaluation(Evaluation evaluation) throws SQLException, IllegalArgumentException {
        if (evaluation == null || evaluation.getIdEvaluation() <= 0 ||
                evaluation.getAcademic() == null || evaluation.getPresentation() == null) {
            throw new IllegalArgumentException("Datos de evaluación inválidos");
        }

        String query = "UPDATE evaluacion SET calificacion = ?, comentarios = ?, fecha = ?, " +
                "id_academicoevaluador = ?, id_presentacion = ? WHERE id_evaluacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, evaluation.getCalification());
            preparedStatement.setString(2, evaluation.getDescription());
            preparedStatement.setTimestamp(3, evaluation.getEvaluationDate());
            preparedStatement.setInt(4, evaluation.getAcademic().getIdUser());
            preparedStatement.setInt(5, evaluation.getPresentation().getIdPresentation());
            preparedStatement.setInt(6, evaluation.getIdEvaluation());

            return preparedStatement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteEvaluation(int idEvaluation) throws SQLException {
        if (idEvaluation <= 0) {
            return false;
        }

        String query = "DELETE FROM evaluacion WHERE id_evaluacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, idEvaluation);
            return preparedStatement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean evaluationExists(int idEvaluation) throws SQLException {
        if (idEvaluation <= 0) {
            return false;
        }

        String query = "SELECT 1 FROM evaluacion WHERE id_evaluacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, idEvaluation);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    @Override
    public int countAllEvaluations() throws SQLException {
        String query = "SELECT COUNT(*) FROM evaluacion";

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }
}
