package userinterface.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import logic.daos.EvaluationDAO;
import logic.daos.ProjectStudentDAO;
import logic.daos.ProjectDAO;
import logic.daos.AcademicDAO;
import logic.daos.StudentDAO;
import logic.logicclasses.Evaluation;
import logic.logicclasses.Presentation;
import logic.logicclasses.Project;
import logic.logicclasses.Academic;
import logic.logicclasses.Student;
import logic.services.ExceptionManager;
import userinterface.windows.ConsultStudentEvaluationsWindow;

import java.util.List;

public class ControllerConsultStudentEvaluationsWindow {
    private final ConsultStudentEvaluationsWindow view;
    private final Stage stage;
    private final Student student;

    public ControllerConsultStudentEvaluationsWindow(ConsultStudentEvaluationsWindow view, Stage stage, Student student) {
        this.view = view;
        this.stage = stage;
        this.student = student;
        loadEvaluations();
    }

    private void loadEvaluations() {
        try {
            List<Evaluation> evaluations = loadEvaluationsData();
            showEvaluations(evaluations);
        } catch (Exception e) {
            String message = ExceptionManager.handleException(e);
            view.showError(message);
        }
    }

    private List<Evaluation> loadEvaluationsData() throws Exception {
        EvaluationDAO evaluationDAO = new EvaluationDAO();
        ProjectStudentDAO projectStudentDAO = new ProjectStudentDAO();
        ProjectDAO projectDAO = new ProjectDAO();
        AcademicDAO academicDAO = new AcademicDAO();

        List<Evaluation> evaluations = evaluationDAO.getEvaluationsByStudent(student.getIdUser());

        for (Evaluation evaluation : evaluations) {
            Presentation presentation = evaluation.getPresentation();

            if (presentation != null && presentation.getStudent() != null && presentation.getStudent().getIdUser() > 0) {
                Student fullStudent = new StudentDAO().getStudentById(presentation.getStudent().getIdUser());
                if (fullStudent != null) {
                    presentation.setStudent(fullStudent);
                }
                Integer projectId = projectStudentDAO.getProyectByStudent(presentation.getStudent().getIdUser());
                String projectTitle = "";
                if (projectId != null && projectId > 0) {
                    Project project = projectDAO.getProyectById(projectId);
                    if (project != null) {
                        projectTitle = project.getTitle();
                    }
                }
                presentation.getStudent().setCellphone(projectTitle);
            }

            if (evaluation.getAcademic() != null && evaluation.getAcademic().getIdUser() > 0) {
                Academic fullAcademic = academicDAO.getAcademicById(evaluation.getAcademic().getIdUser());
                if (fullAcademic != null) {
                    evaluation.setAcademic(fullAcademic);
                }
            }
        }
        return evaluations;
    }

    private void showEvaluations(List<Evaluation> evaluations) {
        ObservableList<Evaluation> observableEvaluations = FXCollections.observableArrayList(evaluations);
        view.setEvaluations(observableEvaluations);
    }
}
