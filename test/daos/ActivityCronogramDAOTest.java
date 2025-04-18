package daos;

import dataaccess.ConnectionDataBase;
import logic.daos.ActivityCronogramDAO;
import logic.daos.ActivityDAO;
import logic.logicclasses.ActivityCronogram;
import logic.logicclasses.Activity;
import logic.enums.ActivityStatus;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.text.SimpleDateFormat;
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

        // Limpiar y crear tablas necesarias
        try (var statement = testConnection.createStatement()) {
            // Limpiar tablas relacionadas
            statement.execute("DELETE FROM cronograma_actividad");
            statement.execute("DELETE FROM cronograma_actividades");
            statement.execute("DELETE FROM actividad");

            // Resetear auto-incrementos
            statement.execute("ALTER TABLE cronograma_actividades AUTO_INCREMENT = 1");
            statement.execute("ALTER TABLE actividad AUTO_INCREMENT = 1");

            // Crear tablas si no existen
            statement.execute("CREATE TABLE IF NOT EXISTS cronograma_actividades (" +
                    "id_cronograma INT AUTO_INCREMENT PRIMARY KEY, " +
                    "fecha_inicial DATE NOT NULL, " +
                    "fecha_terminal DATE NOT NULL)");

            statement.execute("CREATE TABLE IF NOT EXISTS cronograma_actividad (" +
                    "id_cronograma INT NOT NULL, " +
                    "id_actividad INT NOT NULL, " +
                    "PRIMARY KEY (id_cronograma, id_actividad))");

            statement.execute("CREATE TABLE IF NOT EXISTS actividad (" +
                    "id_actividad INT AUTO_INCREMENT PRIMARY KEY, " +
                    "nombre VARCHAR(200) NOT NULL, " +
                    "descripcion TEXT NOT NULL, " +
                    "fecha_inicial DATE NOT NULL, " +
                    "fecha_terminal DATE NOT NULL, " +
                    "estado ENUM('Pendiente','En progreso','Completada','Cancelada') NOT NULL DEFAULT 'Pendiente')");
        }

        // Crear datos de prueba
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

        // Asignar actividades al cronograma
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
        // Limpiar datos antes de cada prueba
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("DELETE FROM cronograma_actividad");
            stmt.execute("DELETE FROM cronograma_actividades");
            stmt.execute("DELETE FROM actividad");
            stmt.execute("ALTER TABLE cronograma_actividades AUTO_INCREMENT = 1");
            stmt.execute("ALTER TABLE actividad AUTO_INCREMENT = 1");
        }

        // Recrear datos de prueba
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

        // Usa fechas sin hora (o con hora 00:00:00)
        Timestamp startDate = Timestamp.valueOf("2025-04-17 00:00:00");
        Timestamp endDate = Timestamp.valueOf("2025-04-24 00:00:00");

        newCronogram.setDateStart(startDate);
        newCronogram.setDateEnd(endDate);
        newCronogram.setActivities(new ArrayList<>());

        boolean result = cronogramDAO.addCronogram(newCronogram);
        assertTrue(result);

        ActivityCronogram addedCronogram = cronogramDAO.getCronogramById(newCronogram.getIdCronogram());
        assertNotNull(addedCronogram);

        assertEquals(
                new SimpleDateFormat("yyyy-MM-dd").format(newCronogram.getDateStart()),
                new SimpleDateFormat("yyyy-MM-dd").format(addedCronogram.getDateStart())
        );

        assertEquals(
                new SimpleDateFormat("yyyy-MM-dd").format(newCronogram.getDateEnd()),
                new SimpleDateFormat("yyyy-MM-dd").format(addedCronogram.getDateEnd())
        );
    }

    @Test
    void testAddCronogram_NullDates_ShouldThrowException() {
        ActivityCronogram invalidCronogram = new ActivityCronogram();
        invalidCronogram.setDateStart(null);
        invalidCronogram.setDateEnd(null);

        assertThrows(SQLException.class,
                () -> cronogramDAO.addCronogram(invalidCronogram));
    }

    @Test
    void testGetCronogramById_Exists() throws SQLException {
        ActivityCronogram testCronogram = testCronograms.get(0);
        ActivityCronogram foundCronogram = cronogramDAO.getCronogramById(testCronogram.getIdCronogram());

        assertNotNull(foundCronogram);

        assertEquals(
                testCronogram.getDateStart().toLocalDateTime().toLocalDate(),
                foundCronogram.getDateStart().toLocalDateTime().toLocalDate()
        );

        assertEquals(
                testCronogram.getDateEnd().toLocalDateTime().toLocalDate(),
                foundCronogram.getDateEnd().toLocalDateTime().toLocalDate()
        );

        assertEquals(
                testCronogram.getActivities().size(),
                foundCronogram.getActivities().size()
        );
    }

    @Test
    void testGetCronogramById_NotExists() throws SQLException {
        ActivityCronogram foundCronogram = cronogramDAO.getCronogramById(9999);
        assertNull(foundCronogram);
    }

    @Test
    void testGetAllCronograms_WithData() throws SQLException {
        List<ActivityCronogram> cronograms = cronogramDAO.getAllCronograms();
        assertEquals(testCronograms.size(), cronograms.size());

        for (ActivityCronogram testCronogram : testCronograms) {
            boolean found = cronograms.stream()
                    .anyMatch(c -> c.getIdCronogram() == testCronogram.getIdCronogram());
            assertTrue(found, "No se encontró el cronograma esperado");
        }
    }

    @Test
    void testUpdateCronogram_Success() throws SQLException {
        ActivityCronogram cronogramToUpdate = testCronograms.get(0);

        // Crear fechas con hora 00:00:00 para que coincidan con el tipo DATE de la BD
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate nextWeek = LocalDate.now().plusDays(8);

        Timestamp newStartDate = Timestamp.valueOf(tomorrow.atStartOfDay());
        Timestamp newEndDate = Timestamp.valueOf(nextWeek.atStartOfDay());

        cronogramToUpdate.setDateStart(newStartDate);
        cronogramToUpdate.setDateEnd(newEndDate);

        boolean result = cronogramDAO.updateCronogram(cronogramToUpdate);
        assertTrue(result);

        ActivityCronogram updatedCronogram = cronogramDAO.getCronogramById(cronogramToUpdate.getIdCronogram());

        assertEquals(
                newStartDate.toLocalDateTime().toLocalDate(),
                updatedCronogram.getDateStart().toLocalDateTime().toLocalDate()
        );

        assertEquals(
                newEndDate.toLocalDateTime().toLocalDate(),
                updatedCronogram.getDateEnd().toLocalDateTime().toLocalDate()
        );
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
        assertFalse(cronogramDAO.cronogramExists(cronogramId));
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

        ActivityCronogram updatedCronogram = cronogramDAO.getCronogramById(cronogram.getIdCronogram());
        assertTrue(updatedCronogram.getActivities().stream()
                .anyMatch(a -> a.getIdActivity() == newActivity.getIdActivity()));
    }

    @Test
    void testRemoveActivityFromCronogram_Success() throws SQLException {
        ActivityCronogram cronogram = testCronograms.get(0);
        Activity activityToRemove = cronogram.getActivities().get(0);

        boolean result = cronogramDAO.removeActivityFromCronogram(
                cronogram.getIdCronogram(), activityToRemove.getIdActivity());
        assertTrue(result);

        ActivityCronogram updatedCronogram = cronogramDAO.getCronogramById(cronogram.getIdCronogram());
        assertFalse(updatedCronogram.getActivities().stream()
                .anyMatch(a -> a.getIdActivity() == activityToRemove.getIdActivity()));
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