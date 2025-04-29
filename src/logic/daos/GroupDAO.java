package logic.daos;

import dataaccess.ConnectionDataBase;
import logic.logicclasses.Group;
import logic.logicclasses.Student;
import logic.interfaces.IGroupDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GroupDAO implements IGroupDAO {
    private static final Logger logger = LogManager.getLogger(GroupDAO.class);
    private final StudentDAO studentDAO;

    public GroupDAO() {
        this.studentDAO = new StudentDAO();
    }

    public boolean addGroup(Group group) throws SQLException {
        if (group == null || group.getGroupName() == null) {
            logger.warn("Intento de agregar grupo nulo o con nombre nulo");
            return false;
        }

        logger.debug("Agregando nuevo grupo con NRC: {} y nombre: {}", group.getNrc(), group.getGroupName());

        String sql = "INSERT INTO grupo (nrc, nombre) VALUES (?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, group.getNrc());
            ps.setString(2, group.getGroupName());

            boolean result = ps.executeUpdate() > 0;
            if (result) {
                logger.info("Grupo agregado exitosamente - NRC: {}, Nombre: {}", group.getNrc(), group.getGroupName());
            } else {
                logger.warn("No se pudo agregar el grupo - NRC: {}", group.getNrc());
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al agregar grupo - NRC: {}", group.getNrc(), e);
            throw e;
        }
    }

    public boolean deleteGroup(Group group) throws SQLException {
        if (group == null) {
            logger.warn("Intento de eliminar grupo nulo");
            return false;
        }

        logger.debug("Intentando eliminar grupo con NRC: {}", group.getNrc());

        // Verificar estudiantes en grupo_estudiantes
        List<Student> studentsInGroup = studentDAO.getStudentsByGroup(group.getNrc());
        if (!studentsInGroup.isEmpty()) {
            logger.warn("No se puede eliminar grupo {} porque tiene estudiantes asignados", group.getNrc());
            throw new SQLException("El grupo tiene estudiantes asignados");
        }

        String checkSql = "SELECT COUNT(*) FROM estudiante WHERE nrc_grupo = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(checkSql)) {

            ps.setInt(1, group.getNrc());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    logger.warn("No se puede eliminar grupo {} porque tiene estudiantes asignados (segunda verificación)", group.getNrc());
                    throw new SQLException("El grupo tiene estudiantes asignados");
                }
            }
        }

        String deleteSql = "DELETE FROM grupo WHERE nrc = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(deleteSql)) {

            ps.setInt(1, group.getNrc());
            boolean result = ps.executeUpdate() > 0;
            if (result) {
                logger.info("Grupo eliminado exitosamente - NRC: {}", group.getNrc());
            } else {
                logger.warn("No se encontró grupo con NRC: {} para eliminar", group.getNrc());
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al eliminar grupo - NRC: {}", group.getNrc(), e);
            throw e;
        }
    }

    public boolean updateGroup(Group group) throws SQLException {
        if (group == null || group.getGroupName() == null) {
            logger.warn("Intento de actualizar grupo nulo o con nombre nulo");
            return false;
        }

        logger.debug("Actualizando grupo con NRC: {}", group.getNrc());

        String sql = "UPDATE grupo SET nombre = ? WHERE nrc = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, group.getGroupName());
            ps.setInt(2, group.getNrc());

            boolean result = ps.executeUpdate() > 0;
            if (result) {
                logger.info("Grupo actualizado exitosamente - NRC: {}, Nuevo nombre: {}", group.getNrc(), group.getGroupName());
            } else {
                logger.warn("No se encontró grupo con NRC: {} para actualizar", group.getNrc());
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al actualizar grupo - NRC: {}", group.getNrc(), e);
            throw e;
        }
    }

    public List<Group> getAllGroups() throws SQLException {
        logger.info("Obteniendo todos los grupos");

        String sql = "SELECT nrc, nombre FROM grupo";
        List<Group> groups = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int nrc = rs.getInt("nrc");
                Group group = new Group(
                        nrc,
                        rs.getString("nombre"),
                        studentDAO.getStudentsByGroup(nrc) // Cargar estudiantes del grupo
                );
                groups.add(group);
            }
            logger.debug("Se encontraron {} grupos", groups.size());
        } catch (SQLException e) {
            logger.error("Error al obtener todos los grupos", e);
            throw e;
        }
        return groups;
    }

    public Group getGroupByNrc(int nrc) throws SQLException {
        if (nrc <= 0) {
            logger.warn("Intento de buscar grupo con NRC inválido: {}", nrc);
            return null;
        }

        logger.debug("Buscando grupo por NRC: {}", nrc);

        String sql = "SELECT nrc, nombre FROM grupo WHERE nrc = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, nrc);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    logger.debug("Grupo encontrado con NRC: {}", nrc);
                    return new Group(
                            nrc,
                            rs.getString("nombre"),
                            studentDAO.getStudentsByGroup(nrc)
                    );
                }
            }
        } catch (SQLException e) {
            logger.error("Error al buscar grupo por NRC: {}", nrc, e);
            throw e;
        }
        logger.info("No se encontró grupo con NRC: {}", nrc);
        return null;
    }

    public Group getGroupByName(String groupName) throws SQLException {
        if (groupName == null || groupName.isEmpty()) {
            logger.warn("Intento de buscar grupo con nombre nulo o vacío");
            return null;
        }

        logger.debug("Buscando grupo por nombre: {}", groupName);

        String sql = "SELECT nrc, nombre FROM grupo WHERE nombre = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, groupName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int nrc = rs.getInt("nrc");
                    logger.debug("Grupo encontrado con nombre: {}", groupName);
                    return new Group(
                            nrc,
                            rs.getString("nombre"),
                            studentDAO.getStudentsByGroup(nrc)
                    );
                }
            }
        } catch (SQLException e) {
            logger.error("Error al buscar grupo por nombre: {}", groupName, e);
            throw e;
        }
        logger.info("No se encontró grupo con nombre: {}", groupName);
        return null;
    }

    public boolean groupExists(int nrc) throws SQLException {
        if (nrc <= 0) {
            logger.warn("Intento de verificar existencia de grupo con NRC inválido: {}", nrc);
            return false;
        }

        logger.debug("Verificando existencia de grupo con NRC: {}", nrc);

        String sql = "SELECT 1 FROM grupo WHERE nrc = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, nrc);
            try (ResultSet rs = ps.executeQuery()) {
                boolean exists = rs.next();
                logger.debug("¿Grupo con NRC {} existe?: {}", nrc, exists);
                return exists;
            }
        } catch (SQLException e) {
            logger.error("Error al verificar existencia de grupo con NRC: {}", nrc, e);
            throw e;
        }
    }

    public int countGroups() throws SQLException {
        logger.debug("Contando grupos");

        String sql = "SELECT COUNT(*) FROM grupo";

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            int count = rs.next() ? rs.getInt(1) : 0;
            logger.info("Total de grupos: {}", count);
            return count;
        } catch (SQLException e) {
            logger.error("Error al contar grupos", e);
            throw e;
        }
    }
}