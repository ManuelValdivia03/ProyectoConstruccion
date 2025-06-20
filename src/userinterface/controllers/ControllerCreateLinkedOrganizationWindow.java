package userinterface.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import logic.daos.LinkedOrganizationDAO;
import logic.exceptions.RepeatedNameLinkedOrganizationException;
import logic.exceptions.RepeatedEmailException;
import logic.exceptions.RepeatedCellPhoneException;
import logic.logicclasses.LinkedOrganization;
import userinterface.utilities.Validators;
import userinterface.windows.CreateLinkedOrganizationWindow;
import userinterface.windows.DocumentUploadWindow;

import java.sql.SQLException;
import java.util.Objects;

public class ControllerCreateLinkedOrganizationWindow implements EventHandler<ActionEvent> {
    private static final String ERROR_STYLE = "-fx-border-color: #ff0000; -fx-border-width: 1px;";
    private static final String ERROR_TEXT_STYLE = "-fx-text-fill: #cc0000;";
    private static final String SUCCESS_DIALOG_STYLE = "-fx-background-color: #f8f8f8; -fx-border-color: #4CAF50; -fx-border-width: 2px;";
    private static final String SUCCESS_MESSAGE_STYLE = "-fx-font-size: 14px; -fx-font-weight: bold;";
    private static final String SUCCESS_ICON_PATH = "/images/exito.png";
    private static final int DOCUMENT_WINDOW_WIDTH = 500;
    private static final int DOCUMENT_WINDOW_HEIGHT = 350;

    private final CreateLinkedOrganizationWindow view;
    private final LinkedOrganizationDAO linkedOrganizationDAO;
    private final Validators validators;
    private LinkedOrganization organization;

    public ControllerCreateLinkedOrganizationWindow(CreateLinkedOrganizationWindow view) {
        this.view = Objects.requireNonNull(view, "CreateLinkedOrganizationWindow no puede ser nulo");
        this.linkedOrganizationDAO = new LinkedOrganizationDAO();
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

            OrganizationData data = collectOrganizationData();
            verifyDataUniqueness(data.name(), data.phone(), data.email());

            organization = createOrganization(data);
            if (linkedOrganizationDAO.addLinkedOrganization(organization)) {
                showSuccessAndReset();
            } else {
                showError("No se pudo registrar la organización");
            }

        } catch (RepeatedNameLinkedOrganizationException e) {
            showFieldError(e.getMessage(), view.getNameField());
        } catch (RepeatedCellPhoneException e) {
            showFieldError(e.getMessage(), view.getPhoneField());
        } catch (RepeatedEmailException e) {
            showFieldError(e.getMessage(), view.getEmailField());
        } catch (SQLException e) {
            showError("Error de base de datos: " + e.getMessage());
        }
    }

    private OrganizationData collectOrganizationData() {
        return new OrganizationData(
                view.getNameField().getText().trim(),
                view.getPhoneField().getText().trim(),
                view.getPhoneExtensionField().getText().trim(),
                view.getDepartmentField().getText().trim(),
                view.getEmailField().getText().trim()
        );
    }

    private boolean validateAllFields() {
        boolean isValid = true;

        isValid &= validateField(view.getNameField().getText().isEmpty(),
                "Nombre de la organización es obligatorio", view.getNameField());

        isValid &= validateField(!validators.validateCellPhone(view.getPhoneField().getText()),
                "Teléfono debe tener 10 dígitos", view.getPhoneField());

        isValid &= validateField(!validators.validateEmail(view.getEmailField().getText()),
                "Formato de email inválido", view.getEmailField());

        String extension = view.getPhoneExtensionField().getText().trim();
        if (!extension.isEmpty() && !validators.validatePhoneExtension(extension)) {
            showFieldError("Extensión debe ser numérica y máximo 5 dígitos", view.getPhoneExtensionField());
            isValid = false;
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

    private LinkedOrganization createOrganization(OrganizationData data) {
        LinkedOrganization organization = new LinkedOrganization();
        organization.setNameLinkedOrganization(data.name());
        organization.setCellPhoneLinkedOrganization(data.phone());
        organization.setPhoneExtension(data.phoneExtension());
        organization.setDepartment(data.department());
        organization.setEmailLinkedOrganization(data.email());
        organization.setStatus('A');
        return organization;
    }

    private void showSuccessAndReset() {
        Platform.runLater(() -> openDocumentUploadWindow(organization.getIdLinkedOrganization()));
    }

    private void showCustomSuccessDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Operación exitosa");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getButtonTypes().add(new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE));
        dialog.getDialogPane().setContent(createSuccessContent());
        dialog.getDialogPane().setStyle(SUCCESS_DIALOG_STYLE);
        dialog.showAndWait();
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

        Label message = new Label("¡Organización registrada correctamente!");
        message.setStyle(SUCCESS_MESSAGE_STYLE);
        content.getChildren().add(message);

        return content;
    }

    private void handleCancel(ActionEvent event) {
        clearFields();
        view.getView().getScene().getWindow().hide();
    }

    private void clearFields() {
        view.getNameField().clear();
        view.getPhoneField().clear();
        view.getEmailField().clear();
        view.getPhoneExtensionField().clear();
        view.getDepartmentField().clear();
        clearError();
    }

    private void resetFieldStyles() {
        view.getNameField().setStyle("");
        view.getPhoneField().setStyle("");
        view.getEmailField().setStyle("");
        view.getPhoneExtensionField().setStyle("");
        view.getDepartmentField().setStyle("");
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

    private void openDocumentUploadWindow(int organizationId) {
        try {
            DocumentUploadWindow uploadWindow = new DocumentUploadWindow(organizationId);
            Stage uploadStage = new Stage();

            uploadStage.initModality(Modality.APPLICATION_MODAL);
            uploadStage.setTitle("Subir Documento Justificativo (Obligatorio)");

            new ControllerDocumentUploadWindow(
                    uploadWindow,
                    uploadStage,
                    this::handleUploadSuccess,
                    this::handleUploadCancel
            );

            uploadStage.setScene(new Scene(uploadWindow.getView(), DOCUMENT_WINDOW_WIDTH, DOCUMENT_WINDOW_HEIGHT));
            uploadStage.showAndWait();

        } catch (Exception e) {
            showError("Error al abrir ventana de documentos");
            handleUploadCancel();
        }
    }

    private void handleUploadSuccess() {
        showCustomSuccessDialog();
        clearFields();
    }

    private void handleUploadCancel() {
        try {
            if (organization != null) {
                linkedOrganizationDAO.deleteLinkedOrganization(organization);
                showAlert(Alert.AlertType.INFORMATION, "Registro cancelado",
                        "El registro fue cancelado porque no se subió el documento.");
            }
        } catch (SQLException e) {
            showError("Error al cancelar el registro");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    private record OrganizationData(
            String name,
            String phone,
            String phoneExtension,
            String department,
            String email
    ) {}
}
