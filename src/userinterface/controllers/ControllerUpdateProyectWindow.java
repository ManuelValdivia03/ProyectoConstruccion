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
import userinterface.windows.UpdateProyectWindow;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ControllerUpdateProyectWindow implements EventHandler<ActionEvent> {
    private final UpdateProyectWindow view;
    private final ProyectDAO proyectDAO;
    private final Validators validators;
    private Proyect currentProyect;

    public ControllerUpdateProyectWindow(UpdateProyectWindow updateProyectWindow, Proyect proyect) {
        this.view = updateProyectWindow;
        this.proyectDAO = new ProyectDAO();
        this.validators = new Validators();
        this.currentProyect = proyect;

        initializeFields();
        setupEventHandlers();
    }

    private void initializeFields() {
        view.getIdLabel().setText(String.valueOf(currentProyect.getIdProyect()));
        view.getTitleTextField().setText(currentProyect.getTitle());
        view.getDescriptionTextField().setText(currentProyect.getDescription());

        String startDate = currentProyect.getDateStart().toLocalDateTime().toLocalDate().toString();
        String endDate = currentProyect.getDateEnd().toLocalDateTime().toLocalDate().toString();

        view.getDateStartTextField().setText(startDate);
        view.getDateEndTextField().setText(endDate);
    }

    private void setupEventHandlers() {
        view.getUpdateButton().setOnAction(this);
        view.getCancelButton().setOnAction(event -> handleCancel());
    }

    @Override
    public void handle(ActionEvent event) {
        if (event.getSource() == view.getUpdateButton()) {
            handleUpdateProyect();
        }
    }

    private void handleUpdateProyect() {
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

            if (!title.equals(currentProyect.getTitle())){
                if (proyectDAO.proyectExists(title)) {
                    throw new RepeatedProyectException("Ya existe un proyecto con ese título");
                }
            }

            currentProyect.setTitle(title);
            currentProyect.setDescription(description);
            currentProyect.setDateStart(dateStart);
            currentProyect.setDateEnd(dateEnd);
            currentProyect.setStatus(view.getStatusComboBox().getValue().charAt(0));

            if (proyectDAO.updateProyect(currentProyect)) {
                showSuccessAndClose();
            } else {
                showError("No se pudo actualizar el proyecto");
            }

        } catch (RepeatedProyectException e) {
            showError(e.getMessage());
            highlightField(view.getTitleTextField());
        } catch (DateTimeParseException e) {
            showError("Formato de fecha inválido. Use YYYY-MM-DD o DD/MM/YYYY");
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

    private void showSuccessAndClose() {
        Platform.runLater(() -> {
            showCustomSuccessDialog();
            handleCancel();
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

        Label message = new Label("¡Proyecto actualizado correctamente!");
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
        view.getView().getScene().getWindow().hide();
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