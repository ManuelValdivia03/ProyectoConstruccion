package logic.interfaces;

import logic.Activity;
import logic.Student;
import logic.enums.ActivityStatus;
import java.sql.SQLException;
import java.util.List;

public interface IActivityDAO {
    boolean addActivity(Activity activity) throws SQLException;
    boolean updateActivity(Activity activity) throws SQLException;
    boolean deleteActivity(int idActivity) throws SQLException;
    Activity getActivityById(int idActivity) throws SQLException;
    List<Activity> getAllActivities() throws SQLException;
    List<Activity> getActivitiesByStatus(ActivityStatus status) throws SQLException;
    boolean changeActivityStatus(int idActivity, ActivityStatus newStatus) throws SQLException;
    boolean activityExists(int idActivity) throws SQLException;
    boolean assignActivityToStudent(int idActivity, int idStudent ) throws SQLException;
    boolean assignActivityToCronogram(int idActivity, int idCronogram ) throws SQLException;
}