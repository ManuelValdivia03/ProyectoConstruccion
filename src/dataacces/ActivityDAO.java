package dataacces;

import logic.Activity;
import logic.enums.ActivityStatus;
import logic.interfaces.IActivityDAO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityDAO implements IActivityDAO {

    public boolean addActivity(Activity activity) throws SQLException {
        String sql = "INSERT INTO actividades (nombre, descripcion, estado, fecha_inicio, fecha_fin) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, activity.getNameActivity());
            statement.setString(2, activity.getDescriptionActivity());
            statement.setString(3, activity.getActivityStatus().toString());
            statement.setTimestamp(4, activity.getStartDate());
            statement.setTimestamp(5, activity.getEndDate());

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        activity.setIdActivity(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    public boolean updateActivity(Activity activity) throws SQLException {
        String sql = "UPDATE actividades SET nombre = ?, descripcion = ?, estado = ?, fecha_inicio = ?, fecha_fin = ? WHERE id_actividad = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, activity.getNameActivity());
            statement.setString(2, activity.getDescriptionActivity());
            statement.setString(3, activity.getActivityStatus().toString());
            statement.setTimestamp(4, activity.getStartDate());
            statement.setTimestamp(5, activity.getEndDate());
            statement.setInt(6, activity.getIdActivity());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteActivity(int idActivity) throws SQLException {
        String sql = "DELETE FROM actividades WHERE id_actividad = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idActivity);
            return statement.executeUpdate() > 0;
        }
    }

    public Activity getActivityById(int idActivity) throws SQLException {
        String sql = "SELECT * FROM actividades WHERE id_actividad = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idActivity);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Activity(
                            resultSet.getInt("id_actividad"),
                            resultSet.getString("nombre"),
                            resultSet.getString("descripcion"),
                            ActivityStatus.valueOf(resultSet.getString("estado")),
                            resultSet.getTimestamp("fecha_inicio"),
                            resultSet.getTimestamp("fecha_fin")
                    );
                }
            }
        }
        return null;
    }

    public List<Activity> getAllActivities() throws SQLException {
        String sql = "SELECT * FROM actividades";
        List<Activity> activities = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                activities.add(new Activity(
                        resultSet.getInt("id_actividad"),
                        resultSet.getString("nombre"),
                        resultSet.getString("descripcion"),
                        ActivityStatus.valueOf(resultSet.getString("estado")),
                        resultSet.getTimestamp("fecha_inicio"),
                        resultSet.getTimestamp("fecha_fin")
                ));
            }
        }
        return activities;
    }

    public List<Activity> getActivitiesByStatus(ActivityStatus status) throws SQLException {
        String sql = "SELECT * FROM actividades WHERE estado = ?";
        List<Activity> activities = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status.toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    activities.add(new Activity(
                            resultSet.getInt("id_actividad"),
                            resultSet.getString("nombre"),
                            resultSet.getString("descripcion"),
                            ActivityStatus.valueOf(resultSet.getString("estado")),
                            resultSet.getTimestamp("fecha_inicio"),
                            resultSet.getTimestamp("fecha_fin")
                    ));
                }
            }
        }
        return activities;
    }

    public boolean changeActivityStatus(int idActivity, ActivityStatus newStatus) throws SQLException {
        String sql = "UPDATE actividades SET estado = ? WHERE id_actividad = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, newStatus.toString());
            statement.setInt(2, idActivity);

            return statement.executeUpdate() > 0;
        }
    }

    public boolean activityExists(int idActivity) throws SQLException {
        String sql = "SELECT 1 FROM actividades WHERE id_actividad = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idActivity);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }
}