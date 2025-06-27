package userinterface.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import logic.daos.LinkedOrganizationDAO;
import logic.daos.RepresentativeDAO;
import logic.logicclasses.LinkedOrganization;
import logic.logicclasses.Representative;
import logic.services.ExceptionManager;
import userinterface.utilities.Validators;
import userinterface.windows.RegistRepresentativeWindow;
import java.sql.SQLException;
import java.util.List;

public class ControllerRegistRepresentativeWindow implements EventHandler<ActionEvent> {
    private final RegistRepresentativeWindow view;
    private final RepresentativeDAO representativeDAO;
    private final LinkedOrganizationDAO organizationDAO;
    private final Validators validators;
    private Stage currentStage;
    private Representative createdRepresentative;

    public ControllerRegistRepresentativeWindow(RegistRepresentativeWindow view, Stage stage) {
        this.view = view;
        this.representativeDAO = new RepresentativeDAO();
        this.organizationDAO = new LinkedOrganizationDAO();
        this.validators = new Validators();
        this.currentStage = stage;
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        view.getRegisterButton().setOnAction(this);
        view.getCancelButton().setOnAction(e -> handleCancel());
    }

    @Override
    public void handle(ActionEvent event) {
        if (event.getSource() == view.getRegisterButton()) {
            handleRegisterRepresentative();
        }
    }

    private void handleRegisterRepresentative() {
        clearError();
        resetFieldStyles();

        if (!validateAllFields()) {
            return;
        }

        String name = view.getNameTextField().getText().trim();
        String email = view.getEmailTextField().getText().trim();
        String phone = view.getPhoneTextField().getText().trim();

        createdRepresentative = new Representative(0, name, email, phone, null);

        try {
            boolean success = representativeDAO.addRepresentativeWithoutOrganization(createdRepresentative);
            if (success) {
                showSuccessAndOpenOrganizationSelector();
            } else {
                showError("No se pudo registrar el representante");
            }
        } catch (SQLException e) {
            String message = ExceptionManager.handleException(e);
            showError(message);
        }
    }

    private boolean validateAllFields() {
        boolean isValid = true;

        if (view.getNameTextField().getText().isEmpty()) {
            showError("El nombre es obligatorio");
            highlightField(view.getNameTextField());
            isValid = false;
        }

        if (!validators.validateEmail(view.getEmailTextField().getText())) {
            showError("Formato de email inválido");
            highlightField(view.getEmailTextField());
            isValid = false;
        }

        if (!view.getPhoneTextField().getText().isEmpty() &&
                !validators.validateCellPhone(view.getPhoneTextField().getText())) {
            showError("Teléfono debe tener 10 dígitos");
            highlightField(view.getPhoneTextField());
            isValid = false;
        }

        return isValid;
    }

    private void showSuccessAndOpenOrganizationSelector() {
        Platform.runLater(() -> {
            showCustomSuccessDialog();
            openOrganizationSelectionWindow();
        });
    }

    private void openOrganizationSelectionWindow() {
        userinterface.windows.LinkOrganizationWindow linkWindow =
                new userinterface.windows.LinkOrganizationWindow(this::handleLinkOrganization);
        Stage orgStage = new Stage();

        try {
            List<LinkedOrganization> orgs = organizationDAO.getAllLinkedOrganizations();
            linkWindow.setOrganizationData(FXCollections.observableArrayList(orgs));
        } catch (SQLException e) {
            String message = ExceptionManager.handleException(e);
            showError(message);
        }

        linkWindow.getBackButton().setOnAction(e -> orgStage.close());

        orgStage.setScene(new Scene(linkWindow.getView(), 700, 500));
        orgStage.setTitle("Vincular Representante a Organización");
        orgStage.show();
    }

    private void handleLinkOrganization(ActionEvent event) {
        LinkedOrganization org = (LinkedOrganization) ((Button) event.getSource()).getUserData();
        if (org == null && event.getSource() instanceof Button) {
            TableCell<?, ?> cell = (TableCell<?, ?>) ((Button) event.getSource()).getParent();
            org = (LinkedOrganization) cell.getTableRow().getItem();
        }
        if (org == null) return;

        try {
            createdRepresentative.setLinkedOrganization(org);
            boolean updated = representativeDAO.linkRepresentativeToOrganization(
                    createdRepresentative.getIdRepresentative(),
                    org.getIdLinkedOrganization()
            );
            if (updated) {
                showFinalSuccessDialog();
                currentStage.close();
            } else {
                showError("No se pudo vincular el representante");
            }
        } catch (SQLException e) {
            String message = ExceptionManager.handleException(e);
            showError(message);
        }
    }

    private void showCustomSuccessDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Operación exitosa");
        alert.setHeaderText(null);
        alert.setContentText("¡Representante registrado!\nAhora seleccione una organización para vincular.");
        alert.showAndWait();
    }

    private void showFinalSuccessDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Proceso completado");
        alert.setHeaderText(null);
        alert.setContentText("¡Representante registrado y vinculado exitosamente!");
        alert.showAndWait();
    }

    private void handleCancel() {
        clearFields();
        currentStage.close();
    }

    private void clearFields() {
        view.getNameTextField().clear();
        view.getEmailTextField().clear();
        view.getPhoneTextField().clear();
        clearError();
    }

    private void resetFieldStyles() {
        view.getNameTextField().setStyle("");
        view.getEmailTextField().setStyle("");
        view.getPhoneTextField().setStyle("");
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
