package logic.interfaces;

import logic.logicclasses.Representative;
import java.sql.SQLException;
import java.util.List;

public interface IRepresentativeDAO {
    boolean addRepresentative(Representative representative) throws SQLException;
    boolean deleteRepresentative(Representative representative) throws SQLException;
    boolean updateRepresentative(Representative representative) throws SQLException;
    List<Representative> getAllRepresentatives() throws SQLException;
    Representative getRepresentativeById(int id) throws SQLException;
    Representative getRepresentativeByEmail(String email) throws SQLException;
    List<Representative> getRepresentativesByOrganization(int organizationId) throws SQLException;
    boolean representativeExists(String email) throws SQLException;
    int countRepresentatives() throws SQLException;
}