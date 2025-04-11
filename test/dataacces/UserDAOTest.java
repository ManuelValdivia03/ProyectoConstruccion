package dataacces;

import logic.User;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserDAOTest {
    private static UserDAO userDAO;
    private static Connection testConnection;
    private static List<User> testUsers;

    @BeforeAll
    static void setUpAll() throws SQLException {
        userDAO = new UserDAO();
        testConnection = ConnectionDataBase.getConnection();

        try (var statement = testConnection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS usuario (" +
                    "id_usuario INT AUTO_INCREMENT PRIMARY KEY, " +
                    "nombre_completo VARCHAR(100) NOT NULL, " +
                    "telefono VARCHAR(20))");
        }
    }

    @AfterAll
    static void tearDownAll() throws SQLException {
        if (testConnection != null && !testConnection.isClosed()) {
            testConnection.close();
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        testUsers = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            User user = new User();
            user.setFullName("Usuario Prueba " + i);
            user.setCellphone("55500000" + i);
            userDAO.addUser(user);
            testUsers.add(user);
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (var statement = testConnection.createStatement()) {
            statement.execute("DELETE FROM usuario");
            statement.execute("ALTER TABLE usuario AUTO_INCREMENT = 1");
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
        assertTrue(users.size() >= testUsers.size());

        for (User testUser : testUsers) {
            assertTrue(users.stream().anyMatch(u -> u.getIdUser() == testUser.getIdUser()));
        }
    }

    @Test
    void testGetUserById_Exists() throws SQLException {
        User testUser = testUsers.get(0);
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
        User userToUpdate = testUsers.get(0);
        userToUpdate.setFullName("Nuevo Nombre");
        userToUpdate.setCellphone("5551111111");

        boolean result = userDAO.updateUser(userToUpdate);
        assertTrue(result);

        User updatedUser = userDAO.getUserById(userToUpdate.getIdUser());
        assertEquals("Nuevo Nombre", updatedUser.getFullName());
        assertEquals("5551111111", updatedUser.getCellPhone());
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
        User userToDelete = testUsers.get(1);
        int initialCount = userDAO.countUsers();

        boolean result = userDAO.deleteUser(userToDelete.getIdUser());
        assertTrue(result);
        assertEquals(initialCount - 1, userDAO.countUsers());
        assertNull(userDAO.getUserById(userToDelete.getIdUser()));
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
        List<User> results = userDAO.searchUsersByName("Prueba");
        assertTrue(results.size() >= testUsers.size());

        for (User testUser : testUsers) {
            assertTrue(results.stream().anyMatch(u -> u.getIdUser() == testUser.getIdUser()));
        }
    }

    @Test
    void testSearchUsersByName_NoMatch() throws SQLException {
        List<User> results = userDAO.searchUsersByName("XYZ123NoExist");
        assertEquals(0, results.size());
    }

    @Test
    void testUserExists_True() throws SQLException {
        User testUser = testUsers.get(2);
        assertTrue(userDAO.userExists(testUser.getIdUser()));
    }

    @Test
    void testUserExists_False() throws SQLException {
        assertFalse(userDAO.userExists(9999));
    }

    @Test
    void testCountUsers_WithData() throws SQLException {
        int count = userDAO.countUsers();
        assertEquals(testUsers.size(), count);

        User extraUser = new User();
        extraUser.setFullName("Ana López Rojo");
        extraUser.setCellphone("5551112222");
        userDAO.addUser(extraUser);

        assertEquals(count + 1, userDAO.countUsers());
    }
}
