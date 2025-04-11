package logic.interfaces;

import logic.ActivityCronogram;
import java.sql.SQLException;
import java.util.List;

public interface IActivityCronogramDAO {
    boolean addCronogram(ActivityCronogram cronogram) throws SQLException;
    boolean updateCronogram(ActivityCronogram cronogram) throws SQLException;
    boolean deleteCronogram(int idCronogram) throws SQLException;
    ActivityCronogram getCronogramById(int idCronogram) throws SQLException;
    List<ActivityCronogram> getAllCronograms() throws SQLException;
    boolean addActivityToCronogram(int idCronogram, int idActivity) throws SQLException;
    boolean removeActivityFromCronogram(int idCronogram, int idActivity) throws SQLException;
    boolean cronogramExists(int idCronogram) throws SQLException;
}