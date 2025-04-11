package logic.interfaces;

import logic.Report;
import java.sql.SQLException;
import java.util.List;

public interface IReportDAO {
    boolean addReport(Report report) throws SQLException;
    Report getReportById(int idReport) throws SQLException;
    List<Report> getAllReports() throws SQLException;
    List<Report> getReportsByStudent(int studentId) throws SQLException;
    boolean updateReport(Report report) throws SQLException;
    boolean deleteReport(int idReport) throws SQLException;
    boolean reportExists(int idReport) throws SQLException;
    int countReports() throws SQLException;
}