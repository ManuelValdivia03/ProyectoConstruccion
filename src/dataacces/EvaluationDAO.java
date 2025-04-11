package dataacces;

import logic.Evaluation;
import logic.Academic;
import logic.Presentation;
import logic.interfaces.IEvaluationDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvaluationDAO implements IEvaluationDAO {

    @Override
    public boolean addEvaluation(Evaluation evaluation) throws SQLException {
        String sql = "INSERT INTO evaluacion (calificacion, comentarios, fecha, id_academicoevaluador, id_presentacion) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, evaluation.getCalification());
            statement.setString(2, evaluation.getDescription());
            statement.setTimestamp(3, evaluation.getEvaluationDate());
            statement.setInt(4, evaluation.getAcademic().getIdUser());
            statement.setInt(5, evaluation.getPresentation().getIdPresentation());

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        evaluation.setIdEvaluation(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    @Override
    public Evaluation getEvaluationById(int idEvaluation) throws SQLException {
        String sql = "SELECT * FROM evaluacion WHERE id_evaluacion = ?";
        Evaluation evaluation = null;

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idEvaluation);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    evaluation = new Evaluation();
                    evaluation.setIdEvaluation(resultSet.getInt("id_evaluacion"));
                    evaluation.setCalification(resultSet.getInt("calificacion"));
                    evaluation.setDescription(resultSet.getString("comentarios"));
                    evaluation.setEvaluationDate(resultSet.getTimestamp("fecha"));

                    // Get associated academic
                    AcademicDAO academicDAO = new AcademicDAO();
                    evaluation.setAcademic(academicDAO.getAcademicById(resultSet.getInt("id_academicoevaluador")));

                    // Get associated presentation
                    PresentationDAO presentationDAO = new PresentationDAO();
                    evaluation.setPresentation(presentationDAO.getPresentationById(resultSet.getInt("id_presentacion")));
                }
            }
        }
        return evaluation;
    }

    @Override
    public List<Evaluation> getAllEvaluations() throws SQLException {
        String sql = "SELECT * FROM evaluacion";
        List<Evaluation> evaluations = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Evaluation evaluation = new Evaluation();
                evaluation.setIdEvaluation(resultSet.getInt("id_evaluacion"));
                evaluation.setCalification(resultSet.getInt("calificacion"));
                evaluation.setDescription(resultSet.getString("comentarios"));
                evaluation.setEvaluationDate(resultSet.getTimestamp("fecha"));

                // Get associated academic
                AcademicDAO academicDAO = new AcademicDAO();
                evaluation.setAcademic(academicDAO.getAcademicById(resultSet.getInt("id_academicoevaluador")));

                // Get associated presentation
                PresentationDAO presentationDAO = new PresentationDAO();
                evaluation.setPresentation(presentationDAO.getPresentationById(resultSet.getInt("id_presentacion")));

                evaluations.add(evaluation);
            }
        }
        return evaluations;
    }

    @Override
    public List<Evaluation> getEvaluationsByAcademic(int academicId) throws SQLException {
        String sql = "SELECT * FROM evaluacion WHERE id_academicoevaluador = ?";
        List<Evaluation> evaluations = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, academicId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Evaluation evaluation = new Evaluation();
                    evaluation.setIdEvaluation(resultSet.getInt("id_evaluacion"));
                    evaluation.setCalification(resultSet.getInt("calificacion"));
                    evaluation.setDescription(resultSet.getString("comentarios"));
                    evaluation.setEvaluationDate(resultSet.getTimestamp("fecha"));

                    // Get associated academic
                    AcademicDAO academicDAO = new AcademicDAO();
                    evaluation.setAcademic(academicDAO.getAcademicById(academicId));

                    // Get associated presentation
                    PresentationDAO presentationDAO = new PresentationDAO();
                    evaluation.setPresentation(presentationDAO.getPresentationById(resultSet.getInt("id_presentacion")));

                    evaluations.add(evaluation);
                }
            }
        }
        return evaluations;
    }

    @Override
    public List<Evaluation> getEvaluationsByPresentation(int presentationId) throws SQLException {
        String sql = "SELECT * FROM evaluacion WHERE id_presentacion = ?";
        List<Evaluation> evaluations = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, presentationId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Evaluation evaluation = new Evaluation();
                    evaluation.setIdEvaluation(resultSet.getInt("id_evaluacion"));
                    evaluation.setCalification(resultSet.getInt("calificacion"));
                    evaluation.setDescription(resultSet.getString("comentarios"));
                    evaluation.setEvaluationDate(resultSet.getTimestamp("fecha"));

                    // Get associated academic
                    AcademicDAO academicDAO = new AcademicDAO();
                    evaluation.setAcademic(academicDAO.getAcademicById(resultSet.getInt("id_academicoevaluador")));

                    // Get associated presentation
                    PresentationDAO presentationDAO = new PresentationDAO();
                    evaluation.setPresentation(presentationDAO.getPresentationById(presentationId));

                    evaluations.add(evaluation);
                }
            }
        }
        return evaluations;
    }

    @Override
    public boolean updateEvaluation(Evaluation evaluation) throws SQLException {
        String sql = "UPDATE evaluacion SET calificacion = ?, comentarios = ?, fecha = ?, id_academicoevaluador = ?, id_presentacion = ? WHERE id_evaluacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, evaluation.getCalification());
            statement.setString(2, evaluation.getDescription());
            statement.setTimestamp(3, evaluation.getEvaluationDate());
            statement.setInt(4, evaluation.getAcademic().getIdUser());
            statement.setInt(5, evaluation.getPresentation().getIdPresentation());
            statement.setInt(6, evaluation.getIdEvaluation());

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteEvaluation(int idEvaluation) throws SQLException {
        String sql = "DELETE FROM evaluacion WHERE id_evaluacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idEvaluation);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean evaluationExists(int idEvaluation) throws SQLException {
        String sql = "SELECT 1 FROM evaluacion WHERE id_evaluacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idEvaluation);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    @Override
    public int countEvaluations() throws SQLException {
        String sql = "SELECT COUNT(*) FROM evaluacion";

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