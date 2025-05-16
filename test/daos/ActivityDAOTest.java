package daos;

import dataaccess.ConnectionDataBase;
import logic.daos.ActivityDAO;
import logic.logicclasses.Activity;
import logic.enums.ActivityStatus;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActivityDAOTest {
    private static ActivityDAO activityDAO;
    private static Connection testConnection;
    private static List<Activity> testActivities;

    @BeforeAll
    static void setUpAll() throws SQLException {
        activityDAO = new ActivityDAO();
        testConnection = ConnectionDataBase.getConnection();

        try (var statement = testConnection.createStatement()) {
            statement.execute("DELETE FROM actividad");
            statement.execute("ALTER TABLE actividad AUTO_INCREMENT = 1");

            statement.execute("CREATE TABLE IF NOT EXISTS actividad (" +
                    "id_actividad INT AUTO_INCREMENT PRIMARY KEY, " +
                    "nombre VARCHAR(200) NOT NULL, " +
                    "descripcion TEXT NOT NULL, " +
                    "fecha_inicial DATE NOT NULL, " +
                    "fecha_terminal DATE NOT NULL, " +
                    "estado ENUM('Pendiente','En progreso','Completada','Cancelada') NOT NULL DEFAULT 'Pendiente', " +
                    "id_usuario INT, " +
                    "id_cronograma INT)");
        }

        testActivities = new ArrayList<>();
        testActivities.add(createTestActivity(
                "Revisión de documentos",
                "Revisar documentos del proyecto",
                Timestamp.from(Instant.now()),
                Timestamp.from(Instant.now().plusSeconds(86400)),
                ActivityStatus.fromDbValue("Pendiente")));

        testActivities.add(createTestActivity(
                "Presentación final",
                "Preparar presentación para la entrega",
                Timestamp.from(Instant.now().plusSeconds(172800)),
                Timestamp.from(Instant.now().plusSeconds(259200)),
                ActivityStatus.fromDbValue("En progreso")));

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

    @AfterAll
    static void tearDownAll() throws SQLException {
        if (testConnection != null && !testConnection.isClosed()) {
            testConnection.close();
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("DELETE FROM actividad");
            stmt.execute("ALTER TABLE actividad AUTO_INCREMENT = 1");
        }

        testActivities = new ArrayList<>();
        testActivities.add(createTestActivity(
                "Revisión de documentos",
                "Revisar documentos del proyecto",
                Timestamp.from(Instant.now()),
                Timestamp.from(Instant.now().plusSeconds(86400)),
                ActivityStatus.Pendiente));

        testActivities.add(createTestActivity(
                "Presentación final",
                "Preparar presentación para la entrega",
                Timestamp.from(Instant.now().plusSeconds(172800)),
                Timestamp.from(Instant.now().plusSeconds(259200)),
                ActivityStatus.En_progreso));
    }

    @Test
    void testAddActivity_Success() throws SQLException {
        Activity newActivity = new Activity();
        newActivity.setNameActivity("Nueva Actividad");
        newActivity.setDescriptionActivity("Descripción de la nueva actividad");
        newActivity.setStartDate(Timestamp.from(Instant.now()));
        newActivity.setEndDate(Timestamp.from(Instant.now().plusSeconds(86400)));
        newActivity.setActivityStatus(ActivityStatus.Pendiente);

        int initialCount = testActivities.size();
        boolean result = activityDAO.addActivity(newActivity);

        assertTrue(result);
        assertTrue(newActivity.getIdActivity() > 0);

        Activity addedActivity = activityDAO.getActivityById(newActivity.getIdActivity());
        assertNotNull(addedActivity);
        assertEquals("Nueva Actividad", addedActivity.getNameActivity());
        assertEquals("Descripción de la nueva actividad", addedActivity.getDescriptionActivity());
        assertEquals(ActivityStatus.Pendiente, addedActivity.getActivityStatus());
    }

    @Test
    void testAddActivity_NullFields_ShouldThrowException() {
        Activity invalidActivity = new Activity();
        invalidActivity.setNameActivity(null);
        invalidActivity.setDescriptionActivity(null);
        invalidActivity.setStartDate(null);
        invalidActivity.setEndDate(null);
        invalidActivity.setActivityStatus(null);

        assertThrows(SQLException.class,
                () -> activityDAO.addActivity(invalidActivity));
    }

    @Test
    void testGetActivityById_Exists() throws SQLException {
        Activity testActivity = testActivities.get(0);
        Activity foundActivity = activityDAO.getActivityById(testActivity.getIdActivity());

        assertNotNull(foundActivity);
        assertEquals(testActivity.getNameActivity(), foundActivity.getNameActivity());
        assertEquals(testActivity.getDescriptionActivity(), foundActivity.getDescriptionActivity());
        assertEquals(testActivity.getActivityStatus(), foundActivity.getActivityStatus());
    }

    @Test
    void testGetActivityById_NotExists() throws SQLException {
        Activity foundActivity = activityDAO.getActivityById(9999);
        assertNull(foundActivity);
    }

    @Test
    void testGetAllActivities_WithData() throws SQLException {
        List<Activity> activities = activityDAO.getAllActivities();
        assertEquals(testActivities.size(), activities.size());

        for (Activity testActivity : testActivities) {
            boolean found = activities.stream()
                    .anyMatch(a -> a.getIdActivity() == testActivity.getIdActivity());
            assertTrue(found, "No se encontró la actividad esperada");
        }
    }

    @Test
    void testGetActivitiesByStatus() throws SQLException {
        List<Activity> pendingActivities = activityDAO.getActivitiesByStatus(ActivityStatus.Pendiente);
        assertFalse(pendingActivities.isEmpty());

        for (Activity activity : pendingActivities) {
            assertEquals(ActivityStatus.Pendiente, activity.getActivityStatus());
        }
    }

    @Test
    void testUpdateActivity_Success() throws SQLException {
        Activity activityToUpdate = testActivities.get(0);
        activityToUpdate.setNameActivity("Nombre actualizado");
        activityToUpdate.setDescriptionActivity("Descripción actualizada");
        activityToUpdate.setActivityStatus(ActivityStatus.Completada);

        boolean result = activityDAO.updateActivity(activityToUpdate);
        assertTrue(result);

        Activity updatedActivity = activityDAO.getActivityById(activityToUpdate.getIdActivity());
        assertEquals("Nombre actualizado", updatedActivity.getNameActivity());
        assertEquals("Descripción actualizada", updatedActivity.getDescriptionActivity());
        assertEquals(ActivityStatus.Completada, updatedActivity.getActivityStatus());
    }

    @Test
    void testUpdateActivity_NotExists() throws SQLException {
        Activity nonExistentActivity = new Activity();
        nonExistentActivity.setIdActivity(9999);
        nonExistentActivity.setNameActivity("Actividad inexistente");
        nonExistentActivity.setDescriptionActivity("Esta actividad no existe");
        nonExistentActivity.setStartDate(Timestamp.from(Instant.now()));
        nonExistentActivity.setEndDate(Timestamp.from(Instant.now().plusSeconds(86400)));
        nonExistentActivity.setActivityStatus(ActivityStatus.Pendiente);

        boolean result = activityDAO.updateActivity(nonExistentActivity);
        assertFalse(result);
    }

    @Test
    void testDeleteActivity_Success() throws SQLException {
        Activity testActivity = testActivities.get(0);
        int activityId = testActivity.getIdActivity();

        boolean result = activityDAO.deleteActivity(activityId);
        assertTrue(result);
        assertFalse(activityDAO.activityExists(activityId));
    }

    @Test
    void testDeleteActivity_NotExists() throws SQLException {
        boolean result = activityDAO.deleteActivity(9999);
        assertFalse(result);
    }

    @Test
    void testChangeActivityStatus_Success() throws SQLException {
        Activity testActivity = testActivities.get(0);
        boolean result = activityDAO.changeActivityStatus(testActivity.getIdActivity(), ActivityStatus.Completada);
        assertTrue(result);

        Activity updatedActivity = activityDAO.getActivityById(testActivity.getIdActivity());
        assertEquals(ActivityStatus.Completada, updatedActivity.getActivityStatus());
    }

    @Test
    void testChangeActivityStatus_NotExists() throws SQLException {
        boolean result = activityDAO.changeActivityStatus(9999, ActivityStatus.Completada);
        assertFalse(result);
    }

    @Test
    void testActivityExists_True() throws SQLException {
        Activity testActivity = testActivities.get(0);
        assertTrue(activityDAO.activityExists(testActivity.getIdActivity()));
    }

    @Test
    void testActivityExists_False() throws SQLException {
        assertFalse(activityDAO.activityExists(9999));
    }

    @Test
    void testAssignActivityToStudent_Success() throws SQLException {
        Activity testActivity = testActivities.get(0);
        int studentId = 1;

        boolean result = activityDAO.assignActivityToStudent(testActivity.getIdActivity(), studentId);
        assertTrue(result);

        Activity assignedActivity = activityDAO.getActivityById(testActivity.getIdActivity());
    }

    @Test
    void testAssignActivityToCronogram_Success() throws SQLException {
        Activity testActivity = testActivities.get(0);
        int cronogramId = 1;

        boolean result = activityDAO.assignActivityToCronogram(testActivity.getIdActivity(), cronogramId);
        assertTrue(result);

        Activity assignedActivity = activityDAO.getActivityById(testActivity.getIdActivity());
    }
}