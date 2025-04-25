package logic.daos;

import dataaccess.ConnectionDataBase;
import logic.exceptions.InvalidCellPhoneException;
import logic.exceptions.RepeatedCellPhoneException;
import logic.logicclasses.User;
import logic.interfaces.IUserDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO implements IUserDAO {

    public boolean addUser(User user) throws SQLException, IllegalArgumentException, RepeatedCellPhoneException {
        if (user == null) {
            throw new IllegalArgumentException();
        }

        String cleanPhone = user.getCellPhone().replaceAll("[^0-9]", "");

        if (!cleanPhone.matches("^\\d{10}$")) {
            throw new IllegalArgumentException();
        }

        if (cellPhoneExists(cleanPhone)) {
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
                    }
                }
                return true;
            }
        }
        return false;
    }

    public List<User> getAllUsers() throws SQLException {
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
        }
        return users;
    }

    public User getUserById(int id) throws SQLException {
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
                }
            }
        }
        return user;
    }

    public boolean updateUser(User user) throws SQLException {
        String sql = "UPDATE usuario SET nombre_completo = ?, telefono = ?, estado = ? WHERE id_usuario = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getFullName());
            statement.setString(2, user.getCellPhone());
            statement.setString(3, String.valueOf(user.getStatus()));
            statement.setInt(4, user.getIdUser());

            return statement.executeUpdate() > 0;
        }
    }


    public boolean deleteUser(int id) throws SQLException {
        String sql = "DELETE FROM usuario WHERE id_usuario = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    public List<User> searchUsersByName(String name) throws SQLException {
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
        }
        return users;
    }

    public boolean userExists(int id) throws SQLException {
        String sql = "SELECT 1 FROM usuario WHERE id_usuario = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public boolean cellPhoneExists(String cellPhone) throws SQLException, InvalidCellPhoneException {
        if (cellPhone == null || !cellPhone.matches("^\\d{10}$")){
            throw new InvalidCellPhoneException();
        }

        String sql = "SELECT 1 FROM usuario WHERE telefono = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cellPhone);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public int countUsers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuario";

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return 0;
        }
    }

    public int getIdUserByCellPhone(String cellPhone) throws SQLException {
        String sql = "SELECT id_usuario FROM usuario WHERE telefono = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cellPhone);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id_usuario");
                }
            }
        }
        return 0;
    }
}