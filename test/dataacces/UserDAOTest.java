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
    private static int initialUserCount;

    @BeforeAll
    static void setUpAll() throws SQLException {
        userDAO = new UserDAO();
        testConnection = ConnectionDataBase.getConnection();

        // Crear tabla si no existe
        try (var statement = testConnection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS usuario (" +
                    "id_usuario INT AUTO_INCREMENT PRIMARY KEY, " +
                    "nombre_completo VARCHAR(100) NOT NULL, " +
                    "telefono VARCHAR(20))");
        }

        // Obtener el conteo inicial de usuarios
        initialUserCount = userDAO.countUsers();
    }

    @AfterAll
    static void tearDownAll() throws SQLException {
        if (testConnection != null && !testConnection.isClosed()) {
            testConnection.close();
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        testUser = new User();
        testUser.setFullName("Juan Pérez");
        testUser.setCellphone("5551234567");
        userDAO.addUser(testUser);
    }

    @AfterEach
    void tearDown() throws SQLException {
        // Solo eliminar los usuarios creados en las pruebas
        try (var statement = testConnection.createStatement()) {
            statement.execute("DELETE FROM usuario WHERE id_usuario >= " + testUser.getIdUser());
            statement.execute("ALTER TABLE usuario AUTO_INCREMENT = " + (testUser.getIdUser()));
        }
    }

    @Test
    void testAddUser_Success() throws SQLException {
        User newUser = new User();
        newUser.setFullName("María García");
        newUser.setCellphone("5559876543");

        int initialCount = userDAO.countUsers();
        boolean result = userDAO.addUser(newUser);

        assertTrue(result);
        assertTrue(newUser.getIdUser() > 0);
        assertEquals(initialCount + 1, userDAO.countUsers());
    }

    @Test
    void testAddUser_NullName_ShouldThrowException() {
        User invalidUser = new User();
        invalidUser.setFullName(null);
        invalidUser.setCellphone("5551112233");

        assertThrows(SQLException.class, () -> userDAO.addUser(invalidUser));
    }

    @Test
    void testGetAllUsers_WithData() throws SQLException {
        List<User> users = userDAO.getAllUsers();
        assertTrue(users.size() > initialUserCount);

        // Verificar que nuestro usuario de prueba está en la lista
        boolean found = false;
        for (User user : users) {
            if (user.getIdUser() == testUser.getIdUser()) {
                found = true;
                assertEquals("Juan Pérez", user.getFullName());
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    void testGetUserById_Exists() throws SQLException {
        User foundUser = userDAO.getUserById(testUser.getIdUser());

        assertNotNull(foundUser);
        assertEquals(testUser.getFullName(), foundUser.getFullName());
        assertEquals(testUser.getCellPhone(), foundUser.getCellPhone());
    }

    @Test
    void testGetUserById_NotExists() throws SQLException {
        User foundUser = userDAO.getUserById(9999);
        assertNull(foundUser);
    }

    @Test
    void testUpdateUser_Success() throws SQLException {
        testUser.setFullName("Nombre Actualizado");
        testUser.setCellphone("5550000000");

        boolean result = userDAO.updateUser(testUser);
        assertTrue(result);

        User updatedUser = userDAO.getUserById(testUser.getIdUser());
        assertEquals("Nombre Actualizado", updatedUser.getFullName());
        assertEquals("5550000000", updatedUser.getCellPhone());
    }

    @Test
    void testUpdateUser_NotExists() throws SQLException {
        User nonExistentUser = new User();
        nonExistentUser.setIdUser(9999);
        nonExistentUser.setFullName("No existe");

        boolean result = userDAO.updateUser(nonExistentUser);
        assertFalse(result);
    }

    @Test
    void testDeleteUser_Success() throws SQLException {
        int initialCount = userDAO.countUsers();
        boolean result = userDAO.deleteUser(testUser.getIdUser());

        assertTrue(result);
        assertNull(userDAO.getUserById(testUser.getIdUser()));
        assertEquals(initialCount - 1, userDAO.countUsers());
    }

    @Test
    void testDeleteUser_NotExists() throws SQLException {
        int initialCount = userDAO.countUsers();
        boolean result = userDAO.deleteUser(9999);

        assertFalse(result);
        assertEquals(initialCount, userDAO.countUsers());
    }

    @Test
    void testSearchUsersByName_Match() throws SQLException {
        List<User> results = userDAO.searchUsersByName("Juan");
        assertTrue(results.size() > 0);

        boolean found = false;
        for (User user : results) {
            if (user.getIdUser() == testUser.getIdUser()) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    void testSearchUsersByName_NoMatch() throws SQLException {
        List<User> results = userDAO.searchUsersByName("XYZ123NoExist");
        assertEquals(0, results.size());
    }

    @Test
    void testUserExists_True() throws SQLException {
        assertTrue(userDAO.userExists(testUser.getIdUser()));
    }

    @Test
    void testUserExists_False() throws SQLException {
        assertFalse(userDAO.userExists(9999));
    }

    @Test
    void testCountUsers_WithData() throws SQLException {
        int initialCount = userDAO.countUsers();

        User newUser = new User();
        newUser.setFullName("Ana López Rojo");
        userDAO.addUser(newUser);

        assertEquals(initialCount + 1, userDAO.countUsers());
    }
}