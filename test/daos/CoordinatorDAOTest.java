package daos;

import dataaccess.ConnectionDataBase;
import logic.daos.CoordinatorDAO;
import logic.daos.UserDAO;
import logic.logicclasses.Coordinator;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CoordinatorDAOTest {
    private static CoordinatorDAO coordinatorDAO;
    private static UserDAO userDAO;
    private static Connection testConnection;
    private static List<Coordinator> testCoordinators;

    @BeforeAll
    static void setUpAll() throws SQLException {
        coordinatorDAO = new CoordinatorDAO();
        userDAO = new UserDAO();
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

        testCoordinators = List.of(
                createTestCoordinator("C001", "Coordinador 1", "5551111111"),
                createTestCoordinator("C002", "Coordinador 2", "5552222222"),
                createTestCoordinator("C003", "Coordinador 3", "5553333333")
        );
    }

    private static Coordinator createTestCoordinator(String staffNumber, String fullName, String phone) throws SQLException {
        Coordinator coordinator = new Coordinator();
        coordinator.setStaffNumber(staffNumber);
        coordinator.setFullName(fullName);
        coordinator.setCellphone(phone);
        coordinatorDAO.addCoordinator(coordinator);
        return coordinator;
    }

    @AfterAll
    static void tearDownAll() throws SQLException {
        if (testConnection != null && !testConnection.isClosed()) {
            testConnection.close();
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        try (var statement = testConnection.createStatement()) {
            statement.execute("DELETE FROM reporte");
            statement.execute("DELETE FROM coordinador");
            statement.execute("DELETE FROM usuario");
            statement.execute("ALTER TABLE usuario AUTO_INCREMENT = 1");
        }

        for (Coordinator coordinator : testCoordinators) {
            Coordinator newCoordinator = new Coordinator();
            newCoordinator.setStaffNumber(coordinator.getStaffNumber());
            newCoordinator.setFullName(coordinator.getFullName());
            newCoordinator.setCellphone(coordinator.getCellPhone());
            coordinatorDAO.addCoordinator(newCoordinator);
        }
    }

    @Test
    void testAddCoordinator_Success() throws SQLException {
        Coordinator newCoordinator = new Coordinator();
        newCoordinator.setStaffNumber("C004");
        newCoordinator.setFullName("Nuevo Coordinador");
        newCoordinator.setCellphone("5554444444");
        int initialCount = coordinatorDAO.countCoordinators();
        boolean result = coordinatorDAO.addCoordinator(newCoordinator);
        assertTrue(result);
        assertEquals(initialCount + 1, coordinatorDAO.countCoordinators());
    }

    @Test
    void testAddCoordinator_DuplicateStaffNumber_ShouldThrowException() {
        Coordinator duplicateCoordinator = new Coordinator();
        duplicateCoordinator.setStaffNumber("C001");
        duplicateCoordinator.setFullName("Coordinador Duplicado");
        duplicateCoordinator.setCellphone("5555555555");
        assertThrows(SQLException.class, () -> coordinatorDAO.addCoordinator(duplicateCoordinator));
    }

    @Test
    void testAddCoordinator_NullCoordinator_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> coordinatorDAO.addCoordinator(null));
    }

    @Test
    void testAddCoordinator_NullName_ShouldThrowException() {
        Coordinator invalidCoordinator = new Coordinator();
        invalidCoordinator.setStaffNumber("C005");
        invalidCoordinator.setFullName(null);
        invalidCoordinator.setCellphone("5556666666");
        assertThrows(SQLException.class, () -> coordinatorDAO.addCoordinator(invalidCoordinator));
    }

    @Test
    void testAddCoordinator_NullStaffNumber_ShouldThrowException() {
        Coordinator invalidCoordinator = new Coordinator();
        invalidCoordinator.setStaffNumber(null);
        invalidCoordinator.setFullName("Nombre");
        invalidCoordinator.setCellphone("5556666666");
        assertThrows(SQLException.class, () -> coordinatorDAO.addCoordinator(invalidCoordinator));
    }

    @Test
    void testGetAllCoordinators_WithData() throws SQLException {
        List<Coordinator> coordinators = coordinatorDAO.getAllCoordinators();
        assertEquals(testCoordinators.size(), coordinators.size());
    }

    @Test
    void testGetAllCoordinators_EmptyTable() throws SQLException {
        try (var statement = testConnection.createStatement()) {
            statement.execute("DELETE FROM coordinador");
            statement.execute("DELETE FROM usuario");
        }
        List<Coordinator> coordinators = coordinatorDAO.getAllCoordinators();
        assertTrue(coordinators.isEmpty());
        setUp();
    }

    @Test
    void testGetCoordinatorByStaffNumber_Exists() throws SQLException {
        Coordinator testCoordinator = testCoordinators.get(0);
        Coordinator foundCoordinator = coordinatorDAO.getCoordinatorByStaffNumber(testCoordinator.getStaffNumber());
        assertEquals(testCoordinator.getIdUser(), foundCoordinator.getIdUser());
        assertEquals(testCoordinator.getFullName(), foundCoordinator.getFullName());
        assertEquals(testCoordinator.getCellPhone(), foundCoordinator.getCellPhone());
        assertEquals(testCoordinator.getPhoneExtension(), foundCoordinator.getPhoneExtension());
        assertEquals(testCoordinator.getStaffNumber(), foundCoordinator.getStaffNumber());
        assertEquals(testCoordinator.getStatus(), foundCoordinator.getStatus());
    }

    @Test
    void testGetCoordinatorByStaffNumber_NotExists() throws SQLException {
        Coordinator foundCoordinator = coordinatorDAO.getCoordinatorByStaffNumber("NOEXISTE");
        assertEquals(-1, foundCoordinator.getIdUser());
        assertEquals("", foundCoordinator.getFullName());
        assertEquals("", foundCoordinator.getCellPhone());
        assertEquals("", foundCoordinator.getPhoneExtension());
        assertEquals("", foundCoordinator.getStaffNumber());
        assertEquals('I', foundCoordinator.getStatus());
    }

    @Test
    void testGetCoordinatorByStaffNumber_NullOrEmpty() throws SQLException {
        assertThrows(IllegalArgumentException.class, () -> coordinatorDAO.getCoordinatorByStaffNumber(null));
        assertThrows(IllegalArgumentException.class, () -> coordinatorDAO.getCoordinatorByStaffNumber(""));
    }

    @Test
    void testUpdateCoordinator_Success() throws SQLException {
        Coordinator coordinatorToUpdate = coordinatorDAO.getCoordinatorByStaffNumber("C001");
        coordinatorToUpdate.setFullName("Nombre Actualizado");
        coordinatorToUpdate.setCellphone("5559999999");
        coordinatorToUpdate.setStaffNumber("C001UPD");
        boolean result = coordinatorDAO.updateCoordinator(coordinatorToUpdate);
        assertTrue(result);
    }

    @Test
    void testUpdateCoordinator_NotExists() throws SQLException {
        Coordinator nonExistentCoordinator = new Coordinator();
        nonExistentCoordinator.setIdUser(9999);
        nonExistentCoordinator.setFullName("No existe");
        nonExistentCoordinator.setStaffNumber("NOEXISTE");
        nonExistentCoordinator.setCellphone("5550000000");
        boolean result = coordinatorDAO.updateCoordinator(nonExistentCoordinator);
        assertFalse(result);
    }

    @Test
    void testUpdateCoordinator_NullCoordinator_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> coordinatorDAO.updateCoordinator(null));
    }

    @Test
    void testUpdateCoordinator_NullName_ShouldThrowException() {
        Coordinator invalidCoordinator = new Coordinator();
        invalidCoordinator.setIdUser(1);
        invalidCoordinator.setStaffNumber("C006");
        invalidCoordinator.setFullName(null);
        invalidCoordinator.setCellphone("5558888888");
        assertThrows(IllegalArgumentException.class, () -> coordinatorDAO.updateCoordinator(invalidCoordinator));
    }

    @Test
    void testDeleteCoordinator_Success() throws SQLException {
        Coordinator coordinatorToDelete = coordinatorDAO.getCoordinatorByStaffNumber("C001");
        int initialCount = coordinatorDAO.countCoordinators();
        boolean result = coordinatorDAO.deleteCoordinator(coordinatorToDelete);
        assertTrue(result);
        assertEquals(initialCount - 1, coordinatorDAO.countCoordinators());
    }

    @Test
    void testDeleteCoordinator_NotExists() throws SQLException {
        Coordinator nonExistentCoordinator = new Coordinator();
        nonExistentCoordinator.setIdUser(9999);
        nonExistentCoordinator.setStaffNumber("NOEXISTE");
        int initialCount = coordinatorDAO.countCoordinators();
        boolean result = coordinatorDAO.deleteCoordinator(nonExistentCoordinator);
        assertFalse(result);
        assertEquals(initialCount, coordinatorDAO.countCoordinators());
    }

    @Test
    void testDeleteCoordinator_NullCoordinator() throws SQLException {
        assertThrows(IllegalArgumentException.class, () -> coordinatorDAO.deleteCoordinator(null));
    }

    @Test
    void testCoordinatorExists_True() throws SQLException {
        assertTrue(coordinatorDAO.coordinatorExists("C001"));
    }

    @Test
    void testCoordinatorExists_False() throws SQLException {
        assertFalse(coordinatorDAO.coordinatorExists("NOEXISTE"));
    }

    @Test
    void testCoordinatorExists_NullOrEmpty() throws SQLException {
        assertThrows(IllegalArgumentException.class, () -> coordinatorDAO.coordinatorExists(null));
        assertThrows(IllegalArgumentException.class, () -> coordinatorDAO.coordinatorExists(""));
    }

    @Test
    void testCountCoordinators_WithData() throws SQLException {
        int count = coordinatorDAO.countCoordinators();
        assertEquals(testCoordinators.size(), count);
    }

    @Test
    void testCountCoordinators_EmptyTable() throws SQLException {
        try (var statement = testConnection.createStatement()) {
            statement.execute("DELETE FROM coordinador");
            statement.execute("DELETE FROM usuario");
        }
        assertEquals(0, coordinatorDAO.countCoordinators());
        setUp();
    }
}
