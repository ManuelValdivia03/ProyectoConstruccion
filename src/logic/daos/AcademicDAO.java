package logic.daos;

import dataaccess.ConnectionDataBase;
import logic.exceptions.RepeatedStaffNumberException;
import logic.logicclasses.Academic;
import logic.enums.AcademicType;
import logic.interfaces.IAcademicDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AcademicDAO implements IAcademicDAO {
    private static final Academic EMPTY_ACADEMIC = new Academic(-1, "", "", "",'I', "", AcademicType.NONE);
    private final UserDAO userDAO;

    public AcademicDAO() {
        this.userDAO = new UserDAO();
    }

    @Override
    public boolean addAcademic(Academic academic) throws SQLException, RepeatedStaffNumberException, IllegalArgumentException {
        validateAcademic(academic);

        String query = "INSERT INTO academico (id_usuario, numero_personal, tipo) VALUES (?, ?, ?)";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, academic.getIdUser());
            preparedStatement.setString(2, academic.getStaffNumber());
            preparedStatement.setString(3, academic.getAcademicType().toString());

            return preparedStatement.executeUpdate() > 0;
        }
    }

    @Override
    public List<Academic> getAllAcademics() throws SQLException{
        String query = "SELECT u.id_usuario, u.nombre_completo, u.telefono, u.extension_telefono, u.estado, " +
                "a.numero_personal, a.tipo FROM academico a " +
                "JOIN usuario u ON a.id_usuario = u.id_usuario";
        List<Academic> academics = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                academics.add(new Academic(
                        resultSet.getInt("id_usuario"),
                        resultSet.getString("nombre_completo"),
                        resultSet.getString("telefono"),
                        resultSet.getString("extension_telefono"),
                        resultSet.getString("estado").charAt(0),
                        resultSet.getString("numero_personal"),
                        AcademicType.valueOf(resultSet.getString("tipo"))
                ));
            }
        }
        return academics;
    }

    @Override
    public Academic getAcademicByStaffNumber(String staffNumber) throws SQLException, IllegalArgumentException {
        if (staffNumber == null || staffNumber.isEmpty()) {
            throw new IllegalArgumentException("Staff number must not be null or empty");
        }

        String query = "SELECT u.id_usuario, u.nombre_completo, u.telefono, u.extension_telefono, u.estado, " +
                "a.numero_personal, a.tipo FROM academico a " +
                "JOIN usuario u ON a.id_usuario = u.id_usuario " +
                "WHERE a.numero_personal = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, staffNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Academic(
                            resultSet.getInt("id_usuario"),
                            resultSet.getString("nombre_completo"),
                            resultSet.getString("telefono"),
                            resultSet.getString("extension_telefono"),
                            resultSet.getString("estado").charAt(0),
                            resultSet.getString("numero_personal"),
                            AcademicType.valueOf(resultSet.getString("tipo"))
                    );
                }
            }
        }
        return EMPTY_ACADEMIC;
    }

    @Override
    public boolean updateAcademic(Academic academic) throws SQLException, IllegalArgumentException {
        if (academic == null) {
            throw new IllegalArgumentException("Academic must not be null");
        }

        if (!userDAO.updateUser(academic)) {
            return false;
        }

        String query = "UPDATE academico SET numero_personal = ?, tipo = ? WHERE id_usuario = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, academic.getStaffNumber());
            statement.setString(2, academic.getAcademicType().toString());
            statement.setInt(3, academic.getIdUser());

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteAcademic(Academic academic) throws SQLException, IllegalArgumentException {
        if (academic == null) {
            throw new IllegalArgumentException("Academic must not be null");
        }

        String query = "DELETE FROM academico WHERE id_usuario = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, academic.getIdUser());
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                return userDAO.deleteUser(academic.getIdUser());
            }
            return false;
        }
    }

    @Override
    public List<Academic> getAllAcademicsByType(AcademicType type) throws SQLException {
        if (type == null) {
            return Collections.emptyList();
        }

        String query = "SELECT u.id_usuario, u.nombre_completo, u.telefono, u.extension_telefono, u.estado, " +
                "a.numero_personal, a.tipo FROM academico a " +
                "JOIN usuario u ON a.id_usuario = u.id_usuario " +
                "WHERE a.tipo = ?";

        List<Academic> academics = new ArrayList<>();
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, type.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    academics.add(new Academic(
                            resultSet.getInt("id_usuario"),
                            resultSet.getString("nombre_completo"),
                            resultSet.getString("telefono"),
                            resultSet.getString("extension_telefono"),
                            resultSet.getString("estado").charAt(0),
                            resultSet.getString("numero_personal"),
                            AcademicType.valueOf(resultSet.getString("tipo"))
                    ));
                }
            }
        }
        return academics;
    }

    @Override
    public Academic getAcademicById(int idUser) throws SQLException {
        String query = "SELECT u.id_usuario, u.nombre_completo, u.telefono, u.extension_telefono, u.estado, " +
                "a.numero_personal, a.tipo FROM academico a " +
                "JOIN usuario u ON a.id_usuario = u.id_usuario " +
                "WHERE u.id_usuario = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, idUser);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Academic(
                            resultSet.getInt("id_usuario"),
                            resultSet.getString("nombre_completo"),
                            resultSet.getString("telefono"),
                            resultSet.getString("extension_telefono"),
                            resultSet.getString("estado").charAt(0),
                            resultSet.getString("numero_personal"),
                            AcademicType.valueOf(resultSet.getString("tipo"))
                    );
                }
            }
        }
        return EMPTY_ACADEMIC;
    }

    public boolean academicExists(String staffNumber) throws SQLException, IllegalArgumentException {
        if (staffNumber == null || staffNumber.isEmpty()) {
            throw new IllegalArgumentException("Staff number must not be null or empty");
        }

        String query = "SELECT 1 FROM academico WHERE numero_personal = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, staffNumber);
            return statement.executeQuery().next();
        }
    }

    @Override
    public int countAllAcademics() throws SQLException {
        String query = "SELECT COUNT(*) FROM academico";
        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(query)) {

            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    @Override
    public boolean changeAcademicType(Academic academic) throws SQLException, IllegalArgumentException {
        if (academic == null) {
            throw new IllegalArgumentException("Academic must not be null");
        }

        String query = "UPDATE academico SET tipo = ? WHERE numero_personal = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, academic.getAcademicType().toString());
            statement.setString(2, academic.getStaffNumber());

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean staffNumberExists(String staffNumber) throws RepeatedStaffNumberException, IllegalArgumentException {
        if (staffNumber == null || staffNumber.isEmpty()) {
            throw new IllegalArgumentException("Staff number must not be null or empty");
        }

        String query = "SELECT 1 FROM academico WHERE numero_personal = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, staffNumber);
            return statement.executeQuery().next();
        } catch (SQLException e) {
            throw new RepeatedStaffNumberException();
        }
    }

    @Override
    public List<Academic> getAllAcademicsFromView() throws SQLException {
        String query = "SELECT * FROM vista_academicos_completa";
        List<Academic> academics = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                academics.add(new Academic(
                        resultSet.getInt("id_usuario"),
                        resultSet.getString("nombre_completo"),
                        resultSet.getString("telefono"),
                        resultSet.getString("extension_telefono"),
                        resultSet.getString("estado").charAt(0),
                        resultSet.getString("numero_personal"),
                        AcademicType.valueOf(resultSet.getString("tipo_academico"))
                ));
            }
        }
        return academics;
    }

    @Override
    public List<Academic> getAcademicsByStatusFromView(char estado) throws SQLException {
        String query = "SELECT * FROM vista_academicos_completa WHERE estado = ?";
        List<Academic> academics = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, String.valueOf(estado));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    academics.add(new Academic(
                            resultSet.getInt("id_usuario"),
                            resultSet.getString("nombre_completo"),
                            resultSet.getString("telefono"),
                            resultSet.getString("extension_telefono"),
                            resultSet.getString("estado").charAt(0),
                            resultSet.getString("numero_personal"),
                            AcademicType.valueOf(resultSet.getString("tipo_academico"))
                    ));
                }
            }
        }
        return academics;
    }

    @Override
    public boolean existsAcademic(int userId) throws SQLException {
        String query = "SELECT 1 FROM academico WHERE id_usuario = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            return statement.executeQuery().next();
        }
    }

    private void validateAcademic(Academic academic) throws IllegalArgumentException, SQLException, RepeatedStaffNumberException {
        if (academic == null) {
            throw new IllegalArgumentException("Academic must not be null");
        }

        if (academicExists(academic.getStaffNumber())) {
            throw new RepeatedStaffNumberException();
        }
    }
}
