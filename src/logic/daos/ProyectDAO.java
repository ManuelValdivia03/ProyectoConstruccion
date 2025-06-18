package logic.daos;

import logic.logicclasses.Proyect;
import logic.interfaces.IProyectDAO;
import logic.logicclasses.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static dataaccess.ConnectionDataBase.getConnection;

public class ProyectDAO implements IProyectDAO {
    private static final Logger logger = LogManager.getLogger(ProyectDAO.class);
    private static final Proyect EMPTY_PROYECT = new Proyect(-1, "", "", null, null, 'I');

    public boolean addProyect(Proyect proyect) throws SQLException {
        if (proyect == null) {
            throw new IllegalArgumentException("El proyecto no debe ser nulo");
        }

        logger.debug("Agregando nuevo proyecto: {}", proyect.getTitle());

        String sql = "INSERT INTO proyecto (titulo, descripcion, fecha_inicial, fecha_terminal, estado) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, proyect.getTitle());
            preparedStatement.setString(2, proyect.getDescription());
            preparedStatement.setTimestamp(3, proyect.getDateStart());
            preparedStatement.setTimestamp(4, proyect.getDateEnd());
            preparedStatement.setString(5, String.valueOf(proyect.getStatus()));

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        proyect.setIdProyect(generatedId);
                        logger.info("Proyecto agregado exitosamente - ID: {}, Título: {}",
                                generatedId, proyect.getTitle());
                        return true;
                    }
                }
            }
            logger.warn("No se pudo agregar el proyecto: {}", proyect.getTitle());
            return false;
        } catch (SQLException e) {
            logger.error("Error al agregar proyecto: {}", proyect.getTitle(), e);
            throw e;
        }
    }

    public boolean updateProyect(Proyect proyect) throws SQLException {
        if (proyect == null) {
            throw new IllegalArgumentException("El proyecto no debe ser nulo");
        }

        logger.debug("Actualizando proyecto ID: {}", proyect.getIdProyect());

        String sql = "UPDATE proyecto SET titulo = ?, descripcion = ?, fecha_inicial = ?, fecha_terminal = ?, estado = ? WHERE id_proyecto = ?";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, proyect.getTitle());
            preparedStatement.setString(2, proyect.getDescription());
            preparedStatement.setTimestamp(3, proyect.getDateStart());
            preparedStatement.setTimestamp(4, proyect.getDateEnd());
            preparedStatement.setString(5, String.valueOf(proyect.getStatus()));
            preparedStatement.setInt(6, proyect.getIdProyect());

            boolean result = preparedStatement.executeUpdate() > 0;
            if (result) {
                logger.info("Proyecto actualizado exitosamente - ID: {}", proyect.getIdProyect());
            } else {
                logger.warn("No se encontró proyecto con ID: {} para actualizar", proyect.getIdProyect());
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al actualizar proyecto ID: {}", proyect.getIdProyect(), e);
            throw e;
        }
    }

    public boolean deleteProyect(Proyect proyect) throws SQLException {
        if (proyect == null) {
            throw new IllegalArgumentException("El proyecto no debe ser nulo");
        }

        logger.debug("Eliminando proyecto ID: {}", proyect.getIdProyect());

        String sql = "DELETE FROM proyecto WHERE id_proyecto = ?";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, proyect.getIdProyect());
            boolean result = preparedStatement.executeUpdate() > 0;
            if (result) {
                logger.info("Proyecto eliminado exitosamente - ID: {}", proyect.getIdProyect());
            } else {
                logger.warn("No se encontró proyecto con ID: {} para eliminar", proyect.getIdProyect());
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al eliminar proyecto ID: {}", proyect.getIdProyect(), e);
            throw e;
        }
    }

    public List<Proyect> getAllProyects() throws SQLException {
        logger.info("Obteniendo todos los proyectos");

        String sql = "SELECT id_proyecto, titulo, descripcion, fecha_inicial, fecha_terminal, estado FROM proyecto";
        List<Proyect> proyects = new ArrayList<>();

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Proyect proyect = new Proyect(
                        resultSet.getInt("id_proyecto"),
                        resultSet.getString("titulo"),
                        resultSet.getString("descripcion"),
                        resultSet.getTimestamp("fecha_inicial"),
                        resultSet.getTimestamp("fecha_terminal"),
                        resultSet.getString("estado").charAt(0)
                );
                proyects.add(proyect);
            }
            logger.debug("Se encontraron {} proyectos", proyects.size());
        } catch (SQLException e) {
            logger.error("Error al obtener todos los proyectos", e);
            throw e;
        }
        return proyects;
    }

    public List<Proyect> getProyectsByStatus(char status) throws SQLException {
        logger.debug("Buscando proyectos con estado: {}", status);

        String sql = "SELECT id_proyecto, titulo, descripcion, fecha_inicial, fecha_terminal, estado FROM proyecto WHERE estado = ?";
        List<Proyect> proyects = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, String.valueOf(status));
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Proyect proyect = new Proyect(
                            resultSet.getInt("id_proyecto"),
                            resultSet.getString("titulo"),
                            resultSet.getString("descripcion"),
                            resultSet.getTimestamp("fecha_inicial"),
                            resultSet.getTimestamp("fecha_terminal"),
                            resultSet.getString("estado").charAt(0)
                    );
                    proyects.add(proyect);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al buscar proyectos por estado: {}", status, e);
            throw e;
        }
        logger.info("Se encontraron {} proyectos con estado: {}", proyects.size(), status);
        return proyects;
    }

    public Proyect getProyectById(int id) throws SQLException {
        if (id <= 0) {
            return EMPTY_PROYECT;
        }

        logger.debug("Buscando proyecto por ID: {}", id);

        String sql = "SELECT id_proyecto, titulo, descripcion, fecha_inicial, fecha_terminal, estado FROM proyecto WHERE id_proyecto = ?";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    logger.debug("Proyecto encontrado con ID: {}", id);
                    return new Proyect(
                            resultSet.getInt("id_proyecto"),
                            resultSet.getString("titulo"),
                            resultSet.getString("descripcion"),
                            resultSet.getTimestamp("fecha_inicial"),
                            resultSet.getTimestamp("fecha_terminal"),
                            resultSet.getString("estado").charAt(0)
                    );
                }
            }
        } catch (SQLException e) {
            logger.error("Error al buscar proyecto por ID: {}", id, e);
            throw e;
        }
        logger.info("No se encontró proyecto con ID: {}", id);
        return EMPTY_PROYECT;
    }

    public Proyect getProyectByTitle(String title) throws SQLException {
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("El título no debe ser nulo o vacío");
        }

        logger.debug("Buscando proyecto por título: {}", title);

        String sql = "SELECT id_proyecto, titulo, descripcion, fecha_inicial, fecha_terminal, estado FROM proyecto WHERE titulo = ?";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, title);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    logger.debug("Proyecto encontrado con título: {}", title);
                    return new Proyect(
                            resultSet.getInt("id_proyecto"),
                            resultSet.getString("titulo"),
                            resultSet.getString("descripcion"),
                            resultSet.getTimestamp("fecha_inicial"),
                            resultSet.getTimestamp("fecha_terminal"),
                            resultSet.getString("estado").charAt(0)
                    );
                }
            }
        } catch (SQLException e) {
            logger.error("Error al buscar proyecto por título: {}", title, e);
            throw e;
        }
        logger.info("No se encontró proyecto con título: {}", title);
        return EMPTY_PROYECT;
    }

    public int countProyects() throws SQLException {
        logger.debug("Contando proyectos");

        String sql = "SELECT COUNT(*) FROM proyecto";

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            int count = resultSet.next() ? resultSet.getInt(1) : 0;
            logger.info("Total de proyectos: {}", count);
            return count;
        } catch (SQLException e) {
            logger.error("Error al contar proyectos", e);
            throw e;
        }
    }

    public boolean proyectExists(String title) throws SQLException {
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("El título no debe ser nulo o vacío");
        }

        logger.debug("Verificando existencia de proyecto con título: {}", title);

        String sql = "SELECT 1 FROM proyecto WHERE titulo = ?";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, title);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                boolean exists = resultSet.next();
                logger.debug("¿Proyecto '{}' existe?: {}", title, exists);
                return exists;
            }
        } catch (SQLException e) {
            logger.error("Error al verificar existencia de proyecto: {}", title, e);
            throw e;
        }
    }

    public boolean changeProyectStatus(Proyect proyect) throws SQLException {
        if (proyect == null) {
            throw new IllegalArgumentException("El proyecto no debe ser nulo");
        }

        logger.debug("Cambiando estado de proyecto ID: {} a {}",
                proyect.getIdProyect(), proyect.getStatus());

        String sql = "UPDATE proyecto SET estado = ? WHERE id_proyecto = ?";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, String.valueOf(proyect.getStatus()));
            preparedStatement.setInt(2, proyect.getIdProyect());

            boolean result = preparedStatement.executeUpdate() > 0;
            if (result) {
                logger.info("Estado de proyecto ID {} cambiado a {}",
                        proyect.getIdProyect(), proyect.getStatus());
            } else {
                logger.warn("No se encontró proyecto ID {} para cambiar estado",
                        proyect.getIdProyect());
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al cambiar estado de proyecto ID: {}",
                    proyect.getIdProyect(), e);
            throw e;
        }
    }

    public int addProyectAndGetId(Proyect proyect) throws SQLException {
        if (proyect == null) {
            throw new IllegalArgumentException("El proyecto no debe ser nulo");
        }
        String sql = "INSERT INTO proyecto (titulo, descripcion, fecha_inicial, fecha_terminal, estado) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, proyect.getTitle());
            statement.setString(2, proyect.getDescription());
            statement.setTimestamp(3, proyect.getDateStart());
            statement.setTimestamp(4, proyect.getDateEnd());
            statement.setString(5, String.valueOf(proyect.getStatus()));

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating project failed, no rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating project failed, no ID obtained.");
                }
            }
        }
    }

    public boolean linkProjectToRepresentative(int projectId, int representativeId) throws SQLException {
        if (projectId <= 0 || representativeId <= 0) {
            return false;
        }

        String sql = "UPDATE proyecto SET id_representante = ? WHERE id_proyecto = ?";

        try (Connection conn = getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, representativeId);
            statement.setInt(2, projectId);

            return statement.executeUpdate() > 0;
        }
    }

    public List<Proyect> getUnassignedProyects() throws SQLException {
        String sql = "SELECT * FROM proyecto WHERE id_usuario IS NULL AND estado = 'A'";
        try (Connection conn = getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            return executeQuery(statement);
        }
    }

    public Proyect getUnassignedProyectByTitle(String title) throws SQLException {
        String sql = "SELECT * FROM proyecto WHERE titulo LIKE ? AND id_usuario IS NULL AND estado = 'A' LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + title + "%");
            List<Proyect> result = executeQuery(stmt);
            return result.isEmpty() ? null : result.get(0);
        }
    }

    public boolean assignProjectToCurrentUser(int projectId, User user) throws SQLException {
        String sql = "UPDATE proyecto SET id_usuario = ? WHERE id_proyecto = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, getCurrentUserId(user));
            stmt.setInt(2, projectId);
            return stmt.executeUpdate() > 0;
        }
    }

    private int getCurrentUserId(User user) throws SQLException {
        return user != null ? user.getIdUser() : -1;
    }

    private List<Proyect> executeQuery(PreparedStatement statement) throws SQLException {
        List<Proyect> proyects = new ArrayList<>();
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Proyect proyect = new Proyect(
                        resultSet.getInt("id_proyecto"),
                        resultSet.getString("titulo"),
                        resultSet.getString("descripcion"),
                        resultSet.getTimestamp("fecha_inicial"),
                        resultSet.getTimestamp("fecha_terminal"),
                        resultSet.getString("estado").charAt(0)
                );
                proyects.add(proyect);
            }
        }
        return proyects;
    }
}
