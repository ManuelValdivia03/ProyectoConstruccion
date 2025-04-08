package logic.interfaces;

import logic.User;
import java.sql.SQLException;
import java.util.List;

public interface IUserDAO {

    boolean addUser(User user) throws SQLException;

    List<User> getAllUsers() throws SQLException;

    User getUserById(int id) throws SQLException;

    boolean updateUser(User user) throws SQLException;

    boolean deleteUser(int idUser) throws SQLException;

    List<User> searchUsersByName(String name) throws SQLException;

    boolean userExists(int idUser) throws SQLException;

    int countUsers() throws SQLException;
}
