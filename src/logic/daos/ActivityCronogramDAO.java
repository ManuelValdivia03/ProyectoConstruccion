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
        String sql = "INSERT INTO cronograma_actividades (fecha_inicial, fecha_terminal) VALUES (?, ?)";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

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
        String sql = "UPDATE cronograma_actividades SET fecha_inicial = ?, fecha_terminal = ? WHERE id_cronograma = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDate(1, new java.sql.Date(cronogram.getDateStart().getTime()));
            statement.setDate(2, new java.sql.Date(cronogram.getDateEnd().getTime()));
            statement.setInt(3, cronogram.getIdCronogram());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteCronogram(int idCronogram) throws SQLException {
        String sql = "DELETE FROM cronograma_actividades WHERE id_cronograma = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idCronogram);
            return statement.executeUpdate() > 0;
        }
    }

    public ActivityCronogram getCronogramById(int idCronogram) throws SQLException {
        String sql = "SELECT * FROM cronograma_actividades WHERE id_cronograma = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

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
        String sql = "SELECT * FROM cronograma_actividades";
        List<ActivityCronogram> cronograms = new ArrayList<>();
        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

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
        String sql = "SELECT 1 FROM cronograma_actividades WHERE id_cronograma = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idCronogram);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public List<Activity> getActivitiesByCronogram(int idCronogram) throws SQLException {
        String sql = "SELECT a.*, sa.estado FROM actividad a " +
                "JOIN cronograma_actividad ca ON a.id_actividad = ca.id_actividad " +
                "LEFT JOIN seguimiento_actividad sa ON a.id_actividad = sa.id_actividad " +
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
        String sql = "INSERT INTO estudiante_cronograma (id_estudiante, id_cronograma) " +
                "SELECT id_usuario, ? FROM estudiante";

        try (Connection conn = ConnectionDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, cronogramId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean initializeActivityForAllStudents(int activityId) throws SQLException {
        String sql = "INSERT INTO seguimiento_actividad (id_estudiante, id_actividad, completada) " +
                "SELECT id_estudiante, ?, FALSE FROM estudiante_cronograma " +
                "WHERE id_cronograma = (SELECT id_cronograma FROM cronograma_actividad WHERE id_actividad = ? LIMIT 1)";

        try (Connection conn = ConnectionDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, activityId);
            stmt.setInt(2, activityId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteActivityCompletely(int activityId) throws SQLException {
        Connection conn = null;
        try {
            conn = ConnectionDataBase.getConnection();
            conn.setAutoCommit(false);

            String sql1 = "DELETE FROM seguimiento_actividad WHERE id_actividad = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql1)) {
                stmt.setInt(1, activityId);
                stmt.executeUpdate();
            }

            String sql2 = "DELETE FROM cronograma_actividad WHERE id_actividad = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql2)) {
                stmt.setInt(1, activityId);
                stmt.executeUpdate();
            }

            String sql3 = "DELETE FROM actividad WHERE id_actividad = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql3)) {
                stmt.setInt(1, activityId);
                stmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    throw e;
                }
            }
        }
    }

    public boolean isActivityInAnyCronogram(int activityId) throws SQLException {
        String sql = "SELECT 1 FROM cronograma_actividad WHERE id_actividad = ? LIMIT 1";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, activityId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }
}
