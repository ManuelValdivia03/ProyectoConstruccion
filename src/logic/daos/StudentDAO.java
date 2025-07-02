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
import java.util.Collections;
import java.util.List;

public class StudentDAO implements IStudentDAO {
    private static final Logger logger = LogManager.getLogger(StudentDAO.class);
    private static final Student EMPTY_STUDENT = new Student(-1, "", "", "",'I', "", 0);
    private final UserDAO userDAO;

    public StudentDAO() {
        this.userDAO = new UserDAO();
    }

    public boolean addStudent(Student student, int academicId) throws SQLException {
        if (student == null) {
            logger.warn("Intento de agregar estudiante nulo");
            return false;
        }

        logger.debug("Agregando nuevo estudiante con matrícula: {}", student.getEnrollment());

        String sql = "INSERT INTO estudiante (id_usuario, matricula, calificacion) VALUES (?, ?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, student.getIdUser());
            preparedStatement.setString(2, student.getEnrollment());
            preparedStatement.setInt(3, student.getGrade());

            boolean result = preparedStatement.executeUpdate() > 0;
            if (!result) {
                logger.warn("No se pudo agregar el estudiante con matrícula: {}", student.getEnrollment());
                return false;
            }
            if (academicId > 0) {
                String groupSql = "SELECT nrc FROM grupo_academico WHERE id_usuario = ?";
                try (PreparedStatement groupStmt = connection.prepareStatement(groupSql)) {
                    groupStmt.setInt(1, academicId);
                    ResultSet resultSet = groupStmt.executeQuery();

                    if (resultSet.next()) {
                        int nrc = resultSet.getInt("nrc");
                        return assignStudentToGroup(student.getIdUser(), nrc);
                    } else {
                        logger.warn("El académico con ID {} no tiene un grupo asignado", academicId);
                        return true;
                    }
                }
            }

            logger.info("Estudiante agregado exitosamente - ID: {}, Matrícula: {}, Calificación: {}",
                    student.getIdUser(), student.getEnrollment(), student.getGrade());
            return true;
        } catch (SQLException e) {
            logger.error("Error al agregar estudiante con matrícula: {}", student.getEnrollment(), e);
            throw e;
        }
    }

    public Student getStudentByEnrollment(String enrollment) throws SQLException {
        if (enrollment == null || enrollment.isEmpty()) {
            logger.warn("Intento de buscar estudiante con matrícula nula o vacía");
            return EMPTY_STUDENT;
        }

        logger.debug("Buscando estudiante por matrícula: {}", enrollment);

        String sql = "SELECT u.id_usuario, u.nombre_completo, u.telefono, u.estado, e.matricula, e.calificacion " +
                "FROM usuario u JOIN estudiante e ON u.id_usuario = e.id_usuario " +
                "WHERE e.matricula = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, enrollment);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    logger.debug("Estudiante encontrado con matrícula: {}", enrollment);
                    Student student = new Student();
                    student.setIdUser(resultSet.getInt("id_usuario"));
                    student.setFullName(resultSet.getString("nombre_completo"));
                    student.setCellphone(resultSet.getString("telefono"));
                    String estado = resultSet.getString("estado");
                    student.setStatus(estado != null && !estado.isEmpty() ? estado.charAt(0) : 'I');

                    student.setEnrollment(resultSet.getString("matricula"));
                    student.setGrade(resultSet.getInt("calificacion"));
                    return student;
                }

                logger.info("No se encontró estudiante con matrícula: {}", enrollment);
                return EMPTY_STUDENT;
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
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Student student = new Student();
                student.setIdUser(resultSet.getInt("id_usuario"));
                student.setFullName(resultSet.getString("nombre_completo"));
                student.setCellphone(resultSet.getString("telefono"));
                student.setStatus(resultSet.getString("estado").charAt(0));
                student.setEnrollment(resultSet.getString("matricula"));
                student.setGrade(resultSet.getInt("calificacion"));
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
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, String.valueOf(status));

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Student student = new Student();
                    student.setIdUser(resultSet.getInt("id_usuario"));
                    student.setFullName(resultSet.getString("nombre_completo"));
                    student.setCellphone(resultSet.getString("telefono"));
                    student.setStatus(resultSet.getString("estado").charAt(0));
                    student.setEnrollment(resultSet.getString("matricula"));
                    student.setGrade(resultSet.getInt("calificacion"));
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
            return EMPTY_STUDENT;
        }

        logger.debug("Buscando estudiante por ID: {}", id);

        String sql = "SELECT u.*, e.matricula, e.calificacion FROM usuario u JOIN estudiante e ON u.id_usuario = e.id_usuario WHERE u.id_usuario = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    logger.debug("Estudiante encontrado con ID: {}", id);
                    Student student = new Student();
                    student.setIdUser(resultSet.getInt("id_usuario"));
                    student.setFullName(resultSet.getString("nombre_completo"));
                    student.setCellphone(resultSet.getString("telefono"));
                    student.setStatus(resultSet.getString("estado").charAt(0));
                    student.setEnrollment(resultSet.getString("matricula"));
                    student.setGrade(resultSet.getInt("calificacion"));
                    return student;
                }
            }
        } catch (SQLException e) {
            logger.error("Error al buscar estudiante por ID: {}", id, e);
            throw e;
        }
        logger.info("No se encontró estudiante con ID: {}", id);
        return EMPTY_STUDENT;
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
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, student.getEnrollment());
            preparedStatement.setInt(2, student.getGrade());
            preparedStatement.setInt(3, student.getIdUser());

            boolean result = preparedStatement.executeUpdate() > 0;
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
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, grade);
            preparedStatement.setInt(2, studentId);

            boolean result = preparedStatement.executeUpdate() > 0;
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
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
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
            return Collections.emptyList();
        }

        logger.debug("Buscando estudiantes por grupo NRC: {}", nrc);

        String sql = "SELECT u.*, e.matricula, e.calificacion FROM usuario u JOIN estudiante e ON u.id_usuario = e.id_usuario " +
                "JOIN grupo_estudiante ge ON e.id_usuario = ge.id_usuario WHERE ge.nrc = ?";
        List<Student> students = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, nrc);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Student student = new Student();
                    student.setIdUser(resultSet.getInt("id_usuario"));
                    student.setFullName(resultSet.getString("nombre_completo"));
                    student.setCellphone(resultSet.getString("telefono"));
                    student.setStatus(resultSet.getString("estado").charAt(0));
                    student.setEnrollment(resultSet.getString("matricula"));
                    student.setGrade(resultSet.getInt("calificacion"));
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
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, nrcGrupo);
            preparedStatement.setInt(2, studentId);

            boolean result = preparedStatement.executeUpdate() > 0;
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
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                boolean exists = resultSet.next();
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
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, enrollment);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                boolean exists = resultSet.next();
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
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            int count = resultSet.next() ? resultSet.getInt(1) : 0;
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
        PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            return statement.executeQuery().next();
        }
    }

    public Student getFullStudent(int userId) throws SQLException {
        if (userId <= 0) {
            return EMPTY_STUDENT;
        }

        String sql = "SELECT u.id_usuario, u.nombre_completo, u.telefono, u.extension_telefono, u.estado, " +
                "e.matricula, e.calificacion FROM usuario u " +
                "JOIN estudiante e ON u.id_usuario = e.id_usuario " +
                "WHERE u.id_usuario = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return new Student(
                        resultSet.getInt("id_usuario"),
                        resultSet.getString("nombre_completo"),
                        resultSet.getString("telefono"),
                        resultSet.getString("extension_telefono"),
                        resultSet.getString("estado").charAt(0),
                        resultSet.getString("matricula"),
                        resultSet.getInt("calificacion")
                );
            }
        }
        return EMPTY_STUDENT;
    }

    public List<Student> getActiveStudentsByGroup(int nrc) throws SQLException {
        if (nrc <= 0) {
            logger.warn("Intento de buscar estudiantes activos con NRC inválido: {}", nrc);
            return Collections.emptyList();
        }

        logger.debug("Buscando estudiantes activos por grupo NRC: {}", nrc);

        String sql = "SELECT u.*, e.matricula, e.calificacion FROM usuario u " +
                    "JOIN estudiante e ON u.id_usuario = e.id_usuario " +
                    "JOIN grupo_estudiante ge ON e.id_usuario = ge.id_usuario " +
                    "WHERE ge.nrc = ? AND u.estado = 'A'";
        List<Student> students = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, nrc);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Student student = new Student();
                    student.setIdUser(resultSet.getInt("id_usuario"));
                    student.setFullName(resultSet.getString("nombre_completo"));
                    student.setCellphone(resultSet.getString("telefono"));
                    student.setStatus(resultSet.getString("estado").charAt(0));
                    student.setEnrollment(resultSet.getString("matricula"));
                    student.setGrade(resultSet.getInt("calificacion"));
                    students.add(student);
                }
            }
            logger.debug("Se encontraron {} estudiantes activos en el grupo NRC: {}", students.size(), nrc);
        } catch (SQLException e) {
            logger.error("Error al obtener estudiantes activos por grupo NRC: {}", nrc, e);
            throw e;
        }
        return students;
    }
}
