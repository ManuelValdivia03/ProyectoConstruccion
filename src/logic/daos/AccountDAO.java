package logic.daos;

import dataaccess.ConnectionDataBase;
import dataaccess.PasswordUtils;
import logic.exceptions.RepeatedEmailException;
import logic.logicclasses.Account;
import logic.interfaces.IAccountDAO;
import userinterface.utilities.Validators;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO implements IAccountDAO {
    private static final Logger logger = LogManager.getLogger(AccountDAO.class);
    private final UserDAO userDAO;

    public AccountDAO() {
        this.userDAO = new UserDAO();
    }

    public List<Account> getAllAccounts() throws SQLException {
        logger.info("Obteniendo todas las cuentas");
        String sql = "SELECT id_usuario, correo_e, contraseña FROM cuenta";
        List<Account> accounts = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Account account = new Account(
                        resultSet.getInt("id_usuario"),
                        resultSet.getString("correo_e"),
                        resultSet.getString("contraseña")
                );
                accounts.add(account);
            }
            logger.debug("Se recuperaron {} cuentas", accounts.size());
        } catch (SQLException e) {
            logger.error("Error al obtener todas las cuentas", e);
            throw e;
        }
        return accounts;
    }

    public boolean addAccount(Account account) throws SQLException, RepeatedEmailException {
        if (account == null) {
            logger.warn("Intento de agregar una cuenta nula");
            throw new IllegalArgumentException("La cuenta no puede ser nula");
        }

        logger.debug("Intentando agregar cuenta para usuario {}", account.getIdUser());

        if (accountExists(account.getEmail())) {
            logger.warn("Correo electrónico ya registrado: {}", account.getEmail());
            throw new RepeatedEmailException("El correo electrónico ya está registrado");
        }

        if (!userDAO.userExists(account.getIdUser())) {
            logger.error("El usuario asociado no existe: {}", account.getIdUser());
            throw new SQLException("El usuario asociado no existe");
        }

        String sql = "INSERT INTO cuenta (id_usuario, correo_e, contraseña) VALUES (?, ?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {


            preparedStatement.setInt(1, account.getIdUser());
            preparedStatement.setString(2, account.getEmail());
            preparedStatement.setString(3, account.getPassword());

            boolean result = preparedStatement.executeUpdate() > 0;
            if (result) {
                logger.info("Cuenta agregada exitosamente para usuario {}", account.getIdUser());
            } else {
                logger.warn("No se pudo agregar la cuenta para usuario {}", account.getIdUser());
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al agregar cuenta para usuario {}", account.getIdUser(), e);
            throw e;
        }
    }

    public boolean deleteAccount(int idUser) throws SQLException {
        logger.debug("Intentando eliminar cuenta para usuario {}", idUser);
        String sql = "DELETE FROM cuenta WHERE id_usuario = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, idUser);
            boolean result = preparedStatement.executeUpdate() > 0;
            if (result) {
                logger.info("Cuenta eliminada exitosamente para usuario {}", idUser);
            } else {
                logger.warn("No se encontró cuenta para eliminar para usuario {}", idUser);
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al eliminar cuenta para usuario {}", idUser, e);
            throw e;
        }
    }

    public boolean updateAccount(Account account) throws SQLException {
        Validators validators = new Validators();
        if (account == null) {
            logger.warn("Intento de actualizar una cuenta nula");
            throw new IllegalArgumentException();
        }

        logger.debug("Intentando actualizar cuenta para usuario {}", account.getIdUser());

        StringBuilder sql = new StringBuilder("UPDATE cuenta SET ");
        List<String> updates = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (account.getEmail() != null && !account.getEmail().isEmpty()) {
            validators.validateEmail(account.getEmail());
            updates.add("correo_e = ?");
            params.add(account.getEmail());
        }
        if (account.getPassword() != null && !account.getPassword().isEmpty()) {
            updates.add("contraseña = ?");
            params.add(PasswordUtils.hashPassword(account.getPassword()));
        }

        if (updates.isEmpty()) {
            logger.warn("No hay campos para actualizar en la cuenta del usuario {}", account.getIdUser());
            return false;
        }

        sql.append(String.join(", ", updates));
        sql.append(" WHERE id_usuario = ?");
        params.add(account.getIdUser());

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                preparedStatement.setObject(i + 1, params.get(i));
            }

            boolean result = preparedStatement.executeUpdate() > 0;
            if (result) {
                logger.info("Cuenta actualizada exitosamente para usuario {}", account.getIdUser());
            } else {
                logger.warn("No se pudo actualizar la cuenta para usuario {}", account.getIdUser());
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al actualizar cuenta para usuario {}", account.getIdUser(), e);
            throw e;
        }
    }

    public boolean verifyCredentials(String email, String plainPassword) throws SQLException {
        if (email == null || email.trim().isEmpty() ||
                plainPassword == null || plainPassword.trim().isEmpty()) {
            logger.warn("Credenciales vacías o nulas");
            return false;
        }

        logger.debug("Verificando credenciales para: {}", email);

        final String sql = "SELECT c.contraseña, u.estado FROM cuenta c " +
                "JOIN usuario u ON c.id_usuario = u.id_usuario " +
                "WHERE c.correo_e = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    logger.warn("No existe cuenta para el correo: {}", email);
                    return false;
                }

                String estado = rs.getString("estado");
                if (estado == null || estado.charAt(0) != 'A') {
                    logger.warn("Cuenta inactiva o estado inválido para: {}", email);
                    return false;
                }

                String hashAlmacenado = rs.getString("contraseña");
                if (hashAlmacenado == null || hashAlmacenado.trim().isEmpty()) {
                    logger.error("Hash de contraseña inválido en BD para: {}", email);
                    return false;
                }

                return PasswordUtils.checkPassword(plainPassword, hashAlmacenado);
            }
        } catch (SQLException e) {
            logger.error("Error de base de datos al verificar credenciales para: {}", email, e);
            throw e;
        }
    }

    public Account getAccountByUserId(int userId) throws SQLException {
        Account accountVoid = new Account(-1, "", "");
        String sql = "SELECT * FROM cuenta WHERE id_usuario = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Account account = new Account();
                    account.setIdUser(rs.getInt("id_usuario"));
                    account.setEmail(rs.getString("correo_e"));
                    account.setPassword(rs.getString("contraseña"));
                    account.setIdUser(userId);
                    return account;
                }
                return accountVoid;
            }
        } catch (SQLException e) {
            logger.error("Error al obtener cuenta para usuario ID: " + userId, e);
            throw new SQLException();
        }
    }

    public Account getAccountByEmail(String email) throws SQLException {
        Account account = new Account(-1, "", "");
        if (email == null || email.isEmpty()) {
            logger.warn("Correo electrónico nulo o vacío al buscar cuenta");
            return null;
        }

        logger.debug("Buscando cuenta por correo: {}", email);
        String sql = "SELECT id_usuario, correo_e, contraseña FROM cuenta WHERE correo_e = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, email);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    logger.debug("Cuenta encontrada para correo {}", email);
                    return new Account(
                            resultSet.getInt("id_usuario"),
                            resultSet.getString("correo_e"),
                            resultSet.getString("contraseña")
                    );
                }
            }
        } catch (SQLException e) {
            logger.error("Error al buscar cuenta por correo {}", email, e);
            throw e;
        }
        logger.info("No se encontró cuenta para correo {}", email);
        return account;
    }

    public boolean accountExists(String email) throws SQLException {
        if (email == null || email.isEmpty()) {
            logger.warn("Correo electrónico nulo o vacío al verificar existencia");
            return false;
        }

        logger.debug("Verificando existencia de cuenta para correo: {}", email);
        String sql = "SELECT 1 FROM cuenta WHERE correo_e = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, email);
            boolean exists = preparedStatement.executeQuery().next();
            logger.debug("¿Cuenta {} existe?: {}", email, exists);
            return exists;
        } catch (SQLException e) {
            logger.error("Error al verificar existencia de cuenta para correo {}", email, e);
            throw e;
        }
    }

    public String getEmailById(int id) throws SQLException {
        logger.debug("Obteniendo correo por ID de usuario: {}", id);
        String sql = "SELECT correo_e FROM cuenta WHERE id_usuario = ?";
        String email = null;

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    email = resultSet.getString("correo_e");
                    logger.debug("Correo encontrado para usuario {}: {}", id, email);
                } else {
                    logger.info("No se encontró correo para usuario {}", id);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al obtener correo para usuario {}", id, e);
            throw e;
        }
        return email;
    }
}