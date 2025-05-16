package daos;

import dataaccess.ConnectionDataBase;
import logic.daos.ActivityDAO;
import logic.daos.ActivityCronogramDAO;
import logic.logicclasses.Activity;
import logic.logicclasses.ActivityCronogram;
import logic.enums.ActivityStatus;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActivityDAOTest {
    private static ActivityDAO activityDAO;
    private static ActivityCronogramDAO cronogramDAO;
    private static Connection testConnection;
    private static List<Activity> testActivities;
    private static ActivityCronogram testCronogram;
    private static int testStudentId;

    @BeforeAll
    static void setUpAll() throws SQLException {
        activityDAO = new ActivityDAO();
        cronogramDAO = new ActivityCronogramDAO();
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

        // Crea usuario y estudiante de prueba y guarda su ID
        try (PreparedStatement psUser = testConnection.prepareStatement(
                "INSERT INTO usuario (nombre_completo, telefono, estado) VALUES (?, ?, 'A')",
                Statement.RETURN_GENERATED_KEYS)) {
            psUser.setString(1, "Estudiante Test");
            psUser.setString(2, "5551234567");
            psUser.executeUpdate();
            try (ResultSet rs = psUser.getGeneratedKeys()) {
                if (rs.next()) {
                    testStudentId = rs.getInt(1);
                }
            }
        }
        try (PreparedStatement psEst = testConnection.prepareStatement(
                "INSERT INTO estudiante (id_usuario, matricula, calificacion) VALUES (?, ?, ?)")) {
            psEst.setInt(1, testStudentId);
            psEst.setString(2, "S0001");
            psEst.setInt(3, 0);
            psEst.executeUpdate();
        }

        // Usa el DAO para crear un cronograma real
        testCronogram = new ActivityCronogram();
        testCronogram.setDateStart(Timestamp.from(Instant.now()));
        testCronogram.setDateEnd(Timestamp.from(Instant.now().plusSeconds(604800))); // +7 días
        cronogramDAO.addCronogram(testCronogram);

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

        // Asegura que el cronograma de prueba exista antes de cada prueba usando el DAO
        if (testCronogram == null || !cronogramDAO.cronogramExists(testCronogram.getIdCronogram())) {
            testCronogram = new ActivityCronogram();
            testCronogram.setDateStart(Timestamp.from(Instant.now()));
            testCronogram.setDateEnd(Timestamp.from(Instant.now().plusSeconds(604800)));
            cronogramDAO.addCronogram(testCronogram);
        }

        // Asegura que el estudiante de prueba exista antes de cada prueba
        try (PreparedStatement psUser = testConnection.prepareStatement(
                "INSERT IGNORE INTO usuario (id_usuario, nombre_completo, telefono, estado) VALUES (?, ?, ?, 'A')")) {
            psUser.setInt(1, testStudentId);
            psUser.setString(2, "Estudiante Test");
            psUser.setString(3, "5551234567");
            psUser.executeUpdate();
        }
        try (PreparedStatement psEst = testConnection.prepareStatement(
                "INSERT IGNORE INTO estudiante (id_usuario, matricula, calificacion) VALUES (?, ?, ?)")) {
            psEst.setInt(1, testStudentId);
            psEst.setString(2, "S0001");
            psEst.setInt(3, 0);
            psEst.executeUpdate();
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
        // Usa el id real del estudiante de prueba
        int studentId = testStudentId;

        boolean result = activityDAO.assignActivityToStudent(testActivity.getIdActivity(), studentId);
        assertTrue(result);

        Activity assignedActivity = activityDAO.getActivityById(testActivity.getIdActivity());
        // Puedes agregar más asserts si tu modelo lo permite
    }

    @Test
    void testAssignActivityToCronogram_Success() throws SQLException {
        Activity testActivity = testActivities.get(0);
        int cronogramId = testCronogram.getIdCronogram();

        boolean result = activityDAO.assignActivityToCronogram(testActivity.getIdActivity(), cronogramId);
        assertTrue(result);

        Activity assignedActivity = activityDAO.getActivityById(testActivity.getIdActivity());
        // Puedes agregar más asserts si tu modelo lo permite
    }
}
