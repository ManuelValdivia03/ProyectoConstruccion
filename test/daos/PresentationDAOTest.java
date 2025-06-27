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

        try (var connection = ConnectionDataBase.getConnection();
             var statement = connection.createStatement()) {
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
        try (PreparedStatement preparedStatement = testConnection.prepareStatement(
                "INSERT INTO usuario (nombre_completo, telefono, estado) VALUES (?, ?, 'A')",
                Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, fullName);
            preparedStatement.setString(2, phone);
            preparedStatement.executeUpdate();
            try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    int idUsuario = resultSet.getInt(1);
                    Student student = new Student();
                    student.setIdUser(idUsuario);
                    student.setEnrollment(enrollment);
                    student.setFullName(fullName);
                    student.setCellphone(phone);
                    try (PreparedStatement prepareStatement = testConnection.prepareStatement(
                            "INSERT INTO estudiante (id_usuario, matricula) VALUES (?, ?)")) {
                        prepareStatement.setInt(1, idUsuario);
                        prepareStatement.setString(2, enrollment);
                        prepareStatement.executeUpdate();
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
        try (Statement statement = testConnection.createStatement()) {
            statement.execute("DELETE FROM evaluacion");
            statement.execute("DELETE FROM presentacion");
            statement.execute("ALTER TABLE presentacion AUTO_INCREMENT = 1");
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
        assertThrows(IllegalArgumentException.class, () -> presentationDAO.addPresentation(p));
    }

    @Test
    void testAddPresentation_Exception() {
        assertThrows(IllegalArgumentException.class, () -> presentationDAO.addPresentation(null));
    }

    @Test
    void testGetPresentationById_Success() throws SQLException {
        Timestamp now = truncateToSeconds(Timestamp.from(Instant.now()));

        Presentation p = createTestPresentation(testStudents.get(0), now, PresentationType.Parcial);

        Presentation found = presentationDAO.getPresentationById(p.getIdPresentation());
        Timestamp foundDate = truncateToSeconds(found.getPresentationDate());
        Timestamp expectedDate = truncateToSeconds(p.getPresentationDate());

        assertEquals(p.getIdPresentation(), found.getIdPresentation());
        assertEquals(p.getPresentationType(), found.getPresentationType());
        assertEquals(expectedDate, foundDate);
        assertNotNull(found.getStudent());
        assertEquals(p.getStudent().getIdUser(), found.getStudent().getIdUser());
        assertEquals(p.getStudent().getFullName(), found.getStudent().getFullName());
        assertEquals(p.getStudent().getCellPhone(), found.getStudent().getCellPhone());
        assertEquals(p.getStudent().getEnrollment(), found.getStudent().getEnrollment());
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
        Timestamp now = truncateToSeconds(Timestamp.from(Instant.now()));
        Presentation p = createTestPresentation(testStudents.get(0), now, PresentationType.Parcial);

        List<Presentation> list = presentationDAO.getPresentationsByStudent(testStudents.get(0).getIdUser());

        boolean found = false;
        for (Presentation foundP : list) {
            if (foundP.getIdPresentation() == p.getIdPresentation() &&
                    foundP.getPresentationType() == p.getPresentationType() &&
                    truncateToSeconds(foundP.getPresentationDate()).equals(truncateToSeconds(p.getPresentationDate()))) {
                found = true;
                break;
            }
        }
        assertTrue(found);
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
        Timestamp now = truncateToSeconds(Timestamp.from(Instant.now()));
        Presentation p = createTestPresentation(testStudents.get(0), now, PresentationType.Parcial);

        List<Presentation> list = presentationDAO.getPresentationsByType(PresentationType.Parcial.name());

        boolean found = false;
        for (Presentation foundP : list) {
            if (foundP.getIdPresentation() == p.getIdPresentation() &&
                    foundP.getPresentationType() == p.getPresentationType() &&
                    truncateToSeconds(foundP.getPresentationDate()).equals(truncateToSeconds(p.getPresentationDate()))) {
                found = true;
                break;
            }
        }
        assertTrue(found);
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
        Timestamp now = truncateToSeconds(Timestamp.from(Instant.now()));
        Presentation p = createTestPresentation(testStudents.get(0), now, PresentationType.Parcial);

        p.setPresentationType(PresentationType.Final);
        boolean updated = presentationDAO.updatePresentation(p);
        assertTrue(updated);

        Presentation updatedP = presentationDAO.getPresentationById(p.getIdPresentation());

        assertEquals(p.getIdPresentation(), updatedP.getIdPresentation());
        assertEquals(p.getPresentationType(), updatedP.getPresentationType());
        assertEquals(truncateToSeconds(p.getPresentationDate()),
                truncateToSeconds(updatedP.getPresentationDate()));
        assertNotNull(updatedP.getStudent());
        assertEquals(p.getStudent().getIdUser(), updatedP.getStudent().getIdUser());
        assertEquals(p.getStudent().getFullName(), updatedP.getStudent().getFullName());
        assertEquals(p.getStudent().getCellPhone(), updatedP.getStudent().getCellPhone());
        assertEquals(p.getStudent().getEnrollment(), updatedP.getStudent().getEnrollment());
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

    private static Timestamp truncateToSeconds(Timestamp timestamp) {
        if (timestamp == null) return null;
        long seconds = timestamp.getTime() / 1000;
        return new Timestamp(seconds * 1000);
    }
}
