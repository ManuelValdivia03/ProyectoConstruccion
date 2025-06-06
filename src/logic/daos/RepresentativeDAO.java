package logic.daos;

import dataaccess.ConnectionDataBase;
import logic.logicclasses.LinkedOrganization;
import logic.logicclasses.Representative;
import logic.interfaces.IRepresentativeDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RepresentativeDAO implements IRepresentativeDAO {
    private static final Logger logger = LogManager.getLogger(RepresentativeDAO.class);

    public boolean addRepresentative(Representative representative) throws SQLException {
        if (representative == null || representative.getFullName() == null ||
                representative.getLinkedOrganization() == null) {
            throw new SQLException("Datos del representante incompletos");
        }
        String sql = "INSERT INTO representante (nombre_completo, correo_e, telefono, Id_empresa) VALUES (?, ?, ?, ?)";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, representative.getFullName());
            ps.setString(2, representative.getEmail());
            ps.setString(3, representative.getCellPhone());
            ps.setInt(4, representative.getLinkedOrganization().getIdLinkedOrganization());
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        representative.setIdRepresentative(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            logger.error("Error al agregar representante: {}", representative != null ? representative.getFullName() : "null", e);
            throw e;
        }
    }

    public boolean addRepresentativeWithoutOrganization(Representative representative) throws SQLException {
        if (representative == null || representative.getFullName() == null) {
            throw new SQLException("Datos del representante incompletos");
        }
        String sql = "INSERT INTO representante (nombre_completo, correo_e, telefono, Id_empresa) VALUES (?, ?, ?, NULL)";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, representative.getFullName());
            ps.setString(2, representative.getEmail());
            ps.setString(3, representative.getCellPhone());
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        representative.setIdRepresentative(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            logger.error("Error al agregar representante: {}", representative != null ? representative.getFullName() : "null", e);
            throw e;
        }
    }

    public boolean linkRepresentativeToOrganization(int representativeId, int organizationId) throws SQLException {
        String sql = "UPDATE representante SET Id_empresa = ? WHERE id_representante = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, organizationId);
            ps.setInt(2, representativeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error al vincular representante", e);
            throw e;
        }
    }

    public boolean deleteRepresentative(Representative representative) throws SQLException {
        if (representative == null) {
            return false;
        }
        String sql = "DELETE FROM representante WHERE id_representante = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, representative.getIdRepresentative());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error al eliminar representante ID: {}", representative != null ? representative.getIdRepresentative() : "null", e);
            throw e;
        }
    }

    public boolean updateRepresentative(Representative representative) throws SQLException {
        if (representative == null || representative.getFullName() == null ||
                representative.getLinkedOrganization() == null) {
            throw new SQLException("Datos del representante incompletos");
        }
        String sql = "UPDATE representante SET nombre_completo = ?, correo_e = ?, telefono = ?, Id_empresa = ? WHERE id_representante = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, representative.getFullName());
            ps.setString(2, representative.getEmail());
            ps.setString(3, representative.getCellPhone());
            ps.setInt(4, representative.getLinkedOrganization().getIdLinkedOrganization());
            ps.setInt(5, representative.getIdRepresentative());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error al actualizar representante ID: {}", representative != null ? representative.getIdRepresentative() : "null", e);
            throw e;
        }
    }

    public List<Representative> getAllRepresentatives() throws SQLException {
        String sql = "SELECT r.*, o.nombre_empresa, o.telefono as org_telefono, o.correo_empresarial, o.estado " +
                "FROM representante r " +
                "LEFT JOIN organizacion_vinculada o ON r.Id_empresa = o.id_empresa";
        List<Representative> representatives = new ArrayList<>();
        try (Connection connection = ConnectionDataBase.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                representatives.add(mapRepresentativeFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Error al obtener todos los representantes", e);
            throw e;
        }
        return representatives;
    }

    public Representative getRepresentativeById(int id) throws SQLException {
        if (id <= 0) {
            return null;
        }
        String sql = "SELECT r.*, o.nombre_empresa, o.telefono as org_telefono, o.correo_empresarial, o.estado " +
                "FROM representante r " +
                "LEFT JOIN organizacion_vinculada o ON r.Id_empresa = o.id_empresa " +
                "WHERE r.id_representante = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRepresentativeFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al buscar representante por ID: {}", id, e);
            throw e;
        }
        return null;
    }

    public Representative getRepresentativeByEmail(String email) throws SQLException {
        if (email == null || email.isEmpty()) {
            return null;
        }
        String sql = "SELECT r.*, o.nombre_empresa, o.telefono as org_telefono, o.correo_empresarial, o.estado " +
                "FROM representante r " +
                "LEFT JOIN organizacion_vinculada o ON r.Id_empresa = o.id_empresa " +
                "WHERE r.correo_e = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRepresentativeFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al buscar representante por email: {}", email, e);
            throw e;
        }
        return null;
    }

    public List<Representative> getRepresentativesByOrganization(int organizationId) throws SQLException {
        if (organizationId <= 0) {
            return new ArrayList<>();
        }
        String sql = "SELECT r.*, o.nombre_empresa, o.telefono as org_telefono, o.correo_empresarial, o.estado " +
                "FROM representante r " +
                "LEFT JOIN organizacion_vinculada o ON r.Id_empresa = o.id_empresa " +
                "WHERE r.Id_empresa = ?";
        List<Representative> representatives = new ArrayList<>();
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, organizationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    representatives.add(mapRepresentativeFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error al obtener representantes por organización ID: {}", organizationId, e);
            throw e;
        }
        return representatives;
    }

    public boolean representativeExists(String email) throws SQLException {
        if (email == null || email.isEmpty()) {
            return false;
        }
        String sql = "SELECT 1 FROM representante WHERE correo_e = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Error al verificar existencia de representante: {}", email, e);
            throw e;
        }
    }

    public int countRepresentatives() throws SQLException {
        String sql = "SELECT COUNT(*) FROM representante";
        try (Connection connection = ConnectionDataBase.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            logger.error("Error al contar representantes", e);
            throw e;
        }
    }

    private Representative mapRepresentativeFromResultSet(ResultSet rs) throws SQLException {
        LinkedOrganization org = null;
        int orgId = rs.getInt("Id_empresa");
        if (!rs.wasNull()) {
            org = new LinkedOrganization(
                    orgId,
                    rs.getString("nombre_empresa"),
                    rs.getString("org_telefono"),
                    rs.getString("correo_empresarial"),
                    rs.getString("estado") != null && !rs.getString("estado").isEmpty() ? rs.getString("estado").charAt(0) : ' '
            );
        }
        return new Representative(
                rs.getInt("id_representante"),
                rs.getString("nombre_completo"),
                rs.getString("correo_e"),
                rs.getString("telefono"),
                org
        );
    }

    public List<Representative> getAllRepresentativesWithOrganizationName() throws SQLException {
        return getAllRepresentatives();
    }

    public Representative getRepresentativeByNameWithOrganization(String name) throws SQLException {
        if (name == null || name.isEmpty()) {
            return null;
        }
        String sql = "SELECT r.*, o.nombre_empresa, o.telefono as org_telefono, o.correo_empresarial, o.estado " +
                "FROM representante r " +
                "LEFT JOIN organizacion_vinculada o ON r.Id_empresa = o.id_empresa " +
                "WHERE r.nombre_completo = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRepresentativeFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al buscar representante por nombre: {}", name, e);
            throw e;
        }
        return null;
    }
}
