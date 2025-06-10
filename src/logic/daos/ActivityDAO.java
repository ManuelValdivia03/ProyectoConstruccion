package logic.daos;

import dataaccess.ConnectionDataBase;
import logic.logicclasses.Activity;
import logic.enums.ActivityStatus;
import logic.interfaces.IActivityDAO;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActivityDAO implements IActivityDAO {
    private static final Activity EMPTY_ACTIVITY = new Activity(-1, "", "", null, null, ActivityStatus.NONE);

    public boolean addActivity(Activity activity) throws SQLException {
        if (activity == null ||
                activity.getNameActivity() == null ||
                activity.getDescriptionActivity() == null ||
                activity.getStartDate() == null ||
                activity.getEndDate() == null ||
                activity.getActivityStatus() == null) {
            throw new IllegalArgumentException("Datos de actividad incompletos o nulos");
        }

        String sql = "INSERT INTO actividad (nombre, descripcion, fecha_inicial, fecha_terminal, estado) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, activity.getNameActivity());
            statement.setString(2, activity.getDescriptionActivity());
            statement.setDate(3, new Date(activity.getStartDate().getTime()));
            statement.setDate(4, new Date(activity.getEndDate().getTime()));
            statement.setString(5, activity.getActivityStatus().getDbValue());

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        activity.setIdActivity(generatedId);
                    }
                }
                return true;
            }
            return false;
        }
    }

    public boolean updateActivity(Activity activity) throws SQLException {
        if (activity == null) {
            throw new IllegalArgumentException("La actividad no puede ser nula");
        }

        String sql = "UPDATE actividad SET nombre = ?, descripcion = ?, estado = ?, fecha_inicial = ?, fecha_terminal = ? WHERE id_actividad = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, activity.getNameActivity());
            statement.setString(2, activity.getDescriptionActivity());
            statement.setString(3, activity.getActivityStatus().toString());
            statement.setDate(4, new Date(activity.getStartDate().getTime()));
            statement.setDate(5, new Date(activity.getEndDate().getTime()));
            statement.setInt(6, activity.getIdActivity());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteActivity(int idActivity) throws SQLException {
        String sql = "DELETE FROM actividad WHERE id_actividad = ?";

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
                    Date startDate = resultSet.getDate("fecha_inicial");
                    Date endDate = resultSet.getDate("fecha_terminal");
                    return new Activity(
                            resultSet.getInt("id_actividad"),
                            resultSet.getString("nombre"),
                            resultSet.getString("descripcion"),
                            startDate != null ? new Timestamp(startDate.getTime()) : null,
                            endDate != null ? new Timestamp(endDate.getTime()) : null,
                            ActivityStatus.fromDbValue(resultSet.getString("estado"))
                    );
                }
            }
        }
        return EMPTY_ACTIVITY;
    }

    public List<Activity> getAllActivities() throws SQLException {
        String sql = "SELECT * FROM actividad";
        List<Activity> activities = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Date startDate = resultSet.getDate("fecha_inicial");
                Date endDate = resultSet.getDate("fecha_terminal");
                activities.add(new Activity(
                        resultSet.getInt("id_actividad"),
                        resultSet.getString("nombre"),
                        resultSet.getString("descripcion"),
                        startDate != null ? new Timestamp(startDate.getTime()) : null,
                        endDate != null ? new Timestamp(endDate.getTime()) : null,
                        ActivityStatus.fromDbValue(resultSet.getString("estado"))
                ));
            }
        }
        return activities;
    }

    public List<Activity> getActivitiesByStatus(ActivityStatus status) throws SQLException {
        if (status == null) {
            return Collections.emptyList();
        }

        String sql = "SELECT * FROM actividad WHERE estado = ?";
        List<Activity> activities = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status.toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Date startDate = resultSet.getDate("fecha_inicial");
                    Date endDate = resultSet.getDate("fecha_terminal");
                    activities.add(new Activity(
                            resultSet.getInt("id_actividad"),
                            resultSet.getString("nombre"),
                            resultSet.getString("descripcion"),
                            startDate != null ? new Timestamp(startDate.getTime()) : null,
                            endDate != null ? new Timestamp(endDate.getTime()) : null,
                            ActivityStatus.fromDbValue(resultSet.getString("estado"))
                    ));
                }
            }
        }
        return activities;
    }

    public boolean changeActivityStatus(int idActivity, ActivityStatus newStatus) throws SQLException {
        if (newStatus == null) {
            throw new IllegalArgumentException("El nuevo estado no puede ser nulo");
        }

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
