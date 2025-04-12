package dataacces;

import logic.Presentation;
import logic.Student;
import logic.enums.PresentationType;
import logic.interfaces.IPresentationDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PresentationDAO implements IPresentationDAO {

    public boolean addPresentation(Presentation presentation) throws SQLException {
        if (presentation == null || presentation.getStudent() == null ||
                presentation.getPresentationType() == null) {
            throw new SQLException();
        }

        String sql = "INSERT INTO presentacion (tipo, fecha, id_estudiante) VALUES (?, ?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, presentation.getPresentationType().name());
            ps.setTimestamp(2, presentation.getPresentationDate());
            ps.setInt(3, presentation.getStudent().getIdUser());


            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    presentation.setIdPresentation(generatedKeys.getInt(1));
                    return true;
                }
            }

            throw new SQLException();
        }
    }

    public Presentation getPresentationById(int idPresentation) throws SQLException {
        if (idPresentation <= 0) {
            return null;
        }

        String sql = "SELECT p.*, u.nombre_completo, u.telefono, e.matricula " +
                "FROM presentacion p " +
                "JOIN estudiante e ON p.id_estudiante = e.id_usuario " +
                "JOIN usuario u ON e.id_usuario = u.id_usuario " +
                "WHERE p.id_presentacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, idPresentation);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Presentation presentation = new Presentation();
                    presentation.setIdPresentation(rs.getInt("id_presentacion"));
                    presentation.setPresentationDate(rs.getTimestamp("fecha"));

                    String tipoStr = rs.getString("tipo");
                    try {
                        presentation.setPresentationType(PresentationType.valueOf(tipoStr));
                    } catch (IllegalArgumentException e) {
                        throw new SQLException();
                    }

                    Student student = new Student();
                    student.setIdUser(rs.getInt("id_estudiante"));
                    student.setFullName(rs.getString("nombre_completo"));
                    student.setCellphone(rs.getString("telefono"));
                    student.setEnrollment(rs.getString("matricula"));

                    presentation.setStudent(student);
                    return presentation;
                }
            }
        }
        return null;
    }

    public List<Presentation> getAllPresentations() throws SQLException {
        String sql = "SELECT p.*, u.nombre_completo, u.telefono, e.matricula " +
                "FROM presentacion p " +
                "JOIN estudiante e ON p.id_estudiante = e.id_usuario " +
                "JOIN usuario u ON e.id_usuario = u.id_usuario";

        List<Presentation> presentations = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Presentation presentation = new Presentation();
                presentation.setIdPresentation(rs.getInt("id_presentacion"));
                presentation.setPresentationType(PresentationType.valueOf(rs.getString("tipo")));
                presentation.setPresentationDate(rs.getTimestamp("fecha"));


                Student student = new Student();
                student.setIdUser(rs.getInt("id_estudiante"));
                student.setFullName(rs.getString("nombre_completo"));
                student.setCellphone(rs.getString("telefono"));
                student.setEnrollment(rs.getString("matricula"));

                presentation.setStudent(student);
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
        String sql = "SELECT * FROM presentacion WHERE tipo = ?";
        List<Presentation> presentations = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

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

    public boolean updatePresentation(Presentation presentation) throws SQLException {
        if (presentation == null) {
            throw new SQLException();
        }
        if (presentation.getIdPresentation() <= 0) {
            throw new SQLException();
        }
        if (presentation.getStudent() == null || presentation.getPresentationType() == null) {
            throw new SQLException();
        }

        String sql = "UPDATE presentacion SET tipo  = ?, fecha = ?, id_estudiante = ? WHERE id_presentacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, presentation.getPresentationType().name());
            ps.setTimestamp(2, presentation.getPresentationDate());
            ps.setInt(3, presentation.getStudent().getIdUser());
            ps.setInt(4, presentation.getIdPresentation());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean deletePresentation(int idPresentation) throws SQLException {
        // Validar ID
        if (idPresentation <= 0) {
            return false;
        }

        String sql = "DELETE FROM presentacion WHERE id_presentacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, idPresentation);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public boolean presentationExists(int idPresentation) throws SQLException {
        if (idPresentation <= 0) {
            return false;
        }

        String sql = "SELECT 1 FROM presentacion WHERE id_presentacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, idPresentation);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
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