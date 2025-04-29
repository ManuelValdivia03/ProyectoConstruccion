package logic.daos;

import dataaccess.ConnectionDataBase;
import logic.logicclasses.Coordinator;
import logic.interfaces.ICoordinatorDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoordinatorDAO implements ICoordinatorDAO {
    private static final Logger logger = LogManager.getLogger(CoordinatorDAO.class);
    private final UserDAO userDAO;

    public CoordinatorDAO() {
        this.userDAO = new UserDAO();
    }

    public boolean addCoordinator(Coordinator coordinator) throws SQLException {
        if (coordinator == null) {
            logger.warn("Intento de agregar coordinador nulo");
            return false;
        }

        logger.debug("Agregando nuevo coordinador con número de personal: {}", coordinator.getStaffNumber());

        boolean userAdded = userDAO.addUser(coordinator);
        if (!userAdded) {
            logger.error("No se pudo agregar el usuario asociado al coordinador {}", coordinator.getStaffNumber());
            return false;
        }

        String sql = "INSERT INTO coordinador (id_usuario, numero_personal) VALUES (?, ?)";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, coordinator.getIdUser());
            preparedStatement.setString(2, coordinator.getStaffNumber());

            boolean result = preparedStatement.executeUpdate() > 0;
            if (result) {
                logger.info("Coordinador agregado exitosamente: {}", coordinator.getStaffNumber());
            } else {
                logger.warn("No se pudo agregar el coordinador {}", coordinator.getStaffNumber());
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al agregar coordinador {}", coordinator.getStaffNumber(), e);
            throw e;
        }
    }

    public boolean deleteCoordinator(Coordinator coordinator) throws SQLException {
        if (coordinator == null) {
            logger.warn("Intento de eliminar coordinador nulo");
            return false;
        }

        logger.debug("Eliminando coordinador ID: {}, número de personal: {}",
                coordinator.getIdUser(), coordinator.getStaffNumber());

        String sql = "DELETE FROM coordinador WHERE id_usuario = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, coordinator.getIdUser());
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                boolean userDeleted = userDAO.deleteUser(coordinator.getIdUser());
                if (userDeleted) {
                    logger.info("Coordinador eliminado exitosamente: {}", coordinator.getStaffNumber());
                } else {
                    logger.warn("Coordinador eliminado pero no se pudo eliminar el usuario asociado");
                }
                return userDeleted;
            }
            logger.warn("No se encontró coordinador para eliminar: {}", coordinator.getStaffNumber());
            return false;
        } catch (SQLException e) {
            logger.error("Error al eliminar coordinador {}", coordinator.getStaffNumber(), e);
            throw e;
        }
    }

    public boolean updateCoordinator(Coordinator coordinator) throws SQLException {
        if (coordinator == null) {
            logger.warn("Intento de actualizar coordinador nulo");
            return false;
        }

        logger.debug("Actualizando coordinador ID: {}, número de personal: {}",
                coordinator.getIdUser(), coordinator.getStaffNumber());

        boolean userUpdated = userDAO.updateUser(coordinator);
        if (!userUpdated) {
            logger.warn("No se pudo actualizar el usuario asociado al coordinador {}", coordinator.getStaffNumber());
            return false;
        }

        String sql = "UPDATE coordinador SET numero_personal = ? WHERE id_usuario = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, coordinator.getStaffNumber());
            statement.setInt(2, coordinator.getIdUser());

            boolean result = statement.executeUpdate() > 0;
            if (result) {
                logger.info("Coordinador actualizado exitosamente: {}", coordinator.getStaffNumber());
            } else {
                logger.warn("No se pudo actualizar el coordinador {}", coordinator.getStaffNumber());
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al actualizar coordinador {}", coordinator.getStaffNumber(), e);
            throw e;
        }
    }

    public List<Coordinator> getAllCoordinators() throws SQLException {
        logger.info("Obteniendo todos los coordinadores");

        String sql = "SELECT u.id_usuario, u.nombre_completo, u.telefono, u.estado, " +
                "c.numero_personal " +
                "FROM coordinador c " +
                "JOIN usuario u ON c.id_usuario = u.id_usuario";

        List<Coordinator> coordinators = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Coordinator coordinator = new Coordinator(
                        resultSet.getInt("id_usuario"),
                        resultSet.getString("nombre_completo"),
                        resultSet.getString("telefono"),
                        resultSet.getString("numero_personal"),
                        resultSet.getString("estado").charAt(0)
                );
                coordinators.add(coordinator);
            }
            logger.debug("Se encontraron {} coordinadores", coordinators.size());
        } catch (SQLException e) {
            logger.error("Error al obtener todos los coordinadores", e);
            throw e;
        }
        return coordinators;
    }

    public Coordinator getCoordinatorByStaffNumber(String staffNumber) throws SQLException {
        if (staffNumber == null || staffNumber.isEmpty()) {
            logger.warn("Intento de buscar coordinador con número de personal nulo o vacío");
            return null;
        }

        logger.debug("Buscando coordinador con número de personal: {}", staffNumber);

        String sql = "SELECT u.id_usuario, u.nombre_completo, u.telefono, u.estado, " +
                "c.numero_personal " +
                "FROM coordinador c " +
                "JOIN usuario u ON c.id_usuario = u.id_usuario " +
                "WHERE c.numero_personal = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, staffNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    logger.debug("Coordinador encontrado: {}", staffNumber);
                    return new Coordinator(
                            resultSet.getInt("id_usuario"),
                            resultSet.getString("nombre_completo"),
                            resultSet.getString("telefono"),
                            resultSet.getString("numero_personal"),
                            resultSet.getString("estado").charAt(0)
                    );
                }
            }
        } catch (SQLException e) {
            logger.error("Error al buscar coordinador {}", staffNumber, e);
            throw e;
        }
        logger.info("No se encontró coordinador con número de personal: {}", staffNumber);
        return null;
    }

    public boolean coordinatorExists(String staffNumber) throws SQLException {
        if (staffNumber == null || staffNumber.isEmpty()) {
            logger.warn("Intento de verificar existencia con número de personal nulo o vacío");
            return false;
        }

        logger.debug("Verificando existencia de coordinador con número de personal: {}", staffNumber);

        String sql = "SELECT 1 FROM coordinador WHERE numero_personal = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, staffNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                boolean exists = resultSet.next();
                logger.debug("¿Coordinador {} existe?: {}", staffNumber, exists);
                return exists;
            }
        } catch (SQLException e) {
            logger.error("Error al verificar existencia de coordinador {}", staffNumber, e);
            throw e;
        }
    }

    public int countCoordinators() throws SQLException {
        logger.debug("Contando coordinadores");

        String sql = "SELECT COUNT(*) FROM coordinador";
        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            int count = resultSet.next() ? resultSet.getInt(1) : 0;
            logger.info("Total de coordinadores: {}", count);
            return count;
        } catch (SQLException e) {
            logger.error("Error al contar coordinadores", e);
            throw e;
        }
    }
}