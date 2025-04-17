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
        String sql = "INSERT INTO representante (nombre_completo, correo_electronico, telefono, id_organizacion) VALUES (?, ?, ?, ?)";

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
        String sql = "UPDATE representante SET nombre_completo = ?, correo_electronico = ?, telefono = ?, id_organizacion = ? WHERE id_representante = ?";

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
        String sql = "SELECT r.*, o.nombre_empresa as org_nombre, o.telefono as org_telefono, o.correo_empresarial as org_email, o.estado as org_status " +
                "FROM representante r " +
                "JOIN organizacion_vinculada o ON r.id_empresa= o.id_empresa";
        List<Representative> representatives = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                LinkedOrganization org = new LinkedOrganization(
                        rs.getInt("id_empresa"),
                        rs.getString("nombre_empresa"),
                        rs.getString("telefono"),
                        rs.getString("correo_empresarial"),
                        rs.getString("estado").charAt(0)
                );

                Representative rep = new Representative(
                        rs.getInt("id_representante"),
                        rs.getString("nombre_completo"),
                        rs.getString("correo_electronico"),
                        rs.getString("telefono"),
                        org
                );
                representatives.add(rep);
            }
        }
        return representatives;
    }

    public Representative getRepresentativeById(int id) throws SQLException {
        String sql = "SELECT r.*, o.nombre_empresa as org_nombre, o.telefono as org_telefono, o.correo_empresarial as org_email, o.estado as org_status " +
                "FROM representante r " +
                "JOIN organizacion_vinculada o ON r.id_organizacion = o.id_organizacion " +
                "WHERE r.id_representante = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LinkedOrganization org = new LinkedOrganization(
                            rs.getInt("id_empresa"),
                            rs.getString("nombre_empresa"),
                            rs.getString("telefono"),
                            rs.getString("correo_empresarial"),
                            rs.getString("estado").charAt(0)
                    );

                    return new Representative(
                            rs.getInt("id_representante"),
                            rs.getString("nombre_completo"),
                            rs.getString("correo_electronico"),
                            rs.getString("telefono"),
                            org
                    );
                }
            }
        }
        return null;
    }

    public Representative getRepresentativeByEmail(String email) throws SQLException {
        String sql = "SELECT r.*, o.nombre_empresa as org_nombre, o.telefono as org_telefono, o.correo_empresarial as org_email, o.estado as org_status" +
                "FROM representante r " +
                "JOIN organizacion_vinculada o ON r.id_empresa = o.id_empresa " +
                "WHERE r.correo_electronico = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LinkedOrganization org = new LinkedOrganization(
                            rs.getInt("id_empresa"),
                            rs.getString("nombre_empresa"),
                            rs.getString("telefono"),
                            rs.getString("correo_empresarial"),
                            rs.getString("estado").charAt(0)
                    );

                    return new Representative(
                            rs.getInt("id_representante"),
                            rs.getString("nombre_completo"),
                            rs.getString("correo_electronico"),
                            rs.getString("telefono"),
                            org
                    );
                }
            }
        }
        return null;
    }

    public List<Representative> getRepresentativesByOrganization(int organizationId) throws SQLException {
        String sql = "SELECT r.*, o.nombre_empresa as org_nombre, o.telefono as org_telefono, o.correo_empresarial as org_email, o.estado as org_statusil " +
                "FROM representante r " +
                "JOIN organizacion_vinculada o ON r.id_empresa = o.id_empresa " +
                "WHERE r.id_empresa = ?";
        List<Representative> representatives = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, organizationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LinkedOrganization org = new LinkedOrganization(
                            rs.getInt("id_empresa"),
                            rs.getString("nombre_empresa"),
                            rs.getString("telefono"),
                            rs.getString("correo_empresarial"),
                            rs.getString("estado").charAt(0)
                    );

                    Representative rep = new Representative(
                            rs.getInt("id_representante"),
                            rs.getString("nombre_completo"),
                            rs.getString("correo_electronico"),
                            rs.getString("telefono"),
                            org
                    );
                    representatives.add(rep);
                }
            }
        }
        return representatives;
    }

    public boolean representativeExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM representantes WHERE correo_electronico = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int countRepresentatives() throws SQLException {
        String sql = "SELECT COUNT(*) FROM representantes";

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }
}