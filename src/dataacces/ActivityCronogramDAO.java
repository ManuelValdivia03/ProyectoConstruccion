package dataacces;

import logic.Activity;
import logic.ActivityCronogram;
import logic.enums.ActivityStatus;
import logic.interfaces.IActivityCronogramDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityCronogramDAO implements IActivityCronogramDAO {

    public boolean addCronogram(ActivityCronogram cronogram) throws SQLException {
        String sql = "INSERT INTO cronogramas (fecha_inicio, fecha_fin) VALUES (?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setTimestamp(1, cronogram.getDateStart());
            statement.setTimestamp(2, cronogram.getDateEnd());

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        cronogram.setIdCronogram(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    public boolean updateCronogram(ActivityCronogram cronogram) throws SQLException {
        String sql = "UPDATE cronogramas SET fecha_inicio = ?, fecha_fin = ? WHERE id_cronograma = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setTimestamp(1, cronogram.getDateStart());
            statement.setTimestamp(2, cronogram.getDateEnd());
            statement.setInt(3, cronogram.getIdCronogram());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteCronogram(int idCronogram) throws SQLException {
        String sql = "DELETE FROM cronogramas WHERE id_cronograma = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idCronogram);
            return statement.executeUpdate() > 0;
        }
    }

    public ActivityCronogram getCronogramById(int idCronogram) throws SQLException {
        String sql = "SELECT * FROM cronogramas WHERE id_cronograma = ?";
        ActivityCronogram cronogram = null;

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idCronogram);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    cronogram = new ActivityCronogram();
                    cronogram.setIdCronogram(resultSet.getInt("id_cronograma"));
                    cronogram.setDateStart(resultSet.getTimestamp("fecha_inicio"));
                    cronogram.setDateEnd(resultSet.getTimestamp("fecha_fin"));
                    cronogram.setActivities(getActivitiesByCronogram(idCronogram));
                }
            }
        }
        return cronogram;
    }

    public List<ActivityCronogram> getAllCronograms() throws SQLException {
        String sql = "SELECT * FROM cronogramas";
        List<ActivityCronogram> cronograms = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                ActivityCronogram cronogram = new ActivityCronogram();
                int id = resultSet.getInt("id_cronograma");
                cronogram.setIdCronogram(id);
                cronogram.setDateStart(resultSet.getTimestamp("fecha_inicio"));
                cronogram.setDateEnd(resultSet.getTimestamp("fecha_fin"));
                cronogram.setActivities(getActivitiesByCronogram(id));
                cronograms.add(cronogram);
            }
        }
        return cronograms;
    }

    public boolean addActivityToCronogram(int idCronogram, int idActivity) throws SQLException {
        String sql = "INSERT INTO cronograma_actividad (id_cronograma, id_actividad) VALUES (?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idCronogram);
            statement.setInt(2, idActivity);

            return statement.executeUpdate() > 0;
        }
    }

    public boolean removeActivityFromCronogram(int idCronogram, int idActivity) throws SQLException {
        String sql = "DELETE FROM cronograma_actividad WHERE id_cronograma = ? AND id_actividad = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idCronogram);
            statement.setInt(2, idActivity);

            return statement.executeUpdate() > 0;
        }
    }

    public boolean cronogramExists(int idCronogram) throws SQLException {
        String sql = "SELECT 1 FROM cronogramas WHERE id_cronograma = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idCronogram);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private List<Activity> getActivitiesByCronogram(int idCronogram) throws SQLException {
        String sql = "SELECT a.* FROM actividades a " +
                "JOIN cronograma_actividad ca ON a.id_actividad = ca.id_actividad " +
                "WHERE ca.id_cronograma = ?";
        List<Activity> activities = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idCronogram);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    activities.add(new Activity(
                            resultSet.getInt("id_actividad"),
                            resultSet.getString("nombre"),
                            resultSet.getString("descripcion"),
                            resultSet.getTimestamp("fecha_inicio"),
                            resultSet.getTimestamp("fecha_fin"),
                            ActivityStatus.valueOf(resultSet.getString("estado"))
                    ));
                }
            }
        }
        return activities;
    }
}
