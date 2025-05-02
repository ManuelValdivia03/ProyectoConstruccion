package userinterface.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import logic.daos.LinkedOrganizationDAO;
import logic.exceptions.RepeatedNameLinkedOrganizationException;
import logic.exceptions.RepeatedEmailException;
import logic.exceptions.RepeatedCellPhoneException;
import logic.logicclasses.LinkedOrganization;
import userinterface.utilities.Validators;
import userinterface.windows.CreateLinkedOrganizationWindow;

import java.sql.SQLException;

public class ControllerCreateLinkedOrganizationWindow implements EventHandler<ActionEvent> {
    private final CreateLinkedOrganizationWindow view;
    private final LinkedOrganizationDAO linkedOrganizationDAO;
    private final Validators validators;

    public ControllerCreateLinkedOrganizationWindow(CreateLinkedOrganizationWindow view) {
        this.view = view;
        this.linkedOrganizationDAO = new LinkedOrganizationDAO();
        this.validators = new Validators();
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        view.getAddButton().setOnAction(this);
        view.getCancelButton().setOnAction(event -> handleCancel());
    }

    @Override
    public void handle(ActionEvent event) {
        if (event.getSource() == view.getAddButton()) {
            handleAddLinkedOrganization();
        }
    }

    private void handleAddLinkedOrganization() {
        try {
            clearError();
            resetFieldStyles();

            if (!validateAllFields()) {
                return;
            }

            String name = view.getNameField().getText().trim();
            String phone = view.getPhoneField().getText().trim();
            String email = view.getEmailField().getText().trim();

            verifyDataUniqueness(name, phone, email);

            LinkedOrganization organization = createOrganization(name, phone, email);

            if (linkedOrganizationDAO.addLinkedOrganization(organization)) {
                showSuccessAndReset();
            } else {
                showError("No se pudo registrar la organización");
            }

        } catch (RepeatedNameLinkedOrganizationException e) {
            showError(e.getMessage());
            highlightField(view.getNameField());
        } catch (RepeatedCellPhoneException e) {
            showError(e.getMessage());
            highlightField(view.getPhoneField());
        } catch (RepeatedEmailException e) {
            showError(e.getMessage());
            highlightField(view.getEmailField());
        } catch (SQLException e) {
            showError("Error de base de datos: " + e.getMessage());
        }
    }

    private boolean validateAllFields() {
        boolean isValid = true;

        if (view.getNameField().getText().isEmpty()) {
            showError("Nombre de la organización es obligatorio");
            highlightField(view.getNameField());
            isValid = false;
        }

        if (!validators.validateCellPhone(view.getPhoneField().getText())) {
            showError("Teléfono debe tener 10 dígitos");
            highlightField(view.getPhoneField());
            isValid = false;
        }

        if (!validators.validateEmail(view.getEmailField().getText())) {
            showError("Formato de email inválido");
            highlightField(view.getEmailField());
            isValid = false;
        }

        return isValid;
    }

    private void verifyDataUniqueness(String name, String phone, String email)
            throws SQLException, RepeatedNameLinkedOrganizationException,
            RepeatedCellPhoneException, RepeatedEmailException {

        if (linkedOrganizationDAO.linkedOrganizationExists(name)) {
            throw new RepeatedNameLinkedOrganizationException("La organización ya está registrada");
        }

        if (linkedOrganizationDAO.phoneNumberExists(phone)) {
            throw new RepeatedCellPhoneException("El número de teléfono ya está registrado");
        }

        if (linkedOrganizationDAO.emailExists(email)) {
            throw new RepeatedEmailException("El correo electrónico ya está registrado");
        }
    }

    private LinkedOrganization createOrganization(String name, String phone, String email) {
        LinkedOrganization organization = new LinkedOrganization();
        organization.setNameLinkedOrganization(name);
        organization.setCellPhoneLinkedOrganization(phone);
        organization.setEmailLinkedOrganization(email);
        organization.setStatus('A'); // Activo por defecto
        return organization;
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
        dialog.setHeaderText(null);

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

        Label message = new Label("¡Organización registrada correctamente!");
        message.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        content.getChildren().add(message);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setStyle(
                "-fx-background-color: #f8f8f8;" +
                        "-fx-border-color: #4CAF50;" +
                        "-fx-border-width: 2px;"
        );

        dialog.showAndWait();
    }

    private void handleCancel() {
        clearFields();
        view.getView().getScene().getWindow().hide();
    }

    private void clearFields() {
        view.getNameField().clear();
        view.getPhoneField().clear();
        view.getEmailField().clear();
        clearError();
    }

    private void resetFieldStyles() {
        view.getNameField().setStyle("");
        view.getPhoneField().setStyle("");
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