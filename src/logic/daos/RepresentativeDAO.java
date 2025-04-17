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
        String sql = "INSERT INTO representantes (nombre_completo, correo_electronico, telefono, id_organizacion) VALUES (?, ?, ?, ?)";

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
        String sql = "DELETE FROM representantes WHERE id_representante = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, representative.getIdRepresentative());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateRepresentative(Representative representative) throws SQLException {
        String sql = "UPDATE representantes SET nombre_completo = ?, correo_electronico = ?, telefono = ?, id_organizacion = ? WHERE id_representante = ?";

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
        String sql = "SELECT r.*, o.nombre as org_nombre, o.telefono as org_telefono, o.correo_electronico as org_email " +
                "FROM representantes r " +
                "JOIN organizaciones_vinculadas o ON r.id_organizacion = o.id_organizacion";
        List<Representative> representatives = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                LinkedOrganization org = new LinkedOrganization(
                        rs.getInt("id_organizacion"),
                        rs.getString("org_nombre"),
                        rs.getString("org_telefono"),
                        rs.getString("org_email")
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
        String sql = "SELECT r.*, o.nombre as org_nombre, o.telefono as org_telefono, o.correo_electronico as org_email " +
                "FROM representantes r " +
                "JOIN organizaciones_vinculadas o ON r.id_organizacion = o.id_organizacion " +
                "WHERE r.id_representante = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LinkedOrganization org = new LinkedOrganization(
                            rs.getInt("id_organizacion"),
                            rs.getString("org_nombre"),
                            rs.getString("org_telefono"),
                            rs.getString("org_email")
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
        String sql = "SELECT r.*, o.nombre as org_nombre, o.telefono as org_telefono, o.correo_electronico as org_email " +
                "FROM representantes r " +
                "JOIN organizaciones_vinculadas o ON r.id_organizacion = o.id_organizacion " +
                "WHERE r.correo_electronico = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LinkedOrganization org = new LinkedOrganization(
                            rs.getInt("id_organizacion"),
                            rs.getString("org_nombre"),
                            rs.getString("org_telefono"),
                            rs.getString("org_email")
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
        String sql = "SELECT r.*, o.nombre as org_nombre, o.telefono as org_telefono, o.correo_electronico as org_email " +
                "FROM representantes r " +
                "JOIN organizaciones_vinculadas o ON r.id_organizacion = o.id_organizacion " +
                "WHERE r.id_organizacion = ?";
        List<Representative> representatives = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, organizationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LinkedOrganization org = new LinkedOrganization(
                            rs.getInt("id_organizacion"),
                            rs.getString("org_nombre"),
                            rs.getString("org_telefono"),
                            rs.getString("org_email")
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