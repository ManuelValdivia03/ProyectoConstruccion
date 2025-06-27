package logic.logicclasses;

import logic.enums.ReportType;
import java.sql.Timestamp;
import java.util.Objects;

public class Report {
    private int idReport;
    private Timestamp reportDate;
    private int hoursReport;
    private ReportType reportType;
    private String methodology;
    private String description;
    private Student student;

    public Report(int idReport, Timestamp reportDate, int hoursReport, ReportType reportType, String methodology, String description,Student student) {
        this.idReport = idReport;
        this.reportDate = reportDate;
        this.hoursReport = hoursReport;
        this.reportType = reportType;
        this.methodology = methodology;
        this.description = description;
        this.student = student;
    }

    public Report(){
        idReport = 0;
        reportDate = null;
        hoursReport = 0;
        reportType = ReportType.NONE;
        methodology = "";
        description = "";
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

    public String getMethodology() {
        return methodology;
    }

    public void setMethodology(String methodology) {
        this.methodology = methodology;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Report report = (Report) o;
        return idReport == report.idReport &&
                hoursReport == report.hoursReport &&
                Objects.equals(reportDate, report.reportDate) &&
                reportType == report.reportType &&
                Objects.equals(methodology, report.methodology) &&
                Objects.equals(description, report.description) &&
                Objects.equals(student, report.student);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idReport, reportDate, hoursReport, reportType,
                methodology, description, student);
    }
}
