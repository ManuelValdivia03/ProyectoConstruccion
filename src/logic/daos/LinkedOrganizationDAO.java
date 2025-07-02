package logic.daos;

import dataaccess.ConnectionDataBase;
import logic.logicclasses.LinkedOrganization;
import logic.interfaces.ILinkedOrganizationDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class LinkedOrganizationDAO implements ILinkedOrganizationDAO {
    private static final LinkedOrganization EMPTY_ORGANIZATION = new LinkedOrganization(-1, "", "", "", "", "", 'I');

    public boolean addLinkedOrganization(LinkedOrganization linkedOrganization) throws SQLException, IllegalArgumentException {
        if (linkedOrganization == null) {
            throw new IllegalArgumentException("La organización vinculada no debe ser nula");
        }

        String sql = "INSERT INTO organizacion_vinculada (nombre_empresa, telefono, extension_telefono, departamento, " +
                    "correo_empresarial, estado) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, linkedOrganization.getNameLinkedOrganization());
            preparedStatement.setString(2, linkedOrganization.getCellPhoneLinkedOrganization());
            preparedStatement.setString(3, linkedOrganization.getPhoneExtension());
            preparedStatement.setString(4, linkedOrganization.getDepartment());
            preparedStatement.setString(5, linkedOrganization.getEmailLinkedOrganization());
            preparedStatement.setString(6, String.valueOf(linkedOrganization.getStatus()));

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        linkedOrganization.setIdLinkedOrganization(generatedId);
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public boolean deleteLinkedOrganization(LinkedOrganization linkedOrganization) throws SQLException, IllegalArgumentException {
        if (linkedOrganization == null) {
            throw new IllegalArgumentException("La organización vinculada no debe ser nula");
        }

        String sql = "DELETE FROM organizacion_vinculada WHERE id_empresa = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, linkedOrganization.getIdLinkedOrganization());
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public boolean updateLinkedOrganization(LinkedOrganization linkedOrganization) throws SQLException, IllegalArgumentException {
        if (linkedOrganization == null) {
            throw new IllegalArgumentException("La organización vinculada no debe ser nula");
        }

        String sql = "UPDATE organizacion_vinculada SET nombre_empresa = ?, telefono = ?, extension_telefono = ?, " +
                    "departamento = ?, correo_empresarial = ?, estado = ? WHERE id_empresa = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, linkedOrganization.getNameLinkedOrganization());
            preparedStatement.setString(2, linkedOrganization.getCellPhoneLinkedOrganization());
            preparedStatement.setString(3, linkedOrganization.getPhoneExtension());
            preparedStatement.setString(4, linkedOrganization.getDepartment());
            preparedStatement.setString(5, linkedOrganization.getEmailLinkedOrganization());
            preparedStatement.setString(6, String.valueOf(linkedOrganization.getStatus()));
            preparedStatement.setInt(7, linkedOrganization.getIdLinkedOrganization());

            return preparedStatement.executeUpdate() > 0;
        }
    }

    public List<LinkedOrganization> getAllLinkedOrganizations() throws SQLException {
        String sql = "SELECT id_empresa, nombre_empresa, telefono, extension_telefono, departamento, " +
                    "correo_empresarial, estado FROM organizacion_vinculada";
        List<LinkedOrganization> organizations = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                organizations.add(new LinkedOrganization(
                    resultSet.getInt("id_empresa"),
                    resultSet.getString("nombre_empresa"),
                    resultSet.getString("telefono"),
                    resultSet.getString("extension_telefono"),
                    resultSet.getString("departamento"),
                    resultSet.getString("correo_empresarial"),
                    resultSet.getString("estado").charAt(0)
                ));
            }
        }
        return organizations;
    }

    public LinkedOrganization getLinkedOrganizationByTitle(String title) throws SQLException, IllegalArgumentException {
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("El nombre de la organización no debe ser nulo o vacío");
        }

        String sql = "SELECT id_empresa, nombre_empresa, telefono, extension_telefono, departamento, " +
                    "correo_empresarial, estado FROM organizacion_vinculada WHERE nombre_empresa = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, title);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new LinkedOrganization(
                        resultSet.getInt("id_empresa"),
                        resultSet.getString("nombre_empresa"),
                        resultSet.getString("telefono"),
                        resultSet.getString("extension_telefono"),
                        resultSet.getString("departamento"),
                        resultSet.getString("correo_empresarial"),
                        resultSet.getString("estado").charAt(0)
                    );
                }
            }
        }
        return EMPTY_ORGANIZATION;
    }

    public LinkedOrganization getLinkedOrganizationByID(int id) throws SQLException {
        if (id <= 0) {
            return EMPTY_ORGANIZATION;
        }

        String sql = "SELECT id_empresa, nombre_empresa, telefono, extension_telefono, departamento, " +
                    "correo_empresarial, estado FROM organizacion_vinculada WHERE id_empresa = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new LinkedOrganization(
                        resultSet.getInt("id_empresa"),
                        resultSet.getString("nombre_empresa"),
                        resultSet.getString("telefono"),
                        resultSet.getString("extension_telefono"),
                        resultSet.getString("departamento"),
                        resultSet.getString("correo_empresarial"),
                        resultSet.getString("estado").charAt(0)
                    );
                }
            }
        }
        return EMPTY_ORGANIZATION;
    }

    public boolean linkedOrganizationExists(String title) throws SQLException, IllegalArgumentException {
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("El nombre de la organización no debe ser nulo o vacío");
        }

        String sql = "SELECT 1 FROM organizacion_vinculada WHERE nombre_empresa = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, title);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public int countLinkedOrganizations() throws SQLException {
        String sql = "SELECT COUNT(*) FROM organizacion_vinculada";

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    public boolean phoneNumberExists(String phone) throws SQLException, IllegalArgumentException {
        if (phone == null || phone.isEmpty()) {
            throw new IllegalArgumentException("El teléfono no debe ser nulo o vacío");
        }

        String sql = "SELECT 1 FROM organizacion_vinculada WHERE telefono = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, phone);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public boolean emailExists(String email) throws SQLException, IllegalArgumentException {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("El correo electrónico no debe ser nulo o vacío");
        }

        String sql = "SELECT 1 FROM organizacion_vinculada WHERE correo_empresarial = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, email);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public int getRepresentativeId(int organizationId) throws SQLException {
        String sql = "SELECT id_representante FROM organizacionvinculada WHERE id_organizacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, organizationId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id_representante");
                }
                return -1;
            }
        }
    }
}
