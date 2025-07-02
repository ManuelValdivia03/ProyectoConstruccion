package logic.daos;

import dataaccess.ConnectionDataBase;
import logic.logicclasses.Presentation;
import logic.logicclasses.Student;
import logic.enums.PresentationType;
import logic.interfaces.IPresentationDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PresentationDAO implements IPresentationDAO {
    private static final Presentation EMPTY_PRESENTATION = new Presentation();

    public boolean addPresentation(Presentation presentation) throws SQLException, IllegalArgumentException {
        if (presentation == null || presentation.getStudent() == null ||
                presentation.getPresentationType() == null) {
            throw new IllegalArgumentException("Datos de presentación incompletos");
        }

        String query = "INSERT INTO presentacion (tipo, fecha, id_estudiante) VALUES (?, ?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, presentation.getPresentationType().name());
            preparedStatement.setTimestamp(2, presentation.getPresentationDate());
            preparedStatement.setInt(3, presentation.getStudent().getIdUser());

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        presentation.setIdPresentation(generatedId);
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public Presentation getPresentationById(int idPresentation) throws SQLException {
        if (idPresentation <= 0) {
            return EMPTY_PRESENTATION;
        }

        String query = "SELECT p.*, u.nombre_completo, u.telefono, e.matricula " +
                "FROM presentacion p " +
                "JOIN estudiante e ON p.id_estudiante = e.id_usuario " +
                "JOIN usuario u ON e.id_usuario = u.id_usuario " +
                "WHERE p.id_presentacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, idPresentation);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    Presentation presentation = new Presentation();
                    presentation.setIdPresentation(resultSet.getInt("id_presentacion"));
                    presentation.setPresentationDate(resultSet.getTimestamp("fecha"));

                    String tipoStr = resultSet.getString("tipo");
                    presentation.setPresentationType(PresentationType.valueOf(tipoStr));

                    Student student = new Student();
                    student.setIdUser(resultSet.getInt("id_estudiante"));
                    student.setFullName(resultSet.getString("nombre_completo"));
                    student.setCellphone(resultSet.getString("telefono"));
                    student.setEnrollment(resultSet.getString("matricula"));

                    presentation.setStudent(student);
                    return presentation;
                }
            }
        }
        return EMPTY_PRESENTATION;
    }

    public List<Presentation> getAllPresentations() throws SQLException {
        String query = "SELECT p.*, u.nombre_completo, u.telefono, e.matricula " +
                "FROM presentacion p " +
                "JOIN estudiante e ON p.id_estudiante = e.id_usuario " +
                "JOIN usuario u ON e.id_usuario = u.id_usuario";

        List<Presentation> presentations = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                Presentation presentation = new Presentation();
                presentation.setIdPresentation(resultSet.getInt("id_presentacion"));
                presentation.setPresentationType(PresentationType.valueOf(resultSet.getString("tipo")));
                presentation.setPresentationDate(resultSet.getTimestamp("fecha"));

                Student student = new Student();
                student.setIdUser(resultSet.getInt("id_estudiante"));
                student.setFullName(resultSet.getString("nombre_completo"));
                student.setCellphone(resultSet.getString("telefono"));
                student.setEnrollment(resultSet.getString("matricula"));

                presentation.setStudent(student);
                presentations.add(presentation);
            }
        }
        return presentations;
    }

    public List<Presentation> getPresentationsByStudent(int studentId) throws SQLException {
        if (studentId <= 0) {
            return Collections.emptyList();
        }

        String query = "SELECT * FROM presentacion WHERE id_estudiante = ?";
        List<Presentation> presentations = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, studentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Presentation presentation = new Presentation();
                    presentation.setIdPresentation(resultSet.getInt("id_presentacion"));
                    presentation.setPresentationType(
                            PresentationType.valueOf(resultSet.getString("tipo")));
                    presentation.setPresentationDate(resultSet.getTimestamp("fecha"));
                    StudentDAO studentDAO = new StudentDAO();
                    presentation.setStudent(studentDAO.getStudentById(studentId));

                    presentations.add(presentation);
                }
            }
        }
        return presentations;
    }

    public List<Presentation> getPresentationsByType(String presentationType) throws SQLException {
        if (presentationType == null || presentationType.isEmpty()) {
            return Collections.emptyList();
        }

        String query = "SELECT * FROM presentacion WHERE tipo = ?";
        List<Presentation> presentations = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, presentationType);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Presentation presentation = new Presentation();
                    presentation.setIdPresentation(resultSet.getInt("id_presentacion"));
                    presentation.setPresentationType(
                            PresentationType.valueOf(resultSet.getString("tipo")));
                    presentation.setPresentationDate(resultSet.getTimestamp("fecha"));
                    StudentDAO studentDAO = new StudentDAO();
                    presentation.setStudent(studentDAO.getStudentById(resultSet.getInt("id_estudiante")));

                    presentations.add(presentation);
                }
            }
        }
        return presentations;
    }

    public boolean updatePresentation(Presentation presentation) throws SQLException, IllegalArgumentException {
        if (presentation == null || presentation.getIdPresentation() <= 0 ||
            presentation.getStudent() == null || presentation.getPresentationType() == null) {
            throw new IllegalArgumentException("Datos de presentación incompletos o inválidos");
        }

        String query = "UPDATE presentacion SET tipo = ?, fecha = ?, id_estudiante = ? WHERE id_presentacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, presentation.getPresentationType().name());
            preparedStatement.setTimestamp(2, presentation.getPresentationDate());
            preparedStatement.setInt(3, presentation.getStudent().getIdUser());
            preparedStatement.setInt(4, presentation.getIdPresentation());

            return preparedStatement.executeUpdate() > 0;
        }
    }

    public boolean deletePresentation(int idPresentation) throws SQLException {
        if (idPresentation <= 0) {
            return false;
        }

        String query = "DELETE FROM presentacion WHERE id_presentacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, idPresentation);
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public boolean presentationExists(int idPresentation) throws SQLException {
        if (idPresentation <= 0) {
            return false;
        }

        String query = "SELECT 1 FROM presentacion WHERE id_presentacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, idPresentation);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public int countPresentations() throws SQLException {
        String query = "SELECT COUNT(*) FROM presentacion";

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }
}
