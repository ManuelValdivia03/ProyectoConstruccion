package dataacces;

import logic.Academic;
import logic.enumacademic.AcademicType;
import logic.interfaces.IAcademicDAO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AcademicDAO implements IAcademicDAO {
    private final UserDAO userDAO;

    public AcademicDAO() {
        this.userDAO = new UserDAO();
    }

    public boolean addAcademic(Academic academic) throws SQLException {
        boolean userAdded = userDAO.addUser(academic);
        if (!userAdded) {
            return false;
        }

        String sql = "INSERT INTO academicos (id_usuario, numero_personal, tipo) VALUES (?, ?, ?)";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, academic.getIdUser());
            preparedStatement.setString(2, academic.getStaffNumber());
            preparedStatement.setString(3, academic.getAcademicType().toString());

            return preparedStatement.executeUpdate() > 0;
        }
    }

    public List<Academic> getAllAcademics() throws SQLException {
        String sql = "SELECT u.id_usuario, u.nombre_completo, u.telefono, u.estado " +
                "a.numero_personal, a.tipo " +
                "FROM academicos a " +
                "JOIN usuario u ON a.id_usuario = u.id_usuario";

        List<Academic> academics = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Academic academic = new Academic(
                        resultSet.getInt("id_usuario"),
                        resultSet.getString("nombre_completo"),
                        resultSet.getString("telefono"),
                        resultSet.getString("numero_personal"),
                        resultSet.getString("estado").charAt(0),
                        AcademicType.valueOf(resultSet.getString("tipo"))
                );
                academics.add(academic);
            }
        }
        return academics;
    }

    public Academic getAcademicByStaffNumber(String staffNumber) throws SQLException {
        String sql = "SELECT u.id_usuario, u.nombre_completo, u.telefono, u.estado " +
                "a.numero_personal, a.tipo " +
                "FROM academicos a " +
                "JOIN usuario u ON a.id_usuario = u.id_usuario " +
                "WHERE a.numero_personal = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, staffNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Academic(
                            resultSet.getInt("id_usuario"),
                            resultSet.getString("nombre_completo"),
                            resultSet.getString("telefono"),
                            resultSet.getString("numero_personal"),
                            resultSet.getString("estado").charAt(0),
                            AcademicType.valueOf(resultSet.getString("tipo"))
                    );
                }
            }
        }
        return null;
    }

    public boolean updateAcademic(Academic academic) throws SQLException {
        boolean userUpdated = userDAO.updateUser(academic);
        if (!userUpdated) {
            return false;
        }

        String sql = "UPDATE academicos SET numero_personal = ?, tipo = ? WHERE id_usuario = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, academic.getStaffNumber());
            statement.setString(2, academic.getAcademicType().toString());
            statement.setInt(3, academic.getIdUser());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteAcademic(Academic academic) throws SQLException {
        String sql = "DELETE FROM academicos WHERE id_usuario = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, academic.getIdUser());
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                return userDAO.deleteUser(academic.getIdUser());
            }
            return false;
        }
    }

    public List<Academic> getAllAcademicsByType(AcademicType type) throws SQLException {
        String sql = "SELECT u.id_usuario, u.nombre_completo, u.telefono, u.estado " +
                "a.numero_personal, a.tipo " +
                "FROM academicos a " +
                "JOIN usuario u ON a.id_usuario = u.id_usuario " +
                "WHERE a.tipo = ?";

        List<Academic> academics = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, type.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    academics.add(new Academic(
                            resultSet.getInt("id_usuario"),
                            resultSet.getString("nombre_completo"),
                            resultSet.getString("telefono"),
                            resultSet.getString("numero_personal"),
                            resultSet.getString("estado").charAt(0),
                            AcademicType.valueOf(resultSet.getString("tipo"))
                    ));
                }
            }
        }
        return academics;
    }

    public boolean academicExists(String staffNumber) throws SQLException {
        String sql = "SELECT 1 FROM academicos WHERE numero_personal = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, staffNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public int countAcademics() throws SQLException {
        String sql = "SELECT COUNT(*) FROM academicos";

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return 0;
        }
    }

    public boolean changeAcademicType(Academic academic) throws SQLException {
        String sql = "UPDATE academicos SET tipo = ? WHERE numero_personal = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, academic.getAcademicType().toString());
            statement.setString(2, academic.getStaffNumber());

            return statement.executeUpdate() > 0;
        }
    }
}