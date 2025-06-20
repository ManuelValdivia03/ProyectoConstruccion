package userinterface.controllers;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import logic.daos.LinkedOrganizationDAO;
import logic.exceptions.RepeatedEmailException;
import logic.exceptions.RepeatedCellPhoneException;
import logic.logicclasses.LinkedOrganization;
import userinterface.utilities.Validators;
import userinterface.windows.UpdateLinkedOrganizationWindow;

import java.sql.SQLException;
import java.util.function.Consumer;

public class ControllerUpdateLinkedOrganizationWindow {
    private static final String ERROR_BORDER_STYLE = "-fx-border-color: #ff0000;";
    private static final String DEFAULT_BORDER_STYLE = "";

    private final UpdateLinkedOrganizationWindow view;
    private final LinkedOrganization originalOrg;
    private final Stage currentStage;
    private final Consumer<Void> refreshCallback;
    private final LinkedOrganizationDAO organizationDAO;
    private final Validators validators;

    public ControllerUpdateLinkedOrganizationWindow(UpdateLinkedOrganizationWindow view,
                                                    LinkedOrganization org,
                                                    Stage stage,
                                                    Consumer<Void> refreshCallback) {
        this.view = view;
        this.originalOrg = org;
        this.currentStage = stage;
        this.refreshCallback = refreshCallback;
        this.organizationDAO = new LinkedOrganizationDAO();
        this.validators = new Validators();

        initializeView();
        setupEventHandlers();
    }

    private void initializeView() {
        view.setOrganizationData(originalOrg);
    }

    private void setupEventHandlers() {
        view.getUpdateButton().setOnAction(this::handleUpdateOrganization);
        view.getCancelButton().setOnAction(event -> currentStage.close());
    }

    private void handleUpdateOrganization(ActionEvent event) {
        try {
            if (!validateFields()) {
                return;
            }

            LinkedOrganization updatedOrg = createUpdatedOrganization();
            verifyDataUniqueness(updatedOrg);
            updateOrganization(updatedOrg);

        } catch (RepeatedCellPhoneException e) {
            showFieldError("El teléfono ya está registrado en otra organización", view.getPhoneField());
        } catch (RepeatedEmailException e) {
            showFieldError("El email ya está registrado en otra organización", view.getEmailField());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error de base de datos",
                    "Ocurrió un error al actualizar: " + e.getMessage());
        }
    }

    private LinkedOrganization createUpdatedOrganization() {
        return view.getUpdatedOrganization(originalOrg.getIdLinkedOrganization());
    }

    private void updateOrganization(LinkedOrganization updatedOrg) throws SQLException {
        if (organizationDAO.updateLinkedOrganization(updatedOrg)) {
            showSuccessAndClose();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo actualizar la organización");
        }
    }

    private void showSuccessAndClose() {
        showAlert(Alert.AlertType.INFORMATION, "Éxito", "Organización actualizada correctamente");
        refreshCallback.accept(null);
        currentStage.close();
    }

    private boolean validateFields() {
        boolean isValid = true;
        resetFieldStyles();
        clearErrorMessage();

        isValid &= validateField(view.getNameField(),
                !view.getNameField().getText().isEmpty(),
                "Nombre es obligatorio");

        isValid &= validateField(view.getPhoneField(),
                validators.validateCellPhone(view.getPhoneField().getText()),
                "Teléfono debe tener 10 dígitos");

        String extension = view.getPhoneExtensionField().getText().trim();
        if (!extension.isEmpty() && !validators.validatePhoneExtension(extension)) {
            showFieldError("Extensión debe ser numérica y máximo 5 dígitos", view.getPhoneExtensionField());
            isValid = false;
        }

        isValid &= validateField(view.getEmailField(),
                validators.validateEmail(view.getEmailField().getText()),
                "Formato de email inválido");

        return isValid;
    }

    private boolean validateField(javafx.scene.control.TextField field, boolean isValid, String errorMessage) {
        if (!isValid) {
            showFieldError(errorMessage, field);
            return false;
        }
        return true;
    }

    private void verifyDataUniqueness(LinkedOrganization org)
            throws SQLException, RepeatedCellPhoneException, RepeatedEmailException {

        if (isPhoneNumberChanged(org) && organizationDAO.phoneNumberExists(org.getCellPhoneLinkedOrganization())) {
            throw new RepeatedCellPhoneException("Teléfono duplicado");
        }

        if (isEmailChanged(org) && organizationDAO.emailExists(org.getEmailLinkedOrganization())) {
            throw new RepeatedEmailException("Email duplicado");
        }
    }

    private boolean isPhoneNumberChanged(LinkedOrganization org) {
        return !org.getCellPhoneLinkedOrganization().equals(originalOrg.getCellPhoneLinkedOrganization());
    }

    private boolean isEmailChanged(LinkedOrganization org) {
        return !org.getEmailLinkedOrganization().equals(originalOrg.getEmailLinkedOrganization());
    }

    private void resetFieldStyles() {
        view.getNameField().setStyle(DEFAULT_BORDER_STYLE);
        view.getPhoneField().setStyle(DEFAULT_BORDER_STYLE);
        view.getPhoneExtensionField().setStyle(DEFAULT_BORDER_STYLE);
        view.getDepartmentField().setStyle(DEFAULT_BORDER_STYLE);
        view.getEmailField().setStyle(DEFAULT_BORDER_STYLE);
    }

    private void showFieldError(String message, javafx.scene.control.TextField field) {
        view.getResultLabel().setText(message);
        field.setStyle(ERROR_BORDER_STYLE);
    }

    private void clearErrorMessage() {
        view.getResultLabel().setText("");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
