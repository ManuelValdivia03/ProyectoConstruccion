package dataacces;

import dataacces.util.PasswordUtils;
import logic.Account;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {

    public List<Account> getAllAccounts() throws SQLException {
        String sql = "SELECT id_usuario, correo_e FROM cuenta";
        List<Account> accounts = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Account account = new Account();
                account.setIdUser(resultSet.getInt("id_usuario"));
                account.setEmail(resultSet.getString("correo_e"));
                accounts.add(account);
            }
        }
        return accounts;
    }

    public boolean addAccount(Account account) throws SQLException {
        String sql = "INSERT INTO cuenta (id_usuario,correo_e, contraseña) VALUES (?, ?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);) {

            String hashedPassword = PasswordUtils.hashPassword(account.getPassword());

            preparedStatement.setInt(1, account.getIdUser());
            preparedStatement.setString(2, account.getEmail());
            preparedStatement.setString(3, hashedPassword);
            preparedStatement.executeUpdate();

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    account.setIdUser(generatedKeys.getInt(1));
                }
            }
            return true;
        }
    }

    public boolean verifyCredentials(String email, String plainPassword) throws SQLException {
        String sql = "SELECT contraseña FROM cuenta WHERE correo_e = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String storedHash = resultSet.getString("contraseña");
                return PasswordUtils.checkPassword(plainPassword, storedHash);
            }
            return false;
        }
    }

    public boolean updatePassword(int idUser, String newPlainPassword) throws SQLException {
        String sql = "UPDATE cuenta SET contraseña = ? WHERE id_usuario = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            String hashedPassword = PasswordUtils.hashPassword(newPlainPassword);
            preparedStatement.setString(1, hashedPassword);
            preparedStatement.setInt(2, idUser);

            return preparedStatement.executeUpdate() > 0;
        }
    }
}
