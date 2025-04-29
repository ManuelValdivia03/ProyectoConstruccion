package logic.daos;

import dataaccess.ConnectionDataBase;
import logic.logicclasses.LinkedOrganization;
import logic.interfaces.ILinkedOrganizationDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LinkedOrganizationDAO implements ILinkedOrganizationDAO {
    private static final Logger logger = LogManager.getLogger(LinkedOrganizationDAO.class);

    public boolean addLinkedOrganization(LinkedOrganization linkedOrganization) throws SQLException {
        if (linkedOrganization == null) {
            logger.warn("Intento de agregar organización vinculada nula");
            return false;
        }

        logger.debug("Agregando nueva organización vinculada: {}", linkedOrganization.getNameLinkedOrganization());

        String sql = "INSERT INTO organizacion_vinculada (nombre_empresa, telefono, correo_empresarial, estado) VALUES (?, ?, ?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, linkedOrganization.getNameLinkedOrganization());
            ps.setString(2, linkedOrganization.getCellPhoneLinkedOrganization());
            ps.setString(3, linkedOrganization.getEmailLinkedOrganization());
            ps.setString(4, String.valueOf(linkedOrganization.getStatus()));

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        linkedOrganization.setIdLinkedOrganization(generatedId);
                        logger.info("Organización vinculada agregada exitosamente - ID: {}, Nombre: {}",
                                generatedId, linkedOrganization.getNameLinkedOrganization());
                        return true;
                    }
                }
            }
            logger.warn("No se pudo agregar la organización vinculada: {}", linkedOrganization.getNameLinkedOrganization());
            return false;
        } catch (SQLException e) {
            logger.error("Error al agregar organización vinculada: {}", linkedOrganization.getNameLinkedOrganization(), e);
            throw e;
        }
    }

    public boolean deleteLinkedOrganization(LinkedOrganization linkedOrganization) throws SQLException {
        if (linkedOrganization == null) {
            logger.warn("Intento de eliminar organización vinculada nula");
            return false;
        }

        logger.debug("Eliminando organización vinculada ID: {}", linkedOrganization.getIdLinkedOrganization());

        String sql = "DELETE FROM organizacion_vinculada WHERE id_empresa = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, linkedOrganization.getIdLinkedOrganization());
            boolean result = ps.executeUpdate() > 0;
            if (result) {
                logger.info("Organización vinculada eliminada exitosamente - ID: {}", linkedOrganization.getIdLinkedOrganization());
            } else {
                logger.warn("No se encontró organización vinculada con ID: {} para eliminar", linkedOrganization.getIdLinkedOrganization());
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al eliminar organización vinculada ID: {}", linkedOrganization.getIdLinkedOrganization(), e);
            throw e;
        }
    }

    public boolean updateLinkedOrganization(LinkedOrganization linkedOrganization) throws SQLException {
        if (linkedOrganization == null) {
            logger.warn("Intento de actualizar organización vinculada nula");
            return false;
        }

        logger.debug("Actualizando organización vinculada ID: {}", linkedOrganization.getIdLinkedOrganization());

        String sql = "UPDATE organizacion_vinculada SET nombre_empresa = ?, telefono = ?, correo_empresarial = ?, estado = ? WHERE id_empresa = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, linkedOrganization.getNameLinkedOrganization());
            ps.setString(2, linkedOrganization.getCellPhoneLinkedOrganization());
            ps.setString(3, linkedOrganization.getEmailLinkedOrganization());
            ps.setString(4, String.valueOf(linkedOrganization.getStatus()));
            ps.setInt(5, linkedOrganization.getIdLinkedOrganization());

            boolean result = ps.executeUpdate() > 0;
            if (result) {
                logger.info("Organización vinculada actualizada exitosamente - ID: {}", linkedOrganization.getIdLinkedOrganization());
            } else {
                logger.warn("No se encontró organización vinculada con ID: {} para actualizar", linkedOrganization.getIdLinkedOrganization());
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al actualizar organización vinculada ID: {}", linkedOrganization.getIdLinkedOrganization(), e);
            throw e;
        }
    }

    public List<LinkedOrganization> getAllLinkedOrganizations() throws SQLException {
        logger.info("Obteniendo todas las organizaciones vinculadas");

        String sql = "SELECT id_empresa, nombre_empresa, telefono, correo_empresarial, estado FROM organizacion_vinculada";
        List<LinkedOrganization> organizations = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                LinkedOrganization org = new LinkedOrganization(
                        rs.getInt("id_empresa"),
                        rs.getString("nombre_empresa"),
                        rs.getString("telefono"),
                        rs.getString("correo_empresarial"),
                        rs.getString("estado").charAt(0)
                );
                organizations.add(org);
            }
            logger.debug("Se encontraron {} organizaciones vinculadas", organizations.size());
        } catch (SQLException e) {
            logger.error("Error al obtener todas las organizaciones vinculadas", e);
            throw e;
        }
        return organizations;
    }

    public LinkedOrganization getLinkedOrganizationByTitle(String title) throws SQLException {
        if (title == null || title.isEmpty()) {
            logger.warn("Intento de buscar organización vinculada con título nulo o vacío");
            return null;
        }

        logger.debug("Buscando organización vinculada por título: {}", title);

        String sql = "SELECT id_empresa, nombre_empresa, telefono, correo_empresarial, estado FROM organizacion_vinculada WHERE nombre_empresa = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, title);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    logger.debug("Organización vinculada encontrada con título: {}", title);
                    return new LinkedOrganization(
                            rs.getInt("id_empresa"),
                            rs.getString("nombre_empresa"),
                            rs.getString("telefono"),
                            rs.getString("correo_empresarial"),
                            rs.getString("estado").charAt(0)
                    );
                }
            }
        } catch (SQLException e) {
            logger.error("Error al buscar organización vinculada por título: {}", title, e);
            throw e;
        }
        logger.info("No se encontró organización vinculada con título: {}", title);
        return null;
    }

    public LinkedOrganization getLinkedOrganizationByID(int id) throws SQLException {
        if (id <= 0) {
            logger.warn("Intento de buscar organización vinculada con ID inválido: {}", id);
            return null;
        }

        logger.debug("Buscando organización vinculada por ID: {}", id);

        String sql = "SELECT id_empresa, nombre_empresa, telefono, correo_empresarial, estado FROM organizacion_vinculada WHERE id_empresa = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    logger.debug("Organización vinculada encontrada con ID: {}", id);
                    return new LinkedOrganization(
                            rs.getInt("id_empresa"),
                            rs.getString("nombre_empresa"),
                            rs.getString("telefono"),
                            rs.getString("correo_empresarial"),
                            rs.getString("estado").charAt(0)
                    );
                }
            }
        } catch (SQLException e) {
            logger.error("Error al buscar organización vinculada por ID: {}", id, e);
            throw e;
        }
        logger.info("No se encontró organización vinculada con ID: {}", id);
        return null;
    }

    public boolean linkedOrganizationExists(String title) throws SQLException {
        if (title == null || title.isEmpty()) {
            logger.warn("Intento de verificar existencia con título nulo o vacío");
            return false;
        }

        logger.debug("Verificando existencia de organización vinculada con título: {}", title);

        String sql = "SELECT 1 FROM organizacion_vinculada WHERE nombre_empresa = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, title);
            try (ResultSet rs = ps.executeQuery()) {
                boolean exists = rs.next();
                logger.debug("¿Organización vinculada '{}' existe?: {}", title, exists);
                return exists;
            }
        } catch (SQLException e) {
            logger.error("Error al verificar existencia de organización vinculada: {}", title, e);
            throw e;
        }
    }

    public int countLinkedOrganizations() throws SQLException {
        logger.debug("Contando organizaciones vinculadas");

        String sql = "SELECT COUNT(*) FROM organizacion_vinculada";

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int count = rs.next() ? rs.getInt(1) : 0;
            logger.info("Total de organizaciones vinculadas: {}", count);
            return count;
        } catch (SQLException e) {
            logger.error("Error al contar organizaciones vinculadas", e);
            throw e;
        }
    }
}