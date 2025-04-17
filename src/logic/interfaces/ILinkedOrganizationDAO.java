package logic.interfaces;

import logic.logicclasses.LinkedOrganization;

import java.sql.SQLException;
import java.util.List;

public interface ILinkedOrganizationDAO {
    boolean addLinkedOrganization(LinkedOrganization linkedOrganization) throws SQLException;
    boolean deleteLinkedOrganization(LinkedOrganization linkedOrganization) throws SQLException;
    boolean updateLinkedOrganization(LinkedOrganization linkedOrganization) throws SQLException;
    List<LinkedOrganization> getAllLinkedOrganizations() throws SQLException;
    LinkedOrganization getLinkedOrganizationByTitle(String title) throws SQLException;
    LinkedOrganization getLinkedOrganizationByID(int id) throws SQLException;
    boolean linkedOrganizationExists(String title) throws SQLException;
    int countLinkedOrganizations() throws SQLException;
}
