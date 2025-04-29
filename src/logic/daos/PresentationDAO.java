package logic.daos;

import dataaccess.ConnectionDataBase;
import logic.logicclasses.Presentation;
import logic.logicclasses.Student;
import logic.enums.PresentationType;
import logic.interfaces.IPresentationDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PresentationDAO implements IPresentationDAO {
    private static final Logger logger = LogManager.getLogger(PresentationDAO.class);

    public boolean addPresentation(Presentation presentation) throws SQLException {
        if (presentation == null || presentation.getStudent() == null ||
                presentation.getPresentationType() == null) {
            logger.warn("Intento de agregar presentación con datos nulos");
            throw new SQLException("Datos de presentación incompletos");
        }

        logger.debug("Agregando nueva presentación para estudiante ID: {}", presentation.getStudent().getIdUser());

        String sql = "INSERT INTO presentacion (tipo, fecha, id_estudiante) VALUES (?, ?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, presentation.getPresentationType().name());
            ps.setTimestamp(2, presentation.getPresentationDate());
            ps.setInt(3, presentation.getStudent().getIdUser());

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                logger.warn("No se pudo agregar la presentación, ninguna fila afectada");
                return false;
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    presentation.setIdPresentation(generatedId);
                    logger.info("Presentación agregada exitosamente con ID: {}", generatedId);
                    return true;
                }
            }

            logger.error("No se pudo obtener el ID generado para la presentación");
            throw new SQLException("No se pudo obtener el ID generado");
        } catch (SQLException e) {
            logger.error("Error al agregar presentación", e);
            throw e;
        }
    }

    public Presentation getPresentationById(int idPresentation) throws SQLException {
        if (idPresentation <= 0) {
            logger.warn("Intento de buscar presentación con ID inválido: {}", idPresentation);
            return null;
        }

        logger.debug("Buscando presentación por ID: {}", idPresentation);

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
                    logger.debug("Presentación encontrada con ID: {}", idPresentation);

                    Presentation presentation = new Presentation();
                    presentation.setIdPresentation(rs.getInt("id_presentacion"));
                    presentation.setPresentationDate(rs.getTimestamp("fecha"));

                    String tipoStr = rs.getString("tipo");
                    try {
                        presentation.setPresentationType(PresentationType.valueOf(tipoStr));
                    } catch (IllegalArgumentException e) {
                        logger.error("Tipo de presentación inválido: {}", tipoStr);
                        throw new SQLException("Tipo de presentación inválido");
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
        } catch (SQLException e) {
            logger.error("Error al obtener presentación con ID: {}", idPresentation, e);
            throw e;
        }
        logger.info("No se encontró presentación con ID: {}", idPresentation);
        return null;
    }

    public List<Presentation> getAllPresentations() throws SQLException {
        logger.info("Obteniendo todas las presentaciones");

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
            logger.debug("Se encontraron {} presentaciones", presentations.size());
        } catch (SQLException e) {
            logger.error("Error al obtener todas las presentaciones", e);
            throw e;
        }
        return presentations;
    }

    public List<Presentation> getPresentationsByStudent(int studentId) throws SQLException {
        if (studentId <= 0) {
            logger.warn("Intento de buscar presentaciones con ID de estudiante inválido: {}", studentId);
            return new ArrayList<>();
        }

        logger.debug("Buscando presentaciones por estudiante ID: {}", studentId);

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
            logger.debug("Se encontraron {} presentaciones para estudiante ID: {}", presentations.size(), studentId);
        } catch (SQLException e) {
            logger.error("Error al obtener presentaciones por estudiante ID: {}", studentId, e);
            throw e;
        }
        return presentations;
    }

    public List<Presentation> getPresentationsByType(String presentationType) throws SQLException {
        if (presentationType == null || presentationType.isEmpty()) {
            logger.warn("Intento de buscar presentaciones con tipo nulo o vacío");
            return new ArrayList<>();
        }

        logger.debug("Buscando presentaciones por tipo: {}", presentationType);

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
            logger.debug("Se encontraron {} presentaciones de tipo: {}", presentations.size(), presentationType);
        } catch (SQLException e) {
            logger.error("Error al obtener presentaciones por tipo: {}", presentationType, e);
            throw e;
        }
        return presentations;
    }

    public boolean updatePresentation(Presentation presentation) throws SQLException {
        if (presentation == null) {
            logger.warn("Intento de actualizar presentación nula");
            throw new SQLException("Presentación no puede ser nula");
        }
        if (presentation.getIdPresentation() <= 0) {
            logger.warn("Intento de actualizar presentación con ID inválido");
            throw new SQLException("ID de presentación inválido");
        }
        if (presentation.getStudent() == null || presentation.getPresentationType() == null) {
            logger.warn("Intento de actualizar presentación con datos incompletos");
            throw new SQLException("Datos de presentación incompletos");
        }

        logger.debug("Actualizando presentación ID: {}", presentation.getIdPresentation());

        String sql = "UPDATE presentacion SET tipo = ?, fecha = ?, id_estudiante = ? WHERE id_presentacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, presentation.getPresentationType().name());
            ps.setTimestamp(2, presentation.getPresentationDate());
            ps.setInt(3, presentation.getStudent().getIdUser());
            ps.setInt(4, presentation.getIdPresentation());

            boolean result = ps.executeUpdate() > 0;
            if (result) {
                logger.info("Presentación ID {} actualizada exitosamente", presentation.getIdPresentation());
            } else {
                logger.warn("No se encontró presentación ID {} para actualizar", presentation.getIdPresentation());
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al actualizar presentación ID {}", presentation.getIdPresentation(), e);
            throw e;
        }
    }

    public boolean deletePresentation(int idPresentation) throws SQLException {
        if (idPresentation <= 0) {
            logger.warn("Intento de eliminar presentación con ID inválido: {}", idPresentation);
            return false;
        }

        logger.debug("Eliminando presentación ID: {}", idPresentation);

        String sql = "DELETE FROM presentacion WHERE id_presentacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, idPresentation);
            int rowsAffected = ps.executeUpdate();
            boolean result = rowsAffected > 0;

            if (result) {
                logger.info("Presentación ID {} eliminada exitosamente", idPresentation);
            } else {
                logger.warn("No se encontró presentación ID {} para eliminar", idPresentation);
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al eliminar presentación ID {}", idPresentation, e);
            throw e;
        }
    }

    public boolean presentationExists(int idPresentation) throws SQLException {
        if (idPresentation <= 0) {
            logger.warn("Intento de verificar existencia de presentación con ID inválido: {}", idPresentation);
            return false;
        }

        logger.debug("Verificando existencia de presentación ID: {}", idPresentation);

        String sql = "SELECT 1 FROM presentacion WHERE id_presentacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, idPresentation);
            try (ResultSet rs = ps.executeQuery()) {
                boolean exists = rs.next();
                logger.debug("¿Presentación ID {} existe?: {}", idPresentation, exists);
                return exists;
            }
        } catch (SQLException e) {
            logger.error("Error al verificar existencia de presentación ID {}", idPresentation, e);
            throw e;
        }
    }

    public int countPresentations() throws SQLException {
        logger.debug("Contando presentaciones");

        String sql = "SELECT COUNT(*) FROM presentacion";

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            int count = resultSet.next() ? resultSet.getInt(1) : 0;
            logger.info("Total de presentaciones: {}", count);
            return count;
        } catch (SQLException e) {
            logger.error("Error al contar presentaciones", e);
            throw e;
        }
    }
}