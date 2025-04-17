package logic.interfaces;

import logic.logicclasses.Account;
import java.sql.SQLException;
import java.util.List;

public interface IAccountDAO {

    List<Account> getAllAccounts() throws SQLException;

    boolean addAccount(Account account) throws SQLException;

    boolean deleteAccount(int idUser) throws SQLException;

    boolean updateAccount(Account account) throws SQLException;

    boolean verifyCredentials(String email, String plainPassword) throws SQLException;

    Account getAccountByEmail(String email) throws SQLException;

    Account getAccountByUserId(int idUser) throws SQLException;

    boolean accountExists(String email) throws SQLException;
}