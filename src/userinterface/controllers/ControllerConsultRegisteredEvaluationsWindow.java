package userinterface.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import logic.daos.EvaluationDAO;
import logic.daos.ProjectStudentDAO;
import logic.daos.ProjectDAO;
import logic.daos.StudentDAO;
import logic.logicclasses.Academic;
import logic.logicclasses.Evaluation;
import logic.logicclasses.Presentation;
import logic.logicclasses.Project;
import logic.logicclasses.Student;
import logic.services.ExceptionManager;
import userinterface.windows.ConsultRegisteredEvaluationsWindow;

import java.util.List;

public class ControllerConsultRegisteredEvaluationsWindow {
    private final ConsultRegisteredEvaluationsWindow view;
    private final Stage stage;
    private final Academic evaluator;

    public ControllerConsultRegisteredEvaluationsWindow(ConsultRegisteredEvaluationsWindow view, Stage stage, Academic evaluator) {
        this.view = view;
        this.stage = stage;
        this.evaluator = evaluator;
        loadEvaluations();
    }

    private void loadEvaluations() {
        try {
            EvaluationDAO evaluationDAO = new EvaluationDAO();
            ProjectStudentDAO projectStudentDAO = new ProjectStudentDAO();
            ProjectDAO projectDAO = new ProjectDAO();

            List<Evaluation> evaluations = evaluationDAO.getEvaluationsByAcademic(evaluator.getIdUser());

            StudentDAO studentDAO = new StudentDAO();
            for (Evaluation evaluation : evaluations) {
                Presentation presentation = evaluation.getPresentation();
                if (presentation != null && presentation.getStudent() != null && presentation.getStudent().getIdUser() > 0) {                    Student student = presentation.getStudent();
                    Student fullStudent = studentDAO.getStudentById(student.getIdUser());
                    if (fullStudent != null) {
                        presentation.setStudent(fullStudent);
                    }
                    Integer projectId = projectStudentDAO.getProyectByStudent(student.getIdUser());
                    String projectTitle = "";
                    if (projectId != null && projectId > 0) {
                        Project project = projectDAO.getProyectById(projectId);
                        if (project != null) {
                            projectTitle = project.getTitle();
                        }
                    }
                    presentation.getStudent().setCellphone(projectTitle);
                }
            }

            ObservableList<Evaluation> observableEvaluations = FXCollections.observableArrayList(evaluations);
            view.setEvaluations(observableEvaluations);
        } catch (Exception e) {
            String message = ExceptionManager.handleException(e);
            view.showError(message);
        }
    }
}
