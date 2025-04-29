package logic.daos;

import dataaccess.ConnectionDataBase;
import logic.logicclasses.Activity;
import logic.logicclasses.ActivityCronogram;
import logic.enums.ActivityStatus;
import logic.interfaces.IActivityCronogramDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityCronogramDAO implements IActivityCronogramDAO {
    private static final Logger logger = LogManager.getLogger(ActivityCronogramDAO.class);

    public boolean addCronogram(ActivityCronogram cronogram) throws SQLException {
        if (cronogram == null) {
            logger.warn("Intento de agregar un cronograma nulo");
            return false;
        }

        logger.debug("Agregando nuevo cronograma con fechas: inicio={}, fin={}",
                cronogram.getDateStart(), cronogram.getDateEnd());

        String sql = "INSERT INTO cronograma_actividades (fecha_inicial, fecha_terminal) VALUES (?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setTimestamp(1, cronogram.getDateStart());
            statement.setTimestamp(2, cronogram.getDateEnd());

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        cronogram.setIdCronogram(generatedId);
                        logger.info("Cronograma agregado exitosamente con ID: {}", generatedId);
                    }
                }
                return true;
            }
            logger.warn("No se pudo agregar el cronograma, ninguna fila afectada");
            return false;
        } catch (SQLException e) {
            logger.error("Error al agregar cronograma", e);
            throw e;
        }
    }

    public boolean updateCronogram(ActivityCronogram cronogram) throws SQLException {
        if (cronogram == null) {
            logger.warn("Intento de actualizar un cronograma nulo");
            return false;
        }

        logger.debug("Actualizando cronograma ID {} con fechas: inicio={}, fin={}",
                cronogram.getIdCronogram(), cronogram.getDateStart(), cronogram.getDateEnd());

        String sql = "UPDATE cronograma_actividades SET fecha_inicial = ?, fecha_terminal = ? WHERE id_cronograma = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setTimestamp(1, cronogram.getDateStart());
            statement.setTimestamp(2, cronogram.getDateEnd());
            statement.setInt(3, cronogram.getIdCronogram());

            boolean result = statement.executeUpdate() > 0;
            if (result) {
                logger.info("Cronograma ID {} actualizado exitosamente", cronogram.getIdCronogram());
            } else {
                logger.warn("No se encontró cronograma ID {} para actualizar", cronogram.getIdCronogram());
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al actualizar cronograma ID {}", cronogram.getIdCronogram(), e);
            throw e;
        }
    }

    public boolean deleteCronogram(int idCronogram) throws SQLException {
        logger.debug("Eliminando cronograma ID: {}", idCronogram);

        String sql = "DELETE FROM cronograma_actividades WHERE id_cronograma = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idCronogram);
            boolean result = statement.executeUpdate() > 0;
            if (result) {
                logger.info("Cronograma ID {} eliminado exitosamente", idCronogram);
            } else {
                logger.warn("No se encontró cronograma ID {} para eliminar", idCronogram);
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al eliminar cronograma ID {}", idCronogram, e);
            throw e;
        }
    }

    public ActivityCronogram getCronogramById(int idCronogram) throws SQLException {
        logger.debug("Obteniendo cronograma por ID: {}", idCronogram);

        String sql = "SELECT * FROM cronograma_actividades WHERE id_cronograma = ?";
        ActivityCronogram cronogram = null;

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idCronogram);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    cronogram = new ActivityCronogram();
                    cronogram.setIdCronogram(resultSet.getInt("id_cronograma"));
                    cronogram.setDateStart(resultSet.getTimestamp("fecha_inicial"));
                    cronogram.setDateEnd(resultSet.getTimestamp("fecha_terminal"));
                    cronogram.setActivities(getActivitiesByCronogram(idCronogram));

                    logger.debug("Cronograma ID {} encontrado", idCronogram);
                } else {
                    logger.info("No se encontró cronograma con ID: {}", idCronogram);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al obtener cronograma ID {}", idCronogram, e);
            throw e;
        }
        return cronogram;
    }

    public List<ActivityCronogram> getAllCronograms() throws SQLException {
        logger.info("Obteniendo todos los cronogramas");

        String sql = "SELECT * FROM cronograma_actividades";
        List<ActivityCronogram> cronograms = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                ActivityCronogram cronogram = new ActivityCronogram();
                int id = resultSet.getInt("id_cronograma");
                cronogram.setIdCronogram(id);
                cronogram.setDateStart(resultSet.getTimestamp("fecha_inicial"));
                cronogram.setDateEnd(resultSet.getTimestamp("fecha_terminal"));
                cronogram.setActivities(getActivitiesByCronogram(id));
                cronograms.add(cronogram);
            }
            logger.debug("Se encontraron {} cronogramas", cronograms.size());
        } catch (SQLException e) {
            logger.error("Error al obtener todos los cronogramas", e);
            throw e;
        }
        return cronograms;
    }

    public boolean addActivityToCronogram(int idCronogram, int idActivity) throws SQLException {
        logger.debug("Agregando actividad ID {} al cronograma ID {}", idActivity, idCronogram);

        String sql = "INSERT INTO cronograma_actividad (id_cronograma, id_actividad) VALUES (?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idCronogram);
            statement.setInt(2, idActivity);

            boolean result = statement.executeUpdate() > 0;
            if (result) {
                logger.info("Actividad ID {} agregada al cronograma ID {}", idActivity, idCronogram);
            } else {
                logger.warn("No se pudo agregar actividad ID {} al cronograma ID {}", idActivity, idCronogram);
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al agregar actividad ID {} al cronograma ID {}", idActivity, idCronogram, e);
            throw e;
        }
    }

    public boolean removeActivityFromCronogram(int idCronogram, int idActivity) throws SQLException {
        logger.debug("Eliminando actividad ID {} del cronograma ID {}", idActivity, idCronogram);

        String sql = "DELETE FROM cronograma_actividad WHERE id_cronograma = ? AND id_actividad = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idCronogram);
            statement.setInt(2, idActivity);

            boolean result = statement.executeUpdate() > 0;
            if (result) {
                logger.info("Actividad ID {} eliminada del cronograma ID {}", idActivity, idCronogram);
            } else {
                logger.warn("No se encontró actividad ID {} en el cronograma ID {}", idActivity, idCronogram);
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al eliminar actividad ID {} del cronograma ID {}", idActivity, idCronogram, e);
            throw e;
        }
    }

    public boolean cronogramExists(int idCronogram) throws SQLException {
        logger.debug("Verificando existencia de cronograma ID: {}", idCronogram);

        String sql = "SELECT 1 FROM cronograma_actividades WHERE id_cronograma = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idCronogram);

            try (ResultSet resultSet = statement.executeQuery()) {
                boolean exists = resultSet.next();
                logger.debug("¿Cronograma ID {} existe?: {}", idCronogram, exists);
                return exists;
            }
        } catch (SQLException e) {
            logger.error("Error al verificar existencia de cronograma ID {}", idCronogram, e);
            throw e;
        }
    }

    private List<Activity> getActivitiesByCronogram(int idCronogram) throws SQLException {
        logger.debug("Obteniendo actividades para cronograma ID: {}", idCronogram);

        String sql = "SELECT a.* FROM actividad a " +
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
                            resultSet.getTimestamp("fecha_inicial"),
                            resultSet.getTimestamp("fecha_terminal"),
                            ActivityStatus.valueOf(resultSet.getString("estado"))
                    ));
                }
            }
            logger.debug("Se encontraron {} actividades para cronograma ID {}", activities.size(), idCronogram);
        } catch (SQLException e) {
            logger.error("Error al obtener actividades para cronograma ID {}", idCronogram, e);
            throw e;
        }
        return activities;
    }
}