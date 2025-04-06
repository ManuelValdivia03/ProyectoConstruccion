package dataacces;

import logic.User;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserDAOTest {
    private static UserDAO userDAO;
    private static Connection testConnection;
    private User testUser;

    @BeforeAll
    static void setUpAll() throws SQLException {
        // Configuración inicial para todas las pruebas
        userDAO = new UserDAO();
        testConnection = ConnectionDataBase.getConnection();

        // Crear tabla de prueba si no existe
        try (var statement = testConnection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS usuario (" +
                    "id_usuario INT AUTO_INCREMENT PRIMARY KEY, " +
                    "nombre_completo VARCHAR(100) NOT NULL, " +
                    "telefono VARCHAR(20))");
        }
    }

    @AfterAll
    static void tearDownAll() throws SQLException {
        // Limpieza final
        if (testConnection != null && !testConnection.isClosed()) {
            testConnection.close();
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        // Datos de prueba iniciales
        testUser = new User();
        testUser.setFullName("Juan Pérez");
        testUser.setCellphone("5551234567");
        userDAO.addUser(testUser);
    }

    @AfterEach
    void tearDown() throws SQLException {
        // Limpiar la tabla después de cada prueba
        try (var statement = testConnection.createStatement()) {
            statement.execute("DELETE FROM usuario");
            statement.execute("ALTER TABLE usuario AUTO_INCREMENT = 1");
        }
    }

    // Pruebas para addUser()
    @Test
    void testAddUser_Success() throws SQLException {
        User newUser = new User();
        newUser.setFullName("María García");
        newUser.setCellphone("5559876543");

        boolean result = userDAO.addUser(newUser);

        assertTrue(result);
        assertTrue(newUser.getIdUser() > 0);
    }

    @Test
    void testAddUser_NullName_ShouldThrowException() {
        User invalidUser = new User();
        invalidUser.setFullName(null);
        invalidUser.setCellphone("5551112233");

        assertThrows(SQLException.class, () -> userDAO.addUser(invalidUser));
    }

    // Pruebas para getAllUsers()
    @Test
    void testGetAllUsers_WithData() throws SQLException {
        List<User> users = userDAO.getAllUsers();

        assertEquals(1, users.size());
        assertEquals("Juan Pérez", users.get(0).getFullName());
    }

    @Test
    void testGetAllUsers_Empty() throws SQLException {
        // Limpiar datos primero
        try (var statement = testConnection.createStatement()) {
            statement.execute("DELETE FROM usuario");
        }

        List<User> users = userDAO.getAllUsers();
        assertTrue(users.isEmpty());
    }

    // Pruebas para getUserById()
    @Test
    void testGetUserById_Exists() throws SQLException {
        User foundUser = userDAO.getUserById(testUser.getIdUser());

        assertNotNull(foundUser);
        assertEquals(testUser.getFullName(), foundUser.getFullName());
        assertEquals(testUser.getCellphone(), foundUser.getCellphone());
    }

    @Test
    void testGetUserById_NotExists() throws SQLException {
        User foundUser = userDAO.getUserById(9999);
        assertNull(foundUser);
    }

    // Pruebas para updateUser()
    @Test
    void testUpdateUser_Success() throws SQLException {
        testUser.setFullName("Nombre Actualizado");
        testUser.setCellphone("5550000000");

        boolean result = userDAO.updateUser(testUser);
        assertTrue(result);

        User updatedUser = userDAO.getUserById(testUser.getIdUser());
        assertEquals("Nombre Actualizado", updatedUser.getFullName());
    }

    @Test
    void testUpdateUser_NotExists() throws SQLException {
        User nonExistentUser = new User();
        nonExistentUser.setIdUser(9999);
        nonExistentUser.setFullName("No existe");

        boolean result = userDAO.updateUser(nonExistentUser);
        assertFalse(result);
    }

    // Pruebas para deleteUser()
    @Test
    void testDeleteUser_Success() throws SQLException {
        boolean result = userDAO.deleteUser(testUser.getIdUser());
        assertTrue(result);
        assertNull(userDAO.getUserById(testUser.getIdUser()));
    }

    @Test
    void testDeleteUser_NotExists() throws SQLException {
        boolean result = userDAO.deleteUser(9999);
        assertFalse(result);
    }

    // Pruebas para searchUsersByName()
    @Test
    void testSearchUsersByName_Match() throws SQLException {
        List<User> results = userDAO.searchUsersByName("Juan");
        assertEquals(1, results.size());
        assertEquals(testUser.getIdUser(), results.get(0).getIdUser());
    }

    @Test
    void testSearchUsersByName_NoMatch() throws SQLException {
        List<User> results = userDAO.searchUsersByName("XYZ");
        assertTrue(results.isEmpty());
    }

    @Test
    void testSearchUsersByName_PartialMatch() throws SQLException {
        List<User> results = userDAO.searchUsersByName("Pér");
        assertEquals(1, results.size());
    }

    // Pruebas para userExists()
    @Test
    void testUserExists_True() throws SQLException {
        assertTrue(userDAO.userExists(testUser.getIdUser()));
    }

    @Test
    void testUserExists_False() throws SQLException {
        assertFalse(userDAO.userExists(9999));
    }

    // Pruebas para countUsers()
    @Test
    void testCountUsers_WithData() throws SQLException {
        assertEquals(1, userDAO.countUsers());

        User newUser = new User();
        newUser.setFullName("Ana López");
        userDAO.addUser(newUser);

        assertEquals(2, userDAO.countUsers());
    }

    @Test
    void testCountUsers_Empty() throws SQLException {
        // Limpiar datos primero
        try (var statement = testConnection.createStatement()) {
            statement.execute("DELETE FROM usuario");
        }

        assertEquals(0, userDAO.countUsers());
    }
}