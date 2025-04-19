package daos;

import dataaccess.ConnectionDataBase;
import logic.daos.SelfEvaluationDAO;
import logic.daos.StudentDAO;
import logic.logicclasses.SelfEvaluation;
import logic.logicclasses.Student;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SelfEvaluationDAOTest {
    private static SelfEvaluationDAO selfEvaluationDAO;
    private static StudentDAO studentDAO;
    private static Connection testConnection;
    private static List<SelfEvaluation> testSelfEvaluations;
    private static Student testStudent;

    @BeforeAll
    static void setUpAll() throws SQLException {
        selfEvaluationDAO = new SelfEvaluationDAO();
        studentDAO = new StudentDAO();
        testConnection = ConnectionDataBase.getConnection();

        // Limpiar tablas
        try (var statement = testConnection.createStatement()) {
            statement.execute("DELETE FROM reporte");
            statement.execute("DELETE FROM autoevaluacion");
            statement.execute("DELETE FROM estudiante");
            statement.execute("ALTER TABLE reporte AUTO_INCREMENT = 1");
            statement.execute("ALTER TABLE autoevaluacion AUTO_INCREMENT = 1");
            statement.execute("ALTER TABLE estudiante AUTO_INCREMENT = 1");
        }

        // Crear estudiante de prueba
        testStudent = new Student();
        testStudent.setFullName("Estudiante Prueba");
        studentDAO.addStudent(testStudent);

        // Crear autoevaluaciones de prueba
        testSelfEvaluations = List.of(
                createTestSelfEvaluation("Buen desempeño", 8.5f, testStudent),
                createTestSelfEvaluation("Puede mejorar", 6.0f, testStudent)
        );
    }

    private static SelfEvaluation createTestSelfEvaluation(String feedback, float calification,
                                                           Student student) throws SQLException {
        SelfEvaluation evaluation = new SelfEvaluation();
        evaluation.setFeedBack(feedback);
        evaluation.setCalification(calification);
        evaluation.setStudent(student);
        selfEvaluationDAO.addSelfEvaluation(evaluation);
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
        // Limpiar autoevaluaciones antes de cada prueba
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("DELETE FROM autoevaluacion");
            stmt.execute("ALTER TABLE autoevaluacion AUTO_INCREMENT = 1");
        }

        // Recrear autoevaluaciones de prueba
        testSelfEvaluations = List.of(
                createTestSelfEvaluation("Buen desempeño", 8.5f, testStudent),
                createTestSelfEvaluation("Puede mejorar", 6.0f, testStudent)
        );
    }

    @Test
    void testAddSelfEvaluation_Success() throws SQLException {
        SelfEvaluation newEvaluation = new SelfEvaluation();
        newEvaluation.setFeedBack("Excelente trabajo");
        newEvaluation.setCalification(9.5f);
        newEvaluation.setStudent(testStudent);

        int initialCount = selfEvaluationDAO.countSelfEvaluations();
        boolean result = selfEvaluationDAO.addSelfEvaluation(newEvaluation);

        assertTrue(result);
        assertEquals(initialCount + 1, selfEvaluationDAO.countSelfEvaluations());
        assertTrue(newEvaluation.getIdSelfEvaluation() > 0);

        SelfEvaluation addedEvaluation = selfEvaluationDAO.getSelfEvaluationById(newEvaluation.getIdSelfEvaluation());
        assertNotNull(addedEvaluation);
        assertEquals("Excelente trabajo", addedEvaluation.getFeedBack());
        assertEquals(9.5f, addedEvaluation.getCalification(), 0.001);
        assertEquals(testStudent.getIdUser(), addedEvaluation.getStudent().getIdUser());
    }

    @Test
    void testAddSelfEvaluation_NullFields_ShouldThrowException() {
        SelfEvaluation invalidEvaluation = new SelfEvaluation();
        invalidEvaluation.setFeedBack(null);
        invalidEvaluation.setCalification(7.0f);
        invalidEvaluation.setStudent(testStudent);

        assertThrows(SQLException.class,
                () -> selfEvaluationDAO.addSelfEvaluation(invalidEvaluation));
    }

    @Test
    void testGetSelfEvaluationById_Exists() throws SQLException {
        SelfEvaluation testEvaluation = testSelfEvaluations.get(0);
        SelfEvaluation foundEvaluation = selfEvaluationDAO.getSelfEvaluationById(testEvaluation.getIdSelfEvaluation());

        assertNotNull(foundEvaluation);
        assertEquals(testEvaluation.getFeedBack(), foundEvaluation.getFeedBack());
        assertEquals(testEvaluation.getCalification(), foundEvaluation.getCalification(), 0.001);
        assertEquals(testEvaluation.getStudent().getIdUser(), foundEvaluation.getStudent().getIdUser());
    }

    @Test
    void testGetSelfEvaluationById_NotExists() throws SQLException {
        SelfEvaluation foundEvaluation = selfEvaluationDAO.getSelfEvaluationById(9999);
        assertNull(foundEvaluation);
    }

    @Test
    void testGetAllSelfEvaluations_WithData() throws SQLException {
        List<SelfEvaluation> evaluations = selfEvaluationDAO.getAllSelfEvaluations();
        assertEquals(testSelfEvaluations.size(), evaluations.size());

        for (SelfEvaluation testEvaluation : testSelfEvaluations) {
            boolean found = evaluations.stream()
                    .anyMatch(e -> e.getIdSelfEvaluation() == testEvaluation.getIdSelfEvaluation());
            assertTrue(found, "No se encontró la autoevaluación esperada");
        }
    }

    @Test
    void testGetSelfEvaluationsByStudent_Exists() throws SQLException {
        List<SelfEvaluation> evaluations = selfEvaluationDAO.getSelfEvaluationsByStudent(testStudent.getIdUser());

        assertEquals(testSelfEvaluations.size(), evaluations.size());
        for (SelfEvaluation evaluation : evaluations) {
            assertEquals(testStudent.getIdUser(), evaluation.getStudent().getIdUser());
        }
    }

    @Test
    void testGetSelfEvaluationsByStudent_NotExists() throws SQLException {
        List<SelfEvaluation> evaluations = selfEvaluationDAO.getSelfEvaluationsByStudent(9999);
        assertTrue(evaluations.isEmpty());
    }

    @Test
    void testUpdateSelfEvaluation_Success() throws SQLException {
        SelfEvaluation evaluationToUpdate = testSelfEvaluations.get(0);
        evaluationToUpdate.setFeedBack("Feedback actualizado");
        evaluationToUpdate.setCalification(9.0f);

        boolean result = selfEvaluationDAO.updateSelfEvaluation(evaluationToUpdate);
        assertTrue(result);

        SelfEvaluation updatedEvaluation = selfEvaluationDAO.getSelfEvaluationById(evaluationToUpdate.getIdSelfEvaluation());
        assertEquals("Feedback actualizado", updatedEvaluation.getFeedBack());
        assertEquals(9.0f, updatedEvaluation.getCalification(), 0.001);
    }

    @Test
    void testUpdateSelfEvaluation_NotExists() throws SQLException {
        SelfEvaluation nonExistentEvaluation = new SelfEvaluation();
        nonExistentEvaluation.setIdSelfEvaluation(9999);
        nonExistentEvaluation.setFeedBack("No existe");
        nonExistentEvaluation.setCalification(5.0f);
        nonExistentEvaluation.setStudent(testStudent);

        boolean result = selfEvaluationDAO.updateSelfEvaluation(nonExistentEvaluation);
        assertFalse(result);
    }

    @Test
    void testDeleteSelfEvaluation_Success() throws SQLException {
        SelfEvaluation testEvaluation = testSelfEvaluations.get(0);
        int evalId = testEvaluation.getIdSelfEvaluation();

        int countBefore = selfEvaluationDAO.countSelfEvaluations();
        boolean result = selfEvaluationDAO.deleteSelfEvaluation(evalId);

        assertTrue(result);
        assertEquals(countBefore - 1, selfEvaluationDAO.countSelfEvaluations());
        assertFalse(selfEvaluationDAO.selfEvaluationExists(evalId));
    }

    @Test
    void testDeleteSelfEvaluation_NotExists() throws SQLException {
        int initialCount = selfEvaluationDAO.countSelfEvaluations();
        boolean result = selfEvaluationDAO.deleteSelfEvaluation(9999);

        assertFalse(result);
        assertEquals(initialCount, selfEvaluationDAO.countSelfEvaluations());
    }

    @Test
    void testSelfEvaluationExists_True() throws SQLException {
        SelfEvaluation testEvaluation = testSelfEvaluations.get(0);
        assertTrue(selfEvaluationDAO.selfEvaluationExists(testEvaluation.getIdSelfEvaluation()));
    }

    @Test
    void testSelfEvaluationExists_False() throws SQLException {
        assertFalse(selfEvaluationDAO.selfEvaluationExists(9999));
    }

    @Test
    void testCountSelfEvaluations_WithData() throws SQLException {
        int count = selfEvaluationDAO.countSelfEvaluations();
        assertEquals(testSelfEvaluations.size(), count);

        SelfEvaluation extraEvaluation = new SelfEvaluation();
        extraEvaluation.setFeedBack("Extra evaluación");
        extraEvaluation.setCalification(7.5f);
        extraEvaluation.setStudent(testStudent);
        selfEvaluationDAO.addSelfEvaluation(extraEvaluation);

        assertEquals(count + 1, selfEvaluationDAO.countSelfEvaluations());
    }
}