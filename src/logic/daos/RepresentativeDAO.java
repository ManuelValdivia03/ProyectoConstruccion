package logic.daos;

import dataaccess.ConnectionDataBase;
import logic.logicclasses.LinkedOrganization;
import logic.logicclasses.Representative;
import logic.interfaces.IRepresentativeDAO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RepresentativeDAO implements IRepresentativeDAO {

    public boolean addRepresentative(Representative representative) throws SQLException {
        // Validación de campos obligatorios
        if (representative == null || representative.getFullName() == null ||
                representative.getLinkedOrganization() == null) {
            throw new SQLException("Representative data is incomplete or null");
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
        }
    }

    public boolean deleteRepresentative(Representative representative) throws SQLException {
        String sql = "DELETE FROM representante WHERE id_representante = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, representative.getIdRepresentative());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateRepresentative(Representative representative) throws SQLException {
        // Validación de campos obligatorios
        if (representative == null || representative.getFullName() == null ||
                representative.getLinkedOrganization() == null) {
            throw new SQLException("Representative data is incomplete or null");
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
                Representative rep = mapRepresentativeFromResultSet(rs);
                representatives.add(rep);
            }
        }
        return representatives;
    }

    public Representative getRepresentativeById(int id) throws SQLException {
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
        }
        return null;
    }

    public Representative getRepresentativeByEmail(String email) throws SQLException {
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
        }
        return null;
    }

    public List<Representative> getRepresentativesByOrganization(int organizationId) throws SQLException {
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
                    Representative rep = mapRepresentativeFromResultSet(rs);
                    representatives.add(rep);
                }
            }
        }
        return representatives;
    }

    public boolean representativeExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM representante WHERE correo_e = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int countRepresentatives() throws SQLException {
        String sql = "SELECT COUNT(*) FROM representante";

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    // Método auxiliar para mapear un ResultSet a un objeto Representative
    private Representative mapRepresentativeFromResultSet(ResultSet rs) throws SQLException {
        LinkedOrganization org = null;
        int orgId = rs.getInt("Id_empresa");

        // Solo crear la organización si existe la relación
        if (!rs.wasNull()) {
            org = new LinkedOrganization(
                    orgId,
                    rs.getString("nombre_empresa"),
                    rs.getString("org_telefono"),
                    rs.getString("correo_empresarial"),
                    rs.getString("estado").charAt(0)
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
}