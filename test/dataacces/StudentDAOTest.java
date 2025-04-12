package dataacces;

import logic.Student;
import logic.User;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StudentDAOTest {
    private static StudentDAO studentDAO;
    private static UserDAO userDAO;
    private static Connection testConnection;
    private static List<Student> testStudents;

    @BeforeAll
    static void setUpAll() throws SQLException {
        studentDAO = new StudentDAO();
        userDAO = new UserDAO();
        testConnection = ConnectionDataBase.getConnection();

        try (var statement = testConnection.createStatement()) {
            statement.execute("DELETE FROM presentacion");
            statement.execute("ALTER TABLE presentacion AUTO_INCREMENT = 1");
            statement.execute("DELETE FROM grupo_estudiante");
            statement.execute("DELETE FROM estudiante");
            statement.execute("ALTER TABLE estudiante AUTO_INCREMENT = 1");
            statement.execute("DELETE FROM grupo");
            statement.execute("ALTER TABLE grupo AUTO_INCREMENT = 1");
            statement.execute("DELETE FROM academico");
            statement.execute("ALTER TABLE academico AUTO_INCREMENT = 1");
            statement.execute("DELETE FROM cuenta");
            statement.execute("ALTER TABLE cuenta AUTO_INCREMENT = 1");
            statement.execute("DELETE FROM usuario");
            statement.execute("ALTER TABLE usuario AUTO_INCREMENT = 1");

            statement.execute("CREATE TABLE IF NOT EXISTS usuario (" +
                    "id_usuario INT AUTO_INCREMENT PRIMARY KEY, " +
                    "nombre_completo VARCHAR(100) NOT NULL, " +
                    "telefono VARCHAR(20), " +
                    "estado CHAR(1) DEFAULT 'A')");

            statement.execute("CREATE TABLE IF NOT EXISTS estudiante (" +
                    "id_usuario INT PRIMARY KEY, " +
                    "matricula VARCHAR(50) NOT NULL UNIQUE, " +
                    "FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario))");

            statement.execute("CREATE TABLE IF NOT EXISTS grupo_estudiante (" +
                    "nrc INT, " +
                    "id_usuario INT, " +
                    "PRIMARY KEY (nrc, id_usuario), " +
                    "FOREIGN KEY (id_usuario) REFERENCES estudiante(id_usuario))");
        }

        testStudents = List.of(
                createTestStudent("S001", "Estudiante 1", "5551111111"),
                createTestStudent("S002", "Estudiante 2", "5552222222"),
                createTestStudent("S003", "Estudiante 3", "5553333333")
        );
    }

    private static Student createTestStudent(String enrollment, String fullName, String phone) throws SQLException {
        Student student = new Student();
        student.setEnrollment(enrollment);
        student.setFullName(fullName);
        student.setCellphone(phone);
        studentDAO.addStudent(student);
        return student;
    }

    @AfterAll
    static void tearDownAll() throws SQLException {
        if (testConnection != null && !testConnection.isClosed()) {
            testConnection.close();
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        try (var statement = testConnection.createStatement()) {
            statement.execute("DELETE FROM grupo_estudiante");
            statement.execute("DELETE FROM estudiante");
            statement.execute("DELETE FROM usuario");
            statement.execute("ALTER TABLE usuario AUTO_INCREMENT = 1");
        }

        for (Student student : testStudents) {
            createTestStudent(student.getEnrollment(), student.getFullName(), student.getCellPhone());
        }
    }

    @Test
    void testAddStudent_Success() throws SQLException {
        Student newStudent = new Student();
        newStudent.setEnrollment("S004");
        newStudent.setFullName("Nuevo Estudiante");
        newStudent.setCellphone("5554444444");

        int initialCount = studentDAO.countStudents();
        boolean result = studentDAO.addStudent(newStudent);

        assertTrue(result);
        assertEquals(initialCount + 1, studentDAO.countStudents());

        Student addedStudent = studentDAO.getStudentById(newStudent.getIdUser());
        assertNotNull(addedStudent);
        assertEquals("S004", addedStudent.getEnrollment());
        assertEquals("Nuevo Estudiante", addedStudent.getFullName());
    }

    @Test
    void testAddStudent_DuplicateEnrollment_ShouldFail() {
        Student duplicateStudent = new Student();
        duplicateStudent.setEnrollment("S001");
        duplicateStudent.setFullName("Estudiante Duplicado");
        duplicateStudent.setCellphone("5555555555");

        assertThrows(SQLException.class, () -> studentDAO.addStudent(duplicateStudent));
    }

    @Test
    void testAddStudent_NullFields_ShouldThrowException() {
        Student invalidStudent = new Student();
        invalidStudent.setEnrollment(null);
        invalidStudent.setFullName(null);

        assertThrows(SQLException.class, () -> studentDAO.addStudent(invalidStudent));
    }

    @Test
    void testGetAllStudents_WithData() throws SQLException {
        List<Student> students = studentDAO.getAllStudents();
        assertEquals(testStudents.size(), students.size());

        for (Student testStudent : testStudents) {
            boolean found = students.stream()
                    .anyMatch(s -> s.getEnrollment().equals(testStudent.getEnrollment()));
            assertTrue(found, "No se encontró el estudiante con matrícula: " + testStudent.getEnrollment());
        }
    }

    @Test
    void testGetStudentById_Exists() throws SQLException {
        Student testStudent = testStudents.get(0);
        Student foundStudent = studentDAO.getStudentById(testStudent.getIdUser());

        assertNotNull(foundStudent);
        assertEquals(testStudent.getEnrollment(), foundStudent.getEnrollment());
        assertEquals(testStudent.getFullName(), foundStudent.getFullName());
    }

    @Test
    void testGetStudentById_NotExists() throws SQLException {
        Student foundStudent = studentDAO.getStudentById(9999);
        assertNull(foundStudent);
    }

    @Test
    void testUpdateStudent_Success() throws SQLException {
        Student studentToUpdate = studentDAO.getStudentById(testStudents.get(0).getIdUser());
        studentToUpdate.setFullName("Nombre Actualizado");
        studentToUpdate.setCellphone("5550000000");

        boolean result = studentDAO.updateStudent(studentToUpdate);
        assertTrue(result);

        Student updatedStudent = studentDAO.getStudentById(studentToUpdate.getIdUser());
        assertNotNull(updatedStudent);
        assertEquals("Nombre Actualizado", updatedStudent.getFullName());
        assertEquals("5550000000", updatedStudent.getCellPhone());
    }

    @Test
    void testUpdateStudent_NotExists() throws SQLException {
        Student nonExistentStudent = new Student();
        nonExistentStudent.setIdUser(9999);
        nonExistentStudent.setEnrollment("NOEXISTE");
        nonExistentStudent.setFullName("No existe");

        boolean result = studentDAO.updateStudent(nonExistentStudent);
        assertFalse(result);
    }

    @Test
    void testDeleteStudent_Success() throws SQLException {
        Student studentToDelete = testStudents.get(0);
        int initialCount = studentDAO.countStudents();

        boolean result = studentDAO.deleteStudent(studentToDelete.getIdUser());
        assertTrue(result);
        assertEquals(initialCount - 1, studentDAO.countStudents());
        assertNull(studentDAO.getStudentById(studentToDelete.getIdUser()));
    }

    @Test
    void testDeleteStudent_NotExists() throws SQLException {
        int initialCount = studentDAO.countStudents();
        boolean result = studentDAO.deleteStudent(9999);

        assertFalse(result);
        assertEquals(initialCount, studentDAO.countStudents());
    }

    @Test
    void testGetStudentsByGroup() throws SQLException {
        int testNrc = 1001;
        try (PreparedStatement ps = testConnection.prepareStatement(
                "INSERT INTO grupo (nrc, nombre) VALUES (?, 'Grupo Test2')")) {
            ps.setInt(1, testNrc);
            ps.executeUpdate();
        }

        Student student1 = testStudents.get(0);
        Student student2 = testStudents.get(1);

        try (PreparedStatement ps = testConnection.prepareStatement(
                "INSERT INTO grupo_estudiante (nrc, id_usuario) VALUES (?, ?)")) {
            ps.setInt(1, testNrc);
            ps.setInt(2, student1.getIdUser());
            ps.executeUpdate();

            ps.setInt(1, testNrc);
            ps.setInt(2, student2.getIdUser());
            ps.executeUpdate();
        }

        List<Student> studentsInGroup = studentDAO.getStudentsByGroup(testNrc);
        assertEquals(2, studentsInGroup.size());
        assertTrue(studentsInGroup.stream().anyMatch(s -> s.getIdUser() == student1.getIdUser()));
        assertTrue(studentsInGroup.stream().anyMatch(s -> s.getIdUser() == student2.getIdUser()));
    }

    @Test
    void testAssignStudentToGroup_Success() throws SQLException {
        int testNrc = 1005;
        try (Connection conn = ConnectionDataBase.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO grupo (nrc, nombre) VALUES (?, 'Grupo Test')")) {
            ps.setInt(1, testNrc);
            ps.executeUpdate();
        }

        Student student = testStudents.get(0);

        boolean result = studentDAO.assignStudentToGroup(student.getIdUser(), testNrc);
        assertTrue(result, "El método debería reportar éxito en la asignación");

        try (Connection conn = ConnectionDataBase.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM grupo_estudiante WHERE nrc = ? AND id_usuario = ?")) {
            ps.setInt(1, testNrc);
            ps.setInt(2, student.getIdUser());

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Debería haber resultados");
                assertEquals(1, rs.getInt(1), "Debería existir exactamente una relación");
            }
        }
    }

    @Test
    void testStudentExists_True() throws SQLException {
        Student testStudent = testStudents.get(0);
        assertTrue(studentDAO.studentExists(testStudent.getIdUser()));
    }

    @Test
    void testStudentExists_False() throws SQLException {
        assertFalse(studentDAO.studentExists(9999));
    }

    @Test
    void testCountStudents_WithData() throws SQLException {
        int count = studentDAO.countStudents();
        assertEquals(testStudents.size(), count);

        Student extraStudent = new Student();
        extraStudent.setEnrollment("S999");
        extraStudent.setFullName("Extra Estudiante");
        extraStudent.setCellphone("5559999999");
        studentDAO.addStudent(extraStudent);

        assertEquals(count + 1, studentDAO.countStudents());
    }
}