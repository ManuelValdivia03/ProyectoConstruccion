package userinterface.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import logic.daos.AccountDAO;
import logic.logicclasses.Student;
import logic.logicclasses.Account;
import logic.services.ExceptionManager;
import userinterface.utilities.Validators;
import userinterface.windows.EditProfileStudentWindow;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ControllerEditProfileStudentWindow implements EventHandler<ActionEvent> {
    private static final Logger LOGGER = Logger.getLogger(ControllerEditProfileStudentWindow.class.getName());
    private static final String SUCCESS_STYLE = "-fx-text-fill: #009900;";
    private static final String ERROR_STYLE = "-fx-text-fill: #cc0000;";
    private static final int SUCCESS_MESSAGE_DELAY_MS = 2000;

    private final EditProfileStudentWindow view;
    private final AccountDAO accountDAO;
    private final Student originalStudent;
    private final Stage currentStage;
    private final Runnable refreshCallback;
    private final Validators validators;
    private final String originalEmail;

    public ControllerEditProfileStudentWindow(EditProfileStudentWindow view, Student student,
                                              Stage stage, Runnable callback) {
        this.view = Objects.requireNonNull(view, "EditProfileStudentWindow view cannot be null");
        this.accountDAO = new AccountDAO();
        this.originalStudent = Objects.requireNonNull(student, "Student cannot be null");
        this.currentStage = Objects.requireNonNull(stage, "Stage cannot be null");
        this.refreshCallback = Objects.requireNonNull(callback, "Refresh callback cannot be null");
        this.validators = new Validators();

        try {
            this.originalEmail = accountDAO.getAccountByUserId(student.getIdUser()).getEmail();
            String status = student.getStatus() == 'A' ? "Activo" : "Inactivo";
            view.loadStudentData(student, originalEmail, status);
            setupEventHandlers();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading student data", e);
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
        boolean canContinue = true;
        try {
            clearError();

            if (!validateAllFields()) {
                canContinue = false;
            }

            String email = view.getEmailField().getText().trim();
            String password = view.getPassword();
            String confirmPassword = view.getConfirmPassword();

            if (canContinue && !validatePasswordsMatch(password, confirmPassword)) {
                canContinue = false;
            }

            if (canContinue && !email.equals(originalEmail) && accountDAO.accountExists(email)) {
                showError("El email ya está registrado");
                canContinue = false;
            }

            if (canContinue) {
                updateAccount(email, password);
                showSuccessAndClose();
            }

        } catch (SQLException e) {
            String message = ExceptionManager.handleException(e);
            showError(message);
        }
    }

    private boolean validateAllFields() {
        boolean isValid = true;

        if (!validators.validateEmail(view.getEmailField().getText())) {
            showError("Formato de email inválido");
            isValid = false;
        }

        String password = view.getPassword();
        if (!password.isEmpty() && !validators.validatePassword(password)) {
            showError("La contraseña debe tener al menos 8 caracteres");
            isValid = false;
        }

        return isValid;
    }

    private boolean validatePasswordsMatch(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            showError("Las contraseñas no coinciden");
            return false;
        }
        return true;
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
        showSuccessMessage();
        scheduleClose();
    }

    private void showSuccessMessage() {
        view.getResultLabel().setText("Cuenta actualizada correctamente");
        view.getResultLabel().setStyle(SUCCESS_STYLE);
    }

    private void scheduleClose() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                javafx.application.Platform.runLater(() -> {
                    currentStage.close();
                    refreshCallback.run();
                });
            }
        }, SUCCESS_MESSAGE_DELAY_MS);
    }

    private void showError(String message) {
        view.getResultLabel().setText(message);
        view.getResultLabel().setStyle(ERROR_STYLE);
    }

    private void clearError() {
        view.getResultLabel().setText("");
    }
}