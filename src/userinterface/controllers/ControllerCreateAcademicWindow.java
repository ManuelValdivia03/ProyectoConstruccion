package userinterface.controllers;

import dataaccess.PasswordUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import logic.daos.AccountDAO;
import logic.daos.AcademicDAO;
import logic.daos.UserDAO;
import logic.enums.AcademicType;
import logic.exceptions.*;
import logic.logicclasses.Academic;
import logic.logicclasses.Account;
import logic.logicclasses.User;
import userinterface.utilities.Validators;
import userinterface.windows.CreateAcademicWindow;

import java.sql.SQLException;

public class ControllerCreateAcademicWindow implements EventHandler<ActionEvent> {
    private final CreateAcademicWindow view;
    private final AcademicDAO academicDAO;
    private final UserDAO userDAO;
    private final AccountDAO accountDAO;

    public ControllerCreateAcademicWindow(CreateAcademicWindow view) {
        this.view = view;
        this.academicDAO = new AcademicDAO();
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
            handleAddAcademic();
        }
    }

    private void handleAddAcademic() {
        try {
            clearError();

            if (!validateAllFields()) {
                return;
            }

            String name = view.getNameField().getText().trim();
            String phone = view.getPhoneField().getText().trim();
            String staffNumber = view.getStaffNumberField().getText().trim();
            String email = view.getEmailField().getText().trim();
            String passwordPlain = view.getPassword();
            String passwordHashed = PasswordUtils.hashPassword(passwordPlain);
            AcademicType type = AcademicType.valueOf(view.getTypeComboBox().getValue());
            if(!verifyDataUniqueness(phone, staffNumber, email)){
                return;
            }

            User user = createAndSaveUser(name, phone);
            Academic academic = createAndSaveAcademic(user, staffNumber, type);
            createAndSaveAccount(user, email, passwordHashed);

            showSuccessAndReset();

        } catch (RepeatedCellPhoneException e) {
            showError("El número de teléfono ya está registrado");
            highlightField(view.getPhoneField());
        } catch (RepeatedStaffNumberException e) {
            showError("El número de personal ya está registrado");
            highlightField(view.getStaffNumberField());
        } catch (RepeatedEmailException e) {
            showError("El email ya está registrado");
            highlightField(view.getEmailField());
        } catch (SQLException e) {
            showError("Error de base de datos: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            showError("Tipo de académico inválido");
        }
    }

    private void handleCancel() {
        clearFields();
        view.getView().getScene().getWindow().hide();
    }

    private boolean validateAllFields() {
        boolean isValid = true;
        resetFieldStyles();
        Validators validators = new Validators();

        if (view.getNameField().getText().trim().isEmpty()) {
            showError("Nombre completo es obligatorio");
            highlightField(view.getNameField());
            isValid = false;
        }

        if (!validators.validateCellPhone(view.getPhoneField().getText())) {
            showError("Teléfono debe tener 10 dígitos");
            highlightField(view.getPhoneField());
            isValid = false;
        }

        if (!validators.validateStaffNumber(view.getStaffNumberField().getText())) {
            showError("Número de personal debe tener 5 dígitos");
            highlightField(view.getStaffNumberField());
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

    private boolean verifyDataUniqueness(String phone, String staffNumber, String email)
            throws SQLException, RepeatedCellPhoneException, RepeatedStaffNumberException, RepeatedEmailException {
        if (userDAO.cellPhoneExists(phone)) {
            throw new RepeatedCellPhoneException();
        }
        if (academicDAO.academicExists(staffNumber)) {
            throw new RepeatedStaffNumberException();
        }
        if (accountDAO.accountExists(email)) {
            throw new RepeatedEmailException();
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

    private Academic createAndSaveAcademic(User user, String staffNumber, AcademicType type) throws SQLException {
        Academic academic = new Academic(
                user.getIdUser(),
                user.getFullName(),
                user.getCellPhone(),
                'A',
                staffNumber,
                type
        );

        if (!academicDAO.addAcademic(academic)) {
            userDAO.deleteUser(user.getIdUser());
            throw new SQLException("No se pudo registrar el académico");
        }
        return academic;
    }

    private void createAndSaveAccount(User user, String email, String password) throws SQLException {
        Account account = new Account(user.getIdUser(), email, password);
        if (!accountDAO.addAccount(account)) {
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

        Label message = new Label("¡Academico registrado correctamente!");
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

    private void resetFieldStyles() {
        view.getNameField().setStyle("");
        view.getPhoneField().setStyle("");
        view.getStaffNumberField().setStyle("");
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

    private void clearFields() {
        view.getNameField().clear();
        view.getPhoneField().clear();
        view.getStaffNumberField().clear();
        view.getEmailField().clear();
        view.getPasswordToggle().clear();
        view.getTypeComboBox().setValue("Evaluador");
        clearError();
    }
}