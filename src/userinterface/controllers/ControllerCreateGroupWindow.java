package userinterface.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import logic.daos.GroupDAO;
import logic.logicclasses.Group;
import userinterface.windows.CreateGroupWindow;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ControllerCreateGroupWindow implements EventHandler<ActionEvent> {
    private static final Logger LOGGER = Logger.getLogger(ControllerCreateGroupWindow.class.getName());
    private static final String ERROR_STYLE = "-fx-border-color: #ff0000; -fx-border-width: 1px;";
    private static final String SUCCESS_DIALOG_STYLE = "-fx-background-color: #f8f8f8; -fx-border-color: #4CAF50; -fx-border-width: 2px;";
    private static final String ERROR_TEXT_STYLE = "-fx-text-fill: #cc0000;";
    private static final String MESSAGE_STYLE = "-fx-font-size: 14px; -fx-font-weight: bold;";

    private final CreateGroupWindow view;
    private final GroupDAO groupDAO;

    public ControllerCreateGroupWindow(CreateGroupWindow view) {
        this.view = Objects.requireNonNull(view, "CreateGroupWindow view cannot be null");
        this.groupDAO = new GroupDAO();
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        view.getAddButton().setOnAction(this);
        view.getCancelButton().setOnAction(event -> handleCancel());
    }

    @Override
    public void handle(ActionEvent event) {
        if (event.getSource() == view.getAddButton()) {
            handleAddGroup();
        }
    }

    private void handleAddGroup() {
        clearError();
        resetFieldStyles();

        String nrcText = view.getNrcField().getText().trim();
        String groupName = view.getGroupNameField().getText().trim();

        boolean isValid = true;

        if (nrcText.isEmpty()) {
            showFieldError("NRC es obligatorio", view.getNrcField());
            isValid = false;
        } else if (!nrcText.matches("\\d{5}")) {
            showFieldError("NRC debe ser un número de 5 dígitos", view.getNrcField());
            isValid = false;
        }

        if (groupName.isEmpty()) {
            showFieldError("Nombre del grupo es obligatorio", view.getGroupNameField());
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        int nrc = Integer.parseInt(nrcText);

        try {
            if (groupDAO.groupExists(nrc)) {
                showFieldError("Ya existe un grupo con ese NRC", view.getNrcField());
                return;
            }

            Group group = new Group(nrc, groupName, null, null);
            if (groupDAO.addGroup(group)) {
                showSuccessAndReset();
            } else {
                showError("No se pudo registrar el grupo");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during group registration", e);
            showError("Error de base de datos: " + e.getMessage());
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

        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));

        try {
            ImageView icon = new ImageView(new Image(Objects.requireNonNull(
                    getClass().getResourceAsStream("/images/exito.png"))));
            icon.setFitHeight(50);
            icon.setFitWidth(50);
            content.getChildren().add(icon);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not load success icon", e);
        }

        Label message = new Label("¡Grupo registrado correctamente!");
        message.setStyle(MESSAGE_STYLE);
        content.getChildren().add(message);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setStyle(SUCCESS_DIALOG_STYLE);

        dialog.showAndWait();
    }

    private void handleCancel() {
        clearFields();
        view.getView().getScene().getWindow().hide();
    }

    private void clearFields() {
        view.getNrcField().clear();
        view.getGroupNameField().clear();
        clearError();
    }

    private void resetFieldStyles() {
        view.getNrcField().setStyle("");
        view.getGroupNameField().setStyle("");
    }

    private void showFieldError(String message, TextField field) {
        showError(message);
        highlightField(field);
    }

    private void highlightField(TextField field) {
        field.setStyle(ERROR_STYLE);
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
}
