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
        String sql = "INSERT INTO grupos (nrc, nombre_grupo) VALUES (?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, group.getNrc());
            ps.setString(2, group.getGroupName());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteGroup(Group group) throws SQLException {
        // Primero verificar si hay estudiantes en el grupo
        List<Student> studentsInGroup = studentDAO.getStudentsByGroup(group.getNrc());
        if (!studentsInGroup.isEmpty()) {
            throw new SQLException("No se puede eliminar el grupo porque tiene estudiantes asignados");
        }

        String sql = "DELETE FROM grupos WHERE nrc = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, group.getNrc());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateGroup(Group group) throws SQLException {
        String sql = "UPDATE grupos SET nombre_grupo = ? WHERE nrc = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, group.getGroupName());
            ps.setInt(2, group.getNrc());

            return ps.executeUpdate() > 0;
        }
    }

    public List<Group> getAllGroups() throws SQLException {
        String sql = "SELECT nrc, nombre_grupo FROM grupos";
        List<Group> groups = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int nrc = rs.getInt("nrc");
                Group group = new Group(
                        nrc,
                        rs.getString("nombre_grupo"),
                        studentDAO.getStudentsByGroup(nrc) // Cargar estudiantes del grupo
                );
                groups.add(group);
            }
        }
        return groups;
    }

    public Group getGroupByNrc(int nrc) throws SQLException {
        String sql = "SELECT nrc, nombre_grupo FROM grupos WHERE nrc = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, nrc);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Group(
                            nrc,
                            rs.getString("nombre_grupo"),
                            studentDAO.getStudentsByGroup(nrc) // Cargar estudiantes del grupo
                    );
                }
            }
        }
        return null;
    }

    public Group getGroupByName(String groupName) throws SQLException {
        String sql = "SELECT nrc, nombre_grupo FROM grupos WHERE nombre_grupo = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, groupName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int nrc = rs.getInt("nrc");
                    return new Group(
                            nrc,
                            rs.getString("nombre_grupo"),
                            studentDAO.getStudentsByGroup(nrc)
                    );
                }
            }
        }
        return null;
    }

    public boolean groupExists(int nrc) throws SQLException {
        String sql = "SELECT 1 FROM grupos WHERE nrc = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, nrc);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int countGroups() throws SQLException {
        String sql = "SELECT COUNT(*) FROM grupos";

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