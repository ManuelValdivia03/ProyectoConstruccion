package logic.daos;

import dataaccess.ConnectionDataBase;
import logic.logicclasses.Activity;
import logic.enums.ActivityStatus;
import logic.interfaces.IActivityDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityDAO implements IActivityDAO {
    private static final Logger logger = LogManager.getLogger(ActivityDAO.class);

    public boolean addActivity(Activity activity) throws SQLException {
        if (activity == null ||
                activity.getNameActivity() == null ||
                activity.getDescriptionActivity() == null ||
                activity.getStartDate() == null ||
                activity.getEndDate() == null ||
                activity.getActivityStatus() == null) {
            logger.warn("Intento de agregar actividad con datos incompletos o nulos");
            throw new SQLException("Datos de actividad incompletos o nulos");
        }

        logger.debug("Agregando nueva actividad: {}", activity.getNameActivity());

        String sql = "INSERT INTO actividad (nombre, descripcion, fecha_inicial, fecha_terminal, estado) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, activity.getNameActivity());
            statement.setString(2, activity.getDescriptionActivity());
            statement.setDate(3, new java.sql.Date(activity.getStartDate().getTime()));
            statement.setDate(4, new java.sql.Date(activity.getEndDate().getTime()));
            statement.setString(5, activity.getActivityStatus().getDbValue());

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        activity.setIdActivity(generatedId);
                        logger.info("Actividad agregada exitosamente con ID: {}", generatedId);
                    }
                }
                return true;
            }
            logger.warn("No se pudo agregar la actividad, ninguna fila afectada");
            return false;
        } catch (SQLException e) {
            logger.error("Error al agregar actividad '{}'", activity.getNameActivity(), e);
            throw e;
        }
    }

    public boolean updateActivity(Activity activity) throws SQLException {
        if (activity == null) {
            logger.warn("Intento de actualizar actividad nula");
            throw new SQLException("La actividad no puede ser nula");
        }

        logger.debug("Actualizando actividad ID: {}", activity.getIdActivity());

        String sql = "UPDATE actividad SET nombre = ?, descripcion = ?, estado = ?, fecha_inicial = ?, fecha_terminal = ? WHERE id_actividad = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, activity.getNameActivity());
            statement.setString(2, activity.getDescriptionActivity());
            statement.setString(3, activity.getActivityStatus().toString());
            statement.setDate(4, new java.sql.Date(activity.getStartDate().getTime()));
            statement.setDate(5, new java.sql.Date(activity.getEndDate().getTime()));
            statement.setInt(6, activity.getIdActivity());

            boolean result = statement.executeUpdate() > 0;
            if (result) {
                logger.info("Actividad ID {} actualizada exitosamente", activity.getIdActivity());
            } else {
                logger.warn("No se encontró actividad ID {} para actualizar", activity.getIdActivity());
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al actualizar actividad ID {}", activity.getIdActivity(), e);
            throw e;
        }
    }

    public boolean deleteActivity(int idActivity) throws SQLException {
        logger.debug("Eliminando actividad ID: {}", idActivity);

        String sql = "DELETE FROM actividad WHERE id_actividad = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idActivity);
            boolean result = statement.executeUpdate() > 0;
            if (result) {
                logger.info("Actividad ID {} eliminada exitosamente", idActivity);
            } else {
                logger.warn("No se encontró actividad ID {} para eliminar", idActivity);
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al eliminar actividad ID {}", idActivity, e);
            throw e;
        }
    }

    public Activity getActivityById(int idActivity) throws SQLException {
        logger.debug("Buscando actividad por ID: {}", idActivity);

        String sql = "SELECT * FROM actividad WHERE id_actividad = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idActivity);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    logger.debug("Actividad ID {} encontrada", idActivity);
                    // Convierte java.sql.Date a Timestamp
                    java.sql.Date startDate = resultSet.getDate("fecha_inicial");
                    java.sql.Date endDate = resultSet.getDate("fecha_terminal");
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
        } catch (SQLException e) {
            logger.error("Error al obtener actividad ID {}", idActivity, e);
            throw e;
        }
        logger.info("No se encontró actividad con ID: {}", idActivity);
        return null;
    }

    public List<Activity> getAllActivities() throws SQLException {
        logger.info("Obteniendo todas las actividades");

        String sql = "SELECT * FROM actividad";
        List<Activity> activities = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                java.sql.Date startDate = resultSet.getDate("fecha_inicial");
                java.sql.Date endDate = resultSet.getDate("fecha_terminal");
                activities.add(new Activity(
                        resultSet.getInt("id_actividad"),
                        resultSet.getString("nombre"),
                        resultSet.getString("descripcion"),
                        startDate != null ? new Timestamp(startDate.getTime()) : null,
                        endDate != null ? new Timestamp(endDate.getTime()) : null,
                        ActivityStatus.fromDbValue(resultSet.getString("estado"))
                ));
            }
            logger.debug("Se encontraron {} actividades", activities.size());
        } catch (SQLException e) {
            logger.error("Error al obtener todas las actividades", e);
            throw e;
        }
        return activities;
    }

    public List<Activity> getActivitiesByStatus(ActivityStatus status) throws SQLException {
        if (status == null) {
            logger.warn("Intento de buscar actividades con estado nulo");
            throw new SQLException("El estado no puede ser nulo");
        }

        logger.debug("Buscando actividades con estado: {}", status);

        String sql = "SELECT * FROM actividad WHERE estado = ?";
        List<Activity> activities = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status.toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    java.sql.Date startDate = resultSet.getDate("fecha_inicial");
                    java.sql.Date endDate = resultSet.getDate("fecha_terminal");
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
            logger.debug("Se encontraron {} actividades con estado {}", activities.size(), status);
        } catch (SQLException e) {
            logger.error("Error al obtener actividades con estado {}", status, e);
            throw e;
        }
        return activities;
    }

    public boolean changeActivityStatus(int idActivity, ActivityStatus newStatus) throws SQLException {
        if (newStatus == null) {
            logger.warn("Intento de cambiar estado a nulo para actividad ID: {}", idActivity);
            throw new SQLException("El nuevo estado no puede ser nulo");
        }

        logger.debug("Cambiando estado de actividad ID {} a {}", idActivity, newStatus);

        String sql = "UPDATE actividad SET estado = ? WHERE id_actividad = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, newStatus.toString());
            statement.setInt(2, idActivity);

            boolean result = statement.executeUpdate() > 0;
            if (result) {
                logger.info("Estado de actividad ID {} cambiado a {}", idActivity, newStatus);
            } else {
                logger.warn("No se encontró actividad ID {} para cambiar estado", idActivity);
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al cambiar estado de actividad ID {}", idActivity, e);
            throw e;
        }
    }

    public boolean activityExists(int idActivity) throws SQLException {
        logger.debug("Verificando existencia de actividad ID: {}", idActivity);

        String sql = "SELECT 1 FROM actividad WHERE id_actividad = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idActivity);

            try (ResultSet resultSet = statement.executeQuery()) {
                boolean exists = resultSet.next();
                logger.debug("¿Actividad ID {} existe?: {}", idActivity, exists);
                return exists;
            }
        } catch (SQLException e) {
            logger.error("Error al verificar existencia de actividad ID {}", idActivity, e);
            throw e;
        }
    }

    public boolean assignActivityToStudent(int idActivity, int idStudent) throws SQLException {
        logger.debug("Asignando actividad ID {} a estudiante ID {}", idActivity, idStudent);

        String sql = "UPDATE actividad SET id_usuario = ? WHERE id_actividad = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idStudent);
            statement.setInt(2, idActivity);

            boolean result = statement.executeUpdate() > 0;
            if (result) {
                logger.info("Actividad ID {} asignada a estudiante ID {}", idActivity, idStudent);
            } else {
                logger.warn("No se pudo asignar actividad ID {} a estudiante ID {}", idActivity, idStudent);
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al asignar actividad ID {} a estudiante ID {}", idActivity, idStudent, e);
            throw e;
        }
    }

    public boolean assignActivityToCronogram(int idActivity, int idCronogram) throws SQLException {
        logger.debug("Asignando actividad ID {} a cronograma ID {}", idActivity, idCronogram);

        String sql = "UPDATE actividad SET id_cronograma = ? WHERE id_actividad = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idCronogram);
            statement.setInt(2, idActivity);

            boolean result = statement.executeUpdate() > 0;
            if (result) {
                logger.info("Actividad ID {} asignada a cronograma ID {}", idActivity, idCronogram);
            } else {
                logger.warn("No se pudo asignar actividad ID {} a cronograma ID {}", idActivity, idCronogram);
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al asignar actividad ID {} a cronograma ID {}", idActivity, idCronogram, e);
            throw e;
        }
    }
}
