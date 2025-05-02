package userinterface.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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

public class ControllerUpdateLinkedOrganizationWindow implements EventHandler<ActionEvent> {
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

        view.setOrganizationData(org);
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        view.getUpdateButton().setOnAction(this);
        view.getCancelButton().setOnAction(event -> currentStage.close());
    }

    @Override
    public void handle(ActionEvent event) {
        if (event.getSource() == view.getUpdateButton()) {
            handleUpdateOrganization();
        }
    }

    private void handleUpdateOrganization() {
        try {
            if (!validateFields()) {
                return;
            }

            LinkedOrganization updatedOrg = view.getUpdatedOrganization(originalOrg.getIdLinkedOrganization());

            // Verificar unicidad de teléfono y email (excluyendo el registro actual)
            verifyDataUniqueness(updatedOrg);

            if (organizationDAO.updateLinkedOrganization(updatedOrg)) {
                showAlert(Alert.AlertType.INFORMATION, "Éxito",
                        "Organización actualizada correctamente");
                refreshCallback.accept(null);
                currentStage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error",
                        "No se pudo actualizar la organización");
            }
        } catch (RepeatedCellPhoneException e) {
            view.getResultLabel().setText("El teléfono ya está registrado en otra organización");
            view.getPhoneField().setStyle("-fx-border-color: #ff0000;");
        } catch (RepeatedEmailException e) {
            view.getResultLabel().setText("El email ya está registrado en otra organización");
            view.getEmailField().setStyle("-fx-border-color: #ff0000;");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error de base de datos",
                    "Ocurrió un error al actualizar: " + e.getMessage());
        }
    }

    private boolean validateFields() {
        boolean isValid = true;
        resetFieldStyles();
        view.getResultLabel().setText("");

        if (view.getNameField().getText().isEmpty()) {
            view.getResultLabel().setText("Nombre es obligatorio");
            view.getNameField().setStyle("-fx-border-color: #ff0000;");
            isValid = false;
        }

        if (!validators.validateCellPhone(view.getPhoneField().getText())) {
            view.getResultLabel().setText("Teléfono debe tener 10 dígitos");
            view.getPhoneField().setStyle("-fx-border-color: #ff0000;");
            isValid = false;
        }

        if (!validators.validateEmail(view.getEmailField().getText())) {
            view.getResultLabel().setText("Formato de email inválido");
            view.getEmailField().setStyle("-fx-border-color: #ff0000;");
            isValid = false;
        }

        return isValid;
    }

    private void verifyDataUniqueness(LinkedOrganization org)
            throws SQLException, RepeatedCellPhoneException, RepeatedEmailException {

        // Verificar si el teléfono ya existe en otra organización
        if (organizationDAO.phoneNumberExists(org.getCellPhoneLinkedOrganization()) &&
                !org.getCellPhoneLinkedOrganization().equals(originalOrg.getCellPhoneLinkedOrganization())) {
            throw new RepeatedCellPhoneException("Teléfono duplicado");
        }

        // Verificar si el email ya existe en otra organización
        if (organizationDAO.emailExists(org.getEmailLinkedOrganization()) &&
                !org.getEmailLinkedOrganization().equals(originalOrg.getEmailLinkedOrganization())) {
            throw new RepeatedEmailException("Email duplicado");
        }
    }

    private void resetFieldStyles() {
        view.getNameField().setStyle("");
        view.getPhoneField().setStyle("");
        view.getEmailField().setStyle("");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}