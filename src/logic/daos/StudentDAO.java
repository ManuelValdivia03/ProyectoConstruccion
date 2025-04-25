package logic.daos;

import dataaccess.ConnectionDataBase;
import logic.exceptions.RepeatedEnrollmentException;
import logic.logicclasses.Student;
import logic.interfaces.IStudentDAO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO implements IStudentDAO {
    private final UserDAO userDAO;

    public StudentDAO() {
        this.userDAO = new UserDAO();
    }

    public boolean addStudent(Student student) throws SQLException {
        String sql = "INSERT INTO estudiante (id_usuario, matricula) VALUES (?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, student.getIdUser());
            ps.setString(2, student.getEnrollment());

            return ps.executeUpdate() > 0;
        }
    }

    public List<Student> getAllStudents() throws SQLException {
        String sql = "SELECT u.*, e.matricula FROM usuario u JOIN estudiante e ON u.id_usuario = e.id_usuario";
        List<Student> students = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Student student = new Student();
                student.setIdUser(rs.getInt("id_usuario"));
                student.setFullName(rs.getString("nombre_completo"));
                student.setCellphone(rs.getString("telefono"));
                student.setStatus(rs.getString("estado").charAt(0));
                student.setEnrollment(rs.getString("matricula"));
                students.add(student);
            }
        }
        return students;
    }

    public Student getStudentById(int id) throws SQLException {
        String sql = "SELECT u.*, e.matricula FROM usuario u JOIN estudiante e ON u.id_usuario = e.id_usuario WHERE u.id_usuario = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Student student = new Student();
                    student.setIdUser(rs.getInt("id_usuario"));
                    student.setFullName(rs.getString("nombre_completo"));
                    student.setCellphone(rs.getString("telefono"));
                    student.setStatus(rs.getString("estado").charAt(0));
                    student.setEnrollment(rs.getString("matricula"));
                    return student;
                }
            }
        }
        return null;
    }

    public boolean updateStudent(Student student) throws SQLException {
        if (!userDAO.updateUser(student)) {
            return false;
        }

        String sql = "UPDATE estudiante SET matricula = ? WHERE id_usuario = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, student.getEnrollment());
            ps.setInt(2, student.getIdUser());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteStudent(int id) throws SQLException {
        String sql = "DELETE FROM estudiante WHERE id_usuario = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }

        return userDAO.deleteUser(id);
    }

    public List<Student> getStudentsByGroup(int nrc) throws SQLException {
        String sql = "SELECT u.*, e.matricula FROM usuario u JOIN estudiante e ON u.id_usuario = e.id_usuario " +
                "JOIN grupo_estudiante ge ON e.id_usuario = ge.id_usuario WHERE ge.nrc = ?";
        List<Student> students = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, nrc);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Student student = new Student();
                    student.setIdUser(rs.getInt("id_usuario"));
                    student.setFullName(rs.getString("nombre_completo"));
                    student.setCellphone(rs.getString("telefono"));
                    student.setStatus(rs.getString("estado").charAt(0));
                    student.setEnrollment(rs.getString("matricula"));
                    students.add(student);
                }
            }
        }
        return students;
    }

    public boolean assignStudentToGroup(int studentId, int nrcGrupo) throws SQLException {
        String sql = "INSERT INTO grupo_estudiante (nrc, id_usuario) VALUES (?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, nrcGrupo);
            ps.setInt(2, studentId);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean studentExistsById(int id) throws SQLException {
        String sql = "SELECT 1 FROM estudiante WHERE id_usuario = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean enrollmentExists(String enrollment) throws RepeatedEnrollmentException, SQLException {
        String sql = "SELECT 1 FROM estudiante WHERE matricula = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, enrollment);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int countStudents() throws SQLException {
        String sql = "SELECT COUNT(*) FROM estudiante";

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