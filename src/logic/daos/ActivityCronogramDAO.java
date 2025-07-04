package logic.daos;

import dataaccess.ConnectionDataBase;
import logic.logicclasses.Activity;
import logic.logicclasses.ActivityCronogram;
import logic.enums.ActivityStatus;
import logic.interfaces.IActivityCronogramDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ActivityCronogramDAO implements IActivityCronogramDAO {

    private static final Logger logger = LogManager.getLogger(ActivityCronogramDAO.class);
    private static final ActivityCronogram EMPTY_CRONOGRAM = new ActivityCronogram();

    public boolean addCronogram(ActivityCronogram cronogram) throws SQLException, IllegalArgumentException {
        if (cronogram == null) {
            throw new IllegalArgumentException("El cronograma no debe ser nulo");
        }
        String query = "INSERT INTO cronograma_actividades (fecha_inicial, fecha_terminal) VALUES (?, ?)";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            statement.setDate(1, new java.sql.Date(cronogram.getDateStart().getTime()));
            statement.setDate(2, new java.sql.Date(cronogram.getDateEnd().getTime()));

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

    public boolean updateCronogram(ActivityCronogram cronogram) throws SQLException, IllegalArgumentException {
        if (cronogram == null) {
            throw new IllegalArgumentException("El cronograma no debe ser nulo");
        }
        String query = "UPDATE cronograma_actividades SET fecha_inicial = ?, fecha_terminal = ? WHERE id_cronograma = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setDate(1, new java.sql.Date(cronogram.getDateStart().getTime()));
            statement.setDate(2, new java.sql.Date(cronogram.getDateEnd().getTime()));
            statement.setInt(3, cronogram.getIdCronogram());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteCronogram(int idCronogram) throws SQLException {
        String query = "DELETE FROM cronograma_actividades WHERE id_cronograma = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, idCronogram);
            return statement.executeUpdate() > 0;
        }
    }

    public ActivityCronogram getCronogramById(int idCronogram) throws SQLException {
        String query = "SELECT * FROM cronograma_actividades WHERE id_cronograma = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, idCronogram);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    ActivityCronogram cronogram = new ActivityCronogram();
                    cronogram.setIdCronogram(resultSet.getInt("id_cronograma"));
                    cronogram.setDateStart(new Timestamp(resultSet.getDate("fecha_inicial").getTime()));
                    cronogram.setDateEnd(new Timestamp(resultSet.getDate("fecha_terminal").getTime()));
                    cronogram.setActivities(getActivitiesByCronogram(idCronogram));
                    return cronogram;
                }
            }
        }
        return EMPTY_CRONOGRAM;
    }

    public List<ActivityCronogram> getAllCronograms() throws SQLException {
        String query = "SELECT * FROM cronograma_actividades";
        List<ActivityCronogram> cronograms = new ArrayList<>();
        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                ActivityCronogram cronogram = new ActivityCronogram();
                int id = resultSet.getInt("id_cronograma");
                cronogram.setIdCronogram(id);
                cronogram.setDateStart(new Timestamp(resultSet.getDate("fecha_inicial").getTime()));
                cronogram.setDateEnd(new Timestamp(resultSet.getDate("fecha_terminal").getTime()));
                cronogram.setActivities(getActivitiesByCronogram(id));
                cronograms.add(cronogram);
            }
        }
        return cronograms;
    }

    public boolean addActivityToCronogram(int idCronogram, int idActivity) throws SQLException {
        String query = "INSERT INTO cronograma_actividad (id_cronograma, id_actividad) VALUES (?, ?)";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, idCronogram);
            statement.setInt(2, idActivity);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean removeActivityFromCronogram(int idCronogram, int idActivity) throws SQLException {
        String query = "DELETE FROM cronograma_actividad WHERE id_cronograma = ? AND id_actividad = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, idCronogram);
            statement.setInt(2, idActivity);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean cronogramExists(int idCronogram) throws SQLException {
        String query = "SELECT 1 FROM cronograma_actividades WHERE id_cronograma = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, idCronogram);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public List<Activity> getActivitiesByCronogram(int idCronogram) throws SQLException {
        String query = "SELECT a.*, sa.estado FROM actividad a " +
                "JOIN cronograma_actividad ca ON a.id_actividad = ca.id_actividad " +
                "LEFT JOIN seguimiento_actividad sa ON a.id_actividad = sa.id_actividad " +
                "WHERE ca.id_cronograma = ?";
        List<Activity> activities = new ArrayList<>();
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, idCronogram);
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

    public boolean assignCronogramToAllStudents(int cronogramId) throws SQLException {
        String query = "INSERT INTO estudiante_cronograma (id_estudiante, id_cronograma) " +
                "SELECT id_usuario, ? FROM estudiante";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, cronogramId);
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public boolean initializeActivityForAllStudents(int activityId) throws SQLException {
        String query = "INSERT INTO seguimiento_actividad (id_estudiante, id_actividad, completada) " +
                "SELECT id_estudiante, ?, FALSE FROM estudiante_cronograma " +
                "WHERE id_cronograma = (SELECT id_cronograma FROM cronograma_actividad WHERE id_actividad = ? LIMIT 1)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, activityId);
            preparedStatement.setInt(2, activityId);
            return preparedStatement.executeUpdate() > 0;
        }
    }

    public boolean deleteActivityCompletely(int activityId) throws SQLException {
        Connection connection = null;
        try {
            connection = ConnectionDataBase.getConnection();
            connection.setAutoCommit(false);

            String queryDeleteFollow = "DELETE FROM seguimiento_actividad WHERE id_actividad = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(queryDeleteFollow)) {
                preparedStatement.setInt(1, activityId);
                preparedStatement.executeUpdate();
            }

            String queryDeleteCronogram = "DELETE FROM cronograma_actividad WHERE id_actividad = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(queryDeleteCronogram)) {
                preparedStatement.setInt(1, activityId);
                preparedStatement.executeUpdate();
            }

            String querySelect = "DELETE FROM actividad WHERE id_actividad = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(querySelect)) {
                preparedStatement.setInt(1, activityId);
                preparedStatement.executeUpdate();
            }

            connection.commit();
            return true;
        } catch (SQLException e) {
            if (connection != null) connection.rollback();
            throw e;
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    throw e;
                }
            }
        }
    }

    public boolean isActivityInAnyCronogram(int activityId) throws SQLException {
        String query = "SELECT 1 FROM cronograma_actividad WHERE id_actividad = ? LIMIT 1";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, activityId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }
}
