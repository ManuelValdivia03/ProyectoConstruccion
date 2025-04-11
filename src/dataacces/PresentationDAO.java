package dataacces;

import logic.Presentation;
import logic.enums.PresentationType;
import logic.interfaces.IPresentationDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PresentationDAO implements IPresentationDAO {

    public boolean addPresentation(Presentation presentation) throws SQLException {
        String sql = "INSERT INTO presentacion (fecha_presentacion, tipo_presentacion, id_estudiante) VALUES (?, ?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setTimestamp(1, presentation.getPresentationDate());
            statement.setString(2, presentation.getPresentationType().name());
            statement.setInt(3, presentation.getStudent().getIdUser());

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        presentation.setIdPresentation(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    public Presentation getPresentationById(int idPresentation) throws SQLException {
        String sql = "SELECT * FROM presentacion WHERE id_presentacion = ?";
        Presentation presentation = null;

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idPresentation);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    presentation = new Presentation();
                    presentation.setIdPresentation(resultSet.getInt("id_presentacion"));
                    presentation.setPresentationDate(resultSet.getTimestamp("fecha_presentacion"));
                    presentation.setPresentationType(
                            PresentationType.valueOf(resultSet.getString("tipo_presentacion")));
                    StudentDAO studentDAO = new StudentDAO();
                    presentation.setStudent(studentDAO.getStudentById(resultSet.getInt("id_estudiante")));
                }
            }
        }
        return presentation;
    }

    public List<Presentation> getAllPresentations() throws SQLException {
        String sql = "SELECT * FROM presentacion";
        List<Presentation> presentations = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Presentation presentation = new Presentation();
                presentation.setIdPresentation(resultSet.getInt("id_presentacion"));
                presentation.setPresentationDate(resultSet.getTimestamp("fecha_presentacion"));
                presentation.setPresentationType(
                        PresentationType.valueOf(resultSet.getString("tipo_presentacion")));

                StudentDAO studentDAO = new StudentDAO();
                presentation.setStudent(studentDAO.getStudentById(resultSet.getInt("id_estudiante")));

                presentations.add(presentation);
            }
        }
        return presentations;
    }

    public List<Presentation> getPresentationsByStudent(int studentId) throws SQLException {
        String sql = "SELECT * FROM presentacion WHERE id_estudiante = ?";
        List<Presentation> presentations = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, studentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Presentation presentation = new Presentation();
                    presentation.setIdPresentation(resultSet.getInt("id_presentacion"));
                    presentation.setPresentationDate(resultSet.getTimestamp("fecha_presentacion"));
                    presentation.setPresentationType(
                            PresentationType.valueOf(resultSet.getString("tipo_presentacion")));
                    StudentDAO studentDAO = new StudentDAO();
                    presentation.setStudent(studentDAO.getStudentById(studentId));

                    presentations.add(presentation);
                }
            }
        }
        return presentations;
    }

    public List<Presentation> getPresentationsByType(String presentationType) throws SQLException {
        String sql = "SELECT * FROM presentacion WHERE tipo_presentacion = ?";
        List<Presentation> presentations = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, presentationType);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Presentation presentation = new Presentation();
                    presentation.setIdPresentation(resultSet.getInt("id_presentacion"));
                    presentation.setPresentationDate(resultSet.getTimestamp("fecha_presentacion"));
                    presentation.setPresentationType(
                            PresentationType.valueOf(resultSet.getString("tipo_presentacion")));
                    StudentDAO studentDAO = new StudentDAO();
                    presentation.setStudent(studentDAO.getStudentById(resultSet.getInt("id_estudiante")));

                    presentations.add(presentation);
                }
            }
        }
        return presentations;
    }

    public boolean updatePresentation(Presentation presentation) throws SQLException {
        String sql = "UPDATE presentacion SET fecha_presentacion = ?, tipo_presentacion = ?, id_estudiante = ? WHERE id_presentacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setTimestamp(1, presentation.getPresentationDate());
            statement.setString(2, presentation.getPresentationType().name());
            statement.setInt(3, presentation.getStudent().getIdUser());
            statement.setInt(4, presentation.getIdPresentation());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean deletePresentation(int idPresentation) throws SQLException {
        String sql = "DELETE FROM presentacion WHERE id_presentacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idPresentation);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean presentationExists(int idPresentation) throws SQLException {
        String sql = "SELECT 1 FROM presentacion WHERE id_presentacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idPresentation);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public int countPresentations() throws SQLException {
        String sql = "SELECT COUNT(*) FROM presentacion";

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