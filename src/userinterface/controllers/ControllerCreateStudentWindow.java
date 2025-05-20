package userinterface.controllers;

import dataaccess.PasswordUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import logic.daos.AccountDAO;
import logic.daos.StudentDAO;
import logic.daos.UserDAO;
import logic.exceptions.RepeatedCellPhoneException;
import logic.exceptions.RepeatedEmailException;
import logic.exceptions.RepeatedEnrollmentException;
import logic.logicclasses.Account;
import logic.logicclasses.Student;
import logic.logicclasses.User;
import userinterface.utilities.Validators;
import userinterface.windows.CreateStudentWindow;
import java.sql.SQLException;

public class ControllerCreateStudentWindow implements EventHandler<ActionEvent> {
    private final CreateStudentWindow view;
    private final StudentDAO studentDAO;
    private final UserDAO userDAO;
    private final AccountDAO accountDAO;

    public ControllerCreateStudentWindow(CreateStudentWindow view) {
        this.view = view;
        this.studentDAO = new StudentDAO();
        this.userDAO = new UserDAO();
        this.accountDAO = new AccountDAO();
        setupEventHandlers();
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

            String name = view.getNameField().getText().trim();
            String phone = view.getPhoneField().getText().trim();
            String enrollment = view.getEnrollmentField().getText().trim();
            String email = view.getEmailField().getText().trim();
            String passwordPlain = view.getPassword();
            String passwordHashed = PasswordUtils.hashPassword(passwordPlain);

            if (!verifyDataUniqueness(phone, enrollment, email)) {
                return;
            }

            User user = createAndSaveUser(name, phone);
            Student student = createAndSaveStudent(user, enrollment);
            createAndSaveAccount(user, email, passwordHashed);

            showSuccessAndReset();

        } catch (RepeatedCellPhoneException e) {
            showError("El número de teléfono ya está registrado");
            highlightField(view.getPhoneField());
        } catch (RepeatedEnrollmentException e) {
            showError("La matrícula ya está registrada");
            highlightField(view.getEnrollmentField());
        } catch (RepeatedEmailException e) {
            showError("El email ya está registrado");
            highlightField(view.getEmailField());
        } catch (SQLException e) {
            showError("Error de base de datos: " + e.getMessage());
        }
    }

    private boolean validateAllFields() {
        boolean isValid = true;
        resetFieldStyles();
        Validators validators = new Validators();

        if (view.getNameField().getText().isEmpty()) {
            showError("Nombre completo es obligatorio");
            highlightField(view.getNameField());
            isValid = false;
        }

        if (!validators.validateCellPhone(view.getPhoneField().getText())) {
            showError("Teléfono debe tener 10 dígitos");
            highlightField(view.getPhoneField());
            isValid = false;
        }

        if (!validators.validateEnrollment(view.getEnrollmentField().getText())) {
            showError("Matrícula debe comenzar con S y tener 9 dígitos");
            highlightField(view.getEnrollmentField());
            isValid = false;
        }

        if (!validators.validateEmail(view.getEmailField().getText())) {
            showError("Formato de email inválido");
            highlightField(view.getEmailField());
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
            throw new RepeatedCellPhoneException("El número de teléfono ya está registrado");
        }
        if (studentDAO.enrollmentExists(enrollment)) {
            throw new RepeatedEnrollmentException("La matrícula ya está registrada");
        }
        if (accountDAO.accountExists(email)) {
            throw new RepeatedEmailException("El email ya está registrado");
        }
        return true;
    }

    private User createAndSaveUser(String name, String phone) throws SQLException {
        User user = new User(0, name, phone, 'A');
        if (!userDAO.addUser(user)) {
            throw new SQLException("No se pudo registrar el usuario");
        }
        return user;
    }

    private Student createAndSaveStudent(User user, String enrollment) throws SQLException {
        Student student = new Student(user.getIdUser(), user.getFullName(), user.getCellPhone(), 'A', enrollment,0);
        if (!studentDAO.addStudent(student)) {
            userDAO.deleteUser(user.getIdUser());
            throw new SQLException("No se pudo registrar el estudiante");
        }
        return student;
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

        ButtonType okButton = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(okButton);

        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));

        try {
            ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/images/exito.png")));
            icon.setFitHeight(50);
            icon.setFitWidth(50);
            content.getChildren().add(icon);
        } catch (Exception e) {
            System.err.println("No se pudo cargar el icono: " + e.getMessage());
        }

        Label message = new Label("¡Estudiante registrado correctamente!");
        message.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        content.getChildren().add(message);

        dialog.getDialogPane().setContent(content);

        dialog.getDialogPane().setStyle(
                "-fx-background-color: #f8f8f8;" +
                        "-fx-border-color: #4CAF50;" +
                        "-fx-border-width: 2px;"
        );

        dialog.showAndWait().ifPresent(response -> {
            clearFields();
        });
    }

    private void handleCancel() {
        clearFields();
        view.getView().getScene().getWindow().hide();
    }

    private void clearFields() {
        view.getNameField().clear();
        view.getPhoneField().clear();
        view.getEnrollmentField().clear();
        view.getEmailField().clear();
        view.getPasswordField().clear();
        clearError();
    }

    private void resetFieldStyles() {
        view.getNameField().setStyle("");
        view.getPhoneField().setStyle("");
        view.getEnrollmentField().setStyle("");
        view.getEmailField().setStyle("");
    }

    private void highlightField(TextField field) {
        field.setStyle("-fx-border-color: #ff0000; -fx-border-width: 1px;");
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            view.getResultLabel().setText(message);
            view.getResultLabel().setStyle("-fx-text-fill: #cc0000;");
        });
    }

    private void clearError() {
        view.getResultLabel().setText("");
    }
}