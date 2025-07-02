package logic.daos;

import dataaccess.ConnectionDataBase;
import logic.logicclasses.Report;
import logic.enums.ReportType;
import logic.interfaces.IReportDAO;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReportDAO implements IReportDAO {
    private static final Report EMPTY_REPORT = new Report();
    private final StudentDAO studentDAO;

    public ReportDAO() {
        this.studentDAO = new StudentDAO();
    }

    @Override
    public boolean addReport(Report report) throws SQLException, IllegalArgumentException {
        if (report == null || report.getReportDate() == null || report.getStudent() == null) {
            throw new IllegalArgumentException("Datos del reporte incompletos");
        }

        String sql = "INSERT INTO reporte (tipo, horas, fecha_reporte, metodologia, descripcion, id_estudiante) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, report.getReportType().toString());
            statement.setInt(2, report.getHoursReport());
            statement.setDate(3, new Date(report.getReportDate().getTime()));
            statement.setString(4, report.getMethodology());
            statement.setString(5, report.getDescription());
            statement.setInt(6, report.getStudent().getIdUser());

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        report.setIdReport(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
            return false;
        }
    }

    @Override
    public Report getReportById(int idReport) throws SQLException {
        if (idReport <= 0) {
            return EMPTY_REPORT;
        }

        String sql = "SELECT * FROM reporte WHERE id_reporte = ?";
        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idReport);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToReport(resultSet);
                }
            }
        }
        return EMPTY_REPORT;
    }

    @Override
    public List<Report> getAllReports() throws SQLException {
        String sql = "SELECT * FROM reporte";
        List<Report> reports = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                reports.add(mapResultSetToReport(resultSet));
            }
        }
        return reports;
    }

    @Override
    public List<Report> getReportsByStudent(int studentId) throws SQLException {
        if (studentId <= 0) {
            return Collections.emptyList();
        }

        String sql = "SELECT * FROM reporte WHERE id_estudiante = ?";
        List<Report> reports = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, studentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    reports.add(mapResultSetToReport(resultSet));
                }
            }
        }
        return reports;
    }

    @Override
    public boolean updateReport(Report report) throws SQLException, IllegalArgumentException {
        if (report == null || report.getIdReport() <= 0 ||
                report.getStudent() == null || report.getReportDate() == null) {
            throw new IllegalArgumentException("Datos del reporte incompletos");
        }

        String sql = "UPDATE reporte SET tipo = ?, horas = ?, fecha_reporte = ?, " +
                "metodologia = ?, descripcion = ?, id_estudiante = ? " +
                "WHERE id_reporte = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, report.getReportType().toString());
            statement.setInt(2, report.getHoursReport());
            statement.setDate(3, new Date(report.getReportDate().getTime()));
            statement.setString(4, report.getMethodology());
            statement.setString(5, report.getDescription());
            statement.setInt(6, report.getStudent().getIdUser());
            statement.setInt(7, report.getIdReport());

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteReport(int idReport) throws SQLException {
        if (idReport <= 0) {
            return false;
        }

        String sql = "DELETE FROM reporte WHERE id_reporte = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idReport);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean reportExists(int idReport) throws SQLException {
        if (idReport <= 0) {
            return false;
        }

        String sql = "SELECT 1 FROM reporte WHERE id_reporte = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idReport);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    @Override
    public int countReports() throws SQLException {
        String sql = "SELECT COUNT(*) FROM reporte";

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    private Report mapResultSetToReport(ResultSet resultSet) throws SQLException {
        Report report = new Report();
        report.setIdReport(resultSet.getInt("id_reporte"));
        report.setReportDate(resultSet.getTimestamp("fecha_reporte"));
        report.setHoursReport(resultSet.getInt("horas"));
        report.setReportType(ReportType.valueOf(resultSet.getString("tipo")));
        report.setMethodology(resultSet.getString("metodologia"));
        report.setDescription(resultSet.getString("descripcion"));
        report.setStudent(studentDAO.getStudentById(resultSet.getInt("id_estudiante")));
        return report;
    }
}