package daos;

import dataaccess.ConnectionDataBase;
import logic.daos.ActivityCronogramDAO;
import logic.daos.ActivityDAO;
import logic.logicclasses.ActivityCronogram;
import logic.logicclasses.Activity;
import logic.enums.ActivityStatus;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActivityCronogramDAOTest {
    private static ActivityCronogramDAO cronogramDAO;
    private static ActivityDAO activityDAO;
    private static Connection testConnection;
    private static List<ActivityCronogram> testCronograms;
    private static List<Activity> testActivities;

    @BeforeAll
    static void setUpAll() throws SQLException {
        cronogramDAO = new ActivityCronogramDAO();
        activityDAO = new ActivityDAO();
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
            statement.execute("TRUNCATE TABLE estudiante_cronograma");
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

        testActivities = new ArrayList<>();
        testActivities.add(createTestActivity(
                "Revisión inicial",
                "Revisar documentos iniciales",
                Timestamp.from(Instant.now()),
                Timestamp.from(Instant.now().plusSeconds(86400)),
                ActivityStatus.Pendiente));

        testActivities.add(createTestActivity(
                "Presentación final",
                "Preparar presentación de resultados",
                Timestamp.from(Instant.now().plusSeconds(172800)),
                Timestamp.from(Instant.now().plusSeconds(259200)),
                ActivityStatus.Pendiente));

        testCronograms = new ArrayList<>();
        testCronograms.add(createTestCronogram(
                Timestamp.from(Instant.now()),
                Timestamp.from(Instant.now().plusSeconds(259200)),
                testActivities));
    }

    private static Activity createTestActivity(String name, String description,
                                               Timestamp startDate, Timestamp endDate, ActivityStatus status) throws SQLException {
        Activity activity = new Activity();
        activity.setNameActivity(name);
        activity.setDescriptionActivity(description);
        activity.setStartDate(startDate);
        activity.setEndDate(endDate);
        activity.setActivityStatus(status);
        activityDAO.addActivity(activity);
        return activity;
    }

    private static ActivityCronogram createTestCronogram(Timestamp startDate, Timestamp endDate,
                                                         List<Activity> activities) throws SQLException {
        ActivityCronogram cronogram = new ActivityCronogram();
        cronogram.setDateStart(startDate);
        cronogram.setDateEnd(endDate);
        cronogram.setActivities(activities);
        cronogramDAO.addCronogram(cronogram);

        for (Activity activity : activities) {
            cronogramDAO.addActivityToCronogram(cronogram.getIdCronogram(), activity.getIdActivity());
        }

        return cronogram;
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
            statement.execute("DELETE FROM cronograma_actividad");
            statement.execute("DELETE FROM cronograma_actividades");
            statement.execute("DELETE FROM actividad");
            statement.execute("DELETE FROM estudiante_cronograma");
            statement.execute("ALTER TABLE cronograma_actividades AUTO_INCREMENT = 1");
            statement.execute("ALTER TABLE actividad AUTO_INCREMENT = 1");
        }

        testActivities = new ArrayList<>();
        testActivities.add(createTestActivity(
                "Revisión inicial",
                "Revisar documentos iniciales",
                Timestamp.from(Instant.now()),
                Timestamp.from(Instant.now().plusSeconds(86400)),
                ActivityStatus.Pendiente));

        testActivities.add(createTestActivity(
                "Presentación final",
                "Preparar presentación de resultados",
                Timestamp.from(Instant.now().plusSeconds(172800)),
                Timestamp.from(Instant.now().plusSeconds(259200)),
                ActivityStatus.Pendiente));

        testCronograms = new ArrayList<>();
        testCronograms.add(createTestCronogram(
                Timestamp.from(Instant.now()),
                Timestamp.from(Instant.now().plusSeconds(259200)),
                testActivities));
    }

    @Test
    void testAddCronogram_Success() throws SQLException {
        ActivityCronogram newCronogram = new ActivityCronogram();
        Timestamp startDate = Timestamp.valueOf("2025-04-17 00:00:00");
        Timestamp endDate = Timestamp.valueOf("2025-04-24 00:00:00");
        newCronogram.setDateStart(startDate);
        newCronogram.setDateEnd(endDate);
        newCronogram.setActivities(new ArrayList<>());
        boolean result = cronogramDAO.addCronogram(newCronogram);
        assertTrue(result);
    }

    @Test
    void testAddCronogram_NullDates_ShouldThrowException() {
        ActivityCronogram invalidCronogram = new ActivityCronogram();
        invalidCronogram.setDateStart(null);
        invalidCronogram.setDateEnd(null);
        assertThrows(NullPointerException.class,
                () -> cronogramDAO.addCronogram(invalidCronogram));
    }

    @Test
    void testGetCronogramById_Exists() throws SQLException {
        ActivityCronogram testCronogram = testCronograms.get(0);
        ActivityCronogram foundCronogram = cronogramDAO.getCronogramById(testCronogram.getIdCronogram());
        assertEquals(testCronogram.getIdCronogram(), foundCronogram.getIdCronogram());
    }

    @Test
    void testGetCronogramById_NotExists() throws SQLException {
        ActivityCronogram foundCronogram = cronogramDAO.getCronogramById(9999);
        assertEquals(0, foundCronogram.getIdCronogram());
    }

    @Test
    void testGetAllCronograms_WithData() throws SQLException {
        List<ActivityCronogram> cronograms = cronogramDAO.getAllCronograms();
        assertEquals(testCronograms.size(), cronograms.size());
    }

    @Test
    void testUpdateCronogram_Success() throws SQLException {
        ActivityCronogram cronogramToUpdate = testCronograms.get(0);
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate nextWeek = LocalDate.now().plusDays(8);
        Timestamp newStartDate = Timestamp.valueOf(tomorrow.atStartOfDay());
        Timestamp newEndDate = Timestamp.valueOf(nextWeek.atStartOfDay());
        cronogramToUpdate.setDateStart(newStartDate);
        cronogramToUpdate.setDateEnd(newEndDate);
        boolean result = cronogramDAO.updateCronogram(cronogramToUpdate);
        assertTrue(result);
    }

    @Test
    void testUpdateCronogram_NotExists() throws SQLException {
        ActivityCronogram nonExistentCronogram = new ActivityCronogram();
        nonExistentCronogram.setIdCronogram(9999);
        nonExistentCronogram.setDateStart(Timestamp.from(Instant.now()));
        nonExistentCronogram.setDateEnd(Timestamp.from(Instant.now().plusSeconds(86400)));
        boolean result = cronogramDAO.updateCronogram(nonExistentCronogram);
        assertFalse(result);
    }

    @Test
    void testDeleteCronogram_Success() throws SQLException {
        ActivityCronogram testCronogram = testCronograms.get(0);
        int cronogramId = testCronogram.getIdCronogram();
        boolean result = cronogramDAO.deleteCronogram(cronogramId);
        assertTrue(result);
    }

    @Test
    void testDeleteCronogram_NotExists() throws SQLException {
        boolean result = cronogramDAO.deleteCronogram(9999);
        assertFalse(result);
    }

    @Test
    void testAddActivityToCronogram_Success() throws SQLException {
        ActivityCronogram cronogram = testCronograms.get(0);
        Activity newActivity = createTestActivity(
                "Nueva actividad",
                "Descripción nueva",
                Timestamp.from(Instant.now()),
                Timestamp.from(Instant.now().plusSeconds(86400)),
                ActivityStatus.Pendiente);
        boolean result = cronogramDAO.addActivityToCronogram(cronogram.getIdCronogram(), newActivity.getIdActivity());
        assertTrue(result);
    }

    @Test
    void testRemoveActivityFromCronogram_Success() throws SQLException {
        ActivityCronogram cronogram = testCronograms.get(0);
        Activity activityToRemove = cronogram.getActivities().get(0);
        boolean result = cronogramDAO.removeActivityFromCronogram(
                cronogram.getIdCronogram(), activityToRemove.getIdActivity());
        assertTrue(result);
    }

    @Test
    void testCronogramExists_True() throws SQLException {
        ActivityCronogram testCronogram = testCronograms.get(0);
        assertTrue(cronogramDAO.cronogramExists(testCronogram.getIdCronogram()));
    }

    @Test
    void testCronogramExists_False() throws SQLException {
        assertFalse(cronogramDAO.cronogramExists(9999));
    }
}