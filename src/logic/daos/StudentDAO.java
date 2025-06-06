package logic.daos;

import dataaccess.ConnectionDataBase;
import logic.exceptions.RepeatedEnrollmentException;
import logic.logicclasses.Student;
import logic.interfaces.IStudentDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO implements IStudentDAO {
    private static final Logger logger = LogManager.getLogger(StudentDAO.class);
    private final UserDAO userDAO;

    public StudentDAO() {
        this.userDAO = new UserDAO();
    }

    public boolean addStudent(Student student) throws SQLException {
        if (student == null) {
            logger.warn("Intento de agregar estudiante nulo");
            return false;
        }

        logger.debug("Agregando nuevo estudiante con matrícula: {}", student.getEnrollment());

        String sql = "INSERT INTO estudiante (id_usuario, matricula, calificacion) VALUES (?, ?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, student.getIdUser());
            ps.setString(2, student.getEnrollment());
            ps.setInt(3, student.getGrade());

            boolean result = ps.executeUpdate() > 0;
            if (result) {
                logger.info("Estudiante agregado exitosamente - ID: {}, Matrícula: {}, Calificación: {}",
                        student.getIdUser(), student.getEnrollment(), student.getGrade());
            } else {
                logger.warn("No se pudo agregar el estudiante con matrícula: {}", student.getEnrollment());
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al agregar estudiante con matrícula: {}", student.getEnrollment(), e);
            throw e;
        }
    }

    public Student getStudentByEnrollment(String enrollment) throws SQLException {
        Student studentVoid = new Student(-1, null, null, 'I', null, 0);

        if (enrollment == null || enrollment.isEmpty()) {
            logger.warn("Intento de buscar estudiante con matrícula nula o vacía");
            return studentVoid;
        }

        logger.debug("Buscando estudiante por matrícula: {}", enrollment);

        String sql = "SELECT u.id_usuario, u.nombre_completo, u.telefono, u.estado, e.matricula, e.calificacion " +
                "FROM usuario u JOIN estudiante e ON u.id_usuario = e.id_usuario " +
                "WHERE e.matricula = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, enrollment);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    logger.debug("Estudiante encontrado con matrícula: {}", enrollment);
                    Student student = new Student();
                    student.setIdUser(rs.getInt("id_usuario"));
                    student.setFullName(rs.getString("nombre_completo"));
                    student.setCellphone(rs.getString("telefono"));

                    // Manejo seguro del estado
                    String estado = rs.getString("estado");
                    student.setStatus(estado != null && !estado.isEmpty() ? estado.charAt(0) : 'I');

                    student.setEnrollment(rs.getString("matricula"));
                    student.setGrade(rs.getInt("calificacion"));
                    return student;
                }

                logger.info("No se encontró estudiante con matrícula: {}", enrollment);
                return studentVoid;
            }
        } catch (SQLException e) {
            logger.error("Error al buscar estudiante por matrícula: {}", enrollment, e);
            throw e;
        }
    }

    public List<Student> getAllStudents() throws SQLException {
        logger.info("Obteniendo todos los estudiantes");

        String sql = "SELECT u.*, e.matricula, e.calificacion FROM usuario u JOIN estudiante e ON u.id_usuario = e.id_usuario";
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
                student.setGrade(rs.getInt("calificacion"));
                students.add(student);
            }
            logger.debug("Se encontraron {} estudiantes", students.size());
        } catch (SQLException e) {
            logger.error("Error al obtener todos los estudiantes", e);
            throw e;
        }
        return students;
    }

    public List<Student> getSudentsByStatus(char status) throws SQLException {
        logger.info("Obteniendo estudiantes con estado: {}", status);

        String sql = "SELECT u.*, e.matricula, e.calificacion FROM usuario u JOIN estudiante e ON u.id_usuario = e.id_usuario WHERE u.estado = ?";
        List<Student> students = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, String.valueOf(status));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Student student = new Student();
                    student.setIdUser(rs.getInt("id_usuario"));
                    student.setFullName(rs.getString("nombre_completo"));
                    student.setCellphone(rs.getString("telefono"));
                    student.setStatus(rs.getString("estado").charAt(0));
                    student.setEnrollment(rs.getString("matricula"));
                    student.setGrade(rs.getInt("calificacion"));
                    students.add(student);
                }
                logger.debug("Se encontraron {} estudiantes con estado: {}", students.size(), status);
            }
        } catch (SQLException e) {
            logger.error("Error al obtener estudiantes por estado: {}", status, e);
            throw e;
        }
        return students;
    }

    public Student getStudentById(int id) throws SQLException {
        if (id <= 0) {
            logger.warn("Intento de buscar estudiante con ID inválido: {}", id);
            return null;
        }

        logger.debug("Buscando estudiante por ID: {}", id);

        String sql = "SELECT u.*, e.matricula, e.calificacion FROM usuario u JOIN estudiante e ON u.id_usuario = e.id_usuario WHERE u.id_usuario = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    logger.debug("Estudiante encontrado con ID: {}", id);
                    Student student = new Student();
                    student.setIdUser(rs.getInt("id_usuario"));
                    student.setFullName(rs.getString("nombre_completo"));
                    student.setCellphone(rs.getString("telefono"));
                    student.setStatus(rs.getString("estado").charAt(0));
                    student.setEnrollment(rs.getString("matricula"));
                    student.setGrade(rs.getInt("calificacion"));
                    return student;
                }
            }
        } catch (SQLException e) {
            logger.error("Error al buscar estudiante por ID: {}", id, e);
            throw e;
        }
        logger.info("No se encontró estudiante con ID: {}", id);
        return null;
    }

    public boolean updateStudent(Student student) throws SQLException {
        if (student == null) {
            logger.warn("Intento de actualizar estudiante nulo");
            return false;
        }

        logger.debug("Actualizando estudiante ID: {}", student.getIdUser());

        if (!userDAO.updateUser(student)) {
            logger.warn("No se pudo actualizar el usuario asociado al estudiante ID: {}", student.getIdUser());
            return false;
        }

        String sql = "UPDATE estudiante SET matricula = ?, calificacion = ? WHERE id_usuario = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, student.getEnrollment());
            ps.setInt(2, student.getGrade());
            ps.setInt(3, student.getIdUser());

            boolean result = ps.executeUpdate() > 0;
            if (result) {
                logger.info("Estudiante actualizado exitosamente - ID: {}, Calificación: {}",
                        student.getIdUser(), student.getGrade());
            } else {
                logger.warn("No se encontró estudiante con ID: {} para actualizar", student.getIdUser());
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al actualizar estudiante ID: {}", student.getIdUser(), e);
            throw e;
        }
    }

    public boolean updateStudentGrade(int studentId, int grade) throws SQLException {
        if (studentId <= 0) {
            logger.warn("Intento de actualizar calificación con ID inválido: {}", studentId);
            return false;
        }

        logger.debug("Actualizando calificación del estudiante ID: {}", studentId);

        String sql = "UPDATE estudiante SET calificacion = ? WHERE id_usuario = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, grade);
            ps.setInt(2, studentId);

            boolean result = ps.executeUpdate() > 0;
            if (result) {
                logger.info("Calificación actualizada exitosamente - ID: {}, Nueva calificación: {}",
                        studentId, grade);
            } else {
                logger.warn("No se encontró estudiante con ID: {} para actualizar calificación", studentId);
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al actualizar calificación del estudiante ID: {}", studentId, e);
            throw e;
        }
    }

    public boolean deleteStudent(int id) throws SQLException {
        if (id <= 0) {
            logger.warn("Intento de eliminar estudiante con ID inválido: {}", id);
            return false;
        }

        logger.debug("Eliminando estudiante ID: {}", id);

        String sql = "DELETE FROM estudiante WHERE id_usuario = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error al eliminar estudiante de la tabla estudiante ID: {}", id, e);
            throw e;
        }

        boolean userDeleted = userDAO.deleteUser(id);
        if (userDeleted) {
            logger.info("Estudiante eliminado exitosamente - ID: {}", id);
        } else {
            logger.warn("No se pudo eliminar el usuario asociado al estudiante ID: {}", id);
        }
        return userDeleted;
    }

    public List<Student> getStudentsByGroup(int nrc) throws SQLException {
        if (nrc <= 0) {
            logger.warn("Intento de buscar estudiantes con NRC inválido: {}", nrc);
            return new ArrayList<>();
        }

        logger.debug("Buscando estudiantes por grupo NRC: {}", nrc);

        String sql = "SELECT u.*, e.matricula, e.calificacion FROM usuario u JOIN estudiante e ON u.id_usuario = e.id_usuario " +
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
                    student.setGrade(rs.getInt("calificacion"));
                    students.add(student);
                }
            }
            logger.debug("Se encontraron {} estudiantes en el grupo NRC: {}", students.size(), nrc);
        } catch (SQLException e) {
            logger.error("Error al obtener estudiantes por grupo NRC: {}", nrc, e);
            throw e;
        }
        return students;
    }

    public boolean assignStudentToGroup(int studentId, int nrcGrupo) throws SQLException {
        if (studentId <= 0 || nrcGrupo <= 0) {
            logger.warn("Intento de asignar estudiante a grupo con datos inválidos - Estudiante ID: {}, NRC: {}",
                    studentId, nrcGrupo);
            return false;
        }

        logger.debug("Asignando estudiante ID: {} al grupo NRC: {}", studentId, nrcGrupo);

        String sql = "INSERT INTO grupo_estudiante (nrc, id_usuario) VALUES (?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, nrcGrupo);
            ps.setInt(2, studentId);

            boolean result = ps.executeUpdate() > 0;
            if (result) {
                logger.info("Estudiante ID: {} asignado exitosamente al grupo NRC: {}", studentId, nrcGrupo);
            } else {
                logger.warn("No se pudo asignar estudiante ID: {} al grupo NRC: {}", studentId, nrcGrupo);
            }
            return result;
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            logger.error("Violación de integridad al asignar estudiante ID: {} al grupo NRC: {}. Probablemente el estudiante o grupo no existe.", studentId, nrcGrupo, e);
            return false;
        } catch (SQLException e) {
            logger.error("Error al asignar estudiante ID: {} al grupo NRC: {}", studentId, nrcGrupo, e);
            throw e;
        }
    }

    public boolean studentExistsById(int id) throws SQLException {
        if (id <= 0) {
            logger.warn("Intento de verificar existencia de estudiante con ID inválido: {}", id);
            return false;
        }

        logger.debug("Verificando existencia de estudiante con ID: {}", id);

        String sql = "SELECT 1 FROM estudiante WHERE id_usuario = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                boolean exists = rs.next();
                logger.debug("¿Estudiante ID {} existe?: {}", id, exists);
                return exists;
            }
        } catch (SQLException e) {
            logger.error("Error al verificar existencia de estudiante ID: {}", id, e);
            throw e;
        }
    }

    public boolean enrollmentExists(String enrollment) throws RepeatedEnrollmentException, SQLException {
        if (enrollment == null || enrollment.isEmpty()) {
            logger.warn("Intento de verificar existencia con matrícula nula o vacía");
            return false;
        }

        logger.debug("Verificando existencia de matrícula: {}", enrollment);

        String sql = "SELECT 1 FROM estudiante WHERE matricula = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, enrollment);
            try (ResultSet rs = ps.executeQuery()) {
                boolean exists = rs.next();
                logger.debug("¿Matrícula '{}' existe?: {}", enrollment, exists);
                if (exists) {
                    throw new RepeatedEnrollmentException("La matrícula ya está registrada");
                }
                return exists;
            }
        } catch (SQLException e) {
            logger.error("Error al verificar existencia de matrícula: {}", enrollment, e);
            throw e;
        }
    }

    public int countStudents() throws SQLException {
        logger.debug("Contando estudiantes");

        String sql = "SELECT COUNT(*) FROM estudiante";

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int count = rs.next() ? rs.getInt(1) : 0;
            logger.info("Total de estudiantes: {}", count);
            return count;
        } catch (SQLException e) {
            logger.error("Error al contar estudiantes", e);
            throw e;
        }
    }

    public boolean existsForUser(int userId) throws SQLException {
        String sql = "SELECT 1 FROM estudiante WHERE id_usuario = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
        PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeQuery().next();
        }
    }

    public Student getFullStudent(int userId) throws SQLException {
        String sql = "SELECT u.id_usuario, u.nombre_completo, u.telefono, u.estado, " +
                "e.matricula, e.calificacion FROM usuario u " +
                "JOIN estudiante e ON u.id_usuario = e.id_usuario " +
                "WHERE u.id_usuario = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
        PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Student(
                        rs.getInt("id_usuario"),
                        rs.getString("nombre_completo"),
                        rs.getString("telefono"),
                        rs.getString("estado").charAt(0),
                        rs.getString("matricula"),
                        rs.getInt("calificacion")
                );
            }
        }
        return null;
    }
}
