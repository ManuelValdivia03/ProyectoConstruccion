package daos;

import dataaccess.ConnectionDataBase;
import logic.daos.ReportDAO;
import logic.daos.StudentDAO;
import logic.logicclasses.Report;
import logic.logicclasses.Student;
import logic.enums.ReportType;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ReportDAOTest {
    private static ReportDAO reportDAO;
    private static StudentDAO studentDAO;
    private static Connection testConnection;
    private static List<Report> testReports;
    private static Student testStudent;

    @BeforeAll
    static void setUpAll() throws SQLException {
        reportDAO = new ReportDAO();
        studentDAO = new StudentDAO();
        testConnection = ConnectionDataBase.getConnection();

        // Limpiar completamente las tablas relacionadas
        try (var statement = testConnection.createStatement()) {
            statement.execute("DELETE FROM autoevaluacion");
            statement.execute("ALTER TABLE autoevaluacion AUTO_INCREMENT = 1");
            statement.execute("DELETE FROM reporte");
            statement.execute("DELETE FROM evaluacion");
            statement.execute("DELETE FROM presentacion");
            statement.execute("DELETE FROM proyecto");
            statement.execute("DELETE FROM estudiante");
            statement.execute("ALTER TABLE reporte AUTO_INCREMENT = 1");
            statement.execute("ALTER TABLE estudiante AUTO_INCREMENT = 1");
            statement.execute("ALTER TABLE presentacion AUTO_INCREMENT = 1");
            statement.execute("ALTER TABLE proyecto AUTO_INCREMENT = 1");
            statement.execute("ALTER TABLE evaluacion AUTO_INCREMENT = 1");
            // Crear tabla de estudiantes si no existe (simplificado para pruebas)
            statement.execute("CREATE TABLE IF NOT EXISTS estudiante (" +
                    "id_estudiante INT AUTO_INCREMENT PRIMARY KEY, " +
                    "nombre VARCHAR(100) NOT NULL)");

            // Crear tabla de reportes si no existe
            statement.execute("CREATE TABLE IF NOT EXISTS reporte (" +
                    "id_reporte INT AUTO_INCREMENT PRIMARY KEY, " +
                    "fecha_reporte TIMESTAMP NOT NULL, " +
                    "horas_reportadas INT NOT NULL, " +
                    "tipo_reporte ENUM('Semanal','Mensual','Final') NOT NULL, " +
                    "id_estudiante INT, " +
                    "FOREIGN KEY (id_estudiante) REFERENCES estudiante(id_estudiante))");
        }

        testStudent = new Student();
        testStudent.setFullName("Estudiante Prueba");
        studentDAO.addStudent(testStudent);
        testReports = new ArrayList<>();
        testReports.add(createTestReport(Timestamp.valueOf(LocalDateTime.now()), 5, ReportType.Semanal, testStudent));
        testReports.add(createTestReport(Timestamp.valueOf(LocalDateTime.now().minusDays(7)), 10, ReportType.Mensual, testStudent));
    }

    private static Report createTestReport(Timestamp date, int hours, ReportType type, Student student) throws SQLException {
        Report report = new Report();
        report.setReportDate(date);
        report.setHoursReport(hours);
        report.setReportType(type);
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
        // Limpiar reportes
        try (Statement stmt = testConnection.createStatement()) {
            stmt.execute("DELETE FROM reporte");
            stmt.execute("ALTER TABLE reporte AUTO_INCREMENT = 1");
        }

        // Recrear reportes de prueba
        testReports = new ArrayList<>();
        testReports.add(createTestReport(Timestamp.valueOf(LocalDateTime.now()), 5, ReportType.Semanal, testStudent));
        testReports.add(createTestReport(Timestamp.valueOf(LocalDateTime.now().minusDays(7)), 10, ReportType.Mensual, testStudent));
    }

    @Test
    void testAddReport_Success() throws SQLException {
        Report newReport = new Report();
        newReport.setReportDate(Timestamp.valueOf(LocalDateTime.now()));
        newReport.setHoursReport(8);
        newReport.setReportType(ReportType.Final);
        newReport.setStudent(testStudent);

        int initialCount = reportDAO.countReports();
        boolean result = reportDAO.addReport(newReport);

        assertTrue(result);
        assertEquals(initialCount + 1, reportDAO.countReports());
        assertTrue(newReport.getIdReport() > 0);

        Report addedReport = reportDAO.getReportById(newReport.getIdReport());
        assertNotNull(addedReport);
        assertEquals(8, addedReport.getHoursReport());
        assertEquals(ReportType.Final, addedReport.getReportType());
        assertEquals(testStudent.getIdUser(), addedReport.getStudent().getIdUser());
    }

    @Test
    void testAddReport_NullDate_ShouldThrowException() {
        Report invalidReport = new Report();
        invalidReport.setReportDate(null);
        invalidReport.setHoursReport(5);
        invalidReport.setReportType(ReportType.Semanal);
        invalidReport.setStudent(testStudent);

        assertThrows(SQLException.class, () -> reportDAO.addReport(invalidReport));
    }

    @Test
    void testGetReportById_Exists() throws SQLException {
        Report testReport = testReports.get(0);
        Report foundReport = reportDAO.getReportById(testReport.getIdReport());

        assertNotNull(foundReport);
        assertEquals(testReport.getHoursReport(), foundReport.getHoursReport());
        assertEquals(testReport.getReportType(), foundReport.getReportType());
        assertEquals(testReport.getStudent().getIdUser(), foundReport.getStudent().getIdUser());
    }

    @Test
    void testGetReportById_NotExists() throws SQLException {
        Report foundReport = reportDAO.getReportById(9999);
        assertNull(foundReport);
    }

    @Test
    void testGetAllReports_WithData() throws SQLException {
        List<Report> reports = reportDAO.getAllReports();
        assertEquals(testReports.size(), reports.size());

        for (Report testReport : testReports) {
            boolean found = reports.stream()
                    .anyMatch(r -> r.getIdReport() == testReport.getIdReport());
            assertTrue(found, "No se encontró el reporte esperado");
        }
    }

    @Test
    void testGetReportsByStudent_Exists() throws SQLException {
        List<Report> reports = reportDAO.getReportsByStudent(testStudent.getIdUser());
        assertEquals(testReports.size(), reports.size());

        for (Report report : reports) {
            assertEquals(testStudent.getIdUser(), report.getStudent().getIdUser());
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
        reportToUpdate.setReportDate(Timestamp.valueOf(LocalDateTime.now().minusDays(3)));

        boolean result = reportDAO.updateReport(reportToUpdate);
        assertTrue(result);

        Report updatedReport = reportDAO.getReportById(reportToUpdate.getIdReport());
        assertEquals(15, updatedReport.getHoursReport());
        assertEquals(ReportType.Mensual, updatedReport.getReportType());
    }

    @Test
    void testUpdateReport_NotExists() throws SQLException {
        Report nonExistentReport = new Report();
        nonExistentReport.setIdReport(9999);
        nonExistentReport.setReportDate(Timestamp.valueOf(LocalDateTime.now()));
        nonExistentReport.setHoursReport(5);
        nonExistentReport.setReportType(ReportType.Semanal);
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
        extraReport.setStudent(testStudent);
        reportDAO.addReport(extraReport);

        assertEquals(count + 1, reportDAO.countReports());
    }
}