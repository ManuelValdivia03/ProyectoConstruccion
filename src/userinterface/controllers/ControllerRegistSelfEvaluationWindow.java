package userinterface.controllers;

import javafx.scene.control.Toggle;
import javafx.stage.Stage;
import logic.daos.SelfEvaluationDAO;
import logic.logicclasses.SelfEvaluation;
import logic.logicclasses.Student;
import logic.services.ExceptionManager;
import userinterface.windows.RegistSelfEvaluationWindow;

import java.sql.SQLException;
import java.util.Map;

public class ControllerRegistSelfEvaluationWindow {
    private final RegistSelfEvaluationWindow view;
    private final Stage stage;
    private final Student student;

    public ControllerRegistSelfEvaluationWindow(RegistSelfEvaluationWindow view, Stage stage, Student student) {
        this.view = view;
        this.stage = stage;
        this.student = student;
        setupListeners();
    }

    private void setupListeners() {
        view.getSubmitButton().setOnAction(e -> handleSaveSelfEvaluation());
    }

    private void handleSaveSelfEvaluation() {
        try {
            Map<String, javafx.scene.control.ToggleGroup> rubricGroups = view.getRubricGroups();

            if (!validateRubricGroups(rubricGroups)) {
                showMessage("Debes calificar todos los criterios.", true);
                return;
            }

            double grade = calculateGrade(rubricGroups);
            String comments = view.getCommentsArea().getText();

            SelfEvaluation selfEvaluation = buildSelfEvaluation(grade, comments);

            if (saveSelfEvaluation(selfEvaluation)) {
                showMessage("Autoevaluación registrada correctamente.", false);
                stage.close();
            } else {
                showMessage("No se pudo registrar la autoevaluación.", true);
            }
        } catch (Exception ex) {
            String message = ExceptionManager.handleException(ex);
            showMessage(message, true);
        }
    }

    private boolean validateRubricGroups(Map<String, javafx.scene.control.ToggleGroup> rubricGroups) {
        for (var group : rubricGroups.values()) {
            if (group.getSelectedToggle() == null) {
                return false;
            }
        }
        return true;
    }

    private double calculateGrade(Map<String, javafx.scene.control.ToggleGroup> rubricGroups) {
        int total = 0;
        int count = 0;
        for (var group : rubricGroups.values()) {
            Toggle selected = group.getSelectedToggle();
            total += (int) selected.getUserData();
            count++;
        }
        return (total / (double) count) * 2;
    }

    private SelfEvaluation buildSelfEvaluation(double grade, String comments) {
        SelfEvaluation selfEvaluation = new SelfEvaluation();
        selfEvaluation.setCalification((float) grade);
        selfEvaluation.setFeedBack(comments);
        selfEvaluation.setStudent(student);
        return selfEvaluation;
    }

    private boolean saveSelfEvaluation(SelfEvaluation selfEvaluation) throws SQLException {
        SelfEvaluationDAO selfEvaluationDAO = new SelfEvaluationDAO();
        return selfEvaluationDAO.addSelfEvaluation(selfEvaluation);
    }

    private void showMessage(String message, boolean error) {
        view.getGradeLabel().setText((error ? "❌ " : "✔ ") + message);
        if (!error) {
            view.getGradeLabel().setStyle("-fx-text-fill: #388e3c;");
        } else {
            view.getGradeLabel().setStyle("-fx-text-fill: #d32f2f;");
        }
    }
}