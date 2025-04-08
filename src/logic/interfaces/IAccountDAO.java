package logic.interfaces;

import logic.Account;
import java.sql.SQLException;
import java.util.List;

public interface IAccountDAO {

    List<Account> getAllAccounts() throws SQLException;

    boolean addAccount(Account account) throws SQLException;

    boolean deleteAccount(int idUser) throws SQLException;

    boolean updateAccount(int idUser, String newEmail, String newPlainPassword) throws SQLException;

    boolean verifyCredentials(String email, String plainPassword) throws SQLException;

    boolean updatePassword(int idUser, String newPlainPassword) throws SQLException;

    Account searchAccountByEmail(String email) throws SQLException;

    Account searchAccountById(int idUser) throws SQLException;
}