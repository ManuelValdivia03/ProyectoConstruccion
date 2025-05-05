package daos;

import dataaccess.ConnectionDataBase;
import logic.daos.StudentDAO;
import logic.logicclasses.Student;
import logic.logicclasses.User;
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
        // Clean group_student entries first
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

        // Delete test groups
        if (!testGroupNrcs.isEmpty()) {
            String deleteGroups = "DELETE FROM grupo WHERE nrc IN (" +
                    String.join(",", testGroupNrcs.stream().map(String::valueOf).toArray(String[]::new)) + ")";
            try (Statement stmt = testConnection.createStatement()) {
                stmt.execute(deleteGroups);
            }
            testGroupNrcs.clear();
        }

        // Delete test students and users
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

    // =============================================
    // addStudent() Tests
    // =============================================

    @Test
    void addStudent_HappyPath_ValidData_ReturnsTrue() throws SQLException {
        Student newStudent = new Student();
        newStudent.setIdUser(0);
        newStudent.setEnrollment("STEST004");
        newStudent.setFullName("New Student");
        newStudent.setCellphone("5554444444");
        newStudent.setStatus('A'); // Active status
        newStudent.setGrade(6); // Valid grade 1-10

        User newUser = new User();
        newUser.setIdUser(newStudent.getIdUser());
        newUser.setFullName(newUser.getFullName());
        newUser.setCellphone(newStudent.getCellPhone());
        newUser.setStatus(newStudent.getStatus());

        boolean result = studentDAO.addStudent(newStudent);
        assertTrue(result);
        testStudentIds.add(newStudent.getIdUser());
    }

    @Test
    void addStudent_AlternativeFlow_DuplicateEnrollment_ThrowsException() {
        Student duplicateStudent = new Student();
        duplicateStudent.setEnrollment("STEST001"); // Duplicate
        duplicateStudent.setFullName("Duplicate Student");
        duplicateStudent.setCellphone("5555555555");
        duplicateStudent.setGrade(8);

        assertThrows(SQLException.class, () -> studentDAO.addStudent(duplicateStudent));
    }

    @Test
    void addStudent_ErrorFlow_InvalidGrade_ThrowsException() {
        Student invalidStudent = new Student();
        invalidStudent.setEnrollment("STEST005");
        invalidStudent.setFullName("Invalid Student");
        invalidStudent.setCellphone("5556666666");
        invalidStudent.setGrade(11); // Invalid grade >10

        assertThrows(SQLException.class, () -> studentDAO.addStudent(invalidStudent));
    }

    // =============================================
    // getStudentByEnrollment() Tests
    // =============================================

    @Test
    void getStudentByEnrollment_HappyPath_ExistingStudent_ReturnsStudent() throws SQLException {
        Student foundStudent = studentDAO.getStudentByEnrollment("STEST001");
        assertEquals("Test Student 1", foundStudent.getFullName());
    }

    @Test
    void getStudentByEnrollment_AlternativeFlow_NonExistent_ReturnsEmptyStudent() throws SQLException {
        Student foundStudent = studentDAO.getStudentByEnrollment("NONEXIST");
        assertEquals(-1, foundStudent.getIdUser()); // Default empty student
    }

    @Test
    void getStudentByEnrollment_ErrorFlow_NullInput_ReturnsEmptyStudent() throws SQLException {
        Student foundStudent = studentDAO.getStudentByEnrollment(null);
        assertEquals(-1, foundStudent.getIdUser());
    }

    // =============================================
    // getAllStudents() Tests
    // =============================================

    @Test
    void getAllStudents_HappyPath_ReturnsNonEmptyList() throws SQLException {
        List<Student> students = studentDAO.getAllStudents();
        assertFalse(students.isEmpty());
    }

    @Test
    void getAllStudents_AlternativeFlow_IncludesTestStudents() throws SQLException {
        List<Student> students = studentDAO.getAllStudents();
        assertTrue(students.stream().anyMatch(s -> s.getEnrollment().equals("STEST001")));
    }

    // No error flow needed as this is a simple query

    // =============================================
    // getStudentById() Tests
    // =============================================

    @Test
    void getStudentById_HappyPath_ExistingId_ReturnsStudent() throws SQLException {
        Student foundStudent = studentDAO.getStudentById(testStudents.get(0).getIdUser());
        assertEquals("STEST001", foundStudent.getEnrollment());
    }

    @Test
    void getStudentById_AlternativeFlow_NonExistentId_ReturnsNull() throws SQLException {
        Student foundStudent = studentDAO.getStudentById(999999);
        assertNull(foundStudent);
    }

    @Test
    void getStudentById_ErrorFlow_InvalidId_ReturnsNull() throws SQLException {
        Student foundStudent = studentDAO.getStudentById(-1);
        assertNull(foundStudent);
    }

    // =============================================
    // updateStudent() Tests
    // =============================================

    @Test
    void updateStudent_HappyPath_ValidUpdate_ReturnsTrue() throws SQLException {
        Student toUpdate = testStudents.get(0);
        toUpdate.setFullName("Updated Name");
        toUpdate.setGrade(9); // Valid new grade

        boolean result = studentDAO.updateStudent(toUpdate);
        assertTrue(result);
    }

    @Test
    void updateStudent_AlternativeFlow_NonExistentStudent_ReturnsFalse() throws SQLException {
        Student nonExistent = new Student();
        nonExistent.setIdUser(999999);
        nonExistent.setEnrollment("NONEXIST");

        boolean result = studentDAO.updateStudent(nonExistent);
        assertFalse(result);
    }

    @Test
    void updateStudent_ErrorFlow_InvalidGrade_ThrowsException() throws SQLException {
        Student toUpdate = testStudents.get(0);
        toUpdate.setGrade(11); // Invalid grade

        assertThrows(SQLException.class, () -> studentDAO.updateStudent(toUpdate));
    }

    // =============================================
    // updateStudentGrade() Tests
    // =============================================

    @Test
    void updateStudentGrade_HappyPath_ValidGrade_ReturnsTrue() throws SQLException {
        boolean result = studentDAO.updateStudentGrade(testStudents.get(0).getIdUser(), 9);
        assertTrue(result);
    }

    @Test
    void updateStudentGrade_AlternativeFlow_NonExistentStudent_ReturnsFalse() throws SQLException {
        boolean result = studentDAO.updateStudentGrade(999999, 8);
        assertFalse(result);
    }

    @Test
    void updateStudentGrade_ErrorFlow_InvalidGrade_ThrowsException() {
        assertThrows(SQLException.class, () ->
                studentDAO.updateStudentGrade(testStudents.get(0).getIdUser(), 11));
    }

    // =============================================
    // deleteStudent() Tests
    // =============================================

    @Test
    void deleteStudent_HappyPath_ExistingStudent_ReturnsTrue() throws SQLException {
        boolean result = studentDAO.deleteStudent(testStudents.get(0).getIdUser());
        assertTrue(result);
        testStudentIds.remove(testStudents.get(0).getIdUser());
    }

    @Test
    void deleteStudent_AlternativeFlow_NonExistentStudent_ReturnsFalse() throws SQLException {
        boolean result = studentDAO.deleteStudent(999999);
        assertFalse(result);
    }

    @Test
    void deleteStudent_ErrorFlow_InvalidId_ReturnsFalse() throws SQLException {
        boolean result = studentDAO.deleteStudent(-1);
        assertFalse(result);
    }

    // =============================================
    // getStudentsByGroup() Tests
    // =============================================

    @Test
    void getStudentsByGroup_HappyPath_GroupWithStudents_ReturnsList() throws SQLException {
        int testNrc = createTestGroup(1001);
        studentDAO.assignStudentToGroup(testStudents.get(0).getIdUser(), testNrc);
        studentDAO.assignStudentToGroup(testStudents.get(1).getIdUser(), testNrc);

        List<Student> students = studentDAO.getStudentsByGroup(testNrc);
        assertEquals(2, students.size());
    }

    @Test
    void getStudentsByGroup_AlternativeFlow_EmptyGroup_ReturnsEmptyList() throws SQLException {
        int testNrc = createTestGroup(1002);
        List<Student> students = studentDAO.getStudentsByGroup(testNrc);
        assertTrue(students.isEmpty());
    }

    @Test
    void getStudentsByGroup_ErrorFlow_InvalidNrc_ReturnsEmptyList() throws SQLException {
        List<Student> students = studentDAO.getStudentsByGroup(999999);
        assertTrue(students.isEmpty());
    }

    // =============================================
    // assignStudentToGroup() Tests
    // =============================================

    @Test
    void assignStudentToGroup_HappyPath_ValidData_ReturnsTrue() throws SQLException {
        int testNrc = createTestGroup(1003);
        boolean result = studentDAO.assignStudentToGroup(testStudents.get(0).getIdUser(), testNrc);
        assertTrue(result);
    }

    @Test
    void assignStudentToGroup_AlternativeFlow_NonExistentStudent_ReturnsFalse() throws SQLException {
        int testNrc = createTestGroup(1004);
        boolean result = studentDAO.assignStudentToGroup(999999, testNrc);
        assertFalse(result);
    }

    @Test
    void assignStudentToGroup_ErrorFlow_InvalidNrc_ReturnsFalse() throws SQLException {
        boolean result = studentDAO.assignStudentToGroup(testStudents.get(0).getIdUser(), 999999);
        assertFalse(result);
    }

    // =============================================
    // studentExistsById() Tests
    // =============================================

    @Test
    void studentExistsById_HappyPath_ExistingStudent_ReturnsTrue() throws SQLException {
        boolean exists = studentDAO.studentExistsById(testStudents.get(0).getIdUser());
        assertTrue(exists);
    }

    @Test
    void studentExistsById_AlternativeFlow_NonExistentStudent_ReturnsFalse() throws SQLException {
        boolean exists = studentDAO.studentExistsById(999999);
        assertFalse(exists);
    }

    @Test
    void studentExistsById_ErrorFlow_InvalidId_ReturnsFalse() throws SQLException {
        boolean exists = studentDAO.studentExistsById(-1);
        assertFalse(exists);
    }

    // =============================================
    // enrollmentExists() Tests
    // =============================================

    @Test
    void enrollmentExists_HappyPath_DuplicateEnrollment_ThrowsException() {
        assertThrows(SQLException.class, () -> studentDAO.enrollmentExists("STEST001"));
    }

    @Test
    void enrollmentExists_AlternativeFlow_NewEnrollment_ReturnsFalse() throws SQLException {
        boolean exists = studentDAO.enrollmentExists("NEWENROLL");
        assertFalse(exists);
    }

    @Test
    void enrollmentExists_ErrorFlow_NullInput_ReturnsFalse() throws SQLException {
        boolean exists = studentDAO.enrollmentExists(null);
        assertFalse(exists);
    }

    // =============================================
    // countStudents() Tests
    // =============================================

    @Test
    void countStudents_HappyPath_AfterAddingStudent_IncreasesCount() throws SQLException {
        int initialCount = studentDAO.countStudents();

        Student newStudent = new Student();
        newStudent.setEnrollment("STEST004");
        newStudent.setFullName("Count Test");
        newStudent.setCellphone("5554444444");
        studentDAO.addStudent(newStudent);
        testStudentIds.add(newStudent.getIdUser());

        assertEquals(initialCount + 1, studentDAO.countStudents());
    }

}