package userinterface.controllers;

import javafx.stage.Stage;
import logic.daos.SelfEvaluationDAO;
import logic.logicclasses.SelfEvaluation;
import logic.logicclasses.Student;
import logic.services.ExceptionManager;
import userinterface.windows.ConsultSelfEvaluationWindow;

public class ControllerConsultSelfEvaluationWindow {
    private final ConsultSelfEvaluationWindow view;
    private final Stage stage;
    private final Student student;

    public ControllerConsultSelfEvaluationWindow(ConsultSelfEvaluationWindow view, Stage stage, Student student) {
        this.view = view;
        this.stage = stage;
        this.student = student;
        loadSelfEvaluation();
    }

    private void loadSelfEvaluation() {
        try {
            SelfEvaluationDAO selfEvaluationDAO = new SelfEvaluationDAO();
            SelfEvaluation selfEval = selfEvaluationDAO.getSelfEvaluationByStudent(student.getIdUser());
            view.setSelfEvaluation(selfEval);
        } catch (Exception e) {
            String msg = ExceptionManager.handleException(e);
            view.setSelfEvaluation(null);
        }
    }
}

