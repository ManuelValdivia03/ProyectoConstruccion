package logic.interfaces;

import logic.Coordinator;

import java.sql.SQLException;
import java.util.List;

public interface ICoordinatorDAO {
    boolean addCoordinator(Coordinator coordinator) throws SQLException;

    boolean deleteCoordinator(Coordinator coordinator) throws SQLException;

    boolean updateCoordinator(Coordinator coordinator) throws SQLException;

    List<Coordinator> getAllCoordinators() throws SQLException;

    Coordinator getCoordinatorByStaffNumber(String staffNumber) throws SQLException;

}
