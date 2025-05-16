package daos;

import dataaccess.ConnectionDataBase;
import logic.daos.UserDAO;
import logic.exceptions.InvalidCellPhoneException;
import logic.exceptions.RepeatedCellPhoneException;
import logic.logicclasses.User;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserDAOTest {
    private static UserDAO userDAO;
    private static List<User> testUsers;

    @BeforeAll
    static void setUpAll() throws SQLException {
        userDAO = new UserDAO();

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

        testUsers = List.of(
                createTestUser("Usuario Prueba 1", "5550000010"),
                createTestUser("Usuario Prueba 2", "5550000020"),
                createTestUser("Usuario Prueba 3", "5550000030"),
                createTestUser("Usuario Prueba 4", "5550000040"),
                createTestUser("Usuario Prueba 5", "5550000050")
        );
    }

    private static User createTestUser(String name, String phone) throws SQLException {
        User user = new User();
        user.setFullName(name);
        user.setCellphone(phone);
        user.setStatus('A');
        userDAO.addUser(user);
        return user;
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (var conn = ConnectionDataBase.getConnection();
             var statement = conn.createStatement()) {
            statement.execute("DELETE FROM usuario WHERE id_usuario > 5");
            statement.execute("ALTER TABLE usuario AUTO_INCREMENT = 6");
        }
    }

    @Test
    void testAddUser_Success() throws SQLException, RepeatedCellPhoneException {
        User newUser = new User();
        newUser.setFullName("Nuevo Usuario");
        newUser.setCellphone("5551234567");

        assertTrue(userDAO.addUser(newUser));
        assertTrue(newUser.getIdUser() > 0);
    }

    @Test
    void testAddUser_NullUser_ShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> userDAO.addUser(null));
    }

    @Test
    void testAddUser_DuplicatePhone_ShouldThrowRepeatedCellPhoneException() throws SQLException {
        String uniquePhone = "5559876543";
        createTestUser("Usuario Original", uniquePhone);

        User duplicateUser = new User();
        duplicateUser.setFullName("Usuario Duplicado");
        duplicateUser.setCellphone(uniquePhone);

        assertThrows(RepeatedCellPhoneException.class, () -> userDAO.addUser(duplicateUser));
    }

    @Test
    void testGetAllUsers_WithData() throws SQLException {
        List<User> users = userDAO.getAllUsers();
        assertFalse(users.isEmpty());
    }

    @Test
    void testGetAllUsers_EmptyTable() throws SQLException {
        try (var conn = ConnectionDataBase.getConnection();
             var statement = conn.createStatement()) {
            statement.execute("DELETE FROM usuario");
        }

        List<User> users = userDAO.getAllUsers();
        assertTrue(users.isEmpty());

        setUpAll();
    }

    @Test
    void testGetUserById_Exists() throws SQLException {
        User testUser = testUsers.get(0);
        User foundUser = userDAO.getUserById(testUser.getIdUser());

        assertNotNull(foundUser);
        assertEquals(testUser.getFullName(), foundUser.getFullName());
    }

    @Test
    void testGetUserById_NotExists() throws SQLException {
        assertNull(userDAO.getUserById(9999));
    }

    @Test
    void testGetUserById_InvalidId() throws SQLException {
        assertNull(userDAO.getUserById(-1));
    }

    @Test
    void testUpdateUser_Success() throws SQLException {
        User userToUpdate = testUsers.get(0);
        userToUpdate.setFullName("Nombre Actualizado");

        assertTrue(userDAO.updateUser(userToUpdate));

        User updatedUser = userDAO.getUserById(userToUpdate.getIdUser());
        assertEquals("Nombre Actualizado", updatedUser.getFullName());
    }

    @Test
    void testUpdateUser_NotExists() throws SQLException {
        User nonExistentUser = new User();
        nonExistentUser.setIdUser(9999);
        nonExistentUser.setFullName("No existe");

        assertFalse(userDAO.updateUser(nonExistentUser));
    }

    @Test
    void testUpdateUser_NullUser() throws SQLException {
        assertFalse(userDAO.updateUser(null));
    }

    @Test
    void testDeleteUser_Success() throws SQLException {
        User userToDelete = testUsers.get(1);
        assertTrue(userDAO.deleteUser(userToDelete.getIdUser()));
        assertNull(userDAO.getUserById(userToDelete.getIdUser()));
    }

    @Test
    void testDeleteUser_NotExists() throws SQLException {
        assertFalse(userDAO.deleteUser(9999));
    }

    @Test
    void testDeleteUser_InvalidId() throws SQLException {
        assertFalse(userDAO.deleteUser(-1));
    }

    @Test
    void testSearchUsersByName_Match() throws SQLException {
        List<User> results = userDAO.searchUsersByName("Prueba");
        assertFalse(results.isEmpty());
    }

    @Test
    void testSearchUsersByName_NoMatch() throws SQLException {
        List<User> results = userDAO.searchUsersByName("XYZ123NoExist");
        assertTrue(results.isEmpty());
    }

    @Test
    void testSearchUsersByName_NullName() throws SQLException {
        List<User> results = userDAO.searchUsersByName(null);
        assertTrue(results.isEmpty());
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
    void testUserExists_InvalidId() throws SQLException {
        assertFalse(userDAO.userExists(-1));
    }

    @Test
    void testCellPhoneExists_True() throws SQLException, InvalidCellPhoneException {
        User testUser = testUsers.get(3);
        assertTrue(userDAO.cellPhoneExists(testUser.getCellPhone()));
    }

    @Test
    void testCellPhoneExists_False() throws SQLException, InvalidCellPhoneException {
        assertFalse(userDAO.cellPhoneExists("5559999999"));
    }

    @Test
    void testCellPhoneExists_InvalidPhone() {
        assertThrows(InvalidCellPhoneException.class, () -> userDAO.cellPhoneExists("123"));
    }

    @Test
    void testCountUsers_WithData() throws SQLException {
        int count = userDAO.countUsers();
        assertTrue(count >= testUsers.size());
    }

    @Test
    void testCountUsers_EmptyTable() throws SQLException {
        try (var conn = ConnectionDataBase.getConnection();
             var statement = conn.createStatement()) {
            statement.execute("DELETE FROM usuario");
        }

        assertEquals(0, userDAO.countUsers());
        setUpAll();
    }

    @Test
    void testCountUsers_AfterAdd() throws SQLException, RepeatedCellPhoneException {
        int initialCount = userDAO.countUsers();

        User newUser = new User();
        newUser.setFullName("Usuario Temporal");
        newUser.setCellphone("5551112222");
        userDAO.addUser(newUser);

        assertEquals(initialCount + 1, userDAO.countUsers());
    }
}