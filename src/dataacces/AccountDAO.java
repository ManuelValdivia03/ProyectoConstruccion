package dataacces;

import dataacces.util.PasswordUtils;
import logic.Account;
import logic.interfaces.IAccountDAO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO implements IAccountDAO {
    private final UserDAO userDAO;

    public AccountDAO() {
        this.userDAO = new UserDAO();
    }

    public List<Account> getAllAccounts() throws SQLException {
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
        }
        return accounts;
    }

    public boolean addAccount(Account account) throws SQLException {
        if (account == null) {
            throw new IllegalArgumentException("");
        }

        validateEmail(account.getEmail());

        if (!userDAO.userExists(account.getIdUser())) {
            throw new SQLException("");
        }

        AccountDAO accountDAO = new AccountDAO();
        if (accountDAO.accountExists(account.getEmail())){
            throw new SQLException("");
        }

        String sql = "INSERT INTO cuenta (id_usuario, correo_e, contraseña) VALUES (?, ?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            String hashedPassword = PasswordUtils.hashPassword(account.getPassword());

            preparedStatement.setInt(1, account.getIdUser());
            preparedStatement.setString(2, account.getEmail());
            preparedStatement.setString(3, hashedPassword);

            return preparedStatement.executeUpdate() > 0;
        }
    }

    public boolean deleteAccount(int idUser) throws SQLException {
        String sql = "DELETE FROM cuenta WHERE id_usuario = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, idUser);
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public boolean updateAccount(Account account) throws SQLException {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }

        StringBuilder sql = new StringBuilder("UPDATE cuenta SET ");
        List<String> updates = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (account.getEmail() != null && !account.getEmail().isEmpty()) {
            validateEmail(account.getEmail());
            updates.add("correo_e = ?");
            params.add(account.getEmail());
        }
        if (account.getPassword() != null && !account.getPassword().isEmpty()) {
            updates.add("contraseña = ?");
            params.add(PasswordUtils.hashPassword(account.getPassword()));
        }

        if (updates.isEmpty()) {
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

            return preparedStatement.executeUpdate() > 0;
        }
    }

    public boolean verifyCredentials(String email, String plainPassword) throws SQLException {
        if (email == null || plainPassword == null) {
            return false;
        }

        String sql = "SELECT c.contraseña, u.estado FROM cuenta c " +
                "JOIN usuario u ON c.id_usuario = u.id_usuario " +
                "WHERE c.correo_e = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, email);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    if (resultSet.getString("estado").charAt(0) != 'A') {
                        return false;
                    }
                    return PasswordUtils.checkPassword(plainPassword, resultSet.getString("contraseña"));
                }
                return false;
            }
        }
    }

    public Account getAccountByUserId(int idUser) throws SQLException {
        String sql = "SELECT id_usuario, correo_e, contraseña FROM cuenta WHERE id_usuario = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, idUser);
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
        return null;
    }

    public Account getAccountByEmail(String email) throws SQLException {
        if (email == null || email.isEmpty()) {
            return null;
        }

        String sql = "SELECT id_usuario, correo_e, contraseña FROM cuenta WHERE correo_e = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

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
        return null;
    }

    public boolean accountExists(String email) throws SQLException {
        if (email == null || email.isEmpty()) {
            return false;
        }

        String sql = "SELECT 1 FROM cuenta WHERE correo_e = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, email);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private void validateEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }
}