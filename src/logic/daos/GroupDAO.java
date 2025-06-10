package logic.daos;

import dataaccess.ConnectionDataBase;
import logic.logicclasses.Group;
import logic.logicclasses.Student;
import logic.interfaces.IGroupDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GroupDAO implements IGroupDAO {
    private static final Group EMPTY_GROUP = new Group(-1, "", Collections.emptyList());
    private final StudentDAO studentDAO;

    public GroupDAO() {
        this.studentDAO = new StudentDAO();
    }

    public boolean addGroup(Group group) throws SQLException {
        if (group == null || group.getGroupName() == null || group.getGroupName().isEmpty()) {
            throw new IllegalArgumentException("El grupo y el nombre del grupo no deben ser nulos o vacíos");
        }

        String sql = "INSERT INTO grupo (nrc, nombre) VALUES (?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, group.getNrc());
            preparedStatement.setString(2, group.getGroupName());

            return preparedStatement.executeUpdate() > 0;
        }
    }

    public boolean deleteGroup(Group group) throws SQLException {
        if (group == null) {
            throw new IllegalArgumentException("El grupo no debe ser nulo");
        }

        List<Student> studentsInGroup = studentDAO.getStudentsByGroup(group.getNrc());
        if (!studentsInGroup.isEmpty()) {
            throw new SQLException("El grupo tiene estudiantes asignados");
        }

        String checkSql = "SELECT COUNT(*) FROM estudiante WHERE nrc_grupo = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(checkSql)) {

            preparedStatement.setInt(1, group.getNrc());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next() && resultSet.getInt(1) > 0) {
                    throw new SQLException("El grupo tiene estudiantes asignados");
                }
            }
        }

        String deleteSql = "DELETE FROM grupo WHERE nrc = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(deleteSql)) {

            preparedStatement.setInt(1, group.getNrc());
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public boolean updateGroup(Group group) throws SQLException {
        if (group == null || group.getGroupName() == null || group.getGroupName().isEmpty()) {
            throw new IllegalArgumentException("El grupo y el nombre del grupo no deben ser nulos o vacíos");
        }

        String sql = "UPDATE grupo SET nombre = ? WHERE nrc = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, group.getGroupName());
            preparedStatement.setInt(2, group.getNrc());

            return preparedStatement.executeUpdate() > 0;
        }
    }

    public List<Group> getAllGroups() throws SQLException {
        String sql = "SELECT nrc, nombre FROM grupo";
        List<Group> groups = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                int nrc = resultSet.getInt("nrc");
                groups.add(new Group(
                        nrc,
                        resultSet.getString("nombre"),
                        studentDAO.getStudentsByGroup(nrc)
                ));
            }
        }
        return groups;
    }

    public Group getGroupByNrc(int nrc) throws SQLException {
        if (nrc <= 0) {
            return EMPTY_GROUP;
        }

        String sql = "SELECT nrc, nombre FROM grupo WHERE nrc = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, nrc);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new Group(
                            nrc,
                            resultSet.getString("nombre"),
                            studentDAO.getStudentsByGroup(nrc)
                    );
                }
            }
        }
        return EMPTY_GROUP;
    }

    public Group getGroupByName(String groupName) throws SQLException {
        if (groupName == null || groupName.isEmpty()) {
            throw new IllegalArgumentException("El nombre del grupo no debe ser nulo o vacío");
        }

        String sql = "SELECT nrc, nombre FROM grupo WHERE nombre = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, groupName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int nrc = resultSet.getInt("nrc");
                    return new Group(
                            nrc,
                            resultSet.getString("nombre"),
                            studentDAO.getStudentsByGroup(nrc)
                    );
                }
            }
        }
        return EMPTY_GROUP;
    }

    public boolean groupExists(int nrc) throws SQLException {
        if (nrc <= 0) {
            return false;
        }

        String sql = "SELECT 1 FROM grupo WHERE nrc = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, nrc);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int countGroups() throws SQLException {
        String sql = "SELECT COUNT(*) FROM grupo";

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }
}
