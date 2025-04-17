package logic.daos;

import dataaccess.ConnectionDataBase;
import logic.logicclasses.Report;
import logic.enums.ReportType;
import logic.interfaces.IReportDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO implements IReportDAO {

    public boolean addReport(Report report) throws SQLException {
        String sql = "INSERT INTO reporte (fecha_reporte, horas_reportadas, tipo_reporte, id_estudiante) VALUES (?, ?, ?, ?)";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setTimestamp(1, report.getReportDate());
            statement.setInt(2, report.getHoursReport());
            statement.setString(3, report.getReportType().name());
            statement.setInt(4, report.getStudent().getIdUser());

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        report.setIdReport(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    public Report getReportById(int idReport) throws SQLException {
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
                    report.setHoursReport(resultSet.getInt("horas_reportadas"));
                    report.setReportType(ReportType.valueOf(resultSet.getString("tipo_reporte")));

                    // Get associated student (assuming StudentDAO exists)
                    StudentDAO studentDAO = new StudentDAO();
                    report.setStudent(studentDAO.getStudentById(resultSet.getInt("id_estudiante")));
                }
            }
        }
        return report;
    }

    public List<Report> getAllReports() throws SQLException {
        String sql = "SELECT * FROM reporte";
        List<Report> reports = new ArrayList<>();

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Report report = new Report();
                report.setIdReport(resultSet.getInt("id_reporte"));
                report.setReportDate(resultSet.getTimestamp("fecha_reporte"));
                report.setHoursReport(resultSet.getInt("horas_reportadas"));
                report.setReportType(ReportType.valueOf(resultSet.getString("tipo_reporte")));

                // Get associated student
                StudentDAO studentDAO = new StudentDAO();
                report.setStudent(studentDAO.getStudentById(resultSet.getInt("id_estudiante")));

                reports.add(report);
            }
        }
        return reports;
    }

    public List<Report> getReportsByStudent(int studentId) throws SQLException {
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
                    report.setHoursReport(resultSet.getInt("horas_reportadas"));
                    report.setReportType(ReportType.valueOf(resultSet.getString("tipo_reporte")));

                    StudentDAO studentDAO = new StudentDAO();
                    report.setStudent(studentDAO.getStudentById(studentId));

                    reports.add(report);
                }
            }
        }
        return reports;
    }

    public boolean updateReport(Report report) throws SQLException {
        String sql = "UPDATE reporte SET fecha_reporte = ?, horas_reportadas = ?, tipo_reporte = ?, id_estudiante = ? WHERE id_reporte = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setTimestamp(1, report.getReportDate());
            statement.setInt(2, report.getHoursReport());
            statement.setString(3, report.getReportType().name());
            statement.setInt(4, report.getStudent().getIdUser());
            statement.setInt(5, report.getIdReport());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteReport(int idReport) throws SQLException {
        String sql = "DELETE FROM reporte WHERE id_reporte = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idReport);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean reportExists(int idReport) throws SQLException {
        String sql = "SELECT 1 FROM reporte WHERE id_reporte = ?";

        try (Connection connection = ConnectionDataBase.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idReport);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public int countReports() throws SQLException {
        String sql = "SELECT COUNT(*) FROM reporte";

        try (Connection connection = ConnectionDataBase.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return 0;
        }
    }
}