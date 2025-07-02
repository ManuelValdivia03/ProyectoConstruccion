package userinterface.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import logic.daos.StudentDAO;
import logic.logicclasses.Student;
import logic.services.ExceptionManager;
import userinterface.windows.AssignGradeWindow;
import java.sql.SQLException;
import java.util.Objects;

public class ControllerAssignGradeWindow implements EventHandler<ActionEvent> {
    private static final String SUCCESS_STYLE = "-fx-text-fill: #009900;";
    private static final String ERROR_STYLE = "-fx-text-fill: #cc0000;";
    private static final int MIN_GRADE = 0;
    private static final int MAX_GRADE = 10;

    private final AssignGradeWindow view;
    private final StudentDAO studentDAO;
    private final Student student;
    private final Runnable refreshCallback;

    public ControllerAssignGradeWindow(AssignGradeWindow view, Student student, Runnable callback) {
        this.view = Objects.requireNonNull(view, "Vista no puede ser nula");
        this.studentDAO = new StudentDAO();
        this.student = Objects.requireNonNull(student, "Estudiante no puede ser nulo");
        this.refreshCallback = callback;

        initializeView();
    }

    private void initializeView() {
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        view.getConfirmButton().setOnAction(this);
        view.getCancelButton().setOnAction(this::handleCancel);
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

            if (isValidGrade(gradeText)) {
                int grade = Integer.parseInt(gradeText);
                updateStudentGrade(grade);
                handleSuccess();
            }
        } catch (NumberFormatException e) {
            showError("La calificación debe ser un número entero");
        } catch (Exception e) {
            String message = ExceptionManager.handleException(e);
            showError(message);
        }
    }

    private boolean isValidGrade(String gradeText) {
        boolean isValid = false;
        if (gradeText.isEmpty()) {
            showError("La calificación no puede estar vacía");
        } else {
            try {
                int grade = Integer.parseInt(gradeText);
                if (grade < MIN_GRADE || grade > MAX_GRADE) {
                    showError(String.format("La calificación debe estar entre %d y %d", MIN_GRADE, MAX_GRADE));
                } else {
                    isValid = true;
                }
            } catch (NumberFormatException e) {
                showError("Formato de calificación inválido");
            }
        }
        return isValid;
    }

    private void updateStudentGrade(int grade) throws SQLException {
        boolean success = studentDAO.updateStudentGrade(student.getIdUser(), grade);
        if (!success) {
            throw new SQLException("No se pudo actualizar la calificación");
        }
        student.setGrade(grade);
    }

    private void handleSuccess() {
        showSuccess("Calificación actualizada correctamente");
        if (refreshCallback != null) {
            refreshCallback.run();
        }
        closeWindow();
    }

    private void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private void showSuccess(String message) {
        view.getMessageLabel().setText(message);
        view.getMessageLabel().setStyle(SUCCESS_STYLE);
    }

    private void showError(String message) {
        view.getMessageLabel().setText(message);
        view.getMessageLabel().setStyle(ERROR_STYLE);
    }

    private void closeWindow() {
        view.getView().getScene().getWindow().hide();
    }
}