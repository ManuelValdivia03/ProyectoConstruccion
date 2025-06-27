package daos;

import dataaccess.ConnectionDataBase;
import logic.daos.PresentationDAO;
import logic.daos.StudentDAO;
import logic.logicclasses.Presentation;
import logic.logicclasses.Student;
import logic.enums.PresentationType;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PresentationDAOTest {
    private static PresentationDAO presentationDAO;
    private static StudentDAO studentDAO;
    private static Connection testConnection;
    private static List<Student> testStudents;

    @BeforeAll
    static void setUpAll() throws SQLException {
        presentationDAO = new PresentationDAO();
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

        testStudents = new ArrayList<>();
        testStudents.add(createTestStudent("S001", "Estudiante 1", "5551111111"));
        testStudents.add(createTestStudent("S002", "Estudiante 2", "5552222222"));
    }

    private static Student createTestStudent(String enrollment, String fullName, String phone) throws SQLException {
        try (PreparedStatement ps = testConnection.prepareStatement(
                "INSERT INTO usuario (nombre_completo, telefono, estado) VALUES (?, ?, 'A')",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, fullName);
            ps.setString(2, phone);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int idUsuario = rs.getInt(1);
                    Student student = new Student();
                    student.setIdUser(idUsuario);
                    student.setEnrollment(enrollment);
                    student.setFullName(fullName);
                    student.setCellphone(phone);
                    try (PreparedStatement psEst = testConnection.prepareStatement(
                            "INSERT INTO estudiante (id_usuario, matricula) VALUES (?, ?)")) {
                        psEst.setInt(1, idUsuario);
                        psEst.setString(2, enrollment);
                        psEst.executeUpdate();
                    }
                    return student;
                } else {
                    throw new SQLException("No se pudo obtener el id_usuario generado.");
                }
            }
        }
    }

    private Presentation createTestPresentation(Student student, Timestamp date, PresentationType type) throws SQLException {
        Presentation presentation = new Presentation();
        presentation.setStudent(student);
        presentation.setPresentationDate(date);
        presentation.setPresentationType(type);
        presentationDAO.addPresentation(presentation);
        return presentation;
    }

    @AfterAll
    static void tearDownAll() throws SQLException {
        if (testConnection != null && !testConnection.isClosed()) {
            testConnection.close();
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("DELETE FROM evaluacion");
            stmt.execute("DELETE FROM presentacion");
            stmt.execute("ALTER TABLE presentacion AUTO_INCREMENT = 1");
        }
    }

    @Test
    void testAddPresentation_Success() throws SQLException {
        Presentation p = new Presentation();
        p.setStudent(testStudents.get(0));
        p.setPresentationDate(Timestamp.from(Instant.now()));
        p.setPresentationType(PresentationType.Parcial);
        assertTrue(presentationDAO.addPresentation(p));
        assertTrue(p.getIdPresentation() > 0);
    }

    @Test
    void testAddPresentation_Error() {
        Presentation p = new Presentation();
        assertThrows(SQLException.class, () -> presentationDAO.addPresentation(p));
    }

    @Test
    void testAddPresentation_Exception() {
        assertThrows(SQLException.class, () -> presentationDAO.addPresentation(null));
    }

    @Test
    void testGetPresentationById_Success() throws SQLException {
        Presentation p = createTestPresentation(testStudents.get(0), Timestamp.from(Instant.now()), PresentationType.Parcial);
        Presentation found = presentationDAO.getPresentationById(p.getIdPresentation());
        assertEquals(p, found);
    }

    @Test
    void testGetPresentationById_Error() throws SQLException {
        Presentation found = presentationDAO.getPresentationById(9999);
        assertEquals(new Presentation(), found);
    }

    @Test
    void testGetPresentationById_Exception() throws SQLException {
        Presentation found = presentationDAO.getPresentationById(-1);
        assertEquals(new Presentation(), found);
    }

    @Test
    void testGetAllPresentations_Success() throws SQLException {
        Presentation p1 = createTestPresentation(testStudents.get(0), Timestamp.from(Instant.now()), PresentationType.Parcial);
        Presentation p2 = createTestPresentation(testStudents.get(1), Timestamp.from(Instant.now()), PresentationType.Final);
        List<Presentation> list = presentationDAO.getAllPresentations();
        assertEquals(2, list.size());
        assertTrue(list.contains(p1));
        assertTrue(list.contains(p2));
    }

    @Test
    void testGetAllPresentations_Error() throws SQLException {
        List<Presentation> list = presentationDAO.getAllPresentations();
        assertTrue(list.isEmpty());
    }

    @Test
    void testGetAllPresentations_Exception() throws SQLException {
        List<Presentation> list = presentationDAO.getAllPresentations();
        assertNotNull(list);
    }

    @Test
    void testGetPresentationsByStudent_Success() throws SQLException {
        Presentation p = createTestPresentation(testStudents.get(0), Timestamp.from(Instant.now()), PresentationType.Parcial);
        List<Presentation> list = presentationDAO.getPresentationsByStudent(testStudents.get(0).getIdUser());
        assertTrue(list.contains(p));
    }

    @Test
    void testGetPresentationsByStudent_Error() throws SQLException {
        List<Presentation> list = presentationDAO.getPresentationsByStudent(9999);
        assertTrue(list.isEmpty());
    }

    @Test
    void testGetPresentationsByStudent_Exception() throws SQLException {
        List<Presentation> list = presentationDAO.getPresentationsByStudent(-1);
        assertTrue(list.isEmpty());
    }

    @Test
    void testGetPresentationsByType_Success() throws SQLException {
        Presentation p = createTestPresentation(testStudents.get(0), Timestamp.from(Instant.now()), PresentationType.Parcial);
        List<Presentation> list = presentationDAO.getPresentationsByType(PresentationType.Parcial.name());
        assertTrue(list.contains(p));
    }

    @Test
    void testGetPresentationsByType_Error() throws SQLException {
        List<Presentation> list = presentationDAO.getPresentationsByType("NoExiste");
        assertTrue(list.isEmpty());
    }

    @Test
    void testGetPresentationsByType_Exception() throws SQLException {
        List<Presentation> list = presentationDAO.getPresentationsByType(null);
        assertTrue(list.isEmpty());
    }

    @Test
    void testUpdatePresentation_Success() throws SQLException {
        Presentation p = createTestPresentation(testStudents.get(0), Timestamp.from(Instant.now()), PresentationType.Parcial);
        p.setPresentationType(PresentationType.Final);
        boolean updated = presentationDAO.updatePresentation(p);
        assertTrue(updated);
        Presentation updatedP = presentationDAO.getPresentationById(p.getIdPresentation());
        assertEquals(p, updatedP);
    }

    @Test
    void testUpdatePresentation_Error() throws SQLException {
        Presentation p = new Presentation();
        p.setIdPresentation(9999);
        p.setStudent(testStudents.get(0));
        p.setPresentationType(PresentationType.Parcial);
        p.setPresentationDate(Timestamp.from(Instant.now()));
        assertFalse(presentationDAO.updatePresentation(p));
    }

    @Test
    void testUpdatePresentation_Exception() {
        assertThrows(IllegalArgumentException.class, () -> presentationDAO.updatePresentation(null));
    }

    @Test
    void testDeletePresentation_Success() throws SQLException {
        Presentation p = createTestPresentation(testStudents.get(0), Timestamp.from(Instant.now()), PresentationType.Parcial);
        boolean deleted = presentationDAO.deletePresentation(p.getIdPresentation());
        assertTrue(deleted);
        Presentation found = presentationDAO.getPresentationById(p.getIdPresentation());
        assertEquals(new Presentation(), found);
    }

    @Test
    void testDeletePresentation_Error() throws SQLException {
        assertFalse(presentationDAO.deletePresentation(9999));
    }

    @Test
    void testDeletePresentation_Exception() throws SQLException {
        assertFalse(presentationDAO.deletePresentation(-1));
    }

    @Test
    void testPresentationExists_Success() throws SQLException {
        Presentation p = createTestPresentation(testStudents.get(0), Timestamp.from(Instant.now()), PresentationType.Parcial);
        assertTrue(presentationDAO.presentationExists(p.getIdPresentation()));
    }

    @Test
    void testPresentationExists_Error() throws SQLException {
        assertFalse(presentationDAO.presentationExists(9999));
    }

    @Test
    void testPresentationExists_Exception() throws SQLException {
        assertFalse(presentationDAO.presentationExists(-1));
    }

    @Test
    void testCountPresentations_Success() throws SQLException {
        createTestPresentation(testStudents.get(0), Timestamp.from(Instant.now()), PresentationType.Parcial);
        createTestPresentation(testStudents.get(1), Timestamp.from(Instant.now()), PresentationType.Final);
        assertEquals(2, presentationDAO.countPresentations());
    }

    @Test
    void testCountPresentations_Error() throws SQLException {
        assertEquals(0, presentationDAO.countPresentations());
    }

    @Test
    void testCountPresentations_Exception() throws SQLException {
        int count = presentationDAO.countPresentations();
        assertTrue(count >= 0);
    }
}
