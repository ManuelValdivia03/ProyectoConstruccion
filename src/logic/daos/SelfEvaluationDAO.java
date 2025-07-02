package logic.daos;

import dataaccess.ConnectionDataBase;
import logic.logicclasses.SelfEvaluation;
import logic.interfaces.ISelfEvaluationDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SelfEvaluationDAO implements ISelfEvaluationDAO {
    private static final SelfEvaluation EMPTY_SELFEVALUATION = new SelfEvaluation();
    private final StudentDAO studentDAO;

    public SelfEvaluationDAO() {
        this.studentDAO = new StudentDAO();
    }

    public boolean addSelfEvaluation(SelfEvaluation selfEvaluation) throws SQLException, IllegalArgumentException {
        validateSelfEvaluation(selfEvaluation);

        String query = "INSERT INTO autoevaluacion (calificacion, comentarios, id_usuario) VALUES (?, ?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            statement.setFloat(1, selfEvaluation.getCalification());
            statement.setString(2, selfEvaluation.getFeedBack());
            statement.setInt(3, selfEvaluation.getStudent().getIdUser());

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        selfEvaluation.setIdSelfEvaluation(generatedId);
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public SelfEvaluation getSelfEvaluationById(int idSelfEvaluation) throws SQLException {
        if (idSelfEvaluation <= 0) {
            return EMPTY_SELFEVALUATION;
        }

        String query = "SELECT * FROM autoevaluacion WHERE id_autoevaluacion = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, idSelfEvaluation);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    SelfEvaluation selfEvaluation = new SelfEvaluation();
                    selfEvaluation.setIdSelfEvaluation(resultSet.getInt("id_autoevaluacion"));
                    selfEvaluation.setFeedBack(resultSet.getString("comentarios"));
                    selfEvaluation.setCalification(resultSet.getFloat("calificacion"));
                    selfEvaluation.setStudent(studentDAO.getStudentById(resultSet.getInt("id_usuario")));
                    return selfEvaluation;
                }
            }
        }
        return EMPTY_SELFEVALUATION;
    }

    public List<SelfEvaluation> getAllSelfEvaluations() throws SQLException {
        String query = "SELECT * FROM autoevaluacion";
        List<SelfEvaluation> selfEvaluations = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                SelfEvaluation selfEvaluation = new SelfEvaluation();
                selfEvaluation.setIdSelfEvaluation(resultSet.getInt("id_autoevaluacion"));
                selfEvaluation.setFeedBack(resultSet.getString("comentarios"));
                selfEvaluation.setCalification(resultSet.getFloat("calificacion"));
                selfEvaluation.setStudent(studentDAO.getStudentById(resultSet.getInt("id_usuario")));
                selfEvaluations.add(selfEvaluation);
            }
        }
        return selfEvaluations;
    }

    public List<SelfEvaluation> getSelfEvaluationsByStudent(int studentId) throws SQLException {
        if (studentId <= 0) {
            return Collections.emptyList();
        }

        String query = "SELECT * FROM autoevaluacion WHERE id_usuario = ?";
        List<SelfEvaluation> selfEvaluations = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

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
        }
        return selfEvaluations;
    }

    public SelfEvaluation getSelfEvaluationByStudent(int studentId) throws SQLException {
        if (studentId <= 0) {
            return EMPTY_SELFEVALUATION;
        }

        String query = "SELECT * FROM autoevaluacion WHERE id_usuario = ? ORDER BY id_autoevaluacion DESC LIMIT 1";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, studentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    SelfEvaluation selfEvaluation = new SelfEvaluation();
                    selfEvaluation.setIdSelfEvaluation(resultSet.getInt("id_autoevaluacion"));
                    selfEvaluation.setFeedBack(resultSet.getString("comentarios"));
                    selfEvaluation.setCalification(resultSet.getFloat("calificacion"));
                    selfEvaluation.setStudent(studentDAO.getStudentById(studentId));
                    return selfEvaluation;
                }
            }
        }
        return EMPTY_SELFEVALUATION;
    }

    public boolean updateSelfEvaluation(SelfEvaluation selfEvaluation) throws SQLException, IllegalArgumentException {
        if (selfEvaluation == null ||
                selfEvaluation.getIdSelfEvaluation() <= 0 ||
                selfEvaluation.getStudent() == null) {
            throw new IllegalArgumentException("Datos de autoevaluación incompletos");
        }

        String query = "UPDATE autoevaluacion SET calificacion = ?, comentarios = ?, id_usuario = ? WHERE id_autoevaluacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setFloat(1, selfEvaluation.getCalification());
            statement.setString(2, selfEvaluation.getFeedBack());
            statement.setInt(3, selfEvaluation.getStudent().getIdUser());
            statement.setInt(4, selfEvaluation.getIdSelfEvaluation());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteSelfEvaluation(int idSelfEvaluation) throws SQLException {
        if (idSelfEvaluation <= 0) {
            return false;
        }

        String query = "DELETE FROM autoevaluacion WHERE id_autoevaluacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, idSelfEvaluation);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean selfEvaluationExists(int idSelfEvaluation) throws SQLException {
        if (idSelfEvaluation <= 0) {
            return false;
        }

        String sql = "SELECT 1 FROM autoevaluacion WHERE id_autoevaluacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idSelfEvaluation);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public int countSelfEvaluations() throws SQLException {
        String query = "SELECT COUNT(*) FROM autoevaluacion";

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    private void validateSelfEvaluation(SelfEvaluation selfEvaluation) throws IllegalArgumentException {
        if (selfEvaluation == null ||
                selfEvaluation.getFeedBack() == null ||
                selfEvaluation.getStudent() == null) {
            throw new IllegalArgumentException("Datos de autoevaluación incompletos");
        }
    }
}
