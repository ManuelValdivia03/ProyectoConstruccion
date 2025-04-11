package dataacces;

import logic.Coordinator;
import logic.interfaces.ICoordinatorDAO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoordinatorDAO implements ICoordinatorDAO {
    private final UserDAO userDAO;

    public CoordinatorDAO() {
        this.userDAO = new UserDAO();
    }

    public boolean addCoordinator(Coordinator coordinator) throws SQLException {
        boolean userAdded = userDAO.addUser(coordinator);
        if (!userAdded) {
            return false;
        }

        String sql = "INSERT INTO coordinadores (id_usuario, numero_personal) VALUES (?, ?)";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, coordinator.getIdUser());
            preparedStatement.setString(2, coordinator.getStaffNumber());

            return preparedStatement.executeUpdate() > 0;
        }
    }

    public boolean deleteCoordinator(Coordinator coordinator) throws SQLException {
        String sql = "DELETE FROM coordinadores WHERE id_usuario = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, coordinator.getIdUser());
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                return userDAO.deleteUser(coordinator.getIdUser());
            }
            return false;
        }
    }

    public boolean updateCoordinator(Coordinator coordinator) throws SQLException {
        boolean userUpdated = userDAO.updateUser(coordinator);
        if (!userUpdated) {
            return false;
        }

        String sql = "UPDATE coordinadores SET numero_personal = ? WHERE id_usuario = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, coordinator.getStaffNumber());
            statement.setInt(2, coordinator.getIdUser());

            return statement.executeUpdate() > 0;
        }
    }

    public List<Coordinator> getAllCoordinators() throws SQLException {
        String sql = "SELECT u.id_usuario, u.nombre_completo, u.telefono, u.estado, " +
                "c.numero_personal " +
                "FROM coordinadores c " +
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
        }
        return coordinators;
    }

    public Coordinator getCoordinatorByStaffNumber(String staffNumber) throws SQLException {
        String sql = "SELECT u.id_usuario, u.nombre_completo, u.telefono, u.estado, " +
                "c.numero_personal " +
                "FROM coordinadores c " +
                "JOIN usuario u ON c.id_usuario = u.id_usuario " +
                "WHERE c.numero_personal = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, staffNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Coordinator(
                            resultSet.getInt("id_usuario"),
                            resultSet.getString("nombre_completo"),
                            resultSet.getString("telefono"),
                            resultSet.getString("numero_personal"),
                            resultSet.getString("estado").charAt(0)
                    );
                }
            }
        }
        return null;
    }

    public boolean coordinatorExists(String staffNumber) throws SQLException {
        String sql = "SELECT 1 FROM coordinadores WHERE numero_personal = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, staffNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public int countCoordinators() throws SQLException {
        String sql = "SELECT COUNT(*) FROM coordinadores";
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