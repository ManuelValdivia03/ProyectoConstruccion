package logic.daos;

import dataaccess.ConnectionDataBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

public class LinkedOrganizationDocumentDAO {
    private static final Logger logger = LogManager.getLogger(LinkedOrganizationDocumentDAO.class);

    public boolean insertDocument(int organizationId, String fileName, String fileType, byte[] fileBytes) throws SQLException {
        if (organizationId <= 0 || fileName == null || fileType == null || fileBytes == null) {
            logger.warn("Intento de insertar documento con parámetros inválidos");
            return false;
        }

        logger.debug("Insertando documento para organización ID: {} - Nombre archivo: {}", organizationId, fileName);

        String sql = "INSERT INTO documentos_organizacion (id_empresa, nombre_archivo, tipo_archivo, archivo) VALUES (?, ?, ?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, organizationId);
            ps.setString(2, fileName);
            ps.setString(3, fileType);
            ps.setBytes(4, fileBytes);

            int affectedRows = ps.executeUpdate();
            boolean success = affectedRows > 0;

            if (success) {
                logger.info("Documento insertado exitosamente para organización ID: {} - Archivo: {}", organizationId, fileName);
            } else {
                logger.warn("No se pudo insertar documento para organización ID: {}", organizationId);
            }

            return success;
        } catch (SQLException e) {
            logger.error("Error al insertar documento para organización ID: {}", organizationId, e);
            throw e;
        }
    }

    public byte[] getDocumentByOrganizationId(int organizationId) throws SQLException {
        if (organizationId <= 0) {
            logger.warn("Intento de obtener documento con ID de organización inválido: {}", organizationId);
            return null;
        }

        logger.debug("Obteniendo documento para organización ID: {}", organizationId);

        String sql = "SELECT archivo FROM documentos_organizacion WHERE id_empresa = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, organizationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    logger.debug("Documento encontrado para organización ID: {}", organizationId);
                    return rs.getBytes("archivo");
                }
            }
        } catch (SQLException e) {
            logger.error("Error al obtener documento para organización ID: {}", organizationId, e);
            throw e;
        }

        logger.info("No se encontró documento para organización ID: {}", organizationId);
        return null;
    }

    public boolean deleteDocument(int organizationId) throws SQLException {
        if (organizationId <= 0) {
            logger.warn("Intento de eliminar documento con ID de organización inválido: {}", organizationId);
            return false;
        }

        logger.debug("Eliminando documento para organización ID: {}", organizationId);

        String sql = "DELETE FROM documentos_organizacion WHERE id_empresa = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, organizationId);
            int affectedRows = ps.executeUpdate();
            boolean success = affectedRows > 0;

            if (success) {
                logger.info("Documento eliminado exitosamente para organización ID: {}", organizationId);
            } else {
                logger.warn("No se encontró documento para organización ID: {} para eliminar", organizationId);
            }

            return success;
        } catch (SQLException e) {
            logger.error("Error al eliminar documento para organización ID: {}", organizationId, e);
            throw e;
        }
    }

    public boolean documentExists(int organizationId) throws SQLException {
        if (organizationId <= 0) {
            logger.warn("Intento de verificar existencia de documento con ID de organización inválido: {}", organizationId);
            return false;
        }

        logger.debug("Verificando existencia de documento para organización ID: {}", organizationId);

        String sql = "SELECT 1 FROM documentos_organizacion WHERE id_empresa = ? LIMIT 1";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, organizationId);
            try (ResultSet rs = ps.executeQuery()) {
                boolean exists = rs.next();
                logger.debug("¿Documento existe para organización ID {}?: {}", organizationId, exists);
                return exists;
            }
        } catch (SQLException e) {
            logger.error("Error al verificar existencia de documento para organización ID: {}", organizationId, e);
            throw e;
        }
    }

    public String getDocumentInfo(int organizationId) throws SQLException {
        if (organizationId <= 0) {
            logger.warn("Intento de obtener información de documento con ID de organización inválido: {}", organizationId);
            return null;
        }

        logger.debug("Obteniendo información de documento para organización ID: {}", organizationId);

        String sql = "SELECT nombre_archivo, tipo_archivo, fecha_subida FROM documentos_organizacion WHERE id_empresa = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, organizationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String info = String.format("Nombre: %s, Tipo: %s, Fecha: %s",
                            rs.getString("nombre_archivo"),
                            rs.getString("tipo_archivo"),
                            rs.getTimestamp("fecha_subida"));
                    logger.debug("Información de documento encontrada para organización ID: {} - {}", organizationId, info);
                    return info;
                }
            }
        } catch (SQLException e) {
            logger.error("Error al obtener información de documento para organización ID: {}", organizationId, e);
            throw e;
        }

        logger.info("No se encontró información de documento para organización ID: {}", organizationId);
        return null;
    }

    public String getDocumentType(int organizationId) throws SQLException {
        String sql = "SELECT tipo_archivo FROM documentos_organizacion WHERE id_empresa = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, organizationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("tipo_archivo");
                }
            }
        }
        throw new SQLException("No se encontró el tipo de archivo");
    }
}