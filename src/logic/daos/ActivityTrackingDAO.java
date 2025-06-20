package logic.daos;

import dataaccess.ConnectionDataBase;
import logic.enums.ActivityStatus;
import logic.logicclasses.Activity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ActivityTrackingDAO {
    private static final Logger logger = Logger.getLogger(ActivityTrackingDAO.class.getName());

    public boolean assignCronogramToStudent(int studentId, int cronogramId) throws SQLException {
        String sql = "INSERT INTO estudiante_cronograma (id_estudiante, id_cronograma) VALUES (?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, studentId);
            statement.setInt(2, cronogramId);

            return statement.executeUpdate() > 0;
        }
    }

    public boolean markActivityAsCompleted(int studentId, int activityId) throws SQLException {
        String sql = "UPDATE seguimiento_actividad SET completada = TRUE, fecha_completado = CURRENT_TIMESTAMP "
                + "WHERE id_estudiante = ? AND id_actividad = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, studentId);
            statement.setInt(2, activityId);

            return statement.executeUpdate() > 0;
        }
    }

    public boolean initializeActivityForAllStudents(int activityId) throws SQLException {
        String sql = "INSERT INTO seguimiento_actividad (id_estudiante, id_actividad, completada, estado) " +
                "SELECT id_estudiante, ?, FALSE, 'PENDIENTE' FROM estudiante_cronograma " +
                "WHERE id_cronograma = (SELECT id_cronograma FROM cronograma_actividad WHERE id_actividad = ? LIMIT 1)";

        try (Connection conn = ConnectionDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, activityId);
            stmt.setInt(2, activityId);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Integer> getCompletedActivities(int studentId) throws SQLException {
        String sql = "SELECT id_actividad FROM seguimiento_actividad "
                + "WHERE id_estudiante = ? AND completada = TRUE";
        List<Integer> completedActivities = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, studentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    completedActivities.add(resultSet.getInt("id_actividad"));
                }
            }
        }
        return completedActivities;
    }

    public List<Integer> getStudentCronograms(int studentId) throws SQLException {
        String sql = "SELECT id_cronograma FROM estudiante_cronograma WHERE id_estudiante = ?";
        List<Integer> cronogramIds = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, studentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    cronogramIds.add(resultSet.getInt("id_cronograma"));
                }
            }
        }
        return cronogramIds;
    }

    public boolean updateActivityStatus(int studentId, int activityId, ActivityStatus status) throws SQLException {
        String sql = "UPDATE seguimiento_actividad SET estado = ?, completada = ? " +
                "WHERE id_estudiante = ? AND id_actividad = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status.getDbValue());
            statement.setBoolean(2, status == ActivityStatus.Completada);
            statement.setInt(3, studentId);
            statement.setInt(4, activityId);

            return statement.executeUpdate() > 0;
        }
    }

    public Map<Integer, ActivityStatus> getStudentActivitiesStatus(int studentId) throws SQLException {
        String sql = "SELECT id_actividad, estado FROM seguimiento_actividad WHERE id_estudiante = ?";
        Map<Integer, ActivityStatus> statusMap = new HashMap<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, studentId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    statusMap.put(
                            rs.getInt("id_actividad"),
                            ActivityStatus.fromDbValue(rs.getString("estado"))
                    );
                }
            }
        }
        return statusMap;
    }

    public List<Activity> getStudentActivitiesWithStatus(int studentId) throws SQLException {
        String sql = "SELECT a.*, sa.completada FROM actividad a " +
                "JOIN seguimiento_actividad sa ON a.id_actividad = sa.id_actividad " +
                "WHERE sa.id_estudiante = ?";
        List<Activity> activities = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, studentId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    ActivityStatus status = rs.getBoolean("completada") ? ActivityStatus.Completada : ActivityStatus.Pendiente;
                    Activity activity = new Activity(
                            rs.getInt("id_actividad"),
                            rs.getString("nombre"),
                            rs.getString("descripcion"),
                            rs.getTimestamp("fecha_inicial"),
                            rs.getTimestamp("fecha_terminal"),
                            status
                    );
                    activities.add(activity);
                }
            }
        }
        return activities;
    }
}