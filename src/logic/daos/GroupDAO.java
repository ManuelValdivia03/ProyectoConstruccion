package logic.daos;

import dataaccess.ConnectionDataBase;
import logic.enums.AcademicType;
import logic.logicclasses.Group;
import logic.logicclasses.Student;
import logic.logicclasses.Academic;
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
    private static final Group EMPTY_GROUP = new Group(-1, "", Collections.emptyList(), null);
    private final StudentDAO studentDAO;

    public GroupDAO() {
        this.studentDAO = new StudentDAO();
    }

    public boolean assignEeAcademic(int nrc, int academicId) throws SQLException {
        String checkSql = "SELECT Tipo FROM academico WHERE id_usuario = ? AND Tipo = 'EE'";
        String existsSql = "SELECT 1 FROM grupo_academico WHERE nrc = ?";
        String insertSql = "INSERT INTO grupo_academico (nrc, id_usuario) VALUES (?, ?)";
        String updateSql = "UPDATE grupo_academico SET id_usuario = ? WHERE nrc = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement checkStmt = connection.prepareStatement(checkSql);
             PreparedStatement existsStmt = connection.prepareStatement(existsSql);
             PreparedStatement insertStmt = connection.prepareStatement(insertSql);
             PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {

            checkStmt.setInt(1, academicId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
            }

            existsStmt.setInt(1, nrc);
            boolean exists;
            try (ResultSet rs = existsStmt.executeQuery()) {
                exists = rs.next();
            }

            if (exists) {
                updateStmt.setInt(1, academicId);
                updateStmt.setInt(2, nrc);
                return updateStmt.executeUpdate() > 0;
            } else {
                insertStmt.setInt(1, nrc);
                insertStmt.setInt(2, academicId);
                return insertStmt.executeUpdate() > 0;
            }
        }
    }

    public boolean removeEeAcademic(int nrc, int academicId) throws SQLException {
        String sql = "DELETE FROM grupo_academico WHERE nrc = ? AND id_usuario = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, nrc);
            stmt.setInt(2, academicId);
            return stmt.executeUpdate() > 0;
        }
    }

    public Academic getEeAcademicByGroup(int nrc) throws SQLException {
        String sql = "SELECT a.id_usuario, a.numero_personal, a.tipo, u.nombre_completo, u.telefono, u.estado " +
                "FROM academico a " +
                "JOIN usuario u ON a.id_usuario = u.id_usuario " +
                "JOIN grupo_academico ga ON a.id_usuario = ga.id_usuario " +
                "WHERE ga.nrc = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, nrc);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    int idUsuario = rs.getInt("id_usuario");
                    String numeroPersonal = rs.getString("numero_personal").trim();
                    String tipo = rs.getString("tipo").trim();
                    String nombreCompleto = rs.getString("nombre_completo").trim();
                    String telefono = rs.getString("telefono");
                    if (telefono != null) {
                        telefono = telefono.trim();
                    }
                    String estado = rs.getString("estado").trim();

                    Academic academic = new Academic(
                        idUsuario,
                        nombreCompleto,
                        telefono,
                        estado.charAt(0),
                        numeroPersonal,
                        AcademicType.EE
                    );

                    System.out.println("Academic creado: " + academic.getFullName());
                    return academic;
                }
            } catch (Exception e) {
                System.out.println("Error al crear Academic: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }

    public boolean isAcademicAssignedToGroup(int academicId) throws SQLException {
        String sql = "SELECT 1 FROM grupo_academico WHERE id_usuario = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, academicId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
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
        String sql = "SELECT g.nrc, g.nombre, ga.id_usuario " +
                    "FROM grupo g " +
                    "LEFT JOIN grupo_academico ga ON g.nrc = ga.nrc " +
                    "ORDER BY g.nombre";
        
        List<Group> groups = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                int nrc = resultSet.getInt("nrc");
                String nombre = resultSet.getString("nombre");
                Academic academic = null;
                
                Object idUsuario = resultSet.getObject("id_usuario");
                if (idUsuario != null) {
                    academic = getEeAcademicByGroup(nrc);
                    if (academic == null) {
                        System.out.println("No se pudo crear el académico para el NRC: " + nrc);
                    }
                }

                List<Student> students = studentDAO.getStudentsByGroup(nrc);
                Group group = new Group(nrc, nombre, students, academic);
                groups.add(group);
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
                            studentDAO.getStudentsByGroup(nrc),
                            null
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
                            studentDAO.getStudentsByGroup(nrc),
                            null
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

    public Group getGroupByAcademicId(int academicId) throws SQLException {
        String sql = "SELECT g.nrc, g.nombre FROM grupo g " +
                "JOIN grupo_academico ga ON g.nrc = ga.nrc " +
                "WHERE ga.id_usuario = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, academicId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int nrc = resultSet.getInt("nrc");
                    return new Group(
                            nrc,
                            resultSet.getString("nombre"),
                            studentDAO.getStudentsByGroup(nrc),
                            getEeAcademicByGroup(nrc)
                    );
                }
            }
        }
        return EMPTY_GROUP;
    }
}
