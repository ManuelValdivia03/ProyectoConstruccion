package userinterface.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import logic.daos.AccountDAO;
import logic.daos.StudentDAO;
import logic.daos.UserDAO;
import logic.exceptions.RepeatedCellPhoneException;
import logic.exceptions.RepeatedEmailException;
import logic.logicclasses.Student;
import logic.logicclasses.Account;
import logic.logicclasses.User;
import logic.services.ExceptionManager;
import userinterface.utilities.Validators;
import userinterface.windows.UpdateStudentWindow;

import java.sql.SQLException;
import java.util.Objects;

public class ControllerUpdateStudentWindow {
    private static final String ERROR_BORDER_STYLE = "-fx-border-color: #ff0000; -fx-border-width: 1px;";
    private static final String DEFAULT_BORDER_STYLE = "";
    private static final String ERROR_TEXT_STYLE = "-fx-text-fill: #cc0000;";
    private static final String SUCCESS_TEXT_STYLE = "-fx-text-fill: #009900;";

    private final UpdateStudentWindow view;
    private final StudentDAO studentDAO;
    private final UserDAO userDAO;
    private final AccountDAO accountDAO;
    private final Student originalStudent;
    private final Stage currentStage;
    private final Runnable refreshCallback;
    private final Validators validators;
    private String originalEmail;

    public ControllerUpdateStudentWindow(UpdateStudentWindow view, Student student,
                                         Stage stage, Runnable callback) {
        this.view = Objects.requireNonNull(view, "La vista no puede ser nula");
        this.studentDAO = new StudentDAO();
        this.userDAO = new UserDAO();
        this.accountDAO = new AccountDAO();
        this.originalStudent = Objects.requireNonNull(student, "El estudiante no puede ser nulo");
        this.currentStage = Objects.requireNonNull(stage, "El stage no puede ser nulo");
        this.refreshCallback = callback;
        this.validators = new Validators();

        try {
            this.originalEmail = accountDAO.getAccountByUserId(student.getIdUser()).getEmail();
            view.loadStudentData(student, originalEmail);
            setupEventHandlers();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error al cargar datos del estudiante: " + e.getMessage());
        }
    }

    private void setupEventHandlers() {
        view.getUpdateButton().setOnAction(this::handleUpdateStudent);
        view.getCancelButton().setOnAction(event -> currentStage.close());
    }

    private void handleUpdateStudent(ActionEvent event) {
        try {
            clearError();
            resetFieldStyles();

            if (!validateAllFields()) {
                return;
            }

            String name = view.getNameField().getText().trim();
            String phone = view.getPhoneField().getText().trim();
            String email = view.getEmailField().getText().trim();
            String password = view.getPassword();

            verifyDataUniqueness(phone, email);

            updateUser(name, phone);
            updateStudent();
            updateAccount(email, password);

            showSuccessAndClose();

        } catch (RepeatedCellPhoneException e) {
            showFieldError("El número de teléfono ya está registrado", view.getPhoneField());
        } catch (RepeatedEmailException e) {
            showFieldError("El email ya está registrado", view.getEmailField());
        } catch (SQLException e) {
            String message = ExceptionManager.handleException(e);
            showAlert(Alert.AlertType.ERROR, "Error de base de datos", "Error: " + message);
        }
    }

    private boolean validateAllFields() {
        boolean isValid = true;

        if (view.getNameField().getText().trim().isEmpty()) {
            showFieldError("Nombre completo es obligatorio", view.getNameField());
            isValid = false;
        } else if (!validators.validateName(view.getNameField().getText().trim())) {
            showFieldError("El nombre solo debe contener letras y espacios", view.getNameField());
            isValid = false;
        }

        if (!validators.validateCellPhone(view.getPhoneField().getText())) {
            showFieldError("Teléfono debe tener 10 dígitos", view.getPhoneField());
            isValid = false;
        }

        if (!validators.validateEmail(view.getEmailField().getText())) {
            showFieldError("Formato de email inválido", view.getEmailField());
            isValid = false;
        }

        String extension = view.getPhoneExtensionField().getText().trim();
        if (!extension.isEmpty() && !validators.validatePhoneExtension(extension)) {
            showFieldError("Extensión debe ser numérica y máximo 5 dígitos", view.getPhoneExtensionField());
            isValid = false;
        }

        return isValid;
    }

    private void verifyDataUniqueness(String phone, String email)
            throws SQLException, RepeatedCellPhoneException, RepeatedEmailException {
        if (!phone.equals(originalStudent.getCellPhone()) && userDAO.cellPhoneExists(phone)) {
            throw new RepeatedCellPhoneException();
        }
        if (!email.equals(originalEmail) && accountDAO.accountExists(email)) {
            throw new RepeatedEmailException();
        }
    }

    private void updateUser(String name, String phone) throws SQLException {
        User user = new User(
                originalStudent.getIdUser(),
                name,
                phone,
                view.getPhoneExtensionField().getText().trim(),
                originalStudent.getStatus()
        );
        if (!userDAO.updateUser(user)) {
            throw new SQLException("No se pudo actualizar el usuario");
        }
    }

    private void updateStudent() throws SQLException {
        char status = view.getStatusComboBox().getValue().equals("Activo") ? 'A' : 'I';
        Student student = new Student(
                originalStudent.getIdUser(),
                view.getNameField().getText().trim(),
                view.getPhoneField().getText().trim(),
                view.getPhoneExtensionField().getText().trim(),
                status,
                originalStudent.getEnrollment(),
                originalStudent.getGrade()
        );
        if (!studentDAO.updateStudent(student)) {
            throw new SQLException("No se pudo actualizar el estudiante");
        }
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
        Platform.runLater(() -> {
            view.getResultLabel().setText("Estudiante actualizado correctamente");
            view.getResultLabel().setStyle(SUCCESS_TEXT_STYLE);

            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            Platform.runLater(() -> {
                                currentStage.close();
                                if (refreshCallback != null) {
                                    refreshCallback.run();
                                }
                            });
                        }
                    },
                    1500
            );
        });
    }

    private void resetFieldStyles() {
        view.getNameField().setStyle(DEFAULT_BORDER_STYLE);
        view.getPhoneField().setStyle(DEFAULT_BORDER_STYLE);
        view.getEmailField().setStyle(DEFAULT_BORDER_STYLE);
        view.getPhoneExtensionField().setStyle(DEFAULT_BORDER_STYLE);
    }

    private void showFieldError(String message, TextField field) {
        showError(message);
        highlightField(field);
    }

    private void highlightField(TextField field) {
        field.setStyle(ERROR_BORDER_STYLE);
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            view.getResultLabel().setText(message);
            view.getResultLabel().setStyle(ERROR_TEXT_STYLE);
        });
    }

    private void clearError() {
        view.getResultLabel().setText("");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
