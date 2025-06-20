package logic.daos;

import dataaccess.ConnectionDataBase;
import logic.logicclasses.LinkedOrganization;
import logic.logicclasses.Representative;
import logic.interfaces.IRepresentativeDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RepresentativeDAO implements IRepresentativeDAO {
    private static final Logger logger = LogManager.getLogger(RepresentativeDAO.class);
    private static final Representative EMPTY_REPRESENTATIVE = new Representative(-1, "", "", "", null);

    public boolean addRepresentative(Representative representative) throws SQLException {
        if (representative == null || representative.getFullName() == null ||
                representative.getLinkedOrganization() == null) {
            throw new IllegalArgumentException("Datos del representante incompletos");
        }
        String sql = "INSERT INTO representante (nombre_completo, correo_e, telefono, Id_empresa) VALUES (?, ?, ?, ?)";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, representative.getFullName());
            preparedStatement.setString(2, representative.getEmail());
            preparedStatement.setString(3, representative.getCellPhone());
            preparedStatement.setInt(4, representative.getLinkedOrganization().getIdLinkedOrganization());
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        representative.setIdRepresentative(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public boolean addRepresentativeWithoutOrganization(Representative representative) throws SQLException {
        if (representative == null || representative.getFullName() == null) {
            throw new IllegalArgumentException("Datos del representante incompletos");
        }
        String sql = "INSERT INTO representante (nombre_completo, correo_e, telefono, Id_empresa) VALUES (?, ?, ?, NULL)";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, representative.getFullName());
            preparedStatement.setString(2, representative.getEmail());
            preparedStatement.setString(3, representative.getCellPhone());
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        representative.setIdRepresentative(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public boolean linkRepresentativeToOrganization(int representativeId, int organizationId) throws SQLException {
        String sql = "UPDATE representante SET Id_empresa = ? WHERE id_representante = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, organizationId);
            preparedStatement.setInt(2, representativeId);
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public boolean deleteRepresentative(Representative representative) throws SQLException {
        if (representative == null) {
            throw new IllegalArgumentException("El representante no debe ser nulo");
        }
        String sql = "DELETE FROM representante WHERE id_representante = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, representative.getIdRepresentative());
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public boolean updateRepresentative(Representative representative) throws SQLException {
        if (representative == null || representative.getFullName() == null ||
                representative.getLinkedOrganization() == null) {
            throw new IllegalArgumentException("Datos del representante incompletos");
        }
        String sql = "UPDATE representante SET nombre_completo = ?, correo_e = ?, telefono = ?, Id_empresa = ? WHERE id_representante = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, representative.getFullName());
            preparedStatement.setString(2, representative.getEmail());
            preparedStatement.setString(3, representative.getCellPhone());
            preparedStatement.setInt(4, representative.getLinkedOrganization().getIdLinkedOrganization());
            preparedStatement.setInt(5, representative.getIdRepresentative());
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public List<Representative> getAllRepresentatives() throws SQLException {
        String sql = "SELECT r.*, o.nombre_empresa, o.telefono as org_telefono, " +
                "o.extension_telefono, o.departamento, o.correo_empresarial, o.estado " +
                "FROM representante r " +
                "LEFT JOIN organizacion_vinculada o ON r.Id_empresa = o.id_empresa";
        List<Representative> representatives = new ArrayList<>();
        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                representatives.add(mapRepresentativeFromResultSet(resultSet));
            }
        }
        return representatives;
    }

    public Representative getRepresentativeById(int id) throws SQLException {
        if (id <= 0) {
            return EMPTY_REPRESENTATIVE;
        }
        String sql = "SELECT r.*, o.nombre_empresa, o.telefono as org_telefono, " +
                "o.extension_telefono, o.departamento, o.correo_empresarial, o.estado " +
                "FROM representante r " +
                "LEFT JOIN organizacion_vinculada o ON r.Id_empresa = o.id_empresa " +
                "WHERE r.id_representante = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRepresentativeFromResultSet(resultSet);
                }
            }
        }
        return EMPTY_REPRESENTATIVE;
    }

    public Representative getRepresentativeByEmail(String email) throws SQLException {
        if (email == null || email.isEmpty()) {
            return EMPTY_REPRESENTATIVE;
        }
        String sql = "SELECT r.*, o.nombre_empresa, o.telefono as org_telefono, " +
                "o.extension_telefono, o.departamento, o.correo_empresarial, o.estado " +
                "FROM representante r " +
                "LEFT JOIN organizacion_vinculada o ON r.Id_empresa = o.id_empresa " +
                "WHERE r.correo_e = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, email);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRepresentativeFromResultSet(resultSet);
                }
            }
        }
        return EMPTY_REPRESENTATIVE;
    }

    public List<Representative> getRepresentativesByOrganization(int organizationId) throws SQLException {
        if (organizationId <= 0) {
            return Collections.emptyList();
        }
        String sql = "SELECT r.*, o.nombre_empresa, o.telefono as org_telefono, " +
                "o.extension_telefono, o.departamento, o.correo_empresarial, o.estado " +
                "FROM representante r " +
                "LEFT JOIN organizacion_vinculada o ON r.Id_empresa = o.id_empresa " +
                "WHERE r.Id_empresa = ?";
        List<Representative> representatives = new ArrayList<>();
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, organizationId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    representatives.add(mapRepresentativeFromResultSet(resultSet));
                }
            }
        }
        return representatives;
    }

    public boolean representativeExists(String email) throws SQLException {
        if (email == null || email.isEmpty()) {
            return false;
        }
        String sql = "SELECT 1 FROM representante WHERE correo_e = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, email);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public int countRepresentatives() throws SQLException {
        String sql = "SELECT COUNT(*) FROM representante";
        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    private Representative mapRepresentativeFromResultSet(ResultSet resultSet) throws SQLException {
        LinkedOrganization org = null;
        int orgId = resultSet.getInt("Id_empresa");
        if (!resultSet.wasNull()) {
            org = new LinkedOrganization(
                    orgId,
                    resultSet.getString("nombre_empresa"),
                    resultSet.getString("org_telefono"),
                    resultSet.getString("extension_telefono"),
                    resultSet.getString("departamento"),
                    resultSet.getString("correo_empresarial"),
                    resultSet.getString("estado") != null && !resultSet.getString("estado").isEmpty() ? 
                        resultSet.getString("estado").charAt(0) : ' '
            );
        }
        return new Representative(
                resultSet.getInt("id_representante"),
                resultSet.getString("nombre_completo"),
                resultSet.getString("correo_e"),
                resultSet.getString("telefono"),
                org
        );
    }

    public List<Representative> getAllRepresentativesWithOrganizationName() throws SQLException {
        return getAllRepresentatives();
    }

    public Representative getRepresentativeByNameWithOrganization(String name) throws SQLException {
        if (name == null || name.isEmpty()) {
            return EMPTY_REPRESENTATIVE;
        }
        String sql = "SELECT r.*, o.nombre_empresa, o.telefono as org_telefono, " +
                "o.extension_telefono, o.departamento, o.correo_empresarial, o.estado " +
                "FROM representante r " +
                "LEFT JOIN organizacion_vinculada o ON r.Id_empresa = o.id_empresa " +
                "WHERE r.nombre_completo = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, name);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRepresentativeFromResultSet(resultSet);
                }
            }
        }
        return EMPTY_REPRESENTATIVE;
    }

    public String getRepresentativeNameByProjectId(int projectId) throws SQLException {
        if (projectId <= 0) {
            return "";
        }
        String sql = "SELECT r.nombre_completo FROM representante r " +
                     "JOIN proyecto p ON r.id_representante = p.id_representante " +
                     "WHERE p.id_proyecto = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, projectId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("nombre_completo");
                }
            }
        }
        return "";
    }
}
