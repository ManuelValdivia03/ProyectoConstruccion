package userinterface.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import logic.daos.AccountDAO;
import logic.daos.StudentDAO;
import logic.daos.UserDAO;
import logic.exceptions.RepeatedCellPhoneException;
import logic.exceptions.RepeatedEmailException;
import logic.logicclasses.Student;
import logic.logicclasses.Account;
import logic.logicclasses.User;
import userinterface.utilities.Validators;
import userinterface.windows.UpdateStudentWindow;

import java.sql.SQLException;

public class ControllerUpdateStudentWindow implements EventHandler<ActionEvent> {
    private final UpdateStudentWindow view;
    private final StudentDAO studentDAO;
    private final UserDAO userDAO;
    private final AccountDAO accountDAO;
    private final Student originalStudent;
    private final Stage currentStage;
    private final Runnable refreshCallback;
    private String originalEmail;

    public ControllerUpdateStudentWindow(UpdateStudentWindow view, Student student,
                                         Stage stage, Runnable callback) {
        this.view = view;
        this.studentDAO = new StudentDAO();
        this.userDAO = new UserDAO();
        this.accountDAO = new AccountDAO();
        this.originalStudent = student;
        this.currentStage = stage;
        this.refreshCallback = callback;

        try {
            this.originalEmail = accountDAO.getAccountByUserId(student.getIdUser()).getEmail();
            view.loadStudentData(student, originalEmail);
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
            handleUpdateStudent();
        }
    }

    private void handleUpdateStudent() {
        try {
            clearError();

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
            showError("El número de teléfono ya está registrado");
        } catch (RepeatedEmailException e) {
            showError("El email ya está registrado");
        } catch (SQLException e) {
            showError("Error de base de datos: " + e.getMessage());
        }
    }

    private boolean validateAllFields() {
        boolean isValid = true;
        resetFieldStyles();
        Validators validators = new Validators();

        if (view.getNameField().getText().trim().isEmpty()) {
            showError("Nombre completo es obligatorio");
            isValid = false;
        }

        if (!validators.validateCellPhone(view.getPhoneField().getText())) {
            showError("Teléfono debe tener 10 dígitos");
            isValid = false;
        }

        if (!validators.validateEmail(view.getEmailField().getText())) {
            showError("Formato de email inválido");
            isValid = false;
        }

        return isValid;
    }

    private void verifyDataUniqueness(String phone, String email)
            throws SQLException, RepeatedCellPhoneException, RepeatedEmailException {
        if (!phone.equals(originalStudent.getCellPhone())) {
            if (userDAO.cellPhoneExists(phone)) {
                throw new RepeatedCellPhoneException();
            }
        }

        if (!email.equals(originalEmail)) {
            if (accountDAO.accountExists(email)) {
                throw new RepeatedEmailException();
            }
        }
    }

    private void updateUser(String name, String phone) throws SQLException {
        User user = new User(
                originalStudent.getIdUser(),
                name,
                phone,
                originalStudent.getStatus()
        );

        if (!userDAO.updateUser(user)) {
            throw new SQLException("No se pudo actualizar el usuario");
        }
    }

    private void updateStudent() throws SQLException {
        Student student = new Student(
                originalStudent.getIdUser(),
                view.getNameField().getText().trim(),
                view.getPhoneField().getText().trim(),
                originalStudent.getStatus(),
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
        view.getResultLabel().setText("Estudiante actualizado correctamente");
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

    private void resetFieldStyles() {
        view.getNameField().setStyle("");
        view.getPhoneField().setStyle("");
        view.getEmailField().setStyle("");
    }

    private void showError(String message) {
        view.getResultLabel().setText(message);
        view.getResultLabel().setStyle("-fx-text-fill: #cc0000;");
    }

    private void clearError() {
        view.getResultLabel().setText("");
    }
}