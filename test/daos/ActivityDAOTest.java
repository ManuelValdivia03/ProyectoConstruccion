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

        // Limpiar y crear la tabla de actividades
        try (var statement = testConnection.createStatement()) {
            statement.execute("DELETE FROM actividad");
            statement.execute("ALTER TABLE actividad AUTO_INCREMENT = 1");

            // Crear tabla de actividades si no existe
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

        // Crear actividades de prueba
        testActivities = new ArrayList<>();
        testActivities.add(createTestActivity(
                "Revisión de documentos",
                "Revisar documentos del proyecto",
                Timestamp.from(Instant.now()),
                Timestamp.from(Instant.now().plusSeconds(86400)),
                ActivityStatus.PENDIENTE));

        testActivities.add(createTestActivity(
                "Presentación final",
                "Preparar presentación para la entrega",
                Timestamp.from(Instant.now().plusSeconds(172800)),
                Timestamp.from(Instant.now().plusSeconds(259200)),
                ActivityStatus.EN_PROGRESO));
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
        // Limpiar actividades antes de cada prueba
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("DELETE FROM actividad");
            stmt.execute("ALTER TABLE actividad AUTO_INCREMENT = 1");
        }

        // Recrear actividades de prueba
        testActivities = new ArrayList<>();
        testActivities.add(createTestActivity(
                "Revisión de documentos",
                "Revisar documentos del proyecto",
                Timestamp.from(Instant.now()),
                Timestamp.from(Instant.now().plusSeconds(86400)),
                ActivityStatus.PENDIENTE));

        testActivities.add(createTestActivity(
                "Presentación final",
                "Preparar presentación para la entrega",
                Timestamp.from(Instant.now().plusSeconds(172800)),
                Timestamp.from(Instant.now().plusSeconds(259200)),
                ActivityStatus.EN_PROGRESO));
    }

    @Test
    void testAddActivity_Success() throws SQLException {
        Activity newActivity = new Activity();
        newActivity.setNameActivity("Nueva Actividad");
        newActivity.setDescriptionActivity("Descripción de la nueva actividad");
        newActivity.setStartDate(Timestamp.from(Instant.now()));
        newActivity.setEndDate(Timestamp.from(Instant.now().plusSeconds(86400)));
        newActivity.setActivityStatus(ActivityStatus.PENDIENTE);

        int initialCount = testActivities.size();
        boolean result = activityDAO.addActivity(newActivity);

        assertTrue(result);
        assertTrue(newActivity.getIdActivity() > 0);

        Activity addedActivity = activityDAO.getActivityById(newActivity.getIdActivity());
        assertNotNull(addedActivity);
        assertEquals("Nueva Actividad", addedActivity.getNameActivity());
        assertEquals("Descripción de la nueva actividad", addedActivity.getDescriptionActivity());
        assertEquals(ActivityStatus.PENDIENTE, addedActivity.getActivityStatus());
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
        List<Activity> pendingActivities = activityDAO.getActivitiesByStatus(ActivityStatus.PENDIENTE);
        assertFalse(pendingActivities.isEmpty());

        for (Activity activity : pendingActivities) {
            assertEquals(ActivityStatus.PENDIENTE, activity.getActivityStatus());
        }
    }

    @Test
    void testUpdateActivity_Success() throws SQLException {
        Activity activityToUpdate = testActivities.get(0);
        activityToUpdate.setNameActivity("Nombre actualizado");
        activityToUpdate.setDescriptionActivity("Descripción actualizada");
        activityToUpdate.setActivityStatus(ActivityStatus.COMPLETADA);

        boolean result = activityDAO.updateActivity(activityToUpdate);
        assertTrue(result);

        Activity updatedActivity = activityDAO.getActivityById(activityToUpdate.getIdActivity());
        assertEquals("Nombre actualizado", updatedActivity.getNameActivity());
        assertEquals("Descripción actualizada", updatedActivity.getDescriptionActivity());
        assertEquals(ActivityStatus.COMPLETADA, updatedActivity.getActivityStatus());
    }

    @Test
    void testUpdateActivity_NotExists() throws SQLException {
        Activity nonExistentActivity = new Activity();
        nonExistentActivity.setIdActivity(9999);
        nonExistentActivity.setNameActivity("Actividad inexistente");
        nonExistentActivity.setDescriptionActivity("Esta actividad no existe");
        nonExistentActivity.setStartDate(Timestamp.from(Instant.now()));
        nonExistentActivity.setEndDate(Timestamp.from(Instant.now().plusSeconds(86400)));
        nonExistentActivity.setActivityStatus(ActivityStatus.PENDIENTE);

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
        boolean result = activityDAO.changeActivityStatus(testActivity.getIdActivity(), ActivityStatus.COMPLETADA);
        assertTrue(result);

        Activity updatedActivity = activityDAO.getActivityById(testActivity.getIdActivity());
        assertEquals(ActivityStatus.COMPLETADA, updatedActivity.getActivityStatus());
    }

    @Test
    void testChangeActivityStatus_NotExists() throws SQLException {
        boolean result = activityDAO.changeActivityStatus(9999, ActivityStatus.COMPLETADA);
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
        int studentId = 1; // ID de estudiante de prueba

        boolean result = activityDAO.assignActivityToStudent(testActivity.getIdActivity(), studentId);
        assertTrue(result);

        Activity assignedActivity = activityDAO.getActivityById(testActivity.getIdActivity());
        // Nota: Necesitarías un método getStudentId() en Activity para verificar esto completamente
    }

    @Test
    void testAssignActivityToCronogram_Success() throws SQLException {
        Activity testActivity = testActivities.get(0);
        int cronogramId = 1; // ID de cronograma de prueba

        boolean result = activityDAO.assignActivityToCronogram(testActivity.getIdActivity(), cronogramId);
        assertTrue(result);

        Activity assignedActivity = activityDAO.getActivityById(testActivity.getIdActivity());
        // Nota: Necesitarías un método getCronogramId() en Activity para verificar esto completamente
    }
}