package dataacces;

import logic.Coordinator;
import logic.interfaces.ICoordinatorDAO;

import java.sql.SQLException;
import java.util.List;

public class CoordinatorDAO implements ICoordinatorDAO {

    public boolean addCoordinator(Coordinator coordinator) throws SQLException {
        return false;
    }

    public boolean deleteCoordinator(Coordinator coordinator) throws SQLException {
        return false;
    }

    public boolean updateCoordinator(Coordinator coordinator) throws SQLException {
        return false;
    }

    public List<Coordinator> getAllCoordinators() throws SQLException {
        return List.of();
    }

    public Coordinator getCoordinatorByStaffNumber(String staffNumber) throws SQLException {
        return null;
    }

}
