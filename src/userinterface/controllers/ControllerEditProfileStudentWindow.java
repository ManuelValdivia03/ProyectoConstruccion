package userinterface.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import logic.daos.AccountDAO;
import logic.logicclasses.Student;
import logic.logicclasses.Account;
import userinterface.utilities.Validators;
import userinterface.windows.EditProfileStudentWindow;

import java.sql.SQLException;

public class ControllerEditProfileStudentWindow implements EventHandler<ActionEvent> {
    private final EditProfileStudentWindow view;
    private final AccountDAO accountDAO;
    private final Student originalStudent;
    private final Stage currentStage;
    private final Runnable refreshCallback;
    private String originalEmail;

    public ControllerEditProfileStudentWindow(EditProfileStudentWindow view, Student student,
                                              Stage stage, Runnable callback) {
        this.view = view;
        this.accountDAO = new AccountDAO();
        this.originalStudent = student;
        this.currentStage = stage;
        this.refreshCallback = callback;

        try {
            this.originalEmail = accountDAO.getAccountByUserId(student.getIdUser()).getEmail();
            String status = student.getStatus() == 'A' ? "Activo" : "Inactivo";
            view.loadStudentData(student, originalEmail, status);
            setupEventHandlers();
        } catch (SQLException e) {
            throw new RuntimeException("Error al cargar datos del estudiante", e);
        }
    }

    private void setupEventHandlers() {
        view.getUpdateButton().setOnAction(this);
        view.getCancelButton().setOnAction(event -> currentStage.close());
    }

    @Override
    public void handle(ActionEvent event) {
        if (event.getSource() == view.getUpdateButton()) {
            handleUpdateAccount();
        }
    }

    private void handleUpdateAccount() {
        try {
            clearError();

            if (!validateAllFields()) {
                return;
            }

            String email = view.getEmailField().getText().trim();
            String password = view.getPassword();
            String confirmPassword = view.getConfirmPassword();

            if (!password.equals(confirmPassword)) {
                showError("Las contraseñas no coinciden");
                return;
            }

            if (!email.equals(originalEmail)) {
                if (accountDAO.accountExists(email)) {
                    showError("El email ya está registrado");
                    return;
                }
            }

            updateAccount(email, password);

            showSuccessAndClose();

        } catch (SQLException e) {
            showError("Error de base de datos: " + e.getMessage());
        }
    }

    private boolean validateAllFields() {
        boolean isValid = true;
        Validators validators = new Validators();

        if (!validators.validateEmail(view.getEmailField().getText())) {
            showError("Formato de email inválido");
            isValid = false;
        }

        String password = view.getPassword();
        if (!password.isEmpty() && password.length() < 8) {
            showError("La contraseña debe tener al menos 8 caracteres");
            isValid = false;
        }

        return isValid;
    }

    private void updateAccount(String email, String password) throws SQLException {
        Account account = new Account(
                originalStudent.getIdUser(),
                email,
                password.isEmpty() ? null : password
        );

        if (!accountDAO.updateAccount(account)) {
            throw new SQLException("No se pudo actualizar la cuenta");
        }
    }

    private void showSuccessAndClose() {
        view.getResultLabel().setText("Cuenta actualizada correctamente");
        view.getResultLabel().setStyle("-fx-text-fill: #009900;");

        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        javafx.application.Platform.runLater(() -> {
                            currentStage.close();
                            if (refreshCallback != null) {
                                refreshCallback.run();
                            }
                        });
                    }
                },
                2000
        );
    }

    private void showError(String message) {
        view.getResultLabel().setText(message);
        view.getResultLabel().setStyle("-fx-text-fill: #cc0000;");
    }

    private void clearError() {
        view.getResultLabel().setText("");
    }
}