package userinterface.controllers;

import javafx.stage.Stage;
import logic.daos.GroupDAO;
import logic.daos.ReportDAO;
import logic.daos.StudentDAO;
import logic.logicclasses.Academic;
import logic.logicclasses.Student;
import logic.logicclasses.Report;
import userinterface.windows.ConsultGroupStudentReportsWindow;
import logic.services.ExceptionManager;

import java.util.List;

public class ControllerConsultGroupStudentReportsWindow {
    private final ConsultGroupStudentReportsWindow view;
    private final Stage stage;
    private final Academic academic;
    private final GroupDAO groupDAO = new GroupDAO();
    private final ReportDAO reportDAO = new ReportDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    public ControllerConsultGroupStudentReportsWindow(ConsultGroupStudentReportsWindow view, Stage stage, Academic academic) {
        this.view = view;
        this.stage = stage;
        this.academic = academic;

        loadStudents();
        setupListeners();
    }

    private void loadStudents() {
        try {
            List<Student> students = studentDAO.getStudentsByGroup(groupDAO.getGroupByAcademicId(academic.getIdUser()).getNrc());
            view.setStudents(students);
        } catch (Exception e) {
            ExceptionManager.handleException(e);
        }
    }

    private void setupListeners() {
        view.setOnViewReportsListener(student -> {
            try {
                List<Report> reports = reportDAO.getReportsByStudent(student.getIdUser());
                view.setReports(reports);
            } catch (Exception e) {
                ExceptionManager.handleException(e);
            }
        });
    }
}

