package dataacces;

import logic.Group;
import logic.Student;
import logic.interfaces.IGroupDAO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GroupDAO implements IGroupDAO {
    private final StudentDAO studentDAO;

    public GroupDAO() {
        this.studentDAO = new StudentDAO();
    }

    public boolean addGroup(Group group) throws SQLException {
        String sql = "INSERT INTO grupo (nrc, nombre) VALUES (?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, group.getNrc());
            ps.setString(2, group.getGroupName());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteGroup(Group group) throws SQLException {
        // Verificar estudiantes en grupo_estudiantes
        List<Student> studentsInGroup = studentDAO.getStudentsByGroup(group.getNrc());
        if (!studentsInGroup.isEmpty()) {
            throw new SQLException();
        }
        String checkSql = "SELECT COUNT(*) FROM estudiante WHERE nrc_grupo = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(checkSql)) {

            ps.setInt(1, group.getNrc());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException();
                }
            }
        }
        String deleteSql = "DELETE FROM grupo WHERE nrc = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(deleteSql)) {

            ps.setInt(1, group.getNrc());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateGroup(Group group) throws SQLException {
        String sql = "UPDATE grupo SET nombre = ? WHERE nrc = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, group.getGroupName());
            ps.setInt(2, group.getNrc());

            return ps.executeUpdate() > 0;
        }
    }

    public List<Group> getAllGroups() throws SQLException {
        String sql = "SELECT nrc, nombre FROM grupo";
        List<Group> groups = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int nrc = rs.getInt("nrc");
                Group group = new Group(
                        nrc,
                        rs.getString("nombre"),
                        studentDAO.getStudentsByGroup(nrc) // Cargar estudiantes del grupo
                );
                groups.add(group);
            }
        }
        return groups;
    }

    public Group getGroupByNrc(int nrc) throws SQLException {
        String sql = "SELECT nrc, nombre FROM grupo WHERE nrc = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, nrc);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Group(
                            nrc,
                            rs.getString("nombre"),
                            studentDAO.getStudentsByGroup(nrc) // Cargar estudiantes del grupo
                    );
                }
            }
        }
        return null;
    }

    public Group getGroupByName(String groupName) throws SQLException {
        String sql = "SELECT nrc, nombre FROM grupo WHERE nombre = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, groupName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int nrc = rs.getInt("nrc");
                    return new Group(
                            nrc,
                            rs.getString("nombre"),
                            studentDAO.getStudentsByGroup(nrc)
                    );
                }
            }
        }
        return null;
    }

    public boolean groupExists(int nrc) throws SQLException {
        String sql = "SELECT 1 FROM grupo WHERE nrc = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, nrc);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int countGroups() throws SQLException {
        String sql = "SELECT COUNT(*) FROM grupo";

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