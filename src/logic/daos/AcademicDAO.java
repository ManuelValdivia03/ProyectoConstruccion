package logic.daos;

import dataaccess.ConnectionDataBase;
import logic.exceptions.RepeatedStaffNumberException;
import logic.logicclasses.Academic;
import logic.enums.AcademicType;
import logic.interfaces.IAcademicDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AcademicDAO implements IAcademicDAO {
    private static final Logger logger = LogManager.getLogger(AcademicDAO.class);
    private final UserDAO userDAO;

    // Consultas SQL centralizadas
    private static final String BASE_ACADEMIC_QUERY =
            "SELECT u.id_usuario, u.nombre_completo, u.telefono, u.estado, " +
                    "a.numero_personal, a.tipo FROM academico a " +
                    "JOIN usuario u ON a.id_usuario = u.id_usuario";

    private static final String INSERT_ACADEMIC =
            "INSERT INTO academico (id_usuario, numero_personal, tipo) VALUES (?, ?, ?)";

    private static final String UPDATE_ACADEMIC =
            "UPDATE academico SET numero_personal = ?, tipo = ? WHERE id_usuario = ?";

    private static final String DELETE_ACADEMIC =
            "DELETE FROM academico WHERE id_usuario = ?";

    private static final String CHECK_STAFF_NUMBER =
            "SELECT 1 FROM academico WHERE numero_personal = ?";

    private static final String CHECK_USER_EXISTS =
            "SELECT 1 FROM academico WHERE id_usuario = ?";

    public AcademicDAO() {
        this.userDAO = new UserDAO();
    }

    public AcademicDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public boolean addAcademic(Academic academic) throws SQLException, RepeatedStaffNumberException {
        if (academic == null) {
            logger.warn("Attempt to add null academic");
            return false;
        }

        if (academicExists(academic.getStaffNumber())) {
            throw new RepeatedStaffNumberException("Staff number already exists: " + academic.getStaffNumber());
        }

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement stmt = connection.prepareStatement(INSERT_ACADEMIC)) {

            stmt.setInt(1, academic.getIdUser());
            stmt.setString(2, academic.getStaffNumber());
            stmt.setString(3, academic.getAcademicType().toString());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Error adding academic with staff number: {}", academic.getStaffNumber(), e);
            throw e;
        }
    }

    @Override
    public List<Academic> getAllAcademics() throws SQLException {
        List<Academic> academics = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(BASE_ACADEMIC_QUERY)) {

            while (resultSet.next()) {
                academics.add(mapResultSetToAcademic(resultSet));
            }
        } catch (SQLException e) {
            logger.error("Error getting all academics", e);
            throw e;
        }
        return academics;
    }

    @Override
    public Optional<Academic> getAcademicByStaffNumber(String staffNumber) throws SQLException {
        if (staffNumber == null || staffNumber.isEmpty()) {
            return Optional.empty();
        }

        String sql = BASE_ACADEMIC_QUERY + " WHERE a.numero_personal = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, staffNumber);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapResultSetToAcademic(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            logger.error("Error getting academic with staff number: {}", staffNumber, e);
            throw e;
        }
    }

    @Override
    public boolean updateAcademic(Academic academic) throws SQLException {
        if (academic == null) {
            logger.warn("Attempt to update null academic");
            return false;
        }

        if (!userDAO.updateUser(academic)) {
            logger.warn("Failed to update user data for academic ID: {}", academic.getIdUser());
            return false;
        }

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_ACADEMIC)) {

            statement.setString(1, academic.getStaffNumber());
            statement.setString(2, academic.getAcademicType().toString());
            statement.setInt(3, academic.getIdUser());

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Error updating academic with ID: {}", academic.getIdUser(), e);
            throw e;
        }
    }

    @Override
    public boolean deleteAcademic(Academic academic) throws SQLException {
        if (academic == null) {
            logger.warn("Attempt to delete null academic");
            return false;
        }

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_ACADEMIC)) {

            statement.setInt(1, academic.getIdUser());

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                return userDAO.deleteUser(academic.getIdUser());
            }
            return false;
        } catch (SQLException e) {
            logger.error("Error deleting academic with ID: {}", academic.getIdUser(), e);
            throw e;
        }
    }

    @Override
    public List<Academic> getAllAcademicsByType(AcademicType type) throws SQLException {
        if (type == null) {
            return new ArrayList<>();
        }

        String sql = BASE_ACADEMIC_QUERY + " WHERE a.tipo = ?";
        List<Academic> academics = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, type.toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    academics.add(mapResultSetToAcademic(resultSet));
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting academics by type: {}", type, e);
            throw e;
        }
        return academics;
    }

    @Override
    public Optional<Academic> getAcademicById(int idUser) throws SQLException {
        String sql = BASE_ACADEMIC_QUERY + " WHERE u.id_usuario = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idUser);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapResultSetToAcademic(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            logger.error("Error getting academic by ID: {}", idUser, e);
            throw e;
        }
    }

    @Override
    public boolean academicExists(String staffNumber) throws SQLException {
        if (staffNumber == null || staffNumber.isEmpty()) {
            return false;
        }

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(CHECK_STAFF_NUMBER)) {

            statement.setString(1, staffNumber);
            return statement.executeQuery().next();
        } catch (SQLException e) {
            logger.error("Error checking academic existence with staff number: {}", staffNumber, e);
            throw e;
        }
    }

    @Override
    public int countAcademics() throws SQLException {
        String sql = "SELECT COUNT(*) FROM academico";

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            return resultSet.next() ? resultSet.getInt(1) : 0;
        } catch (SQLException e) {
            logger.error("Error counting academics", e);
            throw e;
        }
    }

    @Override
    public boolean changeAcademicType(Academic academic) throws SQLException {
        if (academic == null) {
            logger.warn("Attempt to change type for null academic");
            return false;
        }

        String sql = "UPDATE academico SET tipo = ? WHERE numero_personal = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, academic.getAcademicType().toString());
            statement.setString(2, academic.getStaffNumber());

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Error changing type for academic with staff number: {}", academic.getStaffNumber(), e);
            throw e;
        }
    }

    @Override
    public boolean staffNumberExists(String staffNumber) throws RepeatedStaffNumberException {
        try {
            return academicExists(staffNumber);
        } catch (SQLException e) {
            logger.error("Error checking staff number existence: {}", staffNumber, e);
            throw new RepeatedStaffNumberException("Error verifying staff number", e);
        }
    }

    @Override
    public List<Academic> getAllAcademicsFromView() throws SQLException {
        String sql = "SELECT * FROM vista_academicos_completa";
        List<Academic> academics = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            while (rs.next()) {
                academics.add(mapViewResultSetToAcademic(rs));
            }
        } catch (SQLException e) {
            logger.error("Error getting academics from view", e);
            throw e;
        }
        return academics;
    }

    @Override
    public List<Academic> getAcademicsByStatusFromView(char estado) throws SQLException {
        String sql = "SELECT * FROM vista_academicos_completa WHERE estado = ?";
        List<Academic> academics = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, String.valueOf(estado));

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    academics.add(mapViewResultSetToAcademic(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting academics by status: {}", estado, e);
            throw e;
        }
        return academics;
    }

    @Override
    public boolean existsForUser(int userId) throws SQLException {
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement stmt = connection.prepareStatement(CHECK_USER_EXISTS)) {

            stmt.setInt(1, userId);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            logger.error("Error checking academic existence for user ID: {}", userId, e);
            throw e;
        }
    }

    public Optional<Academic> getFullAcademic(int userId) throws SQLException {
        return getAcademicById(userId);
    }

    private Academic mapResultSetToAcademic(ResultSet rs) throws SQLException {
        return new Academic(
                rs.getInt("id_usuario"),
                rs.getString("nombre_completo"),
                rs.getString("telefono"),
                rs.getString("estado").charAt(0),
                rs.getString("numero_personal"),
                AcademicType.valueOf(rs.getString("tipo"))
        );
    }

    private Academic mapViewResultSetToAcademic(ResultSet rs) throws SQLException {
        return new Academic(
                rs.getInt("id_usuario"),
                rs.getString("nombre_completo"),
                rs.getString("telefono"),
                rs.getString("estado").charAt(0),
                rs.getString("numero_personal"),
                AcademicType.valueOf(rs.getString("tipo_academico"))
        );
    }
}