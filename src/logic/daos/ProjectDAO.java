package logic.daos;

import logic.logicclasses.Project;
import logic.interfaces.IProjectDAO;
import logic.logicclasses.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static dataaccess.ConnectionDataBase.getConnection;

public class ProjectDAO implements IProjectDAO {
    private static final Logger logger = LogManager.getLogger(ProjectDAO.class);
    private static final Project EMPTY_PROJECT = new Project(-1, "", "", null, null, 'I', 0, 0);

    public boolean addProyect(Project project) throws SQLException, IllegalArgumentException {
        if (project == null) {
            throw new IllegalArgumentException("El proyecto no debe ser nulo");
        }

        logger.debug("Agregando nuevo proyecto: {}", project.getTitle());

        String query = "INSERT INTO proyecto (titulo, descripcion, fecha_inicial, fecha_terminal, estado, cupo, estudiantes_actuales) VALUES (?, ?, ?, ?, ?, ?, 0)";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, project.getTitle());
            preparedStatement.setString(2, project.getDescription());
            preparedStatement.setTimestamp(3, project.getDateStart());
            preparedStatement.setTimestamp(4, project.getDateEnd());
            preparedStatement.setString(5, String.valueOf(project.getStatus()));
            preparedStatement.setInt(6, project.getCapacity());

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        project.setIdProyect(generatedId);
                        logger.info("Proyecto agregado exitosamente - ID: {}, Título: {}",
                                generatedId, project.getTitle());
                        return true;
                    }
                }
            }
            logger.warn("No se pudo agregar el proyecto: {}", project.getTitle());
            return false;
        } catch (SQLException e) {
            logger.error("Error al agregar proyecto: {}", project.getTitle(), e);
            throw e;
        }
    }

    public boolean updateProyect(Project project) throws SQLException, IllegalArgumentException {
        if (project == null) {
            throw new IllegalArgumentException("El proyecto no debe ser nulo");
        }

        logger.debug("Actualizando proyecto ID: {}", project.getIdProyect());

        String query = "UPDATE proyecto SET titulo = ?, descripcion = ?, fecha_inicial = ?, " +
                    "fecha_terminal = ?, estado = ?, cupo = ?, estudiantes_actuales = ? " +
                    "WHERE id_proyecto = ?";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, project.getTitle());
            preparedStatement.setString(2, project.getDescription());
            preparedStatement.setTimestamp(3, project.getDateStart());
            preparedStatement.setTimestamp(4, project.getDateEnd());
            preparedStatement.setString(5, String.valueOf(project.getStatus()));
            preparedStatement.setInt(6, project.getCapacity());
            preparedStatement.setInt(7, project.getCurrentStudents());
            preparedStatement.setInt(8, project.getIdProyect());

            boolean result = preparedStatement.executeUpdate() > 0;
            if (result) {
                logger.info("Proyecto actualizado exitosamente - ID: {}", project.getIdProyect());
            } else {
                logger.warn("No se encontró proyecto con ID: {} para actualizar", project.getIdProyect());
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al actualizar proyecto ID: {}", project.getIdProyect(), e);
            throw e;
        }
    }

    public boolean deleteProyect(Project project) throws SQLException, IllegalArgumentException {
        if (project == null) {
            throw new IllegalArgumentException("El proyecto no debe ser nulo");
        }

        logger.debug("Eliminando proyecto ID: {}", project.getIdProyect());

        String query = "DELETE FROM proyecto WHERE id_proyecto = ?";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, project.getIdProyect());
            boolean result = preparedStatement.executeUpdate() > 0;
            if (result) {
                logger.info("Proyecto eliminado exitosamente - ID: {}", project.getIdProyect());
            } else {
                logger.warn("No se encontró proyecto con ID: {} para eliminar", project.getIdProyect());
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al eliminar proyecto ID: {}", project.getIdProyect(), e);
            throw e;
        }
    }

    public List<Project> getAllProyects() throws SQLException {
        logger.info("Obteniendo todos los proyectos");

        String query = "SELECT id_proyecto, titulo, descripcion, fecha_inicial, fecha_terminal, estado, cupo, estudiantes_actuales FROM proyecto";
        List<Project> projects = new ArrayList<>();

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                Project project = new Project(
                        resultSet.getInt("id_proyecto"),
                        resultSet.getString("titulo"),
                        resultSet.getString("descripcion"),
                        resultSet.getTimestamp("fecha_inicial"),
                        resultSet.getTimestamp("fecha_terminal"),
                        resultSet.getString("estado").charAt(0),
                        resultSet.getInt("cupo"),
                        resultSet.getInt("estudiantes_actuales")
                );
                projects.add(project);
            }
            logger.debug("Se encontraron {} proyectos", projects.size());
        } catch (SQLException e) {
            logger.error("Error al obtener todos los proyectos", e);
            throw e;
        }
        return projects;
    }

    public List<Project> getProyectsByStatus(char status) throws SQLException {
        logger.debug("Buscando proyectos con estado: {}", status);

        String query = "SELECT id_proyecto, titulo, descripcion, fecha_inicial, fecha_terminal, estado, cupo, estudiantes_actuales FROM proyecto WHERE estado = ?";
        List<Project> projects = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, String.valueOf(status));
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Project project = new Project(
                            resultSet.getInt("id_proyecto"),
                            resultSet.getString("titulo"),
                            resultSet.getString("descripcion"),
                            resultSet.getTimestamp("fecha_inicial"),
                            resultSet.getTimestamp("fecha_terminal"),
                            resultSet.getString("estado").charAt(0),
                            resultSet.getInt("cupo"),
                            resultSet.getInt("estudiantes_actuales")
                    );
                    projects.add(project);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al buscar proyectos por estado: {}", status, e);
            throw e;
        }
        logger.info("Se encontraron {} proyectos con estado: {}", projects.size(), status);
        return projects;
    }

    public Project getProyectById(int id) throws SQLException {
        String query = "SELECT id_proyecto, titulo, descripcion, fecha_inicial, fecha_terminal, estado, cupo, estudiantes_actuales " +
                "FROM proyecto WHERE id_proyecto = ?";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new Project(
                            resultSet.getInt("id_proyecto"),
                            resultSet.getString("titulo"),
                            resultSet.getString("descripcion"),
                            resultSet.getTimestamp("fecha_inicial"),
                            resultSet.getTimestamp("fecha_terminal"),
                            resultSet.getString("estado").charAt(0),
                            resultSet.getInt("cupo"),
                            resultSet.getInt("estudiantes_actuales")
                    );
                }
            }
        }
        return EMPTY_PROJECT;
    }

    public Project getProyectByTitle(String title) throws SQLException, IllegalArgumentException {
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("El título no debe ser nulo o vacío");
        }

        logger.debug("Buscando proyecto por título: {}", title);

        String query = "SELECT id_proyecto, titulo, descripcion, fecha_inicial, fecha_terminal, estado, cupo, estudiantes_actuales FROM proyecto WHERE titulo = ?";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, title);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    logger.debug("Proyecto encontrado con título: {}", title);
                    return new Project(
                            resultSet.getInt("id_proyecto"),
                            resultSet.getString("titulo"),
                            resultSet.getString("descripcion"),
                            resultSet.getTimestamp("fecha_inicial"),
                            resultSet.getTimestamp("fecha_terminal"),
                            resultSet.getString("estado").charAt(0),
                            resultSet.getInt("cupo"),
                            resultSet.getInt("estudiantes_actuales")
                    );
                }
            }
        } catch (SQLException e) {
            logger.error("Error al buscar proyecto por título: {}", title, e);
            throw e;
        }
        logger.info("No se encontró proyecto con título: {}", title);
        return EMPTY_PROJECT;
    }

    public int countProyects() throws SQLException {
        logger.debug("Contando proyectos");

        String query = "SELECT COUNT(*) FROM proyecto";

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            int count = resultSet.next() ? resultSet.getInt(1) : 0;
            logger.info("Total de proyectos: {}", count);
            return count;
        } catch (SQLException e) {
            logger.error("Error al contar proyectos", e);
            throw e;
        }
    }

    public boolean proyectExists(String title) throws SQLException, IllegalArgumentException {
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("El título no debe ser nulo o vacío");
        }

        logger.debug("Verificando existencia de proyecto con título: {}", title);

        String query = "SELECT 1 FROM proyecto WHERE titulo = ?";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

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

    public boolean changeProyectStatus(Project project) throws SQLException, IllegalArgumentException {
        if (project == null) {
            throw new IllegalArgumentException("El proyecto no debe ser nulo");
        }

        logger.debug("Cambiando estado de proyecto ID: {} a {}",
                project.getIdProyect(), project.getStatus());

        String query = "UPDATE proyecto SET estado = ? WHERE id_proyecto = ?";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, String.valueOf(project.getStatus()));
            preparedStatement.setInt(2, project.getIdProyect());

            boolean result = preparedStatement.executeUpdate() > 0;
            if (result) {
                logger.info("Estado de proyecto ID {} cambiado a {}",
                        project.getIdProyect(), project.getStatus());
            } else {
                logger.warn("No se encontró proyecto ID {} para cambiar estado",
                        project.getIdProyect());
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al cambiar estado de proyecto ID: {}",
                    project.getIdProyect(), e);
            throw e;
        }
    }

    public int addProyectAndGetId(Project project) throws SQLException, IllegalArgumentException {
        if (project == null) {
            throw new IllegalArgumentException("El proyecto no debe ser nulo");
        }
        String query = "INSERT INTO proyecto (titulo, descripcion, fecha_inicial, fecha_terminal, " +
                    "estado, cupo, estudiantes_actuales) VALUES (?, ?, ?, ?, ?, ?, 0)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, project.getTitle());
            statement.setString(2, project.getDescription());
            statement.setTimestamp(3, project.getDateStart());
            statement.setTimestamp(4, project.getDateEnd());
            statement.setString(5, String.valueOf(project.getStatus()));
            statement.setInt(6, project.getCapacity());

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

        String query = "UPDATE proyecto SET id_representante = ? WHERE id_proyecto = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, representativeId);
            statement.setInt(2, projectId);

            return statement.executeUpdate() > 0;
        }
    }


    public boolean assignProjectToCurrentUser(int projectId, User user) throws SQLException {
        String query = "UPDATE proyecto SET id_usuario = ? WHERE id_proyecto = ?";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, getCurrentUserId(user));
            preparedStatement.setInt(2, projectId);
            return preparedStatement.executeUpdate() > 0;
        }
    }

    private int getCurrentUserId(User user) throws SQLException {
        return user != null ? user.getIdUser() : -1;
    }

    public List<Project> getAvailableProjects() throws SQLException {
        logger.debug("Obteniendo proyectos disponibles");

        String query = "SELECT id_proyecto, titulo, descripcion, fecha_inicial, fecha_terminal, estado, cupo, estudiantes_actuales " +
                     "FROM proyecto WHERE estado = 'A' AND cupo > 0";

        List<Project> projects = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                Project project = new Project(
                        resultSet.getInt("id_proyecto"),
                        resultSet.getString("titulo"),
                        resultSet.getString("descripcion"),
                        resultSet.getTimestamp("fecha_inicial"),
                        resultSet.getTimestamp("fecha_terminal"),
                        resultSet.getString("estado").charAt(0),
                        resultSet.getInt("cupo"),
                        resultSet.getInt("estudiantes_actuales")
                );
                projects.add(project);
            }
            logger.info("Se encontraron {} proyectos disponibles", projects.size());
        } catch (SQLException e) {
            logger.error("Error al obtener proyectos disponibles", e);
            throw e;
        }
        return projects;
    }

    public boolean incrementStudentCount(int projectId) throws SQLException {
        String query = "UPDATE proyecto SET estudiantes_actuales = estudiantes_actuales + 1 " +
                "WHERE id_proyecto = ? AND estudiantes_actuales < cupo";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, projectId);
            return preparedStatement.executeUpdate() > 0;
        }
    }
}
