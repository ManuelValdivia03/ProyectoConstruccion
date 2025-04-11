package logic.interfaces;

import logic.Group;
import java.sql.SQLException;
import java.util.List;

public interface IGroupDAO {
    boolean addGroup(Group group) throws SQLException;
    boolean deleteGroup(Group group) throws SQLException;
    boolean updateGroup(Group group) throws SQLException;
    List<Group> getAllGroups() throws SQLException;
    Group getGroupByNrc(int nrc) throws SQLException;
    Group getGroupByName(String groupName) throws SQLException;
    boolean groupExists(int nrc) throws SQLException;
    int countGroups() throws SQLException;
}