package daos;

import dataaccess.ConnectionDataBase;
import dataaccess.PasswordUtils;
import logic.daos.AccountDAO;
import logic.daos.UserDAO;
import logic.exceptions.RepeatedEmailException;
import logic.logicclasses.Account;
import logic.logicclasses.User;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountDAOTest {
    private static AccountDAO accountDAO;
    private static UserDAO userDAO;
    private static List<User> testUsers;
    private static List<Account> testAccounts;

    @BeforeEach
    void setUp() throws SQLException {
        accountDAO = new AccountDAO();
        userDAO = new UserDAO();

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

        testUsers = new ArrayList<>();
        testAccounts = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            User user = new User();
            user.setFullName("Usuario Prueba " + i);
            user.setCellphone("555000000" + i);
            userDAO.addUser(user);
            testUsers.add(user);

            String password = "password" + i;
            String hashedPassword = PasswordUtils.hashPassword(password);

            Account account = new Account(
                    user.getIdUser(),
                    "user" + i + "@test.com",
                    hashedPassword
            );
            accountDAO.addAccount(account);
            testAccounts.add(account);
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection conn = ConnectionDataBase.getConnection();
             var statement = conn.createStatement()) {
            statement.execute("DELETE FROM cuenta WHERE id_usuario > 5");
        }
    }

    @Test
    void testGetAllAccounts_WithData() throws SQLException {
        List<Account> accounts = accountDAO.getAllAccounts();
        assertFalse(accounts.isEmpty());
    }

    @Test
    void testGetAllAccounts_EmptyTable() throws SQLException {
        try (Connection connection = ConnectionDataBase.getConnection();
             var statement = connection.createStatement()) {
            statement.execute("DELETE FROM cuenta");
        }
        List<Account> accounts = accountDAO.getAllAccounts();
        assertTrue(accounts.isEmpty());
        setUp();
    }

    @Test
    void testAddAccount_Success() throws SQLException {
        User newUser = new User();
        newUser.setFullName("New Test User");
        newUser.setCellphone("5551234567");
        userDAO.addUser(newUser);

        Account newAccount = new Account(
                newUser.getIdUser(),
                "new.user@test.com",
                "newPassword123"
        );

        boolean result = accountDAO.addAccount(newAccount);
        assertTrue(result);
    }

    @Test
    void testAddAccount_UserNotExists_ShouldThrowException() {
        Account invalidAccount = new Account(
                9999,
                "nonexistent@test.com",
                "somePassword"
        );
        assertThrows(SQLException.class, () -> accountDAO.addAccount(invalidAccount));
    }

    @Test
    void testAddAccount_NullAccount_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> accountDAO.addAccount(null));
    }

    @Test
    void testAddAccount_DuplicateEmail_ShouldFail() {
        Account duplicateAccount = new Account(
                testUsers.get(0).getIdUser(),
                testAccounts.get(1).getEmail(),
                "somePassword"
        );
        assertThrows(RepeatedEmailException.class, () -> accountDAO.addAccount(duplicateAccount));
    }

    @Test
    void testDeleteAccount_Success() throws SQLException {
        Account accountToDelete = testAccounts.get(0);
        boolean result = accountDAO.deleteAccount(accountToDelete.getIdUser());
        assertTrue(result);
    }

    @Test
    void testDeleteAccount_NotExists() throws SQLException {
        boolean result = accountDAO.deleteAccount(9999);
        assertFalse(result);
    }

    @Test
    void testUpdateAccount_EmailAndPassword() throws SQLException {
        Account accountToUpdate = testAccounts.get(1);
        accountToUpdate.setEmail("updated.email@test.com");
        accountToUpdate.setPassword("newSecurePassword123");
        boolean result = accountDAO.updateAccount(accountToUpdate);
        assertTrue(result);
    }

    @Test
    void testUpdateAccount_EmailOnly() throws SQLException {
        Account accountToUpdate = testAccounts.get(2);
        accountToUpdate.setEmail("only.email.updated@test.com");
        accountToUpdate.setPassword(null);
        boolean result = accountDAO.updateAccount(accountToUpdate);
        assertTrue(result);
    }

    @Test
    void testUpdateAccount_PasswordOnly() throws SQLException {
        Account updateData = new Account();
        updateData.setIdUser(testAccounts.get(3).getIdUser());
        updateData.setPassword("newPasswordOnly");
        boolean result = accountDAO.updateAccount(updateData);
        assertTrue(result);
    }

    @Test
    void testUpdateAccount_NoChanges() throws SQLException {
        Account originalAccount = testAccounts.get(4);
        Account updateData = new Account();
        updateData.setIdUser(originalAccount.getIdUser());
        boolean result = accountDAO.updateAccount(updateData);
        assertFalse(result);
    }

    @Test
    void testUpdateAccount_NotExists() throws SQLException {
        Account nonExistentAccount = new Account(
                9999,
                "nonexistent@test.com",
                "password"
        );
        boolean result = accountDAO.updateAccount(nonExistentAccount);
        assertFalse(result);
    }

    @Test
    void testVerifyCredentials_Success() throws SQLException {
        User user = new User();
        user.setFullName("Usuario Prueba");
        user.setCellphone("5551234580");
        user.setStatus('A');
        userDAO.addUser(user);

        String password = "micontraseñaSegura123";
        String hashedPassword = PasswordUtils.hashPassword(password);

        Account account = new Account(
                user.getIdUser(),
                "prueba@test.com",
                hashedPassword
        );
        accountDAO.addAccount(account);

        assertTrue(accountDAO.verifyCredentials("prueba@test.com", password));
    }

    @Test
    void testVerifyCredentials_WrongPassword() throws SQLException {
        boolean result = accountDAO.verifyCredentials(
                testAccounts.get(1).getEmail(),
                "wrongPassword"
        );
        assertFalse(result);
    }

    @Test
    void testVerifyCredentials_InvalidEmail() throws SQLException {
        boolean result = accountDAO.verifyCredentials(
                "nonexistent@test.com",
                "somePassword"
        );
        assertFalse(result);
    }

    @Test
    void testVerifyCredentials_NullInputs() throws SQLException {
        assertThrows(IllegalArgumentException.class, () -> accountDAO.verifyCredentials(null, "password"));
    }

    @Test
    void testGetAccountByUserId_Exists() throws SQLException {
        Account expected = testAccounts.get(0);
        Account actual = accountDAO.getAccountByUserId(expected.getIdUser());
        assertEquals(expected, actual);
    }

    @Test
    void testGetAccountByUserId_NotExists() throws SQLException {
        Account expected = new Account(-1, "", "");
        Account actual = accountDAO.getAccountByUserId(9999);
        assertEquals(expected, actual);
    }

    @Test
    void testGetAccountByEmail_Exists() throws SQLException {
        Account expected = testAccounts.get(0);
        Account actual = accountDAO.getAccountByEmail(expected.getEmail());
        assertEquals(expected.getIdUser(), actual.getIdUser());
        assertEquals(expected.getEmail(), actual.getEmail());
        assertTrue(PasswordUtils.checkPassword("password1", actual.getPassword()));
    }

    @Test
    void testGetAccountByEmail_NotExists() throws SQLException {
        Account expected = new Account(-1, "", "");
        Account actual = accountDAO.getAccountByEmail("nonexistent@test.com");
        assertEquals(expected, actual);
    }

    @Test
    void testGetAccountByEmail_NullInput() throws SQLException {
        assertThrows(IllegalArgumentException.class, () -> accountDAO.getAccountByEmail(null));
    }

    @Test
    void testAccountExists_True() throws SQLException {
        boolean exists = accountDAO.accountExists(testAccounts.get(0).getEmail());
        assertTrue(exists);
    }

    @Test
    void testAccountExists_False() throws SQLException {
        boolean exists = accountDAO.accountExists("nonexistent@test.com");
        assertFalse(exists);
    }

    @Test
    void testAccountExists_NullInput() throws SQLException {
        assertThrows(IllegalArgumentException.class, () -> accountDAO.accountExists(null));
    }

    @Test
    void testPasswordHashingAndVerification() {
        String plainPassword = "testPassword";
        String hashed = PasswordUtils.hashPassword(plainPassword);
        assertTrue(PasswordUtils.checkPassword(plainPassword, hashed));
    }
}