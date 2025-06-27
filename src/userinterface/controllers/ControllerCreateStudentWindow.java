package userinterface.controllers;

import dataaccess.PasswordUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import logic.daos.AccountDAO;
import logic.daos.StudentDAO;
import logic.daos.UserDAO;
import logic.exceptions.RepeatedCellPhoneException;
import logic.exceptions.RepeatedEmailException;
import logic.exceptions.RepeatedEnrollmentException;
import logic.logicclasses.Academic;
import logic.logicclasses.Account;
import logic.logicclasses.Student;
import logic.logicclasses.User;
import logic.services.ExceptionManager;
import userinterface.utilities.Validators;
import userinterface.windows.CreateStudentWindow;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ControllerCreateStudentWindow implements EventHandler<ActionEvent> {
    private static final Logger LOGGER = Logger.getLogger(ControllerCreateStudentWindow.class.getName());
    private static final String ERROR_STYLE = "-fx-border-color: #ff0000; -fx-border-width: 1px;";
    private static final String SUCCESS_DIALOG_STYLE = "-fx-background-color: #f8f8f8; -fx-border-color: #4CAF50; -fx-border-width: 2px;";
    private static final String ERROR_TEXT_STYLE = "-fx-text-fill: #cc0000;";
    private static final String MESSAGE_STYLE = "-fx-font-size: 14px; -fx-font-weight: bold;";

    private final CreateStudentWindow view;
    private final StudentDAO studentDAO;
    private final UserDAO userDAO;
    private final AccountDAO accountDAO;
    private final Validators validators;
    private final Academic academic;

    public ControllerCreateStudentWindow(CreateStudentWindow view, Academic academic) {
        this.view = Objects.requireNonNull(view, "CreateStudentWindow view cannot be null");
        this.studentDAO = new StudentDAO();
        this.userDAO = new UserDAO();
        this.accountDAO = new AccountDAO();
        this.validators = new Validators();
        setupEventHandlers();
        this.academic = Objects.requireNonNull(academic, "Academic cannot be null");

    }

    private void setupEventHandlers() {
        view.getAddButton().setOnAction(this);
        view.getCancelButton().setOnAction(event -> handleCancel());
    }

    @Override
    public void handle(ActionEvent event) {
        if (event.getSource() == view.getAddButton()) {
            handleAddStudent();
        }
    }

    private void handleAddStudent() {
        try {
            clearError();

            if (!validateAllFields()) {
                return;
            }

            StudentRegistrationData data = collectRegistrationData();

            if (!verifyDataUniqueness(data.phone(), data.enrollment(), data.email())) {
                return;
            }

            registerNewStudent(data);
            showSuccessAndReset();

        } catch (RepeatedCellPhoneException e) {
            showFieldError("El número de teléfono ya está registrado", view.getPhoneField());
        } catch (RepeatedEnrollmentException e) {
            showFieldError("La matrícula ya está registrada", view.getEnrollmentField());
        } catch (RepeatedEmailException e) {
            showFieldError("El email ya está registrado", view.getEmailField());
        } catch (SQLException e) {
            String message = ExceptionManager.handleException(e);
            showError(message);
        }
    }

    private StudentRegistrationData collectRegistrationData() {
        String name = view.getNameField().getText().trim();
        String phone = view.getPhoneField().getText().trim();
        String extension = view.getPhoneExtensionField().getText().trim();
        String enrollment = view.getEnrollmentField().getText().trim();
        String email = view.getEmailField().getText().trim();
        String passwordPlain = view.getPassword();
        String passwordHashed = PasswordUtils.hashPassword(passwordPlain);

        return new StudentRegistrationData(name, phone, extension, enrollment, email, passwordHashed);
    }

    private boolean validateAllFields() {
        boolean isValid = true;
        resetFieldStyles();

        if (view.getNameField().getText().isEmpty()) {
            showFieldError("Nombre completo es obligatorio", view.getNameField());
            isValid = false;
        }

        if (!validators.validateCellPhone(view.getPhoneField().getText())) {
            showFieldError("Teléfono debe tener 10 dígitos", view.getPhoneField());
            isValid = false;
        }

        String extension = view.getPhoneExtensionField().getText().trim();
        if (!extension.isEmpty() && !validators.validatePhoneExtension(extension)) {
            showFieldError("Extensión debe ser numérica y máximo 5 dígitos", view.getPhoneExtensionField());
            isValid = false;
        }

        if (!validators.validateEnrollment(view.getEnrollmentField().getText())) {
            showFieldError("Matrícula debe comenzar con S y tener 9 dígitos", view.getEnrollmentField());
            isValid = false;
        }

        if (!validators.validateEmail(view.getEmailField().getText())) {
            showFieldError("Formato de email inválido", view.getEmailField());
            isValid = false;
        }

        if (!validators.validatePassword(view.getPassword())) {
            showError("Contraseña debe tener al menos 8 caracteres");
            isValid = false;
        }

        return isValid;
    }

    private boolean verifyDataUniqueness(String phone, String enrollment, String email)
            throws SQLException, RepeatedCellPhoneException, RepeatedEnrollmentException, RepeatedEmailException {
        if (userDAO.cellPhoneExists(phone)) {
            throw new RepeatedCellPhoneException();
        }
        if (studentDAO.enrollmentExists(enrollment)) {
            throw new RepeatedEnrollmentException();
        }
        if (accountDAO.accountExists(email)) {
            throw new RepeatedEmailException();
        }
        return true;
    }

    private void registerNewStudent(StudentRegistrationData data) throws SQLException {
        User user = createAndSaveUser(data.name(), data.phone());
        createAndSaveStudent(user, data.enrollment());
        createAndSaveAccount(user, data.email(), data.passwordHashed());
    }

    private User createAndSaveUser(String name, String phone) throws SQLException {
        User user = new User(0, name, phone, view.getPhoneExtensionField().getText().trim(), 'A');
        if (!userDAO.addUser(user)) {
            throw new SQLException("No se pudo registrar el usuario");
        }
        return user;
    }

    private void createAndSaveStudent(User user, String enrollment) throws SQLException {
        Student student = new Student(user.getIdUser(), user.getFullName(), user.getCellPhone(), user.getPhoneExtension() ,'A', enrollment, 0);
        if (!studentDAO.addStudent(student, academic.getIdUser())) {
            userDAO.deleteUser(user.getIdUser());
            throw new SQLException("No se pudo registrar el estudiante");
        }
    }

    private void createAndSaveAccount(User user, String email, String password) throws SQLException {
        Account account = new Account(user.getIdUser(), email, password);
        if (!accountDAO.addAccount(account)) {
            studentDAO.deleteStudent(user.getIdUser());
            userDAO.deleteUser(user.getIdUser());
            throw new SQLException("No se pudo registrar la cuenta");
        }
    }

    private void showSuccessAndReset() {
        Platform.runLater(() -> {
            showCustomSuccessDialog();
            clearFields();
        });
    }

    private void showCustomSuccessDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Operación exitosa");
        dialog.getDialogPane().getButtonTypes().add(new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE));

        VBox content = createSuccessDialogContent();
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setStyle(SUCCESS_DIALOG_STYLE);

        dialog.showAndWait();
    }

    private VBox createSuccessDialogContent() {
        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));

        try {
            ImageView icon = new ImageView(new Image(Objects.requireNonNull(
                    getClass().getResourceAsStream("/images/exito.png"))));
            icon.setFitHeight(50);
            icon.setFitWidth(50);
            content.getChildren().add(icon);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not load success icon", e);
        }

        Label message = new Label("¡Estudiante registrado correctamente!");
        message.setStyle(MESSAGE_STYLE);
        content.getChildren().add(message);

        return content;
    }

    private void handleCancel() {
        clearFields();
        view.getView().getScene().getWindow().hide();
    }

    private void clearFields() {
        view.getNameField().clear();
        view.getPhoneField().clear();
        view.getPhoneExtensionField().clear();
        view.getEnrollmentField().clear();
        view.getEmailField().clear();
        view.getPasswordField().clear();
        clearError();
    }

    private void resetFieldStyles() {
        view.getNameField().setStyle("");
        view.getPhoneField().setStyle("");
        view.getPhoneExtensionField().setStyle("");
        view.getEnrollmentField().setStyle("");
        view.getEmailField().setStyle("");
    }

    private void showFieldError(String message, TextField field) {
        showError(message);
        highlightField(field);
    }

    private void highlightField(TextField field) {
        field.setStyle(ERROR_STYLE);
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

    private record StudentRegistrationData(
            String name,
            String phone,
            String phoneExtension,
            String enrollment,
            String email,
            String passwordHashed
    ) {}
}
