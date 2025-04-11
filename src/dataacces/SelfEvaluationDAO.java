package dataacces;

import logic.SelfEvaluation;
import logic.interfaces.ISelfEvaluationDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SelfEvaluationDAO implements ISelfEvaluationDAO {

    public boolean addSelfEvaluation(SelfEvaluation selfEvaluation) throws SQLException {
        String sql = "INSERT INTO autoevaluacion (feedback, calificacion, id_estudiante) VALUES (?, ?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, selfEvaluation.getFeedBack());
            statement.setFloat(2, selfEvaluation.getCalification());
            statement.setInt(3, selfEvaluation.getStudent().getIdUser());

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        selfEvaluation.setIdSelfEvaluation(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    public SelfEvaluation getSelfEvaluationById(int idSelfEvaluation) throws SQLException {
        String sql = "SELECT * FROM autoevaluacion WHERE id_autoevaluacion = ?";
        SelfEvaluation selfEvaluation = null;

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idSelfEvaluation);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    selfEvaluation = new SelfEvaluation();
                    selfEvaluation.setIdSelfEvaluation(resultSet.getInt("id_autoevaluacion"));
                    selfEvaluation.setFeedBack(resultSet.getString("feedback"));
                    selfEvaluation.setCalification(resultSet.getFloat("calificacion"));

                    StudentDAO studentDAO = new StudentDAO();
                    selfEvaluation.setStudent(studentDAO.getStudentById(resultSet.getInt("id_estudiante")));
                }
            }
        }
        return selfEvaluation;
    }

    public List<SelfEvaluation> getAllSelfEvaluations() throws SQLException {
        String sql = "SELECT * FROM autoevaluacion";
        List<SelfEvaluation> selfEvaluations = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                SelfEvaluation selfEvaluation = new SelfEvaluation();
                selfEvaluation.setIdSelfEvaluation(resultSet.getInt("id_autoevaluacion"));
                selfEvaluation.setFeedBack(resultSet.getString("feedback"));
                selfEvaluation.setCalification(resultSet.getFloat("calificacion"));

                StudentDAO studentDAO = new StudentDAO();
                selfEvaluation.setStudent(studentDAO.getStudentById(resultSet.getInt("id_estudiante")));

                selfEvaluations.add(selfEvaluation);
            }
        }
        return selfEvaluations;
    }

    public List<SelfEvaluation> getSelfEvaluationsByStudent(int studentId) throws SQLException {
        String sql = "SELECT * FROM autoevaluacion WHERE id_estudiante = ?";
        List<SelfEvaluation> selfEvaluations = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, studentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    SelfEvaluation selfEvaluation = new SelfEvaluation();
                    selfEvaluation.setIdSelfEvaluation(resultSet.getInt("id_autoevaluacion"));
                    selfEvaluation.setFeedBack(resultSet.getString("feedback"));
                    selfEvaluation.setCalification(resultSet.getFloat("calificacion"));

                    StudentDAO studentDAO = new StudentDAO();
                    selfEvaluation.setStudent(studentDAO.getStudentById(studentId));

                    selfEvaluations.add(selfEvaluation);
                }
            }
        }
        return selfEvaluations;
    }

    public boolean updateSelfEvaluation(SelfEvaluation selfEvaluation) throws SQLException {
        String sql = "UPDATE autoevaluacion SET feedback = ?, calificacion = ?, id_estudiante = ? WHERE id_autoevaluacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, selfEvaluation.getFeedBack());
            statement.setFloat(2, selfEvaluation.getCalification());
            statement.setInt(3, selfEvaluation.getStudent().getIdUser());
            statement.setInt(4, selfEvaluation.getIdSelfEvaluation());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteSelfEvaluation(int idSelfEvaluation) throws SQLException {
        String sql = "DELETE FROM autoevaluacion WHERE id_autoevaluacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idSelfEvaluation);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean selfEvaluationExists(int idSelfEvaluation) throws SQLException {
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
        String sql = "SELECT COUNT(*) FROM autoevaluacion";

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return 0;
        }
    }
}