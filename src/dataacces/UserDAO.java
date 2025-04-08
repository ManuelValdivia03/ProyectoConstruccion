package dataacces;

import logic.User;
import logic.interfaces.IUserDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO implements IUserDAO {

    public boolean addUser(User user) throws SQLException {
        String sql = "INSERT INTO usuario (nombre_completo, telefono) VALUES (?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, user.getFullName());
            preparedStatement.setString(2, user.getCellphone());
            preparedStatement.executeUpdate();

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setIdUser(generatedKeys.getInt(1));
                }
            }
            return true;
        }
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
                }
            }
        }
        return user;
    }

    public boolean updateUser(User user) throws SQLException {
        String sql = "UPDATE usuario SET nombre_completo = ?, telefono = ? WHERE id_usuario = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getFullName());
            statement.setString(2, user.getCellphone());
            statement.setInt(3, user.getIdUser());

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
}