package daos;

import dataaccess.ConnectionDataBase;
import logic.daos.*;
import logic.logicclasses.Evaluation;
import logic.logicclasses.Academic;
import logic.logicclasses.Presentation;
import logic.logicclasses.Student;
import logic.enums.AcademicType;
import logic.enums.PresentationType;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EvaluationDAOTest {
    private static EvaluationDAO evaluationDAO;
    private static PresentationDAO presentationDAO;
    private static AcademicDAO academicDAO;
    private static StudentDAO studentDAO;
    private static Connection testConnection;
    private static List<Evaluation> testEvaluations;
    private static List<Presentation> testPresentations;
    private static List<Academic> testAcademics;
    private static List<Student> testStudents;

    @BeforeAll
    static void setUpAll() throws SQLException {
        evaluationDAO = new EvaluationDAO();
        presentationDAO = new PresentationDAO();
        academicDAO = new AcademicDAO();
        studentDAO = new StudentDAO();
        testConnection = ConnectionDataBase.getConnection();

        // Limpiar completamente la base de datos
        try (var statement = testConnection.createStatement()) {
            // Eliminar tablas en el orden correcto (primero las que tienen FK)
            statement.execute("DELETE FROM evaluacion");
            statement.execute("DELETE FROM presentacion");
            statement.execute("DELETE FROM academico");
            statement.execute("DELETE FROM estudiante");
            statement.execute("DELETE FROM usuario");

            // Resetear auto-incrementos
            statement.execute("ALTER TABLE usuario AUTO_INCREMENT = 1");
            statement.execute("ALTER TABLE estudiante AUTO_INCREMENT = 1");
            statement.execute("ALTER TABLE presentacion AUTO_INCREMENT = 1");
            statement.execute("ALTER TABLE academico AUTO_INCREMENT = 1");
            statement.execute("ALTER TABLE evaluacion AUTO_INCREMENT = 1");

            // Crear tablas (versión actualizada)
            statement.execute("CREATE TABLE IF NOT EXISTS usuario (" +
                    "id_usuario INT AUTO_INCREMENT PRIMARY KEY, " +
                    "nombre_completo VARCHAR(100) NOT NULL, " +
                    "telefono VARCHAR(20), " +
                    "estado CHAR(1) DEFAULT 'A')");

            statement.execute("CREATE TABLE IF NOT EXISTS estudiante (" +
                    "id_usuario INT PRIMARY KEY, " +
                    "matricula VARCHAR(50) NOT NULL UNIQUE, " +
                    "FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario))");

            statement.execute("CREATE TABLE IF NOT EXISTS academico (" +
                    "id_usuario INT PRIMARY KEY, " +
                    "numero_personal VARCHAR(50) NOT NULL UNIQUE, " +
                    "FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario))");

            statement.execute("CREATE TABLE IF NOT EXISTS presentacion (" +
                    "id_presentacion INT AUTO_INCREMENT PRIMARY KEY, " +
                    "fecha TIMESTAMP NOT NULL, " +
                    "tipo enum('Final', 'Parcial') NOT NULL, " +
                    "id_estudiante INT NOT NULL, " +
                    "FOREIGN KEY (id_estudiante) REFERENCES estudiante(id_usuario))");

            statement.execute("CREATE TABLE IF NOT EXISTS evaluacion (" +
                    "id_evaluacion INT AUTO_INCREMENT PRIMARY KEY, " +
                    "calificacion INT NOT NULL, " +
                    "comentarios TEXT, " +
                    "fecha TIMESTAMP NOT NULL, " +
                    "id_academicoevaluador INT NOT NULL, " +
                    "id_presentacion INT NOT NULL, " +
                    "FOREIGN KEY (id_academicoevaluador) REFERENCES academico(id_usuario), " +
                    "FOREIGN KEY (id_presentacion) REFERENCES presentacion(id_presentacion))");
        }

        // Crear estudiantes
        testStudents = new ArrayList<>();
        testStudents.add(createTestStudent("S001", "Estudiante 1", "5551111111"));
        testStudents.add(createTestStudent("S002", "Estudiante 2", "5552222222"));

        // Crear académicos
        testAcademics = new ArrayList<>();
        testAcademics.add(createTestAcademic(1, "Académico 1", "5553333333",'A', "12345", AcademicType.Evaluador ));
        testAcademics.add(createTestAcademic(2, "Académico 2", "5553333333", 'A', "123456", AcademicType.Evaluador));

        // Crear presentaciones
        testPresentations = new ArrayList<>();
        testPresentations.add(createTestPresentation(
                testStudents.get(0),
                PresentationType.valueOf(PresentationType.Parcial.toString()),
                Timestamp.from(Instant.now())

        ));
        testPresentations.add(createTestPresentation(
                testStudents.get(1),
                PresentationType.valueOf(PresentationType.Final.toString()),
                Timestamp.from(Instant.now())
        ));

        // Crear evaluaciones
        testEvaluations = new ArrayList<>();
        testEvaluations.add(createTestEvaluation(
                testAcademics.get(0),
                testPresentations.get(0),
                85,
                "Buen trabajo",
                Timestamp.from(Instant.now())
        ));
        testEvaluations.add(createTestEvaluation(
                testAcademics.get(1),
                testPresentations.get(1),
                92,
                "Excelente presentación",
                Timestamp.from(Instant.now())
        ));

        // Verificaciones iniciales
        assertEquals(2, evaluationDAO.countEvaluations());
    }

    private static Student createTestStudent(String enrollment, String fullName, String phone) throws SQLException {
        Student student = new Student();
        student.setEnrollment(enrollment);
        student.setFullName(fullName);
        student.setCellphone(phone);
        studentDAO.addStudent(student);
        return student;
    }

    private static Academic createTestAcademic(int idAcademic, String fullName, String phone, char status, String staffNumber, AcademicType type) throws SQLException {
        Academic academic = new Academic();
        academic.setIdUser(idAcademic);
        academic.setFullName(fullName);
        academic.setCellphone(phone);
        academic.setStatus(status);
        academic.setStaffNumber(staffNumber);
        academic.setAcademicType(type);
        academicDAO.addAcademic(academic);
        return academic;
    }

    private static Presentation createTestPresentation(Student student, PresentationType type, Timestamp date) throws SQLException {
        Presentation presentation = new Presentation();
        presentation.setPresentationType(type);
        presentation.setPresentationDate(date);
        presentation.setStudent(student);
        presentationDAO.addPresentation(presentation);
        return presentation;
    }

    private static Evaluation createTestEvaluation(Academic academic, Presentation presentation,
                                                   int calification, String comments, Timestamp date) throws SQLException {
        Evaluation evaluation = new Evaluation();
        evaluation.setAcademic(academic);
        evaluation.setPresentation(presentation);
        evaluation.setCalification(calification);
        evaluation.setDescription(comments);
        evaluation.setEvaluationDate(date);
        evaluationDAO.addEvaluation(evaluation);
        return evaluation;
    }

    @AfterAll
    static void tearDownAll() throws SQLException {
        if (testConnection != null && !testConnection.isClosed()) {
            testConnection.close();
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        // Limpiar evaluaciones
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("DELETE FROM evaluacion");
            stmt.execute("ALTER TABLE evaluacion AUTO_INCREMENT = 1");
        }

        // Recrear evaluaciones de prueba
        testEvaluations = new ArrayList<>();
        testEvaluations.add(createTestEvaluation(
                testAcademics.get(0),
                testPresentations.get(0),
                85,
                "Buen trabajo",
                Timestamp.from(Instant.now())
        ));
        testEvaluations.add(createTestEvaluation(
                testAcademics.get(1),
                testPresentations.get(1),
                92,
                "Excelente presentación",
                Timestamp.from(Instant.now())
        ));
    }

    @Test
    void testAddEvaluation_Success() throws SQLException {
        Evaluation newEvaluation = new Evaluation();
        newEvaluation.setAcademic(testAcademics.get(0));
        newEvaluation.setPresentation(testPresentations.get(0));
        newEvaluation.setCalification(88);
        newEvaluation.setDescription("Muy buen desempeño");
        newEvaluation.setEvaluationDate(Timestamp.from(Instant.now()));

        int initialCount = evaluationDAO.countEvaluations();
        boolean result = evaluationDAO.addEvaluation(newEvaluation);

        assertTrue(result);
        assertEquals(initialCount + 1, evaluationDAO.countEvaluations());

        Evaluation addedEvaluation = evaluationDAO.getEvaluationById(newEvaluation.getIdEvaluation());
        assertNotNull(addedEvaluation);
        assertEquals(88, addedEvaluation.getCalification());
        assertEquals("Muy buen desempeño", addedEvaluation.getDescription());
    }

    @Test
    void testAddEvaluation_NullFields_ShouldThrowException() {
        Evaluation invalidEvaluation = new Evaluation();
        invalidEvaluation.setAcademic(null);
        invalidEvaluation.setPresentation(null);
        invalidEvaluation.setCalification(0);
        invalidEvaluation.setDescription(null);
        invalidEvaluation.setEvaluationDate(null);

        assertThrows(SQLException.class,
                () -> evaluationDAO.addEvaluation(invalidEvaluation));
    }

    @Test
    void testGetEvaluationById_Exists() throws SQLException {
        Evaluation testEvaluation = testEvaluations.get(0);
        Evaluation foundEvaluation = evaluationDAO.getEvaluationById(testEvaluation.getIdEvaluation());

        assertNotNull(foundEvaluation);
        assertEquals(testEvaluation.getCalification(), foundEvaluation.getCalification());
        assertEquals(testEvaluation.getDescription(), foundEvaluation.getDescription());
        assertEquals(testEvaluation.getAcademic().getIdUser(), foundEvaluation.getAcademic().getIdUser());
        assertEquals(testEvaluation.getPresentation().getIdPresentation(), foundEvaluation.getPresentation().getIdPresentation());
    }

    @Test
    void testGetEvaluationById_NotExists() throws SQLException {
        Evaluation foundEvaluation = evaluationDAO.getEvaluationById(9999);
        assertNull(foundEvaluation);
    }

    @Test
    void testGetAllEvaluations_WithData() throws SQLException {
        List<Evaluation> evaluations = evaluationDAO.getAllEvaluations();
        assertEquals(testEvaluations.size(), evaluations.size());

        for (Evaluation testEvaluation : testEvaluations) {
            boolean found = evaluations.stream()
                    .anyMatch(e -> e.getIdEvaluation() == testEvaluation.getIdEvaluation());
            assertTrue(found, "No se encontró la evaluación esperada");
        }
    }

    @Test
    void testGetEvaluationsByAcademic() throws SQLException {
        Academic academic = testAcademics.get(0);
        List<Evaluation> evaluations = evaluationDAO.getEvaluationsByAcademic(academic.getIdUser());

        assertFalse(evaluations.isEmpty());
        for (Evaluation evaluation : evaluations) {
            assertEquals(academic.getIdUser(), evaluation.getAcademic().getIdUser());
        }
    }

    @Test
    void testGetEvaluationsByPresentation() throws SQLException {
        Presentation presentation = testPresentations.get(0);
        List<Evaluation> evaluations = evaluationDAO.getEvaluationsByPresentation(presentation.getIdPresentation());

        assertFalse(evaluations.isEmpty());
        for (Evaluation evaluation : evaluations) {
            assertEquals(presentation.getIdPresentation(), evaluation.getPresentation().getIdPresentation());
        }
    }

    @Test
    void testUpdateEvaluation_Success() throws SQLException {
        Evaluation evaluationToUpdate = testEvaluations.get(0);
        evaluationToUpdate.setCalification(90);
        evaluationToUpdate.setDescription("Mejoró significativamente");

        boolean result = evaluationDAO.updateEvaluation(evaluationToUpdate);
        assertTrue(result);

        Evaluation updatedEvaluation = evaluationDAO.getEvaluationById(evaluationToUpdate.getIdEvaluation());
        assertEquals(90, updatedEvaluation.getCalification());
        assertEquals("Mejoró significativamente", updatedEvaluation.getDescription());
    }

    @Test
    void testUpdateEvaluation_NotExists() throws SQLException {
        Evaluation nonExistentEvaluation = new Evaluation();
        nonExistentEvaluation.setIdEvaluation(9999);
        nonExistentEvaluation.setAcademic(testAcademics.get(0));
        nonExistentEvaluation.setPresentation(testPresentations.get(0));
        nonExistentEvaluation.setCalification(75);
        nonExistentEvaluation.setDescription("Evaluación ficticia");

        boolean result = evaluationDAO.updateEvaluation(nonExistentEvaluation);
        assertFalse(result);
    }

    @Test
    void testDeleteEvaluation_Success() throws SQLException {
        Evaluation testEvaluation = testEvaluations.get(0);
        int evaluationId = testEvaluation.getIdEvaluation();

        int countBefore = evaluationDAO.countEvaluations();
        boolean result = evaluationDAO.deleteEvaluation(evaluationId);

        assertTrue(result);
        assertEquals(countBefore - 1, evaluationDAO.countEvaluations());
        assertFalse(evaluationDAO.evaluationExists(evaluationId));
    }

    @Test
    void testDeleteEvaluation_NotExists() throws SQLException {
        int initialCount = evaluationDAO.countEvaluations();
        boolean result = evaluationDAO.deleteEvaluation(9999);

        assertFalse(result);
        assertEquals(initialCount, evaluationDAO.countEvaluations());
    }

    @Test
    void testEvaluationExists_True() throws SQLException {
        Evaluation testEvaluation = testEvaluations.get(0);
        assertTrue(evaluationDAO.evaluationExists(testEvaluation.getIdEvaluation()));
    }

    @Test
    void testEvaluationExists_False() throws SQLException {
        assertFalse(evaluationDAO.evaluationExists(9999));
    }

    @Test
    void testCountEvaluations_WithData() throws SQLException {
        int count = evaluationDAO.countEvaluations();
        assertEquals(testEvaluations.size(), count);

        Evaluation extraEvaluation = new Evaluation();
        extraEvaluation.setAcademic(testAcademics.get(0));
        extraEvaluation.setPresentation(testPresentations.get(0));
        extraEvaluation.setCalification(80);
        extraEvaluation.setDescription("Evaluación adicional");
        extraEvaluation.setEvaluationDate(Timestamp.from(Instant.now()));
        evaluationDAO.addEvaluation(extraEvaluation);

        assertEquals(count + 1, evaluationDAO.countEvaluations());
    }
}