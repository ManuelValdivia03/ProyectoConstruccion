package logic.daos;

import dataaccess.ConnectionDataBase;
import logic.logicclasses.LinkedOrganization;
import logic.logicclasses.Representative;
import logic.interfaces.IRepresentativeDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RepresentativeDAO implements IRepresentativeDAO {
    private static final Logger logger = LogManager.getLogger(RepresentativeDAO.class);

    public boolean addRepresentative(Representative representative) throws SQLException {
        if (representative == null || representative.getFullName() == null ||
                representative.getLinkedOrganization() == null) {
            logger.warn("Intento de agregar representante con datos incompletos o nulos");
            throw new SQLException("Datos del representante incompletos");
        }

        logger.debug("Agregando nuevo representante: {}", representative.getFullName());

        String sql = "INSERT INTO representante (nombre_completo, correo_e, telefono, Id_empresa) VALUES (?, ?, ?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, representative.getFullName());
            ps.setString(2, representative.getEmail());
            ps.setString(3, representative.getCellPhone());
            ps.setInt(4, representative.getLinkedOrganization().getIdLinkedOrganization());

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        representative.setIdRepresentative(generatedId);
                        logger.info("Representante agregado exitosamente - ID: {}, Nombre: {}",
                                generatedId, representative.getFullName());
                        return true;
                    }
                }
            }
            logger.warn("No se pudo agregar el representante: {}", representative.getFullName());
            return false;
        } catch (SQLException e) {
            logger.error("Error al agregar representante: {}", representative.getFullName(), e);
            throw e;
        }
    }

    public boolean deleteRepresentative(Representative representative) throws SQLException {
        if (representative == null) {
            logger.warn("Intento de eliminar representante nulo");
            return false;
        }

        logger.debug("Eliminando representante ID: {}", representative.getIdRepresentative());

        String sql = "DELETE FROM representante WHERE id_representante = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, representative.getIdRepresentative());
            boolean result = ps.executeUpdate() > 0;
            if (result) {
                logger.info("Representante eliminado exitosamente - ID: {}", representative.getIdRepresentative());
            } else {
                logger.warn("No se encontró representante con ID: {} para eliminar", representative.getIdRepresentative());
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al eliminar representante ID: {}", representative.getIdRepresentative(), e);
            throw e;
        }
    }

    public boolean updateRepresentative(Representative representative) throws SQLException {
        if (representative == null || representative.getFullName() == null ||
                representative.getLinkedOrganization() == null) {
            logger.warn("Intento de actualizar representante con datos incompletos o nulos");
            throw new SQLException("Datos del representante incompletos");
        }

        logger.debug("Actualizando representante ID: {}", representative.getIdRepresentative());

        String sql = "UPDATE representante SET nombre_completo = ?, correo_e = ?, telefono = ?, Id_empresa = ? WHERE id_representante = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, representative.getFullName());
            ps.setString(2, representative.getEmail());
            ps.setString(3, representative.getCellPhone());
            ps.setInt(4, representative.getLinkedOrganization().getIdLinkedOrganization());
            ps.setInt(5, representative.getIdRepresentative());

            boolean result = ps.executeUpdate() > 0;
            if (result) {
                logger.info("Representante actualizado exitosamente - ID: {}", representative.getIdRepresentative());
            } else {
                logger.warn("No se encontró representante con ID: {} para actualizar", representative.getIdRepresentative());
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al actualizar representante ID: {}", representative.getIdRepresentative(), e);
            throw e;
        }
    }

    public List<Representative> getAllRepresentatives() throws SQLException {
        logger.info("Obteniendo todos los representantes");

        String sql = "SELECT r.*, o.nombre_empresa, o.telefono as org_telefono, o.correo_empresarial, o.estado " +
                "FROM representante r " +
                "LEFT JOIN organizacion_vinculada o ON r.Id_empresa = o.id_empresa";
        List<Representative> representatives = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Representative rep = mapRepresentativeFromResultSet(rs);
                representatives.add(rep);
            }
            logger.debug("Se encontraron {} representantes", representatives.size());
        } catch (SQLException e) {
            logger.error("Error al obtener todos los representantes", e);
            throw e;
        }
        return representatives;
    }

    public Representative getRepresentativeById(int id) throws SQLException {
        if (id <= 0) {
            logger.warn("Intento de buscar representante con ID inválido: {}", id);
            return null;
        }

        logger.debug("Buscando representante por ID: {}", id);

        String sql = "SELECT r.*, o.nombre_empresa, o.telefono as org_telefono, o.correo_empresarial, o.estado " +
                "FROM representante r " +
                "LEFT JOIN organizacion_vinculada o ON r.Id_empresa = o.id_empresa " +
                "WHERE r.id_representante = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    logger.debug("Representante encontrado con ID: {}", id);
                    return mapRepresentativeFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al buscar representante por ID: {}", id, e);
            throw e;
        }
        logger.info("No se encontró representante con ID: {}", id);
        return null;
    }

    public Representative getRepresentativeByEmail(String email) throws SQLException {
        if (email == null || email.isEmpty()) {
            logger.warn("Intento de buscar representante con email nulo o vacío");
            return null;
        }

        logger.debug("Buscando representante por email: {}", email);

        String sql = "SELECT r.*, o.nombre_empresa, o.telefono as org_telefono, o.correo_empresarial, o.estado " +
                "FROM representante r " +
                "LEFT JOIN organizacion_vinculada o ON r.Id_empresa = o.id_empresa " +
                "WHERE r.correo_e = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    logger.debug("Representante encontrado con email: {}", email);
                    return mapRepresentativeFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al buscar representante por email: {}", email, e);
            throw e;
        }
        logger.info("No se encontró representante con email: {}", email);
        return null;
    }

    public List<Representative> getRepresentativesByOrganization(int organizationId) throws SQLException {
        if (organizationId <= 0) {
            logger.warn("Intento de buscar representantes con ID de organización inválido: {}", organizationId);
            return new ArrayList<>();
        }

        logger.debug("Buscando representantes por organización ID: {}", organizationId);

        String sql = "SELECT r.*, o.nombre_empresa, o.telefono as org_telefono, o.correo_empresarial, o.estado " +
                "FROM representante r " +
                "LEFT JOIN organizacion_vinculada o ON r.Id_empresa = o.id_empresa " +
                "WHERE r.Id_empresa = ?";
        List<Representative> representatives = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, organizationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Representative rep = mapRepresentativeFromResultSet(rs);
                    representatives.add(rep);
                }
            }
            logger.debug("Se encontraron {} representantes para organización ID: {}", representatives.size(), organizationId);
        } catch (SQLException e) {
            logger.error("Error al obtener representantes por organización ID: {}", organizationId, e);
            throw e;
        }
        return representatives;
    }

    public boolean representativeExists(String email) throws SQLException {
        if (email == null || email.isEmpty()) {
            logger.warn("Intento de verificar existencia con email nulo o vacío");
            return false;
        }

        logger.debug("Verificando existencia de representante con email: {}", email);

        String sql = "SELECT 1 FROM representante WHERE correo_e = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                boolean exists = rs.next();
                logger.debug("¿Representante con email '{}' existe?: {}", email, exists);
                return exists;
            }
        } catch (SQLException e) {
            logger.error("Error al verificar existencia de representante: {}", email, e);
            throw e;
        }
    }

    public int countRepresentatives() throws SQLException {
        logger.debug("Contando representantes");

        String sql = "SELECT COUNT(*) FROM representante";

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int count = rs.next() ? rs.getInt(1) : 0;
            logger.info("Total de representantes: {}", count);
            return count;
        } catch (SQLException e) {
            logger.error("Error al contar representantes", e);
            throw e;
        }
    }

    private Representative mapRepresentativeFromResultSet(ResultSet rs) throws SQLException {
        LinkedOrganization org = null;
        int orgId = rs.getInt("Id_empresa");

        if (!rs.wasNull()) {
            org = new LinkedOrganization(
                    orgId,
                    rs.getString("nombre_empresa"),
                    rs.getString("org_telefono"),
                    rs.getString("correo_empresarial"),
                    rs.getString("estado").charAt(0)
            );
        }

        return new Representative(
                rs.getInt("id_representante"),
                rs.getString("nombre_completo"),
                rs.getString("correo_e"),
                rs.getString("telefono"),
                org
        );
    }
}