package logic.daos;

import dataaccess.ConnectionDataBase;
import logic.logicclasses.Coordinator;
import logic.interfaces.ICoordinatorDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CoordinatorDAO implements ICoordinatorDAO {
    private static final Coordinator EMPTY_COORDINATOR = new Coordinator(-1, "", "", "", "",'I');
    private final UserDAO userDAO;

    public CoordinatorDAO() {
        this.userDAO = new UserDAO();
    }

    public boolean addCoordinator(Coordinator coordinator) throws SQLException, IllegalArgumentException {
        if (coordinator == null) {
            throw new IllegalArgumentException("El coordinador no debe ser nulo");
        }
        boolean userAdded = userDAO.addUser(coordinator);
        if (!userAdded) {
            return false;
        }

        String sql = "INSERT INTO coordinador (id_usuario, numero_personal) VALUES (?, ?)";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, coordinator.getIdUser());
            preparedStatement.setString(2, coordinator.getStaffNumber());

            return preparedStatement.executeUpdate() > 0;
        }
    }

    public boolean deleteCoordinator(Coordinator coordinator) throws SQLException, IllegalArgumentException {
        if (coordinator == null) {
            throw new IllegalArgumentException("El coordinador no debe ser nulo");
        }
        String sql = "DELETE FROM coordinador WHERE id_usuario = ?";
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

    public boolean updateCoordinator(Coordinator coordinator) throws SQLException, IllegalArgumentException {
        if (coordinator == null ||
            coordinator.getStaffNumber() == null || coordinator.getStaffNumber().isEmpty() ||
            coordinator.getFullName() == null || coordinator.getFullName().isEmpty() ||
            coordinator.getCellPhone() == null || coordinator.getCellPhone().isEmpty()) {
            throw new IllegalArgumentException("Los datos del coordinador no deben ser nulos o vacíos");
        }

        boolean userUpdated = userDAO.updateUser(coordinator);
        if (!userUpdated) {
            return false;
        }

        String sql = "UPDATE coordinador SET numero_personal = ? WHERE id_usuario = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, coordinator.getStaffNumber());
            statement.setInt(2, coordinator.getIdUser());

            return statement.executeUpdate() > 0;
        }
    }

    public List<Coordinator> getAllCoordinators() throws SQLException {
        String sql = "SELECT u.id_usuario, u.nombre_completo, u.telefono, u.extension_telefono, u.estado, " +
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
                        resultSet.getString("extension_telefono"),
                        resultSet.getString("numero_personal"),
                        resultSet.getString("estado").charAt(0)
                );
                coordinators.add(coordinator);
            }
        }
        return coordinators;
    }

    public Coordinator getCoordinatorByStaffNumber(String staffNumber) throws SQLException, IllegalArgumentException {
        if (staffNumber == null || staffNumber.isEmpty()) {
            throw new IllegalArgumentException("Numero de personal no debe ser nulo o vacío");
        }
        String sql = "SELECT u.id_usuario, u.nombre_completo, u.telefono, u.extension_telefono, u.estado, " +
                "c.numero_personal " +
                "FROM coordinador c " +
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
                            resultSet.getString("extension_telefono"),
                            resultSet.getString("numero_personal"),
                            resultSet.getString("estado").charAt(0)
                    );
                }
            }
        }
        return EMPTY_COORDINATOR;
    }

    public boolean coordinatorExists(String staffNumber) throws SQLException, IllegalArgumentException {
        if (staffNumber == null || staffNumber.isEmpty()) {
            throw new IllegalArgumentException("Numero de personal no debe ser nulo o vacío");
        }
        String sql = "SELECT 1 FROM coordinador WHERE numero_personal = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, staffNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public int countCoordinators() throws SQLException {
        String sql = "SELECT COUNT(*) FROM coordinador";
        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    public boolean existsForUser(int userId) throws SQLException {
        String sql = "SELECT 1 FROM coordinador WHERE id_usuario = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, userId);
            return preparedStatement.executeQuery().next();
        }
    }

    public Coordinator getFullCoordinator(int userId) throws SQLException {
        String sql = "SELECT u.id_usuario, u.nombre_completo, u.telefono, u.extension_telefono, u.estado, " +
                "c.numero_personal FROM usuario u " +
                "JOIN coordinador c ON u.id_usuario = c.id_usuario " +
                "WHERE u.id_usuario = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Coordinator(
                            resultSet.getInt("id_usuario"),
                            resultSet.getString("nombre_completo"),
                            resultSet.getString("telefono"),
                            resultSet.getString("extension_telefono"),
                            resultSet.getString("numero_personal"),
                            resultSet.getString("estado").charAt(0)
                    );
                }
            }
        }
        return EMPTY_COORDINATOR;
    }
}
