package userinterface.controllers;

import dataaccess.PasswordUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
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
import java.util.Objects;

public class ControllerCreateAcademicWindow implements EventHandler<ActionEvent> {
    private static final String ERROR_STYLE = "-fx-border-color: #ff0000; -fx-border-width: 1px;";
    private static final String ERROR_TEXT_STYLE = "-fx-text-fill: #cc0000;";
    private static final String SUCCESS_DIALOG_STYLE = "-fx-background-color: #f8f8f8; -fx-border-color: #4CAF50; -fx-border-width: 2px;";
    private static final String SUCCESS_MESSAGE_STYLE = "-fx-font-size: 14px; -fx-font-weight: bold;";
    private static final String SUCCESS_ICON_PATH = "/images/exito.png";

    private final CreateAcademicWindow view;
    private final AcademicDAO academicDAO;
    private final UserDAO userDAO;
    private final AccountDAO accountDAO;
    private final Validators validators;

    public ControllerCreateAcademicWindow(CreateAcademicWindow view) {
        this.view = Objects.requireNonNull(view, "CreateAcademicWindow no puede ser nulo");
        this.academicDAO = new AcademicDAO();
        this.userDAO = new UserDAO();
        this.accountDAO = new AccountDAO();
        this.validators = new Validators();
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        view.getAddButton().setOnAction(this);
        view.getCancelButton().setOnAction(this::handleCancel);
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

            AcademicData academicData = collectAcademicData();
            if (!verifyDataUniqueness(academicData.phone(), academicData.staffNumber(), academicData.email())) {
                return;
            }

            User user = createAndSaveUser(academicData.name(), academicData.phone());
            createAndSaveAcademic(user, academicData.staffNumber(), academicData.type());
            createAndSaveAccount(user, academicData.email(), PasswordUtils.hashPassword(academicData.password()));

            showSuccessAndReset();

        } catch (RepeatedCellPhoneException e) {
            showFieldError("El número de teléfono ya está registrado", view.getPhoneField());
        } catch (RepeatedStaffNumberException e) {
            showFieldError("El número de personal ya está registrado", view.getStaffNumberField());
        } catch (RepeatedEmailException e) {
            showFieldError("El email ya está registrado", view.getEmailField());
        } catch (SQLException e) {
            showError("Error de base de datos: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            showError("Tipo de académico inválido");
        }
    }

    private AcademicData collectAcademicData() {
        return new AcademicData(
                view.getNameField().getText().trim(),
                view.getPhoneField().getText().trim(),
                view.getStaffNumberField().getText().trim(),
                view.getEmailField().getText().trim(),
                view.getPassword(),
                AcademicType.valueOf(view.getTypeComboBox().getValue())
        );
    }

    private void handleCancel(ActionEvent event) {
        clearFields();
        view.getView().getScene().getWindow().hide();
    }

    private boolean validateAllFields() {
        boolean isValid = true;
        resetFieldStyles();

        isValid &= validateField(view.getNameField().getText().trim().isEmpty(),
                "Nombre completo es obligatorio", view.getNameField());

        isValid &= validateField(!validators.validateCellPhone(view.getPhoneField().getText()),
                "Teléfono debe tener 10 dígitos", view.getPhoneField());

        isValid &= validateField(!validators.validateStaffNumber(view.getStaffNumberField().getText()),
                "Número de personal debe tener 5 dígitos", view.getStaffNumberField());

        isValid &= validateField(!validators.validateEmail(view.getEmailField().getText()),
                "Formato de email inválido", view.getEmailField());

        isValid &= validateField(!validators.validatePassword(view.getPassword()),
                "Contraseña debe tener al menos 8 caracteres", null);

        String extension = view.getPhoneExtensionField().getText().trim();
        if (!extension.isEmpty() && !validators.validatePhoneExtension(extension)) {
            isValid = false;
            showFieldError("Extensión debe ser numérica y máximo 5 dígitos", view.getPhoneExtensionField());
        }

        return isValid;
    }

    private boolean validateField(boolean condition, String errorMessage, TextField field) {
        if (condition) {
            showError(errorMessage);
            if (field != null) {
                highlightField(field);
            }
            return false;
        }
        return true;
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
        User user = new User(0, name, phone, view.getPhoneExtensionField().getText().trim(), 'A');
        if (!userDAO.addUser(user)) {
            throw new SQLException("No se pudo registrar el usuario");
        }
        return user;
    }

    private void createAndSaveAcademic(User user, String staffNumber, AcademicType type) throws SQLException {
        Academic academic = new Academic(
                user.getIdUser(),
                user.getFullName(),
                user.getCellPhone(),
                user.getPhoneExtension(),
                'A',
                staffNumber,
                type
        );

        if (!academicDAO.addAcademic(academic)) {
            userDAO.deleteUser(user.getIdUser());
            throw new SQLException("No se pudo registrar el académico");
        }
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
        dialog.getDialogPane().getButtonTypes().add(new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE));
        dialog.getDialogPane().setContent(createSuccessContent());
        dialog.getDialogPane().setStyle(SUCCESS_DIALOG_STYLE);
        dialog.showAndWait().ifPresent(response -> clearFields());
    }

    private VBox createSuccessContent() {
        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));

        try {
            ImageView icon = new ImageView(new Image(Objects.requireNonNull(
                    getClass().getResourceAsStream(SUCCESS_ICON_PATH))));
            icon.setFitHeight(50);
            icon.setFitWidth(50);
            content.getChildren().add(icon);
        } catch (Exception e) {
            System.err.println("No se pudo cargar el icono: " + e.getMessage());
        }

        Label message = new Label("¡Académico registrado correctamente!");
        message.setStyle(SUCCESS_MESSAGE_STYLE);
        content.getChildren().add(message);

        return content;
    }

    private void resetFieldStyles() {
        view.getNameField().setStyle("");
        view.getPhoneField().setStyle("");
        view.getStaffNumberField().setStyle("");
        view.getEmailField().setStyle("");
    }

    private void highlightField(TextField field) {
        field.setStyle(ERROR_STYLE);
    }

    private void showFieldError(String message, TextField field) {
        showError(message);
        highlightField(field);
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

    private void clearFields() {
        view.getNameField().clear();
        view.getPhoneField().clear();
        view.getStaffNumberField().clear();
        view.getEmailField().clear();
        view.getPasswordToggle().clear();
        view.getTypeComboBox().setValue("Evaluador");
        view.getPhoneExtensionField().clear();
        clearError();
    }

    private record AcademicData(
            String name,
            String phone,
            String staffNumber,
            String email,
            String password,
            AcademicType type
    ) {}
}
