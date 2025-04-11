package dataacces;

import logic.Activity;
import logic.enums.ActivityStatus;
import logic.interfaces.IActivityDAO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityDAO implements IActivityDAO {

    public boolean addActivity(Activity activity) throws SQLException {
        String sql = "INSERT INTO actividad (nombre, descripcion, fecha_inicial, fecha_terminal, estado) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, activity.getNameActivity());
            statement.setString(2, activity.getDescriptionActivity());
            statement.setTimestamp(3, activity.getStartDate());
            statement.setTimestamp(4, activity.getEndDate());
            statement.setString(5, activity.getActivityStatus().toString());

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
        String sql = "UPDATE actividad SET nombre = ?, descripcion = ?, estado = ?, fecha_inicial = ?, fecha_terminal = ? WHERE id_actividad = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, activity.getNameActivity());
            statement.setString(2, activity.getDescriptionActivity());
            statement.setTimestamp(4, activity.getStartDate());
            statement.setTimestamp(5, activity.getEndDate());
            statement.setString(3, activity.getActivityStatus().toString());
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
        String sql = "SELECT * FROM actividad WHERE id_actividad = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idActivity);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Activity(
                            resultSet.getInt("id_actividad"),
                            resultSet.getString("nombre"),
                            resultSet.getString("descripcion"),
                            resultSet.getTimestamp("fecha_inicial"),
                            resultSet.getTimestamp("fecha_terminal"),
                            ActivityStatus.valueOf(resultSet.getString("estado"))
                    );
                }
            }
        }
        return null;
    }

    public List<Activity> getAllActivities() throws SQLException {
        String sql = "SELECT * FROM actividad";
        List<Activity> activities = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                activities.add(new Activity(
                        resultSet.getInt("id_actividad"),
                        resultSet.getString("nombre"),
                        resultSet.getString("descripcion"),
                        resultSet.getTimestamp("fecha_inicial"),
                        resultSet.getTimestamp("fecha_terminal"),
                        ActivityStatus.valueOf(resultSet.getString("estado"))
                ));
            }
        }
        return activities;
    }

    public List<Activity> getActivitiesByStatus(ActivityStatus status) throws SQLException {
        String sql = "SELECT * FROM actividad WHERE estado = ?";
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
                            resultSet.getTimestamp("fecha_inicial"),
                            resultSet.getTimestamp("fecha_terminal"),
                            ActivityStatus.valueOf(resultSet.getString("estado"))
                    ));
                }
            }
        }
        return activities;
    }

    public boolean changeActivityStatus(int idActivity, ActivityStatus newStatus) throws SQLException {
        String sql = "UPDATE actividad SET estado = ? WHERE id_actividad = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, newStatus.toString());
            statement.setInt(2, idActivity);

            return statement.executeUpdate() > 0;
        }
    }

    public boolean activityExists(int idActivity) throws SQLException {
        String sql = "SELECT 1 FROM actividad WHERE id_actividad = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idActivity);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public static void main(String[] args) throws SQLException {
        Activity activity = new Activity(
                1,
                "prueba",
                "prueba",
                Timestamp.valueOf("2025-04-11 14:30:00"),
                Timestamp.valueOf("2025-04-11 14:30:00"),
                ActivityStatus.PENDIENTE
        );

        ActivityDAO activityDAO = new ActivityDAO();
        activityDAO.addActivity(activity);


    }

    public boolean assignActivityToStudent(int idActivity, int idStudent) throws SQLException {
        String sql = "UPDATE actividad SET id_usuario = ? WHERE id_actividad = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idStudent);
            statement.setInt(2, idActivity);

            return statement.executeUpdate() > 0;
        }
    }

    public boolean assignActivityToCronogram(int idActivity, int idCronogram) throws SQLException {
        String sql = "UPDATE actividad SET id_cronograma = ? WHERE id_actividad = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idCronogram);
            statement.setInt(2, idActivity);

            return statement.executeUpdate() > 0;
        }
    }
}