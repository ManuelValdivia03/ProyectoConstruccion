package userinterface.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import logic.daos.AcademicDAO;
import logic.daos.AccountDAO;
import logic.daos.UserDAO;
import logic.enums.AcademicType;
import logic.exceptions.RepeatedCellPhoneException;
import logic.exceptions.RepeatedEmailException;
import logic.exceptions.RepeatedStaffNumberException;
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
        view.getAddButton().setOnAction(this);
    }

    @Override
    public void handle(ActionEvent event) {
        if (event.getSource() == view.getAddButton()) {
            handleAddUser();
        }
    }

    private void handleAddUser() {
        try {
            showError("");

            if (!validateFields()) {
                return;
            }

            String name = view.getNameField().getText().trim();
            String phone = view.getPhoneField().getText().trim();
            String staffNumber = view.getStaffNumberField().getText().trim();
            String email = view.getEmailField().getText().trim();
            String password = view.getPassword();
            AcademicType type = AcademicType.valueOf(view.getTypeComboBox().getValue());

            if (userDAO.cellPhoneExists(phone)) {
                throw new RepeatedCellPhoneException("El teléfono ya está registrado");
            }
            if (academicDAO.academicExists(staffNumber)) {
                throw new RepeatedStaffNumberException("El número de personal ya está registrado");
            }
            if (accountDAO.accountExists(email)) {
                throw new RepeatedEmailException("El correo electrónico ya está registrado");
            }

            User user = new User(0, name, phone, 'A');
            if (!userDAO.addUser(user)) {
                showError("Error al registrar el usuario");
                return;
            }

            Academic academic = new Academic(user.getIdUser(), user.getFullName(), user.getCellPhone(), 'A', staffNumber, type);
            if (!academicDAO.addAcademic(academic)) {
                showError("Error al registrar el académico");
                return;
            }

            Account account = new Account(user.getIdUser(), email, password);
            if (!accountDAO.addAccount(account)) {
                showError("Error al registrar la cuenta");
                return;
            }

            showCustomSuccessDialog();

        } catch (RepeatedCellPhoneException | RepeatedStaffNumberException | RepeatedEmailException e) {
            showError(e.getMessage());
        } catch (SQLException e) {
            showError("Error de base de datos: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            showError("Tipo de académico inválido");
        }
    }

    private boolean validateFields() {
        boolean isValid = true;
        resetFieldStyles();

        if (view.getNameField().getText().trim().isEmpty()) {
            showError("El nombre completo es obligatorio");
            view.getNameField().setStyle("-fx-border-color: #ff0000; -fx-border-width: 1px;");
            isValid = false;
        }

        if (!Validators.validateCellPhone(view.getPhoneField().getText())) {
            showError("El teléfono debe tener exactamente 10 dígitos numéricos");
            view.getPhoneField().setStyle("-fx-border-color: #ff0000; -fx-border-width: 1px;");
            isValid = false;
        }

        if (!Validators.validateStaffNumber(view.getStaffNumberField().getText())) {
            showError("El número de personal debe tener exactamente 5 dígitos numéricos");
            view.getStaffNumberField().setStyle("-fx-border-color: #ff0000; -fx-border-width: 1px;");
            isValid = false;
        }

        if (!Validators.validateEmail(view.getEmailField().getText())) {
            showError("Formato de email inválido");
            view.getEmailField().setStyle("-fx-border-color: #ff0000; -fx-border-width: 1px;");
            isValid = false;
        }

        if (!Validators.validatePassword(view.getPassword())) {
            showError("La contraseña debe tener al menos 8 caracteres");
            isValid = false;
        }

        return isValid;
    }

    private void resetFieldStyles() {
        view.getNameField().setStyle("");
        view.getPhoneField().setStyle("");
        view.getStaffNumberField().setStyle("");
        view.getEmailField().setStyle("");
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            view.getResultLabel().setText(message);
            view.getResultLabel().setStyle("-fx-text-fill: #cc0000;");
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

        Label message = new Label("¡Académico registrado correctamente!");
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

    private void clearFields() {
        view.getNameField().clear();
        view.getPhoneField().clear();
        view.getStaffNumberField().clear();
        view.getEmailField().clear();
        view.getPasswordToggle().clear();
        view.getTypeComboBox().setValue("Evaluador");
        view.getResultLabel().setText("");
    }
}