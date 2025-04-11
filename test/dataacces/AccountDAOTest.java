package dataacces;

import dataacces.util.PasswordUtils;
import logic.Account;
import logic.User;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountDAOTest {
    private static AccountDAO accountDAO;
    private static UserDAO userDAO;
    private static Connection testConnection;
    private static List<User> testUsers;
    private static List<Account> testAccounts;

    @BeforeAll
    static void setUpAll() throws SQLException {
        accountDAO = new AccountDAO();
        userDAO = new UserDAO();
        testConnection = ConnectionDataBase.getConnection();
        try (var statement = testConnection.createStatement()) {
            statement.execute("DELETE FROM academico");
            statement.execute("DELETE FROM coordinador");
            statement.execute("ALTER TABLE coordinador AUTO_INCREMENT = 1");
            statement.execute("DELETE FROM cuenta");
            statement.execute("DELETE FROM usuario");
            statement.execute("ALTER TABLE usuario AUTO_INCREMENT = 1");
            statement.execute("ALTER TABLE cuenta AUTO_INCREMENT = 1");
            statement.execute("ALTER TABLE academico AUTO_INCREMENT = 1");
        }

        testUsers = new ArrayList<>();
        testAccounts = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            User user = new User();
            user.setFullName("Usuario Prueba " + i);
            user.setCellphone("55500000" + i);
            userDAO.addUser(user);
            testUsers.add(user);

            Account account = new Account(
                    user.getIdUser(),
                    "user" + i + "@test.com",
                    PasswordUtils.hashPassword("password" + i)
            );
            accountDAO.addAccount(account);
            testAccounts.add(account);
        }
    }

    @AfterAll
    static void tearDownAll() throws SQLException {
        if (testConnection != null && !testConnection.isClosed()) {
            testConnection.close();
        }
    }

    @Test
    void testGetAllAccounts_WithData() throws SQLException {
        List<Account> accounts = accountDAO.getAllAccounts();

        assertFalse(accounts.isEmpty());
        System.out.println(testAccounts.size());
        System.out.println(accounts.size());
        assertTrue(accounts.size() >= testAccounts.size());
        for (Account testAccount : testAccounts) {
            boolean found = accounts.stream()
                    .anyMatch(a -> a.getIdUser() == testAccount.getIdUser()
                            && a.getEmail().equals(testAccount.getEmail()));
            assertTrue(found);
        }
    }

    @Test
    void testDeleteAccount_Success() throws SQLException {
        Account accountToDelete = testAccounts.get(0);
        boolean result = accountDAO.deleteAccount(accountToDelete.getIdUser());
        testAccounts.remove(accountToDelete);
        assertTrue(result);
        assertNull(accountDAO.getAccountByUserId(accountToDelete.getIdUser()));
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

        Account retrievedAccount = accountDAO.getAccountByUserId(newUser.getIdUser());
        assertNotNull(retrievedAccount);
        assertEquals(newAccount.getEmail(), retrievedAccount.getEmail());
        assertTrue(PasswordUtils.checkPassword("newPassword123", retrievedAccount.getPassword()));
    }

    @Test
    void testAddAccount_UserNotExists_ShouldThrowException() {
        Account invalidAccount = new Account(
                9999, // Non-existent user ID
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
    void testAddAccount_DuplicateEmail_ShouldFail() throws SQLException {
        User newUser = new User();
        newUser.setFullName("Duplicate Email User");
        newUser.setCellphone("5559876543");
        userDAO.addUser(newUser);

        Account duplicateAccount = new Account(
                newUser.getIdUser(),
                testAccounts.get(1).getEmail(), // Using existing email
                "somePassword"
        );

        assertThrows(SQLException.class, () -> accountDAO.addAccount(duplicateAccount));
    }

    @Test
    void testDeleteAccount_NotExists() throws SQLException {
        boolean result = accountDAO.deleteAccount(9999);
        assertFalse(result);
    }

    @Test
    void testUpdateAccount_EmailAndPassword() throws SQLException {
        Account accountToUpdate = testAccounts.get(1);
        String oldPassword = accountToUpdate.getPassword();
        accountToUpdate.setEmail("updated.email@test.com");
        accountToUpdate.setPassword("newSecurePassword123");

        boolean result = accountDAO.updateAccount(accountToUpdate);
        assertTrue(result);

        Account updatedAccount = accountDAO.getAccountByUserId(accountToUpdate.getIdUser());
        assertEquals("updated.email@test.com", updatedAccount.getEmail());
        assertTrue(PasswordUtils.checkPassword("newSecurePassword123", updatedAccount.getPassword()));

    }

    @Test
    void testUpdateAccount_EmailOnly() throws SQLException {
        Account accountToUpdate = testAccounts.get(2);
        String originalPassword = accountToUpdate.getPassword();
        accountToUpdate.setEmail("only.email.updated@test.com");
        accountToUpdate.setPassword(null); // Don't update password

        boolean result = accountDAO.updateAccount(accountToUpdate);
        assertTrue(result);

        Account updatedAccount = accountDAO.getAccountByUserId(accountToUpdate.getIdUser());
        assertEquals("only.email.updated@test.com", updatedAccount.getEmail());
    }

    @Test
    void testUpdateAccount_PasswordOnly() throws SQLException {
        int userId = testAccounts.get(3).getIdUser();
        Account originalAccount = accountDAO.getAccountByUserId(userId);
        String oldPassword = originalAccount.getPassword();

        Account updateData = new Account();
        updateData.setIdUser(userId);
        updateData.setPassword("newSecurePassword123");

        boolean updateResult = accountDAO.updateAccount(updateData);
        assertTrue(updateResult);
    }

    @Test
    void testUpdateAccount_NoChanges() throws SQLException {
        int lastIndex = testAccounts.size() - 1;
        Account originalAccount = accountDAO.getAccountByUserId(testAccounts.get(lastIndex).getIdUser());
        String originalEmail = originalAccount.getEmail();
        String originalHashedPassword = originalAccount.getPassword();

        Account updateData = new Account();
        updateData.setIdUser(originalAccount.getIdUser());

        boolean result = accountDAO.updateAccount(updateData);
        assertFalse(result, "Should return false when no changes are provided");

        Account updatedAccount = accountDAO.getAccountByUserId(originalAccount.getIdUser());
        assertEquals(originalEmail, updatedAccount.getEmail());
        assertEquals(originalHashedPassword, updatedAccount.getPassword());
    }

    @Test
    void testBCryptBasicFunctionality() {
        String testPass = "password5";
        String hash = PasswordUtils.hashPassword(testPass);
        assertTrue(PasswordUtils.checkPassword(testPass, hash));
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
        Account testAccount = testAccounts.get(1);
        System.out.println("Testing account: " + testAccount.getEmail());
        System.out.println("Stored hash: " + testAccount.getPassword());
        System.out.println("Verifying password: newSecurePassword123");

        boolean result = accountDAO.verifyCredentials(
                testAccount.getEmail(),
                "newSecurePassword123"
        );

        assertTrue(result);
    }

    @Test
    void testVerifyCredentials_WrongPassword() throws SQLException {
        Account testAccount = testAccounts.get(1);
        boolean result = accountDAO.verifyCredentials(
                testAccount.getEmail(),
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
        assertFalse(accountDAO.verifyCredentials(null, "password"));
        assertFalse(accountDAO.verifyCredentials("test@test.com", null));
        assertFalse(accountDAO.verifyCredentials(null, null));
    }

    @Test
    void testGetAccountByUserId_Exists() throws SQLException {
        assertNotNull(accountDAO.getAccountByUserId(testAccounts.get(0).getIdUser()));
    }

    @Test
    void testGetAccountByUserId_NotExists() throws SQLException {
        Account foundAccount = accountDAO.getAccountByUserId(9999);
        assertNull(foundAccount);
    }

    @Test
    void testGetAccountByEmail_Exists() throws SQLException {
        Account foundAccount = accountDAO.getAccountByEmail(testAccounts.get(0).getEmail().toString());
        assertNotNull(foundAccount);
    }

    @Test
    void testGetAccountByEmail_NotExists() throws SQLException {
        Account foundAccount = accountDAO.getAccountByEmail("nonexistent@test.com");
        assertNull(foundAccount);
    }

    @Test
    void testGetAccountByEmail_NullInput() throws SQLException {
        assertNull(accountDAO.getAccountByEmail(null));
    }

    @Test
    void testAccountExists_True() throws SQLException {
        Account testAccount = testAccounts.get(0);
        assertTrue(accountDAO.accountExists(testAccount.getEmail()));
    }

    @Test
    void testAccountExists_False() throws SQLException {
        assertFalse(accountDAO.accountExists("nonexistent@test.com"));
    }

    @Test
    void testAccountExists_NullInput() throws SQLException {
        assertFalse(accountDAO.accountExists(null));
    }

    @Test
    void testPasswordHashingAndVerification() {
        String plainPassword = "password2";
        String hashed = PasswordUtils.hashPassword(plainPassword);
        assertTrue(PasswordUtils.checkPassword(plainPassword, hashed));
    }
}