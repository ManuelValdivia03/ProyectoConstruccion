package dataacces;

import logic.Evaluation;
import logic.Academic;
import logic.Presentation;
import logic.Student;
import logic.enums.PresentationType;
import logic.interfaces.IEvaluationDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvaluationDAO implements IEvaluationDAO {
    private final AcademicDAO academicDAO;
    private final PresentationDAO presentationDAO;

    public EvaluationDAO() {
        this.academicDAO = new AcademicDAO();
        this.presentationDAO = new PresentationDAO();
    }

    @Override
    public boolean addEvaluation(Evaluation evaluation) throws SQLException {
        if (evaluation == null || evaluation.getAcademic() == null || evaluation.getPresentation() == null) {
            throw new SQLException("Evaluation, Academic y Presentation no pueden ser nulos");
        }

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
                return false;
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    evaluation.setIdEvaluation(generatedKeys.getInt(1));
                    return true;
                } else {
                    throw new SQLException("No se pudo obtener el ID generado");
                }
            }
        }
    }

    @Override
    public Evaluation getEvaluationById(int idEvaluation) throws SQLException {
        if (idEvaluation <= 0) {
            return null;
        }

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
                    // Crear Academic
                    Academic academic = new Academic();
                    academic.setIdUser(rs.getInt("id_academicoevaluador"));
                    academic.setStaffNumber(rs.getString("numero_personal"));

                    // Crear Student
                    Student student = new Student();
                    student.setIdUser(rs.getInt("id_estudiante"));

                    // Crear Presentation
                    Presentation presentation = new Presentation();
                    presentation.setIdPresentation(rs.getInt("id_presentacion"));
                    presentation.setPresentationDate(rs.getTimestamp("p_fecha"));
                    presentation.setPresentationType(PresentationType.fromDatabase(rs.getString("tipo")));
                    presentation.setStudent(student);

                    // Crear y retornar Evaluation usando el constructor
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
        }
        return null;
    }

    @Override
    public List<Evaluation> getAllEvaluations() throws SQLException {
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
        }
        return evaluations;
    }

    @Override
    public List<Evaluation> getEvaluationsByAcademic(int academicId) throws SQLException {
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
        }
        return evaluations;
    }

    @Override
    public List<Evaluation> getEvaluationsByPresentation(int presentationId) throws SQLException {
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

                    evaluations.add(evaluation);
                }
            }
        }
        return evaluations;
    }

    @Override
    public boolean updateEvaluation(Evaluation evaluation) throws SQLException {
        if (evaluation == null || evaluation.getIdEvaluation() <= 0 ||
                evaluation.getAcademic() == null || evaluation.getPresentation() == null) {
            return false;
        }

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

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteEvaluation(int idEvaluation) throws SQLException {
        if (idEvaluation <= 0) {
            return false;
        }

        String sql = "DELETE FROM evaluacion WHERE id_evaluacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, idEvaluation);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean evaluationExists(int idEvaluation) throws SQLException {
        if (idEvaluation <= 0) {
            return false;
        }

        String sql = "SELECT 1 FROM evaluacion WHERE id_evaluacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, idEvaluation);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public int countEvaluations() throws SQLException {
        String sql = "SELECT COUNT(*) FROM evaluacion";

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }
}