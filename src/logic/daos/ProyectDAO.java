package logic.daos;

import dataaccess.ConnectionDataBase;
import logic.logicclasses.Proyect;
import logic.interfaces.IProyectDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static dataaccess.ConnectionDataBase.getConnection;

public class ProyectDAO implements IProyectDAO {
    private static final Logger logger = LogManager.getLogger(ProyectDAO.class);

    public boolean addProyect(Proyect proyect) throws SQLException {
        if (proyect == null) {
            logger.warn("Intento de agregar proyecto nulo");
            return false;
        }

        logger.debug("Agregando nuevo proyecto: {}", proyect.getTitle());

        String sql = "INSERT INTO proyecto (titulo, descripcion, fecha_inicial, fecha_terminal, estado) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, proyect.getTitle());
            ps.setString(2, proyect.getDescription());
            ps.setTimestamp(3, proyect.getDateStart());
            ps.setTimestamp(4, proyect.getDateEnd());
            ps.setString(5, String.valueOf(proyect.getStatus()));

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
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
            logger.warn("Intento de actualizar proyecto nulo");
            return false;
        }

        logger.debug("Actualizando proyecto ID: {}", proyect.getIdProyect());

        String sql = "UPDATE proyecto SET titulo = ?, descripcion = ?, fecha_inicial = ?, fecha_terminal = ?, estado = ? WHERE id_proyecto = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, proyect.getTitle());
            ps.setString(2, proyect.getDescription());
            ps.setTimestamp(3, proyect.getDateStart());
            ps.setTimestamp(4, proyect.getDateEnd());
            ps.setString(5, String.valueOf(proyect.getStatus()));
            ps.setInt(6, proyect.getIdProyect());

            boolean result = ps.executeUpdate() > 0;
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
            logger.warn("Intento de eliminar proyecto nulo");
            return false;
        }

        logger.debug("Eliminando proyecto ID: {}", proyect.getIdProyect());

        String sql = "DELETE FROM proyecto WHERE id_proyecto = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, proyect.getIdProyect());
            boolean result = ps.executeUpdate() > 0;
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
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Proyect proyect = new Proyect(
                        rs.getInt("id_proyecto"),
                        rs.getString("titulo"),
                        rs.getString("descripcion"),
                        rs.getTimestamp("fecha_inicial"),
                        rs.getTimestamp("fecha_terminal"),
                        rs.getString("estado").charAt(0)
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
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, String.valueOf(status));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Proyect proyect = new Proyect(
                            rs.getInt("id_proyecto"),
                            rs.getString("titulo"),
                            rs.getString("descripcion"),
                            rs.getTimestamp("fecha_inicial"),
                            rs.getTimestamp("fecha_terminal"),
                            rs.getString("estado").charAt(0)
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
            logger.warn("Intento de buscar proyecto con ID inválido: {}", id);
            return null;
        }

        logger.debug("Buscando proyecto por ID: {}", id);

        String sql = "SELECT id_proyecto, titulo, descripcion, fecha_inicial, fecha_terminal, estado FROM proyecto WHERE id_proyecto = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    logger.debug("Proyecto encontrado con ID: {}", id);
                    return new Proyect(
                            rs.getInt("id_proyecto"),
                            rs.getString("titulo"),
                            rs.getString("descripcion"),
                            rs.getTimestamp("fecha_inicial"),
                            rs.getTimestamp("fecha_terminal"),
                            rs.getString("estado").charAt(0)
                    );
                }
            }
        } catch (SQLException e) {
            logger.error("Error al buscar proyecto por ID: {}", id, e);
            throw e;
        }
        logger.info("No se encontró proyecto con ID: {}", id);
        return null;
    }

    public Proyect getProyectByTitle(String title) throws SQLException {
        if (title == null || title.isEmpty()) {
            logger.warn("Intento de buscar proyecto con título nulo o vacío");
            return null;
        }

        logger.debug("Buscando proyecto por título: {}", title);

        String sql = "SELECT id_proyecto, titulo, descripcion, fecha_inicial, fecha_terminal, estado FROM proyecto WHERE titulo = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, title);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    logger.debug("Proyecto encontrado con título: {}", title);
                    return new Proyect(
                            rs.getInt("id_proyecto"),
                            rs.getString("titulo"),
                            rs.getString("descripcion"),
                            rs.getTimestamp("fecha_inicial"),
                            rs.getTimestamp("fecha_terminal"),
                            rs.getString("estado").charAt(0)
                    );
                }
            }
        } catch (SQLException e) {
            logger.error("Error al buscar proyecto por título: {}", title, e);
            throw e;
        }
        logger.info("No se encontró proyecto con título: {}", title);
        return null;
    }

    public int countProyects() throws SQLException {
        logger.debug("Contando proyectos");

        String sql = "SELECT COUNT(*) FROM proyecto";

        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int count = rs.next() ? rs.getInt(1) : 0;
            logger.info("Total de proyectos: {}", count);
            return count;
        } catch (SQLException e) {
            logger.error("Error al contar proyectos", e);
            throw e;
        }
    }

    public boolean proyectExists(String title) throws SQLException {
        if (title == null || title.isEmpty()) {
            logger.warn("Intento de verificar existencia con título nulo o vacío");
            return false;
        }

        logger.debug("Verificando existencia de proyecto con título: {}", title);

        String sql = "SELECT 1 FROM proyecto WHERE titulo = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, title);
            try (ResultSet rs = ps.executeQuery()) {
                boolean exists = rs.next();
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
            logger.warn("Intento de cambiar estado de proyecto nulo");
            return false;
        }

        logger.debug("Cambiando estado de proyecto ID: {} a {}",
                proyect.getIdProyect(), proyect.getStatus());

        String sql = "UPDATE proyecto SET estado = ? WHERE id_proyecto = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, String.valueOf(proyect.getStatus()));
            ps.setInt(2, proyect.getIdProyect());

            boolean result = ps.executeUpdate() > 0;
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
        String sql = "INSERT INTO proyecto (titulo, descripcion, fecha_inicial, fecha_terminal, estado) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, proyect.getTitle());
            stmt.setString(2, proyect.getDescription());
            stmt.setTimestamp(3, proyect.getDateStart());
            stmt.setTimestamp(4, proyect.getDateEnd());
            stmt.setString(5, String.valueOf(proyect.getStatus()));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating project failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating project failed, no ID obtained.");
                }
            }
        }
    }

    public boolean linkProjectToRepresentative(int projectId, int representativeId, int organizationId) throws SQLException {
        String sql = "UPDATE proyecto SET id_representante = ?, id_organizacion = ? WHERE id_proyecto = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, representativeId);
            stmt.setInt(2, organizationId);
            stmt.setInt(3, projectId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    public boolean linkProjectToRepresentative(int projectId, int representativeId, Integer organizationId) throws SQLException {
        String sql = "UPDATE proyecto SET id_representante = ?, id_usuario = ? WHERE id_proyecto = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, representativeId);
            if (organizationId != null) {
                stmt.setInt(2, organizationId);
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setInt(3, projectId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
}