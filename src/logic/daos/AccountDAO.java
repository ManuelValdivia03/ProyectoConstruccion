package logic.daos;

import dataaccess.ConnectionDataBase;
import dataaccess.PasswordUtils;
import logic.exceptions.RepeatedEmailException;
import logic.logicclasses.Account;
import logic.interfaces.IAccountDAO;
import userinterface.utilities.Validators;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO implements IAccountDAO {
    private static final Logger logger = LogManager.getLogger(AccountDAO.class);
    private static final Account EMPTY_ACCOUNT = new Account(-1, "", "");
    private final UserDAO userDAO;

    public AccountDAO() {
        this.userDAO = new UserDAO();
    }

    @Override
    public List<Account> getAllAccounts() throws SQLException {
        String query = "SELECT id_usuario, correo_e, contraseña FROM cuenta";
        List<Account> accounts = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                accounts.add(new Account(
                        resultSet.getInt("id_usuario"),
                        resultSet.getString("correo_e"),
                        resultSet.getString("contraseña")
                ));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving accounts", e);
            throw e;
        }
        return accounts;
    }

    @Override
    public boolean addAccount(Account account) throws SQLException, RepeatedEmailException, IllegalArgumentException {
        validateAccountData(account);

        String query = "INSERT INTO cuenta (id_usuario, correo_e, contraseña) VALUES (?, ?, ?)";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, account.getIdUser());
            preparedStatement.setString(2, account.getEmail());
            preparedStatement.setString(3, account.getPassword());

            return preparedStatement.executeUpdate() > 0;
        }
    }

    private void validateAccountData(Account account) throws SQLException, RepeatedEmailException, IllegalArgumentException {
        if (account == null) {
            throw new IllegalArgumentException("La cuenta no debe ser nula");
        }
        if (account.getEmail() == null || account.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Correo electrónico no debe ser nulo o vacío");
        }
        if (!userDAO.userExists(account.getIdUser())) {
            throw new SQLException("El usuario con ID " + account.getIdUser() + " no existe");
        }
        if (accountExists(account.getEmail())) {
            throw new RepeatedEmailException();
        }
    }

    @Override
    public boolean deleteAccount(int idUser) throws SQLException {
        String query = "DELETE FROM cuenta WHERE id_usuario = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, idUser);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting account", e);
            throw e;
        }
    }

    @Override
    public boolean updateAccount(Account account) throws SQLException, IllegalArgumentException {
        if (account == null) {
            throw new IllegalArgumentException("La cuenta no debe ser nula");
        }

        List<String> updates = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        if (account.getEmail() != null && !account.getEmail().isEmpty()) {
            new Validators().validateEmail(account.getEmail());
            updates.add("correo_e = ?");
            parameters.add(account.getEmail());
        }
        if (account.getPassword() != null && !account.getPassword().isEmpty()) {
            updates.add("contraseña = ?");
            parameters.add(PasswordUtils.hashPassword(account.getPassword()));
        }

        if (updates.isEmpty()) {
            return false;
        }

        String query = "UPDATE cuenta SET " + String.join(", ", updates) + " WHERE id_usuario = ?";
        parameters.add(account.getIdUser());

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            for (int i = 0; i < parameters.size(); i++) {
                preparedStatement.setObject(i + 1, parameters.get(i));
            }

            return preparedStatement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean verifyCredentials(String email, String plainPassword) throws SQLException, IllegalArgumentException {
        if (email == null || email.trim().isEmpty() || plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Email and password must not be null or empty");
        }

        final String query = "SELECT c.contraseña, u.estado FROM cuenta c " +
                "JOIN usuario u ON c.id_usuario = u.id_usuario " +
                "WHERE c.correo_e = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return false;
                }

                String status = resultSet.getString("estado");
                if (status == null || status.charAt(0) != 'A') {
                    return false;
                }

                String storedHash = resultSet.getString("contraseña");
                if (storedHash == null || storedHash.trim().isEmpty()) {
                    throw new SQLException("Contraseña invalida para el usuario: " + email);
                }

                return PasswordUtils.checkPassword(plainPassword, storedHash);
            }
        }
    }

    @Override
    public Account getAccountByUserId(int userId) throws SQLException {
        String query = "SELECT id_usuario, correo_e, contraseña FROM cuenta WHERE id_usuario = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, userId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new Account(
                            resultSet.getInt("id_usuario"),
                            resultSet.getString("correo_e"),
                            resultSet.getString("contraseña")
                    );
                }
            }
        }
        return EMPTY_ACCOUNT;
    }

    @Override
    public Account getAccountByEmail(String email) throws SQLException, IllegalArgumentException {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("El correo electrónico no debe ser nulo o vacío");
        }

        String query = "SELECT id_usuario, correo_e, contraseña FROM cuenta WHERE correo_e = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, email);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new Account(
                            resultSet.getInt("id_usuario"),
                            resultSet.getString("correo_e"),
                            resultSet.getString("contraseña")
                    );
                }
            }
        }
        return EMPTY_ACCOUNT;
    }

    @Override
    public boolean accountExists(String email) throws SQLException, IllegalArgumentException {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("El correo electrónico no debe ser nulo o vacío");
        }

        String query = "SELECT 1 FROM cuenta WHERE correo_e = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, email);
            return preparedStatement.executeQuery().next();
        }
    }

    @Override
    public String getEmailById(int id) throws SQLException {
        String query = "SELECT correo_e FROM cuenta WHERE id_usuario = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getString("correo_e") : "";
            }
        }
    }

    @Override
    public boolean updatePasswordByEmail(String email, String newHashedPassword) throws SQLException, IllegalArgumentException {
        if (email == null || email.trim().isEmpty() || newHashedPassword == null || newHashedPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Email and password must not be null or empty");
        }

        String query = "UPDATE cuenta SET contraseña = ? WHERE correo_e = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, newHashedPassword);
            preparedStatement.setString(2, email);

            return preparedStatement.executeUpdate() > 0;
        }
    }
}
