package logic.daos;

import dataaccess.ConnectionDataBase;
import logic.exceptions.InvalidCellPhoneException;
import logic.exceptions.RepeatedCellPhoneException;
import logic.logicclasses.User;
import logic.interfaces.IUserDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserDAO implements IUserDAO {
    private static final Logger logger = LogManager.getLogger(UserDAO.class);
    private static final User EMPTY_USER = new User(-1, "", "", 'I');

    public boolean addUser(User user) throws SQLException, IllegalArgumentException, RepeatedCellPhoneException {
        if (user == null) {
            logger.warn("Intento de agregar usuario nulo");
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }

        String cleanPhone = user.getCellPhone().replaceAll("[^0-9]", "");
        logger.debug("Validando teléfono celular: {}", cleanPhone);

        if (!cleanPhone.matches("^\\d{10}$")) {
            logger.warn("Teléfono celular inválido: {}", cleanPhone);
            throw new IllegalArgumentException("El teléfono celular debe tener 10 dígitos");
        }

        if (cellPhoneExists(cleanPhone)) {
            logger.warn("Teléfono celular ya registrado: {}", cleanPhone);
            throw new RepeatedCellPhoneException();
        }

        String query = "INSERT INTO usuario (nombre_completo, telefono, estado) VALUES (?, ?, ?)";

        try (Connection conn = ConnectionDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getFullName());
            stmt.setString(2, cleanPhone);
            stmt.setString(3, String.valueOf(user.getStatus()));

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted == 1) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setIdUser(generatedKeys.getInt(1));
                        logger.info("Usuario agregado exitosamente - ID: {}, Nombre: {}, Teléfono: {}",
                                user.getIdUser(), user.getFullName(), cleanPhone);
                    }
                }
                return true;
            }
            logger.warn("No se pudo agregar el usuario: {}", user.getFullName());
            return false;
        } catch (SQLException e) {
            logger.error("Error al agregar usuario: {}", user.getFullName(), e);
            throw e;
        }
    }

    public List<User> getAllUsers() throws SQLException {
        logger.debug("Obteniendo todos los usuarios");
        String sql = "SELECT * FROM usuario";
        List<User> users = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                User user = new User();
                user.setIdUser(resultSet.getInt("id_usuario"));
                user.setFullName(resultSet.getString("nombre_completo"));
                user.setCellphone(resultSet.getString("telefono"));
                user.setStatus(resultSet.getString("estado").charAt(0));
                users.add(user);
            }
            logger.debug("Se encontraron {} usuarios", users.size());
        } catch (SQLException e) {
            logger.error("Error al obtener todos los usuarios", e);
            throw new SQLException();
        }
        return users;
    }

    public User getUserById(int id) throws SQLException {
        if (id <= 0) {
            logger.warn("Intento de buscar usuario con ID inválido: {}", id);
            return EMPTY_USER;
        }

        logger.debug("Buscando usuario por ID: {}", id);
        String sql = "SELECT * FROM usuario WHERE id_usuario = ?";
        User user = null;

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    user = new User();
                    user.setIdUser(resultSet.getInt("id_usuario"));
                    user.setFullName(resultSet.getString("nombre_completo"));
                    user.setCellphone(resultSet.getString("telefono"));
                    user.setStatus(resultSet.getString("estado").charAt(0));
                    logger.debug("Usuario encontrado con ID: {}", id);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al buscar usuario por ID: {}", id, e);
            throw e;
        }

        if (user == null) {
            logger.info("No se encontró usuario con ID: {}", id);
            return EMPTY_USER;
        }
        return user;
    }

    public boolean updateUser(User user) throws SQLException {
        if (user == null) {
            logger.warn("Intento de actualizar usuario nulo");
            return false;
        }

        logger.debug("Actualizando usuario ID: {}", user.getIdUser());
        String sql = "UPDATE usuario SET nombre_completo = ?, telefono = ?, estado = ? WHERE id_usuario = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getFullName());
            statement.setString(2, user.getCellPhone());
            statement.setString(3, String.valueOf(user.getStatus()));
            statement.setInt(4, user.getIdUser());

            boolean result = statement.executeUpdate() > 0;
            if (result) {
                logger.info("Usuario actualizado exitosamente - ID: {}", user.getIdUser());
            } else {
                logger.warn("No se encontró usuario con ID: {} para actualizar", user.getIdUser());
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al actualizar usuario ID: {}", user.getIdUser(), e);
            throw e;
        }
    }

    public boolean deleteUser(int id) throws SQLException {
        if (id <= 0) {
            logger.warn("Intento de eliminar usuario con ID inválido: {}", id);
            return false;
        }

        logger.debug("Eliminando usuario ID: {}", id);
        String sql = "DELETE FROM usuario WHERE id_usuario = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            boolean result = statement.executeUpdate() > 0;
            if (result) {
                logger.info("Usuario eliminado exitosamente - ID: {}", id);
            } else {
                logger.warn("No se encontró usuario con ID: {} para eliminar", id);
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al eliminar usuario ID: {}", id, e);
            throw e;
        }
    }

    public List<User> searchUsersByName(String name) throws SQLException {
        if (name == null || name.trim().isEmpty()) {
            logger.warn("Intento de buscar usuarios con nombre vacío o nulo");
            return Collections.emptyList();
        }

        logger.debug("Buscando usuarios por nombre: {}", name);
        String sql = "SELECT * FROM usuario WHERE nombre_completo LIKE ?";
        List<User> users = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, "%" + name + "%");
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    User user = new User();
                    user.setIdUser(resultSet.getInt("id_usuario"));
                    user.setFullName(resultSet.getString("nombre_completo"));
                    user.setCellphone(resultSet.getString("telefono"));
                    user.setStatus(resultSet.getString("estado").charAt(0));
                    users.add(user);
                }
            }
            logger.debug("Se encontraron {} usuarios con el nombre: {}", users.size(), name);
        } catch (SQLException e) {
            logger.error("Error al buscar usuarios por nombre: {}", name, e);
            throw e;
        }
        return users;
    }

    public boolean userExists(int id) throws SQLException {
        if (id <= 0) {
            logger.warn("Intento de verificar existencia de usuario con ID inválido: {}", id);
            return false;
        }

        logger.debug("Verificando existencia de usuario con ID: {}", id);
        String sql = "SELECT 1 FROM usuario WHERE id_usuario = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                boolean exists = resultSet.next();
                logger.debug("¿Usuario ID {} existe?: {}", id, exists);
                return exists;
            }
        } catch (SQLException e) {
            logger.error("Error al verificar existencia de usuario ID: {}", id, e);
            throw e;
        }
    }

    public boolean cellPhoneExists(String cellPhone) throws SQLException, InvalidCellPhoneException {
        if (cellPhone == null || !cellPhone.matches("^\\d{10}$")) {
            logger.warn("Intento de verificar teléfono celular inválido: {}", cellPhone);
            throw new InvalidCellPhoneException();
        }

        logger.debug("Verificando existencia de teléfono celular: {}", cellPhone);
        String sql = "SELECT 1 FROM usuario WHERE telefono = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cellPhone);
            try (ResultSet resultSet = statement.executeQuery()) {
                boolean exists = resultSet.next();
                logger.debug("¿Teléfono celular '{}' existe?: {}", cellPhone, exists);
                return exists;
            }
        } catch (SQLException e) {
            logger.error("Error al verificar existencia de teléfono celular: {}", cellPhone, e);
            throw e;
        }
    }

    public int countUsers() throws SQLException {
        logger.debug("Contando usuarios");
        String sql = "SELECT COUNT(*) FROM usuario";

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            int count = resultSet.next() ? resultSet.getInt(1) : 0;
            logger.info("Total de usuarios: {}", count);
            return count;
        } catch (SQLException e) {
            logger.error("Error al contar usuarios", e);
            throw new SQLException();
        }
    }
}
