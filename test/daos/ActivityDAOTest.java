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

        try (PreparedStatement preparedStatement = testConnection.prepareStatement(
                "INSERT INTO usuario (nombre_completo, telefono, estado) VALUES (?, ?, 'A')",
                Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, "Estudiante Test");
            preparedStatement.setString(2, "5551234567");
            preparedStatement.executeUpdate();
            try (ResultSet rs = preparedStatement.getGeneratedKeys()) {
                if (rs.next()) {
                    testStudentId = rs.getInt(1);
                }
            }
        }
        try (PreparedStatement preparedStatement = testConnection.prepareStatement(
                "INSERT INTO estudiante (id_usuario, matricula, calificacion) VALUES (?, ?, ?)")) {
            preparedStatement.setInt(1, testStudentId);
            preparedStatement.setString(2, "S0001");
            preparedStatement.setInt(3, 0);
            preparedStatement.executeUpdate();
        }

        testCronogram = new ActivityCronogram();
        testCronogram.setDateStart(Timestamp.from(Instant.now()));
        testCronogram.setDateEnd(Timestamp.from(Instant.now().plusSeconds(604800)));
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
        try (Statement statement = testConnection.createStatement()) {
            statement.execute("DELETE FROM actividad");
            statement.execute("ALTER TABLE actividad AUTO_INCREMENT = 1");
        }

        if (testCronogram == null || !cronogramDAO.cronogramExists(testCronogram.getIdCronogram())) {
            testCronogram = new ActivityCronogram();
            testCronogram.setDateStart(Timestamp.from(Instant.now()));
            testCronogram.setDateEnd(Timestamp.from(Instant.now().plusSeconds(604800)));
            cronogramDAO.addCronogram(testCronogram);
        }

        try (PreparedStatement preparedStatement = testConnection.prepareStatement(
                "INSERT IGNORE INTO usuario (id_usuario, nombre_completo, telefono, estado) VALUES (?, ?, ?, 'A')")) {
            preparedStatement.setInt(1, testStudentId);
            preparedStatement.setString(2, "Estudiante Test");
            preparedStatement.setString(3, "5551234567");
            preparedStatement.executeUpdate();
        }
        try (PreparedStatement preparedStatement = testConnection.prepareStatement(
                "INSERT IGNORE INTO estudiante (id_usuario, matricula, calificacion) VALUES (?, ?, ?)")) {
            preparedStatement.setInt(1, testStudentId);
            preparedStatement.setString(2, "S0001");
            preparedStatement.setInt(3, 0);
            preparedStatement.executeUpdate();
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

        boolean result = activityDAO.addActivity(newActivity);
        assertTrue(result);
    }

    @Test
    void testAddActivity_NullFields_ShouldThrowException() {
        Activity invalidActivity = new Activity();
        invalidActivity.setNameActivity(null);
        invalidActivity.setDescriptionActivity(null);
        invalidActivity.setStartDate(null);
        invalidActivity.setEndDate(null);
        invalidActivity.setActivityStatus(null);

        assertThrows(IllegalArgumentException.class,
                () -> activityDAO.addActivity(invalidActivity));
    }

    @Test
    void testGetActivityById_NotExists() throws SQLException {
        Activity foundActivity = activityDAO.getActivityById(9999);
        assertEquals(-1, foundActivity.getIdActivity());
        assertEquals("", foundActivity.getNameActivity());
        assertEquals("", foundActivity.getDescriptionActivity());
        assertNull(foundActivity.getStartDate());
        assertNull(foundActivity.getEndDate());
        assertEquals(ActivityStatus.NONE, foundActivity.getActivityStatus());
    }

    @Test
    void testGetAllActivities_WithData() throws SQLException {
        List<Activity> activities = activityDAO.getAllActivities();
        assertEquals(testActivities.size(), activities.size());
    }

    @Test
    void testGetActivitiesByStatus() throws SQLException {
        List<Activity> pendingActivities = activityDAO.getActivitiesByStatus(ActivityStatus.Pendiente);
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
        int studentId = testStudentId;

        boolean result = activityDAO.assignActivityToStudent(testActivity.getIdActivity(), studentId);
        assertTrue(result);
    }

    @Test
    void testAssignActivityToCronogram_Success() throws SQLException {
        Activity testActivity = testActivities.get(0);
        int cronogramId = testCronogram.getIdCronogram();

        boolean result = activityDAO.assignActivityToCronogram(testActivity.getIdActivity(), cronogramId);
        assertTrue(result);
    }
}
