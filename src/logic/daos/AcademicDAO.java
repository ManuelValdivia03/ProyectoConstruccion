package logic.daos;

import dataaccess.ConnectionDataBase;
import logic.exceptions.RepeatedStaffNumberException;
import logic.logicclasses.Academic;
import logic.enums.AcademicType;
import logic.interfaces.IAcademicDAO;
import logic.logicclasses.Account;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AcademicDAO implements IAcademicDAO {
    private static final Logger logger = LogManager.getLogger(AcademicDAO.class);
    private final UserDAO userDAO;

    public AcademicDAO() {
        this.userDAO = new UserDAO();
    }

    public boolean addAcademic(Academic academic) throws SQLException, RepeatedStaffNumberException {
        if (academic == null) {
            logger.warn("Intento de agregar un académico nulo");
            return false;
        }

        if (academicExists(academic.getStaffNumber())) {
            throw new RepeatedStaffNumberException();
        }

        String sql = "INSERT INTO academico (id_usuario, numero_personal, tipo) VALUES (?, ?, ?)";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, academic.getIdUser());
            stmt.setString(2, academic.getStaffNumber());
            stmt.setString(3, academic.getAcademicType().toString());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error al agregar académico", e);
            throw e;
        }
    }

    public List<Academic> getAllAcademics() throws SQLException {
        String sql = "SELECT u.id_usuario, u.nombre_completo, u.telefono, u.estado, " +
                "a.numero_personal, a.tipo FROM academico a " +
                "JOIN usuario u ON a.id_usuario = u.id_usuario";
        List<Academic> academics = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                academics.add(new Academic(
                        resultSet.getInt("id_usuario"),
                        resultSet.getString("nombre_completo"),
                        resultSet.getString("telefono"),
                        resultSet.getString("estado").charAt(0),
                        resultSet.getString("numero_personal"),
                        AcademicType.valueOf(resultSet.getString("tipo"))
                ));
            }
            logger.info("Se recuperaron {} académicos", academics.size());
        } catch (SQLException e) {
            logger.error("Error al obtener académicos", e);
            throw e;
        }
        return academics;
    }

    public Academic getAcademicByStaffNumber(String staffNumber) throws SQLException {
        Academic academicVoid = new Academic(-1, "", "", 'I', "", AcademicType.NONE);

        if (staffNumber == null || staffNumber.isEmpty()) {
            logger.warn("Número de personal nulo o vacío");
            return academicVoid;
        }

        String sql = "SELECT u.id_usuario, u.nombre_completo, u.telefono, u.estado, " +
                "a.numero_personal, a.tipo FROM academico a " +
                "JOIN usuario u ON a.id_usuario = u.id_usuario " +
                "WHERE a.numero_personal = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, staffNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    logger.debug("Académico encontrado: {}", staffNumber);
                    return new Academic(
                            resultSet.getInt("id_usuario"),
                            resultSet.getString("nombre_completo"),
                            resultSet.getString("telefono"),
                            resultSet.getString("estado").charAt(0),
                            resultSet.getString("numero_personal"),
                            AcademicType.valueOf(resultSet.getString("tipo"))
                    );
                }
                logger.info("No se encontró académico con número de personal {}", staffNumber);
                return academicVoid;
            }
        } catch (SQLException e) {
            logger.error("Error al buscar académico {}", staffNumber, e);
            throw e;
        }
    }

    public boolean updateAcademic(Academic academic) throws SQLException {
        if (academic == null) {
            logger.warn("Intento de actualizar un académico nulo");
            return false;
        }

        boolean userUpdated = userDAO.updateUser(academic);
        if (!userUpdated) {
            logger.warn("No se pudo actualizar el usuario asociado al académico {}", academic.getStaffNumber());
            return false;
        }

        String sql = "UPDATE academico SET numero_personal = ?, tipo = ? WHERE id_usuario = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, academic.getStaffNumber());
            statement.setString(2, academic.getAcademicType().toString());
            statement.setInt(3, academic.getIdUser());

            boolean updated = statement.executeUpdate() > 0;
            if (updated) {
                logger.info("Académico actualizado: {}", academic.getStaffNumber());
            } else {
                logger.warn("No se pudo actualizar académico {}", academic.getStaffNumber());
            }
            return updated;
        } catch (SQLException e) {
            logger.error("Error al actualizar académico {}", academic.getStaffNumber(), e);
            throw e;
        }
    }

    public boolean deleteAcademic(Academic academic) throws SQLException {
        if (academic == null) {
            logger.warn("Intento de eliminar un académico nulo");
            return false;
        }

        String sql = "DELETE FROM academico WHERE id_usuario = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, academic.getIdUser());
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Académico eliminado: {}", academic.getStaffNumber());
                return userDAO.deleteUser(academic.getIdUser());
            } else {
                logger.warn("No se encontró académico para eliminar: {}", academic.getStaffNumber());
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error al eliminar académico {}", academic.getStaffNumber(), e);
            throw e;
        }
    }

    public List<Academic> getAllAcademicsByType(AcademicType type) throws SQLException {
        if (type == null) {
            logger.warn("Tipo de académico nulo");
            return new ArrayList<>();
        }

        String sql = "SELECT u.id_usuario, u.nombre_completo, u.telefono, u.estado, " +
                "a.numero_personal, a.tipo FROM academico a " +
                "JOIN usuario u ON a.id_usuario = u.id_usuario " +
                "WHERE a.tipo = ?";

        List<Academic> academics = new ArrayList<>();
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, type.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    academics.add(new Academic(
                            resultSet.getInt("id_usuario"),
                            resultSet.getString("nombre_completo"),
                            resultSet.getString("telefono"),
                            resultSet.getString("estado").charAt(0),
                            resultSet.getString("numero_personal"),
                            AcademicType.valueOf(resultSet.getString("tipo"))
                    ));
                }
            }
            logger.info("Se encontraron {} académicos del tipo {}", academics.size(), type);
        } catch (SQLException e) {
            logger.error("Error al obtener académicos por tipo {}", type, e);
            throw e;
        }
        return academics;
    }

    public Academic getAcademicById(int idUser) throws SQLException {
        String sql = "SELECT u.id_usuario, u.nombre_completo, u.telefono, u.estado, " +
                "a.numero_personal, a.tipo FROM academico a " +
                "JOIN usuario u ON a.id_usuario = u.id_usuario " +
                "WHERE u.id_usuario = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idUser);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    logger.debug("Académico encontrado con ID {}", idUser);
                    return new Academic(
                            resultSet.getInt("id_usuario"),
                            resultSet.getString("nombre_completo"),
                            resultSet.getString("telefono"),
                            resultSet.getString("estado").charAt(0),
                            resultSet.getString("numero_personal"),
                            AcademicType.valueOf(resultSet.getString("tipo"))
                    );
                }
            }
        } catch (SQLException e) {
            logger.error("Error al buscar académico por ID {}", idUser, e);
            throw e;
        }
        return null;
    }

    public boolean academicExists(String staffNumber) throws SQLException {
        if (staffNumber == null || staffNumber.isEmpty()) {
            logger.warn("Número de personal nulo o vacío en academicExists");
            return false;
        }

        String sql = "SELECT 1 FROM academico WHERE numero_personal = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, staffNumber);
            boolean exists = statement.executeQuery().next();
            logger.debug("¿Académico {} existe?: {}", staffNumber, exists);
            return exists;
        } catch (SQLException e) {
            logger.error("Error al verificar existencia del académico {}", staffNumber, e);
            throw e;
        }
    }

    public int countAcademics() throws SQLException {
        String sql = "SELECT COUNT(*) FROM academico";
        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            int count = resultSet.next() ? resultSet.getInt(1) : 0;
            logger.info("Cantidad total de académicos: {}", count);
            return count;
        } catch (SQLException e) {
            logger.error("Error al contar académicos", e);
            throw e;
        }
    }

    public boolean changeAcademicType(Academic academic) throws SQLException {
        if (academic == null) {
            logger.warn("Académico nulo al intentar cambiar tipo");
            return false;
        }

        String sql = "UPDATE academico SET tipo = ? WHERE numero_personal = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, academic.getAcademicType().toString());
            statement.setString(2, academic.getStaffNumber());

            boolean updated = statement.executeUpdate() > 0;
            logger.info("Cambio de tipo para {} a {}: {}", academic.getStaffNumber(), academic.getAcademicType(), updated);
            return updated;
        } catch (SQLException e) {
            logger.error("Error al cambiar tipo de académico {}", academic.getStaffNumber(), e);
            throw e;
        }
    }

    public boolean staffNumberExists(String staffNumber) throws RepeatedStaffNumberException {
        if (staffNumber == null || staffNumber.isEmpty()) {
            logger.warn("Número de personal nulo o vacío en staffNumberExists");
            return false;
        }

        String sql = "SELECT 1 FROM academico WHERE numero_personal = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, staffNumber);
            boolean exists = statement.executeQuery().next();
            logger.debug("¿Número de personal {} existe?: {}", staffNumber, exists);
            return exists;
        } catch (SQLException e) {
            logger.error("Error al verificar número de personal {}", staffNumber, e);
        }
        return false;
    }

    public List<Academic> getAllAcademicsFromView() throws SQLException {
        String sql = "SELECT * FROM vista_academicos_completa";
        List<Academic> academics = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            while (rs.next()) {
                Academic academic = new Academic(
                        rs.getInt("id_usuario"),
                        rs.getString("nombre_completo"),
                        rs.getString("telefono"),
                        rs.getString("estado").charAt(0),
                        rs.getString("numero_personal"),
                        AcademicType.valueOf(rs.getString("tipo_academico"))
                );
                academics.add(academic);
            }
        }
        return academics;
    }
}
