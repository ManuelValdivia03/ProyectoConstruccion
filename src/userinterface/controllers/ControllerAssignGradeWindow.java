package userinterface.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import logic.daos.StudentDAO;
import logic.logicclasses.Student;
import userinterface.windows.AssignGradeWindow;

import java.sql.SQLException;

public class ControllerAssignGradeWindow implements EventHandler<ActionEvent> {
    private final AssignGradeWindow view;
    private final StudentDAO studentDAO;
    private final Student student;
    private final Runnable refreshCallback;

    public ControllerAssignGradeWindow(AssignGradeWindow view, Student student, Runnable callback) {
        this.view = view;
        this.studentDAO = new StudentDAO();
        this.student = student;
        this.refreshCallback = callback;
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        view.getConfirmButton().setOnAction(this);
        view.getCancelButton().setOnAction(event -> closeWindow());
    }

    @Override
    public void handle(ActionEvent event) {
        if (event.getSource() == view.getConfirmButton()) {
            handleAssignGrade();
        }
    }

    private void handleAssignGrade() {
        try {
            String gradeText = view.getGradeField().getText().trim();

            if (!validateGrade(gradeText)) {
                return;
            }

            int grade = Integer.parseInt(gradeText);
            updateStudentGrade(grade);

            showSuccess();
            if (refreshCallback != null) {
                refreshCallback.run();
            }

        } catch (NumberFormatException e) {
            showError("La calificación debe ser un número entero");
        } catch (SQLException e) {
            showError("Error al actualizar la calificación: " + e.getMessage());
        }
    }

    private boolean validateGrade(String gradeText) {
        if (gradeText.isEmpty()) {
            showError("La calificación no puede estar vacía");
            return false;
        }

        try {
            int grade = Integer.parseInt(gradeText);
            if (grade < 0 || grade > 10) {
                showError("La calificación debe estar entre 0 y 10");
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            showError("La calificación debe ser un número válido");
            return false;
        }
    }

    private void updateStudentGrade(int grade) throws SQLException {
        if (!studentDAO.updateStudentGrade(student.getIdUser(), grade)) {
            throw new SQLException("No se pudo actualizar la calificación");
        }
        student.setGrade(grade);
    }

    private void showSuccess() {
        view.getMessageLabel().setText("Calificación actualizada correctamente");
        view.getMessageLabel().setStyle("-fx-text-fill: #009900;");
    }

    private void showError(String message) {
        view.getMessageLabel().setText(message);
        view.getMessageLabel().setStyle("-fx-text-fill: #cc0000;");
    }

    private void closeWindow() {
        view.getView().getScene().getWindow().hide();
    }
}