package userinterface.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import logic.daos.ProyectDAO;
import logic.exceptions.RepeatedProyectException;
import logic.logicclasses.Proyect;
import userinterface.utilities.Validators;
import userinterface.windows.RegistProyectWindow;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ControllerRegistProyectWindow implements EventHandler<ActionEvent> {
    private final RegistProyectWindow view;
    private final ProyectDAO proyectDAO;
    private final Validators validators;

    public ControllerRegistProyectWindow(RegistProyectWindow registProyectWindow) {
        this.view = registProyectWindow;
        this.proyectDAO = new ProyectDAO();
        this.validators = new Validators();
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        view.getRegisterButton().setOnAction(this);
        view.getCancelButton().setOnAction(event -> handleCancel());
    }

    @Override
    public void handle(ActionEvent event) {
        if (event.getSource() == view.getRegisterButton()) {
            handleRegisterProyect();
        }
    }

    private void handleRegisterProyect() {
        try {
            clearError();
            resetFieldStyles();

            if (!validateAllFields()) {
                return;
            }

            String title = view.getTitleTextField().getText().trim();
            String description = view.getDescriptionTextField().getText().trim();
            Timestamp dateStart = parseDateOnly(view.getDateStartTextField().getText().trim());
            Timestamp dateEnd = parseDateOnly(view.getDateEndTextField().getText().trim());

            if (!validateDates(dateStart, dateEnd)) {
                return;
            }

            if (proyectDAO.proyectExists(title)) {
                throw new RepeatedProyectException("Ya existe un proyecto con ese título");
            }

            Proyect proyect = new Proyect(0, title, description, dateStart, dateEnd, 'A');

            if (proyectDAO.addProyect(proyect)) {
                showSuccessAndReset();
            } else {
                showError("No se pudo registrar el proyecto");
            }

        } catch (RepeatedProyectException e) {
            showError(e.getMessage());
            highlightField(view.getTitleTextField());
        } catch (DateTimeParseException e) {
            showError("Formato de fecha inválido. Use YYYY-MM-DD HH:MM:SS");
        } catch (SQLException e) {
            showError("Error de base de datos: " + e.getMessage());
        } catch (Exception e) {
            showError("Error inesperado: " + e.getMessage());
        }
    }

    private boolean validateAllFields() {
        boolean isValid = true;

        if (view.getTitleTextField().getText().isEmpty()) {
            showError("El título es obligatorio");
            highlightField(view.getTitleTextField());
            isValid = false;
        }

        if (view.getDescriptionTextField().getText().isEmpty()) {
            showError("La descripción es obligatoria");
            highlightField(view.getDescriptionTextField());
            isValid = false;
        }

        try {
            parseDateOnly(view.getDateStartTextField().getText());
        } catch (DateTimeParseException e) {
            showError("Fecha de inicio inválida. Formatos válidos: YYYY-MM-DD, DD/MM/YYYY");
            highlightField(view.getDateStartTextField());
            isValid = false;
        }

        try {
            parseDateOnly(view.getDateEndTextField().getText());
        } catch (DateTimeParseException e) {
            showError("Fecha de fin inválida. Formatos válidos: YYYY-MM-DD, DD/MM/YYYY");
            highlightField(view.getDateEndTextField());
            isValid = false;
        }

        return isValid;
    }

    private boolean validateDates(Timestamp start, Timestamp end) {
        if (start.after(end)) {
            showError("La fecha de inicio debe ser anterior a la fecha de fin");
            highlightField(view.getDateStartTextField());
            highlightField(view.getDateEndTextField());
            return false;
        }
        return true;
    }

    private Timestamp parseDateOnly(String dateString) throws DateTimeParseException {
        dateString = dateString.trim();
        if (dateString.isEmpty()) {
            throw new DateTimeParseException("Fecha vacía", dateString, 0);
        }

        if (dateString.contains(" ")) {
            dateString = dateString.split(" ")[0];
        }

        DateTimeFormatter[] supportedFormats = {
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy")
        };

        for (DateTimeFormatter formatter : supportedFormats) {
            try {
                LocalDate date = LocalDate.parse(dateString, formatter);
                return Timestamp.valueOf(date.atStartOfDay());
            } catch (DateTimeParseException ignored) {
                continue;
            }
        }

        throw new DateTimeParseException("Formato no válido. Use YYYY-MM-DD o DD/MM/YYYY", dateString, 0);
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

        Label message = new Label("¡Proyecto registrado correctamente!");
        message.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        content.getChildren().add(message);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setStyle(
                "-fx-background-color: #f8f8f8;" +
                        "-fx-border-color: #4CAF50;" +
                        "-fx-border-width: 2px;"
        );

        dialog.showAndWait().ifPresent(response -> {
            if (response == okButton) {
                handleCancel();
            }
        });
    }

    private void handleCancel() {
        clearFields();
        view.getView().getScene().getWindow().hide();
    }

    private void clearFields() {
        view.getTitleTextField().clear();
        view.getDescriptionTextField().clear();
        view.getDateStartTextField().clear();
        view.getDateEndTextField().clear();
        clearError();
    }

    private void resetFieldStyles() {
        view.getTitleTextField().setStyle("");
        view.getDescriptionTextField().setStyle("");
        view.getDateStartTextField().setStyle("");
        view.getDateEndTextField().setStyle("");
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