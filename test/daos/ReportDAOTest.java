package daos;

import dataaccess.ConnectionDataBase;
import logic.daos.ReportDAO;
import logic.daos.StudentDAO;
import logic.daos.UserDAO;
import logic.logicclasses.Report;
import logic.logicclasses.Student;
import logic.logicclasses.User;
import logic.enums.ReportType;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ReportDAOTest {
    private static ReportDAO reportDAO;
    private static StudentDAO studentDAO;
    private static UserDAO userDAO;
    private static Connection testConnection;
    private static List<Report> testReports;
    private static Student testStudent;

    @BeforeAll
    static void setUpAll() throws SQLException {
        reportDAO = new ReportDAO();
        studentDAO = new StudentDAO();
        userDAO = new UserDAO();
        testConnection = ConnectionDataBase.getConnection();

        try (var conn = ConnectionDataBase.getConnection();
             var statement = conn.createStatement()) {
            statement.execute("SET FOREIGN_KEY_CHECKS = 0");
            statement.execute("TRUNCATE TABLE grupo_estudiante");
            statement.execute("TRUNCATE TABLE estudiante");
            statement.execute("TRUNCATE TABLE academico");
            statement.execute("TRUNCATE TABLE coordinador");
            statement.execute("TRUNCATE TABLE representante");
            statement.execute("TRUNCATE TABLE actividad");
            statement.execute("TRUNCATE TABLE autoevaluacion");
            statement.execute("TRUNCATE TABLE cronograma_actividad");
            statement.execute("TRUNCATE TABLE cronograma_actividades");
            statement.execute("TRUNCATE TABLE evaluacion");
            statement.execute("TRUNCATE TABLE presentacion");
            statement.execute("TRUNCATE TABLE proyecto");
            statement.execute("TRUNCATE TABLE reporte");
            statement.execute("TRUNCATE TABLE grupo");
            statement.execute("TRUNCATE TABLE organizacion_vinculada");
            statement.execute("TRUNCATE TABLE cuenta");
            statement.execute("TRUNCATE TABLE usuario");
            statement.execute("SET FOREIGN_KEY_CHECKS = 1");
        }

        User user = new User();
        user.setFullName("Estudiante Prueba");
        user.setCellphone("5550000000");
        user.setStatus('A');
        userDAO.addUser(user);

        testStudent = new Student();
        testStudent.setIdUser(user.getIdUser());
        testStudent.setFullName(user.getFullName());
        testStudent.setCellphone(user.getCellPhone());
        testStudent.setStatus(user.getStatus());
        studentDAO.addStudent(testStudent, 0);

        testReports = new ArrayList<>();
        testReports.add(createTestReport(
                Timestamp.valueOf(LocalDateTime.now()),
                5,
                ReportType.Semanal,
                "Metodología 1",
                "Descripción 1",
                testStudent
        ));
        testReports.add(createTestReport(
                Timestamp.valueOf(LocalDateTime.now().minusDays(7)),
                10,
                ReportType.Mensual,
                "Metodología 2",
                "Descripción 2",
                testStudent
        ));
    }

    private static Report createTestReport(Timestamp date, int hours, ReportType type,
                                           String methodology, String description,
                                           Student student) throws SQLException {
        Report report = new Report();
        report.setReportDate(date);
        report.setHoursReport(hours);
        report.setReportType(type);
        report.setMethodology(methodology);
        report.setDescription(description);
        report.setStudent(student);
        reportDAO.addReport(report);
        return report;
    }

    @AfterAll
    static void tearDownAll() throws SQLException {
        if (testConnection != null && !testConnection.isClosed()) {
            testConnection.close();
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("DELETE FROM reporte");
            stmt.execute("ALTER TABLE reporte AUTO_INCREMENT = 1");
        }

        testReports = new ArrayList<>();
        testReports.add(createTestReport(
                Timestamp.valueOf(LocalDateTime.now()),
                5,
                ReportType.Semanal,
                "Metodología 1",
                "Descripción 1",
                testStudent
        ));
        testReports.add(createTestReport(
                Timestamp.valueOf(LocalDateTime.now().minusDays(7)),
                10,
                ReportType.Mensual,
                "Metodología 2",
                "Descripción 2",
                testStudent
        ));
    }

    @Test
    void testAddReport_Success() throws SQLException {
        Report newReport = new Report();
        newReport.setReportDate(Timestamp.valueOf(LocalDateTime.now()));
        newReport.setHoursReport(8);
        newReport.setReportType(ReportType.Final);
        newReport.setMethodology("Nueva metodología");
        newReport.setDescription("Nueva descripción");
        newReport.setStudent(testStudent);

        int initialCount = reportDAO.countReports();
        boolean result = reportDAO.addReport(newReport);

        assertTrue(result);
        assertEquals(initialCount + 1, reportDAO.countReports());
        assertTrue(newReport.getIdReport() > 0);

        Report addedReport = reportDAO.getReportById(newReport.getIdReport());
        assertEquals(newReport, addedReport);
    }

    @Test
    void testAddReport_NullDate_ShouldThrowException() {
        Report invalidReport = new Report();
        invalidReport.setReportDate(null);
        invalidReport.setHoursReport(5);
        invalidReport.setReportType(ReportType.Semanal);
        invalidReport.setMethodology("Metodología");
        invalidReport.setDescription("Descripción");
        invalidReport.setStudent(testStudent);

        assertThrows(IllegalArgumentException.class, () -> reportDAO.addReport(invalidReport));
    }

    @Test
    void testGetReportById_Exists() throws SQLException {
        Report testReport = testReports.get(0);
        Report foundReport = reportDAO.getReportById(testReport.getIdReport());
        assertEquals(testReport, foundReport);
    }

    @Test
    void testGetReportById_NotExists() throws SQLException {
        Report foundReport = reportDAO.getReportById(9999);
        assertEquals(new Report(), foundReport);
    }

    @Test
    void testGetAllReports_WithData() throws SQLException {
        List<Report> reports = reportDAO.getAllReports();
        assertEquals(testReports.size(), reports.size());
        for (Report testReport : testReports) {
            boolean found = reports.stream().anyMatch(r -> r.equals(testReport));
            assertTrue(found);
        }
    }

    @Test
    void testGetReportsByStudent_Exists() throws SQLException {
        List<Report> reports = reportDAO.getReportsByStudent(testStudent.getIdUser());
        assertEquals(testReports.size(), reports.size());
        for (Report report : reports) {
            assertEquals(testStudent, report.getStudent());
        }
    }

    @Test
    void testGetReportsByStudent_NotExists() throws SQLException {
        List<Report> reports = reportDAO.getReportsByStudent(9999);
        assertTrue(reports.isEmpty());
    }

    @Test
    void testUpdateReport_Success() throws SQLException {
        Report reportToUpdate = testReports.get(0);
        reportToUpdate.setHoursReport(15);
        reportToUpdate.setReportType(ReportType.Mensual);
        reportToUpdate.setMethodology("Metodología actualizada");
        reportToUpdate.setDescription("Descripción actualizada");
        reportToUpdate.setReportDate(Timestamp.valueOf(LocalDateTime.now().minusDays(3)));

        boolean result = reportDAO.updateReport(reportToUpdate);
        assertTrue(result);

        Report updatedReport = reportDAO.getReportById(reportToUpdate.getIdReport());
        assertEquals(reportToUpdate, updatedReport);
    }

    @Test
    void testUpdateReport_NotExists() throws SQLException {
        Report nonExistentReport = new Report();
        nonExistentReport.setIdReport(9999);
        nonExistentReport.setReportDate(Timestamp.valueOf(LocalDateTime.now()));
        nonExistentReport.setHoursReport(5);
        nonExistentReport.setReportType(ReportType.Semanal);
        nonExistentReport.setMethodology("Metodología");
        nonExistentReport.setDescription("Descripción");
        nonExistentReport.setStudent(testStudent);

        boolean result = reportDAO.updateReport(nonExistentReport);
        assertFalse(result);
    }

    @Test
    void testDeleteReport_Success() throws SQLException {
        Report testReport = testReports.get(0);
        int reportId = testReport.getIdReport();

        int countBefore = reportDAO.countReports();
        boolean result = reportDAO.deleteReport(reportId);

        assertTrue(result);
        assertEquals(countBefore - 1, reportDAO.countReports());
        assertFalse(reportDAO.reportExists(reportId));
    }

    @Test
    void testDeleteReport_NotExists() throws SQLException {
        int initialCount = reportDAO.countReports();
        boolean result = reportDAO.deleteReport(9999);

        assertFalse(result);
        assertEquals(initialCount, reportDAO.countReports());
    }

    @Test
    void testReportExists_True() throws SQLException {
        Report testReport = testReports.get(0);
        assertTrue(reportDAO.reportExists(testReport.getIdReport()));
    }

    @Test
    void testReportExists_False() throws SQLException {
        assertFalse(reportDAO.reportExists(9999));
    }

    @Test
    void testCountReports_WithData() throws SQLException {
        int count = reportDAO.countReports();
        assertEquals(testReports.size(), count);

        Report extraReport = new Report();
        extraReport.setReportDate(Timestamp.valueOf(LocalDateTime.now()));
        extraReport.setHoursReport(3);
        extraReport.setReportType(ReportType.Semanal);
        extraReport.setMethodology("Extra");
        extraReport.setDescription("Extra");
        extraReport.setStudent(testStudent);
        reportDAO.addReport(extraReport);

        assertEquals(count + 1, reportDAO.countReports());
    }
}