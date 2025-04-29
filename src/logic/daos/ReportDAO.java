package logic.daos;

import dataaccess.ConnectionDataBase;
import logic.logicclasses.Report;
import logic.enums.ReportType;
import logic.interfaces.IReportDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO implements IReportDAO {
    private static final Logger logger = LogManager.getLogger(ReportDAO.class);
    private final StudentDAO studentDAO;

    public ReportDAO() {
        this.studentDAO = new StudentDAO();
    }

    public boolean addReport(Report report) throws SQLException {
        if (report == null || report.getReportDate() == null || report.getStudent() == null) {
            logger.warn("Intento de agregar reporte con datos nulos");
            throw new SQLException("Datos del reporte incompletos");
        }

        logger.debug("Agregando nuevo reporte para estudiante ID: {}", report.getStudent().getIdUser());

        String sql = "INSERT INTO reporte (tipo, horas, fecha_reporte, id_estudiante) VALUES (?, ?, ?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, report.getReportType().toString());
            statement.setInt(2, report.getHoursReport());
            statement.setDate(3, new Date(report.getReportDate().getTime()));
            statement.setInt(4, report.getStudent().getIdUser());

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        report.setIdReport(generatedId);
                        logger.info("Reporte agregado exitosamente - ID: {}, Tipo: {}",
                                generatedId, report.getReportType());
                        return true;
                    }
                }
            }
            logger.warn("No se pudo agregar el reporte");
            return false;
        } catch (SQLException e) {
            logger.error("Error al agregar reporte", e);
            throw e;
        }
    }

    public Report getReportById(int idReport) throws SQLException {
        if (idReport <= 0) {
            logger.warn("Intento de buscar reporte con ID inválido: {}", idReport);
            return null;
        }

        logger.debug("Buscando reporte por ID: {}", idReport);

        String sql = "SELECT * FROM reporte WHERE id_reporte = ?";
        Report report = null;

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idReport);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    report = new Report();
                    report.setIdReport(resultSet.getInt("id_reporte"));
                    report.setReportDate(resultSet.getTimestamp("fecha_reporte"));
                    report.setHoursReport(resultSet.getInt("horas"));
                    report.setReportType(ReportType.valueOf(resultSet.getString("tipo")));

                    report.setStudent(studentDAO.getStudentById(resultSet.getInt("id_estudiante")));
                    logger.debug("Reporte encontrado con ID: {}", idReport);
                }
            }
        } catch (SQLException e) {
            logger.error("Error al obtener reporte con ID: {}", idReport, e);
            throw e;
        }

        if (report == null) {
            logger.info("No se encontró reporte con ID: {}", idReport);
        }
        return report;
    }

    public List<Report> getAllReports() throws SQLException {
        logger.info("Obteniendo todos los reportes");

        String sql = "SELECT * FROM reporte";
        List<Report> reports = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Report report = new Report();
                report.setIdReport(resultSet.getInt("id_reporte"));
                report.setReportDate(resultSet.getTimestamp("fecha_reporte"));
                report.setHoursReport(resultSet.getInt("horas"));
                report.setReportType(ReportType.valueOf(resultSet.getString("tipo")));

                report.setStudent(studentDAO.getStudentById(resultSet.getInt("id_estudiante")));
                reports.add(report);
            }
            logger.debug("Se encontraron {} reportes", reports.size());
        } catch (SQLException e) {
            logger.error("Error al obtener todos los reportes", e);
            throw e;
        }
        return reports;
    }

    public List<Report> getReportsByStudent(int studentId) throws SQLException {
        if (studentId <= 0) {
            logger.warn("Intento de buscar reportes con ID de estudiante inválido: {}", studentId);
            return new ArrayList<>();
        }

        logger.debug("Buscando reportes por estudiante ID: {}", studentId);

        String sql = "SELECT * FROM reporte WHERE id_estudiante = ?";
        List<Report> reports = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, studentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Report report = new Report();
                    report.setIdReport(resultSet.getInt("id_reporte"));
                    report.setReportDate(resultSet.getTimestamp("fecha_reporte"));
                    report.setHoursReport(resultSet.getInt("horas"));
                    report.setReportType(ReportType.valueOf(resultSet.getString("tipo")));

                    report.setStudent(studentDAO.getStudentById(studentId));
                    reports.add(report);
                }
            }
            logger.debug("Se encontraron {} reportes para estudiante ID: {}", reports.size(), studentId);
        } catch (SQLException e) {
            logger.error("Error al obtener reportes por estudiante ID: {}", studentId, e);
            throw e;
        }
        return reports;
    }

    public boolean updateReport(Report report) throws SQLException {
        if (report == null || report.getIdReport() <= 0 ||
                report.getStudent() == null || report.getReportDate() == null) {
            logger.warn("Intento de actualizar reporte con datos inválidos");
            throw new SQLException("Datos del reporte incompletos");
        }

        logger.debug("Actualizando reporte ID: {}", report.getIdReport());

        String sql = "UPDATE reporte SET tipo = ?, horas = ?, fecha_reporte = ?, id_estudiante = ? WHERE id_reporte = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, String.valueOf(report.getReportType()));
            statement.setInt(2, report.getHoursReport());
            statement.setDate(3, new Date(report.getReportDate().getTime()));
            statement.setInt(4, report.getStudent().getIdUser());
            statement.setInt(5, report.getIdReport());

            boolean result = statement.executeUpdate() > 0;
            if (result) {
                logger.info("Reporte actualizado exitosamente - ID: {}", report.getIdReport());
            } else {
                logger.warn("No se encontró reporte con ID: {} para actualizar", report.getIdReport());
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al actualizar reporte ID: {}", report.getIdReport(), e);
            throw e;
        }
    }

    public boolean deleteReport(int idReport) throws SQLException {
        if (idReport <= 0) {
            logger.warn("Intento de eliminar reporte con ID inválido: {}", idReport);
            return false;
        }

        logger.debug("Eliminando reporte ID: {}", idReport);

        String sql = "DELETE FROM reporte WHERE id_reporte = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idReport);
            boolean result = statement.executeUpdate() > 0;
            if (result) {
                logger.info("Reporte eliminado exitosamente - ID: {}", idReport);
            } else {
                logger.warn("No se encontró reporte con ID: {} para eliminar", idReport);
            }
            return result;
        } catch (SQLException e) {
            logger.error("Error al eliminar reporte ID: {}", idReport, e);
            throw e;
        }
    }

    public boolean reportExists(int idReport) throws SQLException {
        if (idReport <= 0) {
            logger.warn("Intento de verificar existencia de reporte con ID inválido: {}", idReport);
            return false;
        }

        logger.debug("Verificando existencia de reporte ID: {}", idReport);

        String sql = "SELECT 1 FROM reporte WHERE id_reporte = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idReport);
            try (ResultSet resultSet = statement.executeQuery()) {
                boolean exists = resultSet.next();
                logger.debug("¿Reporte ID {} existe?: {}", idReport, exists);
                return exists;
            }
        } catch (SQLException e) {
            logger.error("Error al verificar existencia de reporte ID: {}", idReport, e);
            throw e;
        }
    }

    public int countReports() throws SQLException {
        logger.debug("Contando reportes");

        String sql = "SELECT COUNT(*) FROM reporte";

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            int count = resultSet.next() ? resultSet.getInt(1) : 0;
            logger.info("Total de reportes: {}", count);
            return count;
        } catch (SQLException e) {
            logger.error("Error al contar reportes", e);
            throw e;
        }
    }
}