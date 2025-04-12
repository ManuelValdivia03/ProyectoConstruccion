package dataacces;

import logic.Presentation;
import logic.Student;
import logic.enums.PresentationType;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class PresentationDAOTest {
    private static PresentationDAO presentationDAO;
    private static StudentDAO studentDAO;
    private static Connection testConnection;
    private static List<Presentation> testPresentations;
    private static List<Student> testStudents;

    @BeforeAll
    static void setUpAll() throws SQLException {
        presentationDAO = new PresentationDAO();
        studentDAO = new StudentDAO();
        testConnection = ConnectionDataBase.getConnection();

        try (var statement = testConnection.createStatement()) {
            statement.execute("DELETE FROM evaluacion");
            statement.execute("DELETE FROM presentacion");
            statement.execute("DELETE FROM academico");
            statement.execute("DELETE FROM estudiante");
            statement.execute("DELETE FROM usuario");

            statement.execute("ALTER TABLE usuario AUTO_INCREMENT = 1");
            statement.execute("ALTER TABLE estudiante AUTO_INCREMENT = 1");
            statement.execute("ALTER TABLE presentacion AUTO_INCREMENT = 1");
            statement.execute("ALTER TABLE academico AUTO_INCREMENT = 1");

            statement.execute("CREATE TABLE IF NOT EXISTS usuario (" +
                    "id_usuario INT AUTO_INCREMENT PRIMARY KEY, " +
                    "nombre_completo VARCHAR(100) NOT NULL, " +
                    "telefono VARCHAR(20), " +
                    "estado CHAR(1) DEFAULT 'A')");

            statement.execute("CREATE TABLE IF NOT EXISTS estudiante (" +
                    "id_usuario INT PRIMARY KEY, " +
                    "matricula VARCHAR(50) NOT NULL UNIQUE, " +
                    "FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario))");

            statement.execute("CREATE TABLE IF NOT EXISTS presentacion (" +
                    "id_presentacion INT AUTO_INCREMENT PRIMARY KEY, " +
                    "fecha TIMESTAMP NOT NULL, " +
                    "tipo VARCHAR(50) NOT NULL, " +
                    "id_estudiante INT NOT NULL, " +
                    "FOREIGN KEY (id_estudiante) REFERENCES estudiante(id_usuario))");
        }

        testStudents = new ArrayList<>();
        testStudents.add(createTestStudent("S001", "Estudiante 1", "5551111111"));
        testStudents.add(createTestStudent("S002", "Estudiante 2", "5552222222"));

        assertTrue(studentDAO.studentExists(testStudents.get(0).getIdUser()));
        assertTrue(studentDAO.studentExists(testStudents.get(1).getIdUser()));

        testPresentations = new ArrayList<>();
        testPresentations.add(createTestPresentation(
                testStudents.get(0),
                Timestamp.from(Instant.now()),
                PresentationType.Parcial
        ));
        testPresentations.add(createTestPresentation(
                testStudents.get(1),
                Timestamp.from(Instant.now()),
                PresentationType.Final
        ));

        assertEquals(2, presentationDAO.countPresentations());
    }

    private static Student createTestStudent(String enrollment, String fullName, String phone) throws SQLException {
        Student student = new Student();
        student.setEnrollment(enrollment);
        student.setFullName(fullName);
        student.setCellphone(phone);
        studentDAO.addStudent(student);
        return student;
    }

    private static Presentation createTestPresentation(Student student, Timestamp date, PresentationType type) throws SQLException {
        Objects.requireNonNull(student, "El estudiante no puede ser nulo");
        Objects.requireNonNull(date, "La fecha no puede ser nula");
        Objects.requireNonNull(type, "El tipo no puede ser nulo");

        Presentation presentation = new Presentation();
        presentation.setStudent(student);
        presentation.setPresentationDate(date);
        presentation.setPresentationType(type);

        boolean added = presentationDAO.addPresentation(presentation);
        if (!added) {
            throw new SQLException("No se pudo agregar la presentación de prueba");
        }

        if (presentation.getIdPresentation() <= 0) {
            throw new SQLException("No se asignó ID a la presentación");
        }

        Presentation fromDb = presentationDAO.getPresentationById(presentation.getIdPresentation());
        if (fromDb == null) {
            throw new SQLException("La presentación no se pudo recuperar después de crearla");
        }

        return fromDb;
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

        testPresentations = new ArrayList<>();


        Presentation pres1 = new Presentation();
        pres1.setStudent(testStudents.get(0));
        pres1.setPresentationDate(Timestamp.from(Instant.now()));
        pres1.setPresentationType(PresentationType.Parcial);
        assertTrue(presentationDAO.addPresentation(pres1), "No se pudo agregar presentación 1");
        testPresentations.add(pres1);


        Presentation pres2 = new Presentation();
        pres2.setStudent(testStudents.get(1));
        pres2.setPresentationDate(Timestamp.from(Instant.now()));
        pres2.setPresentationType(PresentationType.Final);
        assertTrue(presentationDAO.addPresentation(pres2), "No se pudo agregar presentación 2");
        testPresentations.add(pres2);


        List<Presentation> dbPresentations = presentationDAO.getAllPresentations();
        assertEquals(2, dbPresentations.size(), "Debe haber 2 presentaciones en la BD");
    }

    @Test
    void testAddPresentation_Success() throws SQLException {
        Presentation newPresentation = new Presentation();
        newPresentation.setStudent(testStudents.get(0));
        newPresentation.setPresentationDate(Timestamp.from(Instant.now()));
        newPresentation.setPresentationType(PresentationType.Parcial);

        int initialCount = presentationDAO.countPresentations();
        boolean result = presentationDAO.addPresentation(newPresentation);

        assertTrue(result);
        assertEquals(initialCount + 1, presentationDAO.countPresentations());

        Presentation addedPresentation = presentationDAO.getPresentationById(newPresentation.getIdPresentation());
        assertNotNull(addedPresentation);
        assertEquals(PresentationType.Parcial, addedPresentation.getPresentationType());
        assertEquals(testStudents.get(0).getIdUser(), addedPresentation.getStudent().getIdUser());
    }

    @Test
    void testAddPresentation_NullFields_ShouldThrowException() {
        Presentation invalidPresentation = new Presentation();
        invalidPresentation.setStudent(null);
        invalidPresentation.setPresentationType(null);
        invalidPresentation.setPresentationDate(null);

        assertThrows(SQLException.class,
                () -> presentationDAO.addPresentation(invalidPresentation));
    }


    @Test
    void testGetAllPresentations_WithData() throws SQLException {
        List<Presentation> presentations = presentationDAO.getAllPresentations();
        assertEquals(testPresentations.size(), presentations.size());

        for (Presentation testPresentation : testPresentations) {
            boolean found = presentations.stream()
                    .anyMatch(p -> p.getPresentationType() == testPresentation.getPresentationType() &&
                            p.getStudent().getIdUser() == testPresentation.getStudent().getIdUser());
            assertTrue(found, "No se encontró la presentación esperada");
        }
    }

    @Test
    void testGetPresentationById_Exists() throws SQLException {
        assertFalse(testPresentations.isEmpty(), "No hay presentaciones de prueba disponibles");
        Presentation testPresentation = testPresentations.get(0);
        assertTrue(presentationDAO.presentationExists(testPresentation.getIdPresentation()),
                "La presentación de prueba no existe en la base de datos");
        Presentation foundPresentation = presentationDAO.getPresentationById(testPresentation.getIdPresentation());
        if (foundPresentation == null) {
            System.out.println("=== DEBUG INFO ===");
            System.out.println("Buscando presentación con ID: " + testPresentation.getIdPresentation());
            System.out.println("Presentaciones en BD:");
            presentationDAO.getAllPresentations().forEach(p ->
                    System.out.println("ID: " + p.getIdPresentation() +
                            ", Estudiante: " + p.getStudent().getIdUser() +
                            ", Tipo: " + p.getPresentationType())
            );
        }
        assertNotNull(foundPresentation, "No se encontró la presentación en la base de datos");
        assertEquals(testPresentation.getPresentationType(), foundPresentation.getPresentationType());
        assertEquals(testPresentation.getStudent().getIdUser(), foundPresentation.getStudent().getIdUser());
    }

    @Test
    void testGetPresentationById_NotExists() throws SQLException {
        Presentation foundPresentation = presentationDAO.getPresentationById(9999);
        assertNull(foundPresentation);
    }

    @Test
    void testGetPresentationsByStudent() throws SQLException {
        Student student = testStudents.get(0);
        List<Presentation> presentations = presentationDAO.getPresentationsByStudent(student.getIdUser());

        assertFalse(presentations.isEmpty());
        for (Presentation presentation : presentations) {
            assertEquals(student.getIdUser(), presentation.getStudent().getIdUser());
        }
    }

    @Test
    void testGetPresentationsByType() throws SQLException {
        List<Presentation> presentations = presentationDAO.getPresentationsByType(PresentationType.Parcial.name());

        assertFalse(presentations.isEmpty());
        for (Presentation presentation : presentations) {
            assertEquals(PresentationType.Parcial, presentation.getPresentationType());
        }
    }

    @Test
    void testUpdatePresentation_Success() throws SQLException {
        assertFalse(testPresentations.isEmpty(), "No hay presentaciones de prueba");
        Presentation originalPresentation = testPresentations.get(0);
        int originalId = originalPresentation.getIdPresentation();
        assertTrue(originalId > 0, "ID de presentación inválido");

        Presentation presentationToUpdate = presentationDAO.getPresentationById(originalId);
        assertNotNull(presentationToUpdate, "No se encontró la presentación en BD");

        PresentationType newType = PresentationType.Final;
        Timestamp testDate = new Timestamp(System.currentTimeMillis());
        testDate.setNanos(0);
        presentationToUpdate.setPresentationType(newType);
        presentationToUpdate.setPresentationDate(testDate);

        boolean result = presentationDAO.updatePresentation(presentationToUpdate);
        assertTrue(result, "La actualización falló");

        Presentation updatedPresentation = presentationDAO.getPresentationById(originalId);
        assertNotNull(updatedPresentation, "No se pudo recuperar la presentación actualizada");

        assertEquals(newType, updatedPresentation.getPresentationType(),
                "El tipo de presentación no se actualizó correctamente");
        assertEquals(testDate, updatedPresentation.getPresentationDate(),
                "La fecha no se actualizó correctamente");
        assertEquals(originalPresentation.getStudent().getIdUser(),
                updatedPresentation.getStudent().getIdUser(),
                "El estudiante asociado cambió incorrectamente");
    }

    @Test
    void testUpdatePresentation_NotExists() throws SQLException {
        Presentation nonExistentPresentation = new Presentation();
        nonExistentPresentation.setIdPresentation(9999);
        nonExistentPresentation.setStudent(testStudents.get(0));
        nonExistentPresentation.setPresentationType(PresentationType.Parcial);

        boolean result = presentationDAO.updatePresentation(nonExistentPresentation);
        assertFalse(result);
    }

    @Test
    void testDeletePresentation_Success() throws SQLException {
        Presentation testPresentation = createTestPresentation(
                testStudents.get(0),
                Timestamp.from(Instant.now()),
                PresentationType.Parcial
        );

        int presentationId = testPresentation.getIdPresentation();

        assertTrue(presentationDAO.presentationExists(presentationId),
                "La presentación debe existir antes de eliminarla");

        int countBefore = presentationDAO.countPresentations();

        boolean result = presentationDAO.deletePresentation(presentationId);
        assertTrue(result, "La eliminación debe devolver true cuando tiene éxito");

        assertEquals(countBefore - 1, presentationDAO.countPresentations(),
                "El conteo de presentaciones debe disminuir en 1");

        assertFalse(presentationDAO.presentationExists(presentationId),
                "La presentación no debe existir después de eliminarla");

        assertNull(presentationDAO.getPresentationById(presentationId),
                "No se debe poder recuperar una presentación eliminada");
    }

    @Test
    void testDeletePresentation_NotExists() throws SQLException {
        int initialCount = presentationDAO.countPresentations();
        boolean result = presentationDAO.deletePresentation(9999);

        assertFalse(result);
        assertEquals(initialCount, presentationDAO.countPresentations());
    }

    @Test
    void testPresentationExists_True() throws SQLException {
        Presentation testPresentation = testPresentations.get(0);
        assertTrue(presentationDAO.presentationExists(testPresentation.getIdPresentation()));
    }

    @Test
    void testPresentationExists_False() throws SQLException {
        assertFalse(presentationDAO.presentationExists(9999));
    }

    @Test
    void testCountPresentations_WithData() throws SQLException {
        int count = presentationDAO.countPresentations();
        assertEquals(testPresentations.size(), count);

        Presentation extraPresentation = new Presentation();
        extraPresentation.setStudent(testStudents.get(0));
        extraPresentation.setPresentationDate(Timestamp.from(Instant.now()));
        extraPresentation.setPresentationType(PresentationType.Parcial);
        presentationDAO.addPresentation(extraPresentation);

        assertEquals(count + 1, presentationDAO.countPresentations());
    }
}