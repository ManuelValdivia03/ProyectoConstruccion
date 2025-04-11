package logic;

import logic.enums.ReportType;

import java.sql.Timestamp;

public class Report {
    private int idReport;
    private Timestamp reportDate;
    private int hoursReport;
    private ReportType reportType;
    private Student student;

    public Report(int idReport, Timestamp reportDate, int hoursReport, ReportType reportType, Student student) {
        this.idReport = idReport;
        this.reportDate = reportDate;
        this.hoursReport = hoursReport;
        this.reportType = reportType;
        this.student = student;
    }

    public Report(){
        idReport = 0;
        reportDate = null;
        hoursReport = 0;
        reportType = ReportType.NONE;
        student = null;
    }

    public int getIdReport() {
        return idReport;
    }

    public void setIdReport(int idReport) {
        this.idReport = idReport;
    }

    public Timestamp getReportDate() {
        return reportDate;
    }

    public void setReportDate(Timestamp reportDate) {
        this.reportDate = reportDate;
    }

    public int getHoursReport() {
        return hoursReport;
    }

    public void setHoursReport(int hoursReport) {
        this.hoursReport = hoursReport;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }
}
