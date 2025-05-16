package daos;

import dataaccess.ConnectionDataBase;
import logic.daos.StudentDAO;
import logic.logicclasses.Student;
import logic.logicclasses.User;
import logic.exceptions.RepeatedEnrollmentException;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StudentDAOTest {
    private static StudentDAO studentDAO;
    private static Connection testConnection;
    private List<Student> testStudents;
    private List<Integer> testStudentIds = new ArrayList<>();
    private List<Integer> testGroupNrcs = new ArrayList<>();

    @BeforeAll
    static void setUpAll() throws SQLException {
        studentDAO = new StudentDAO();
        testConnection = ConnectionDataBase.getConnection();
        try (var conn = ConnectionDataBase.getConnection();
             var statement = conn.createStatement()) {
            statement.execute("SET FOREIGN_KEY_CHECKS = 0");
            statement.execute("TRUNCATE TABLE grupo_estudiante");
            statement.execute("TRUNCATE TABLE estudiante");
            statement.execute("TRUNCATE TABLE academico");
            statement.execute("TRUNCATE TABLE coordinador");
            statement.execute("TRUNCATE TABLE representante");
            statement.execute("TRUNCATE TABLE actividad");
            statement.execute("TRUNCATE TABLE autoevaluacion");
            statement.execute("TRUNCATE TABLE cronograma_actividad");
            statement.execute("TRUNCATE TABLE cronograma_actividades");
            statement.execute("TRUNCATE TABLE evaluacion");
            statement.execute("TRUNCATE TABLE presentacion");
            statement.execute("TRUNCATE TABLE proyecto");
            statement.execute("TRUNCATE TABLE reporte");
            statement.execute("TRUNCATE TABLE grupo");
            statement.execute("TRUNCATE TABLE organizacion_vinculada");
            statement.execute("TRUNCATE TABLE cuenta");
            statement.execute("TRUNCATE TABLE usuario");
            statement.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        testStudents = List.of(
                createTestStudent("STEST001", "Test Student 1", "5551111111", 8),
                createTestStudent("STEST002", "Test Student 2", "5552222222", 7),
                createTestStudent("STEST003", "Test Student 3", "5553333333", 5)
        );
    }

    private Student createTestStudent(String enrollment, String fullName, String phone, int grade) throws SQLException {
        // Create user first
        int userId = createTestUser(fullName, phone);

        // Then create student record
        String sql = "INSERT INTO estudiante (id_usuario, matricula, calificacion) VALUES (?, ?, ?)";
        try (PreparedStatement ps = testConnection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, enrollment);
            ps.setInt(3, grade);
            ps.executeUpdate();
        }

        Student student = new Student();
        student.setIdUser(userId);
        student.setEnrollment(enrollment);
        student.setFullName(fullName);
        student.setCellphone(phone);
        student.setGrade(grade);
        testStudentIds.add(userId);
        return student;
    }

    private int createTestUser(String fullName, String phone) throws SQLException {
        String sql = "INSERT INTO usuario (nombre_completo, telefono) VALUES (?, ?)";
        try (PreparedStatement ps = testConnection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, fullName);
            ps.setString(2, phone);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to create test user");
    }

    private int createTestGroup(int nrc) throws SQLException {
        String sql = "INSERT INTO grupo (nrc, nombre) VALUES (?, 'Test Group')";
        try (PreparedStatement ps = testConnection.prepareStatement(sql)) {
            ps.setInt(1, nrc);
            ps.executeUpdate();
        }
        testGroupNrcs.add(nrc);
        return nrc;
    }

    @AfterEach
    void tearDown() throws SQLException {
        cleanTestData();
    }

    private void cleanTestData() throws SQLException {
        if (!testGroupNrcs.isEmpty() || !testStudentIds.isEmpty()) {
            StringBuilder deleteQuery = new StringBuilder("DELETE FROM grupo_estudiante WHERE ");

            if (!testGroupNrcs.isEmpty()) {
                deleteQuery.append("nrc IN (")
                        .append(String.join(",", testGroupNrcs.stream().map(String::valueOf).toArray(String[]::new)))
                        .append(")");
            }

            if (!testGroupNrcs.isEmpty() && !testStudentIds.isEmpty()) {
                deleteQuery.append(" OR ");
            }

            if (!testStudentIds.isEmpty()) {
                deleteQuery.append("id_usuario IN (")
                        .append(String.join(",", testStudentIds.stream().map(String::valueOf).toArray(String[]::new)))
                        .append(")");
            }

            try (Statement stmt = testConnection.createStatement()) {
                stmt.execute(deleteQuery.toString());
            }
        }
        if (!testGroupNrcs.isEmpty()) {
            String deleteGroups = "DELETE FROM grupo WHERE nrc IN (" +
                    String.join(",", testGroupNrcs.stream().map(String::valueOf).toArray(String[]::new)) + ")";
            try (Statement stmt = testConnection.createStatement()) {
                stmt.execute(deleteGroups);
            }
            testGroupNrcs.clear();
        }

        if (!testStudentIds.isEmpty()) {
            String deleteStudents = "DELETE FROM estudiante WHERE id_usuario IN (" +
                    String.join(",", testStudentIds.stream().map(String::valueOf).toArray(String[]::new)) + ")";
            try (Statement stmt = testConnection.createStatement()) {
                stmt.execute(deleteStudents);
            }

            String deleteUsers = "DELETE FROM usuario WHERE id_usuario IN (" +
                    String.join(",", testStudentIds.stream().map(String::valueOf).toArray(String[]::new)) + ")";
            try (Statement stmt = testConnection.createStatement()) {
                stmt.execute(deleteUsers);
            }
            testStudentIds.clear();
        }
    }

    // addStudent
    @Test
    void testAddStudent_Success() throws SQLException {
        // Crea solo el usuario, no el estudiante
        int userId = createTestUser("New Student", "5554444444");
        Student newStudent = new Student();
        newStudent.setIdUser(userId);
        newStudent.setEnrollment("STEST004");
        newStudent.setFullName("New Student");
        newStudent.setCellphone("5554444444");
        newStudent.setGrade(6);
        testStudentIds.add(userId);

        boolean result = studentDAO.addStudent(newStudent);
        assertTrue(result);
    }

    @Test
    void testAddStudent_NullStudent() throws SQLException {
        assertFalse(studentDAO.addStudent(null));
    }

    @Test
    void testAddStudent_DuplicateEnrollment_ShouldThrowException() throws SQLException {
        Student duplicateStudent = testStudents.get(0);
        assertThrows(SQLException.class, () -> studentDAO.addStudent(duplicateStudent));
    }

    // getStudentByEnrollment
    @Test
    void testGetStudentByEnrollment_Exists() throws SQLException {
        Student foundStudent = studentDAO.getStudentByEnrollment("STEST001");
        assertEquals("Test Student 1", foundStudent.getFullName());
    }

    @Test
    void testGetStudentByEnrollment_NotExists() throws SQLException {
        Student foundStudent = studentDAO.getStudentByEnrollment("NONEXIST");
        assertEquals(-1, foundStudent.getIdUser());
    }

    @Test
    void testGetStudentByEnrollment_NullOrEmpty() throws SQLException {
        Student foundStudent = studentDAO.getStudentByEnrollment(null);
        assertEquals(-1, foundStudent.getIdUser());
    }

    // getAllStudents
    @Test
    void testGetAllStudents_WithData() throws SQLException {
        List<Student> students = studentDAO.getAllStudents();
        assertFalse(students.isEmpty());
    }

    @Test
    void testGetAllStudents_EmptyTable() throws SQLException {
        cleanTestData();
        List<Student> students = studentDAO.getAllStudents();
        assertTrue(students.isEmpty());
        setUp();
    }

    // getSudentsByStatus
    @Test
    void testGetStudentsByStatus_WithData() throws SQLException {
        List<Student> students = studentDAO.getSudentsByStatus('A');
        assertNotNull(students);
    }

    @Test
    void testGetStudentsByStatus_EmptyResult() throws SQLException {
        List<Student> students = studentDAO.getSudentsByStatus('Z');
        assertTrue(students.isEmpty());
    }

    // getStudentById
    @Test
    void testGetStudentById_Exists() throws SQLException {
        Student foundStudent = studentDAO.getStudentById(testStudents.get(0).getIdUser());
        assertEquals("STEST001", foundStudent.getEnrollment());
    }

    @Test
    void testGetStudentById_NotExists() throws SQLException {
        Student foundStudent = studentDAO.getStudentById(999999);
        assertNull(foundStudent);
    }

    @Test
    void testGetStudentById_InvalidId() throws SQLException {
        Student foundStudent = studentDAO.getStudentById(-1);
        assertNull(foundStudent);
    }

    // updateStudent
    @Test
    void testUpdateStudent_Success() throws SQLException {
        Student toUpdate = testStudents.get(0);
        toUpdate.setFullName("Updated Name");
        toUpdate.setGrade(9);
        boolean result = studentDAO.updateStudent(toUpdate);
        assertTrue(result);
    }

    @Test
    void testUpdateStudent_NotExists() throws SQLException {
        Student nonExistent = new Student();
        nonExistent.setIdUser(999999);
        nonExistent.setEnrollment("NONEXIST");
        boolean result = studentDAO.updateStudent(nonExistent);
        assertFalse(result);
    }

    @Test
    void testUpdateStudent_NullStudent() throws SQLException {
        assertFalse(studentDAO.updateStudent(null));
    }

    // updateStudentGrade
    @Test
    void testUpdateStudentGrade_Success() throws SQLException {
        boolean result = studentDAO.updateStudentGrade(testStudents.get(0).getIdUser(), 9);
        assertTrue(result);
    }

    @Test
    void testUpdateStudentGrade_NotExists() throws SQLException {
        boolean result = studentDAO.updateStudentGrade(999999, 8);
        assertFalse(result);
    }

    @Test
    void testUpdateStudentGrade_InvalidId() throws SQLException {
        boolean result = studentDAO.updateStudentGrade(-1, 8);
        assertFalse(result);
    }

    // deleteStudent
    @Test
    void testDeleteStudent_Success() throws SQLException {
        int idToDelete = testStudents.get(0).getIdUser();
        boolean result = studentDAO.deleteStudent(idToDelete);
        assertTrue(result);
        testStudentIds.remove(Integer.valueOf(idToDelete)); // Elimina por valor, no por índice
    }

    @Test
    void testDeleteStudent_NotExists() throws SQLException {
        boolean result = studentDAO.deleteStudent(999999);
        assertFalse(result);
    }

    @Test
    void testDeleteStudent_InvalidId() throws SQLException {
        boolean result = studentDAO.deleteStudent(-1);
        assertFalse(result);
    }

    // getStudentsByGroup
    @Test
    void testGetStudentsByGroup_WithData() throws SQLException {
        int testNrc = createTestGroup(1001);
        studentDAO.assignStudentToGroup(testStudents.get(0).getIdUser(), testNrc);
        studentDAO.assignStudentToGroup(testStudents.get(1).getIdUser(), testNrc);
        List<Student> students = studentDAO.getStudentsByGroup(testNrc);
        assertEquals(2, students.size());
    }

    @Test
    void testGetStudentsByGroup_EmptyGroup() throws SQLException {
        int testNrc = createTestGroup(1002);
        List<Student> students = studentDAO.getStudentsByGroup(testNrc);
        assertTrue(students.isEmpty());
    }

    @Test
    void testGetStudentsByGroup_InvalidNrc() throws SQLException {
        List<Student> students = studentDAO.getStudentsByGroup(-1);
        assertTrue(students.isEmpty());
    }

    // assignStudentToGroup
    @Test
    void testAssignStudentToGroup_Success() throws SQLException {
        int testNrc = createTestGroup(1003);
        boolean result = studentDAO.assignStudentToGroup(testStudents.get(0).getIdUser(), testNrc);
        assertTrue(result);
    }

    @Test
    void testAssignStudentToGroup_NotExists() throws SQLException {
        int testNrc = createTestGroup(1004);
        boolean result = studentDAO.assignStudentToGroup(999999, testNrc);
        assertFalse(result);
    }

    @Test
    void testAssignStudentToGroup_InvalidNrc() throws SQLException {
        boolean result = studentDAO.assignStudentToGroup(testStudents.get(0).getIdUser(), -1);
        assertFalse(result);
    }

    // studentExistsById
    @Test
    void testStudentExistsById_True() throws SQLException {
        boolean exists = studentDAO.studentExistsById(testStudents.get(0).getIdUser());
        assertTrue(exists);
    }

    @Test
    void testStudentExistsById_False() throws SQLException {
        boolean exists = studentDAO.studentExistsById(999999);
        assertFalse(exists);
    }

    @Test
    void testStudentExistsById_InvalidId() throws SQLException {
        boolean exists = studentDAO.studentExistsById(-1);
        assertFalse(exists);
    }

    // enrollmentExists
    @Test
    void testEnrollmentExists_DuplicateEnrollment_ShouldThrowException() {
        assertThrows(RepeatedEnrollmentException.class, () -> studentDAO.enrollmentExists("STEST001"));
    }

    @Test
    void testEnrollmentExists_NewEnrollment() throws SQLException {
        boolean exists = studentDAO.enrollmentExists("NEWENROLL");
        assertFalse(exists);
    }

    @Test
    void testEnrollmentExists_NullOrEmpty() throws SQLException {
        boolean exists = studentDAO.enrollmentExists(null);
        assertFalse(exists);
    }

    // countStudents
    @Test
    void testCountStudents_WithData() throws SQLException {
        int initialCount = studentDAO.countStudents();
        // Crea usuario y estudiante nuevos, pero solo inserta el usuario manualmente
        int userId = createTestUser("Count Test", "5555555555");
        Student newStudent = new Student();
        newStudent.setIdUser(userId);
        newStudent.setEnrollment("STEST005");
        newStudent.setFullName("Count Test");
        newStudent.setCellphone("5555555555");
        newStudent.setGrade(7);
        testStudentIds.add(userId);

        studentDAO.addStudent(newStudent);
        assertEquals(initialCount + 1, studentDAO.countStudents());
    }

    @Test
    void testCountStudents_EmptyTable() throws SQLException {
        cleanTestData();
        assertEquals(0, studentDAO.countStudents());
        setUp();
    }
}
