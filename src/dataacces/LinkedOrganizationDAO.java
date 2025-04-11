package dataacces;

import logic.LinkedOrganization;
import logic.interfaces.ILinkedOrganizationDAO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LinkedOrganizationDAO implements ILinkedOrganizationDAO {

    public boolean addLinkedOrganization(LinkedOrganization linkedOrganization) throws SQLException {
        String sql = "INSERT INTO organizaciones_vinculadas (nombre, telefono, correo_electronico) VALUES (?, ?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, linkedOrganization.getNameLinkedOrganization());
            ps.setString(2, linkedOrganization.getCellPhoneLinkedOrganization());
            ps.setString(3, linkedOrganization.getEmailLinkedOrganization());

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        linkedOrganization.setIdLinkedOrganization(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public boolean deleteLinkedOrganization(LinkedOrganization linkedOrganization) throws SQLException {
        String sql = "DELETE FROM organizaciones_vinculadas WHERE id_organizacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, linkedOrganization.getIdLinkedOrganization());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateLinkedOrganization(LinkedOrganization linkedOrganization) throws SQLException {
        String sql = "UPDATE organizaciones_vinculadas SET nombre = ?, telefono = ?, correo_electronico = ? WHERE id_organizacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, linkedOrganization.getNameLinkedOrganization());
            ps.setString(2, linkedOrganization.getCellPhoneLinkedOrganization());
            ps.setString(3, linkedOrganization.getEmailLinkedOrganization());
            ps.setInt(4, linkedOrganization.getIdLinkedOrganization());

            return ps.executeUpdate() > 0;
        }
    }

    public List<LinkedOrganization> getAllLinkedOrganizations() throws SQLException {
        String sql = "SELECT id_organizacion, nombre, telefono, correo_electronico FROM organizaciones_vinculadas";
        List<LinkedOrganization> organizations = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                LinkedOrganization org = new LinkedOrganization(
                        rs.getInt("id_organizacion"),
                        rs.getString("nombre"),
                        rs.getString("telefono"),
                        rs.getString("correo_electronico")
                );
                organizations.add(org);
            }
        }
        return organizations;
    }

    public LinkedOrganization getLinkedOrganizationByTitle(String title) throws SQLException {
        String sql = "SELECT id_organizacion, nombre, telefono, correo_electronico FROM organizaciones_vinculadas WHERE nombre = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, title);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new LinkedOrganization(
                            rs.getInt("id_organizacion"),
                            rs.getString("nombre"),
                            rs.getString("telefono"),
                            rs.getString("correo_electronico")
                    );
                }
            }
        }
        return null;
    }

    public LinkedOrganization getLinkedOrganizationByID(int id) throws SQLException {
        String sql = "SELECT id_organizacion, nombre, telefono, correo_electronico FROM organizaciones_vinculadas WHERE id_organizacion = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new LinkedOrganization(
                            rs.getInt("id_organizacion"),
                            rs.getString("nombre"),
                            rs.getString("telefono"),
                            rs.getString("correo_electronico")
                    );
                }
            }
        }
        return null;
    }

    public boolean linkedOrganizationExists(String title) throws SQLException {
        String sql = "SELECT 1 FROM organizaciones_vinculadas WHERE nombre = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, title);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int countLinkedOrganizations() throws SQLException {
        String sql = "SELECT COUNT(*) FROM organizaciones_vinculadas";

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