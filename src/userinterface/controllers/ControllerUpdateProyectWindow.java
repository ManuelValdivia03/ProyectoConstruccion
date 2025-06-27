package userinterface.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import logic.daos.ProjectDAO;
import logic.exceptions.RepeatedProyectException;
import logic.logicclasses.Project;
import logic.services.ExceptionManager;
import userinterface.utilities.Validators;
import userinterface.windows.UpdateProyectWindow;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

public class ControllerUpdateProyectWindow {
    private static final String ERROR_BORDER_STYLE = "-fx-border-color: #ff0000; -fx-border-width: 1px;";
    private static final String DEFAULT_BORDER_STYLE = "";
    private static final String ERROR_TEXT_STYLE = "-fx-text-fill: #cc0000;";
    private static final DateTimeFormatter[] DATE_FORMATTERS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy")
    };

    private final UpdateProyectWindow view;
    private final ProjectDAO projectDAO;
    private final Validators validators;
    private final Project currentProject;

    public ControllerUpdateProyectWindow(UpdateProyectWindow updateProyectWindow, Project project) {
        this.view = updateProyectWindow;
        this.projectDAO = new ProjectDAO();
        this.validators = new Validators();
        this.currentProject = project;

        initializeFields();
        setupEventHandlers();
    }

    private void initializeFields() {
        view.getIdLabel().setText(String.valueOf(currentProject.getIdProyect()));
        view.getTitleTextField().setText(currentProject.getTitle());
        view.getDescriptionTextField().setText(currentProject.getDescription());
        view.getMaxStudentsTextField().setText(String.valueOf(currentProject.getCapacity()));

        LocalDate startDate = currentProject.getDateStart().toLocalDateTime().toLocalDate();
        LocalDate endDate = currentProject.getDateEnd().toLocalDateTime().toLocalDate();

        view.getDateStartTextField().setText(startDate.toString());
        view.getDateEndTextField().setText(endDate.toString());
    }

    private void setupEventHandlers() {
        view.getUpdateButton().setOnAction(this::handleUpdateProyect);
        view.getCancelButton().setOnAction(this::handleCancel);
    }

    private void handleUpdateProyect(ActionEvent event) {
        try {
            clearError();
            resetFieldStyles();

            if (!validateAllFields()) {
                return;
            }

            Project updatedProject = createUpdatedProyect();
            validateProyectTitleUniqueness(updatedProject);

            if (projectDAO.updateProyect(updatedProject)) {
                showSuccessAndClose();
            } else {
                showError("No se pudo actualizar el proyecto");
            }

        } catch (RepeatedProyectException e) {
            showFieldError(e.getMessage(), view.getTitleTextField());
        } catch (DateTimeParseException e) {
            showError("Formato de fecha inválido. Use YYYY-MM-DD o DD/MM/YYYY");
        } catch (SQLException e) {
            String message = ExceptionManager.handleException(e);
            showError(message);
        } catch (Exception e) {
            String message = ExceptionManager.handleException(e);
            showError(message);
        }
    }

    private Project createUpdatedProyect() throws DateTimeParseException {
        int maxStudents;
        try {
            maxStudents = Integer.parseInt(view.getMaxStudentsTextField().getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El cupo máximo debe ser un número válido");
        }

        return new Project(
                currentProject.getIdProyect(),
                view.getTitleTextField().getText().trim(),
                view.getDescriptionTextField().getText().trim(),
                parseDateOnly(view.getDateStartTextField().getText().trim()),
                parseDateOnly(view.getDateEndTextField().getText().trim()),
                view.getStatusComboBox().getValue().charAt(0),
                maxStudents,
                currentProject.getCurrentStudents()
        );
    }

    private void validateProyectTitleUniqueness(Project updatedProject) throws SQLException {
        if (!updatedProject.getTitle().equals(currentProject.getTitle())) {
            if (projectDAO.proyectExists(updatedProject.getTitle())) {
                throw new RepeatedProyectException("Ya existe un proyecto con ese título");
            }
        }
    }

    private boolean validateAllFields() {
        boolean isValid = true;

        isValid &= validateField(view.getTitleTextField(),
                !view.getTitleTextField().getText().isEmpty(),
                "El título es obligatorio");

        isValid &= validateField(view.getDescriptionTextField(),
                !view.getDescriptionTextField().getText().isEmpty(),
                "La descripción es obligatoria");

        try {
            int maxStudents = Integer.parseInt(view.getMaxStudentsTextField().getText().trim());
            if (maxStudents <= 0) {
                showFieldError("El cupo máximo debe ser mayor a 0", view.getMaxStudentsTextField());
                isValid = false;
            }
            if (maxStudents < currentProject.getCurrentStudents()) {
                showFieldError("El cupo máximo no puede ser menor que el número actual de estudiantes", 
                    view.getMaxStudentsTextField());
                isValid = false;
            }
        } catch (NumberFormatException e) {
            showFieldError("El cupo máximo debe ser un número válido", view.getMaxStudentsTextField());
            isValid = false;
        }

        isValid &= validateDateField(view.getDateStartTextField(),
                "Fecha de inicio inválida. Formatos válidos: YYYY-MM-DD, DD/MM/YYYY");

        isValid &= validateDateField(view.getDateEndTextField(),
                "Fecha de fin inválida. Formatos válidos: YYYY-MM-DD, DD/MM/YYYY");

        if (isValid) {
            try {
                Timestamp startDate = parseDateOnly(view.getDateStartTextField().getText());
                Timestamp endDate = parseDateOnly(view.getDateEndTextField().getText());
                isValid = validateDateOrder(startDate, endDate);
            } catch (DateTimeParseException e) {
                isValid = false;
            }
        }

        return isValid;
    }

    private boolean validateField(TextField field, boolean isValid, String errorMessage) {
        if (!isValid) {
            showFieldError(errorMessage, field);
            return false;
        }
        return true;
    }

    private boolean validateDateField(TextField field, String errorMessage) {
        try {
            parseDateOnly(field.getText());
            return true;
        } catch (DateTimeParseException e) {
            showFieldError(errorMessage, field);
            return false;
        }
    }

    private boolean validateDateOrder(Timestamp start, Timestamp end) {
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

        String finalDateString = dateString;
        return Arrays.stream(DATE_FORMATTERS)
                .filter(formatter -> {
                    try {
                        LocalDate.parse(finalDateString, formatter);
                        return true;
                    } catch (DateTimeParseException e) {
                        return false;
                    }
                })
                .findFirst()
                .map(formatter -> Timestamp.valueOf(LocalDate.parse(finalDateString, formatter).atStartOfDay()))
                .orElseThrow(() -> new DateTimeParseException(
                        "Formato no válido. Use YYYY-MM-DD o DD/MM/YYYY", finalDateString, 0));
    }

    private void showSuccessAndClose() {
        Platform.runLater(() -> {
            showCustomSuccessDialog();
            handleCancel(null);
        });
    }

    private void showCustomSuccessDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Operación exitosa");
        alert.setHeaderText(null);
        alert.setContentText("¡Proyecto actualizado correctamente!");

        try {
            ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/images/exito.png")));
            icon.setFitHeight(50);
            icon.setFitWidth(50);
            alert.setGraphic(icon);
        } catch (Exception e) {
            System.err.println("No se pudo cargar el icono: " + e.getMessage());
        }

        alert.showAndWait();
    }

    private void handleCancel(ActionEvent event) {
        view.getView().getScene().getWindow().hide();
    }

    private void resetFieldStyles() {
        view.getTitleTextField().setStyle(DEFAULT_BORDER_STYLE);
        view.getDescriptionTextField().setStyle(DEFAULT_BORDER_STYLE);
        view.getDateStartTextField().setStyle(DEFAULT_BORDER_STYLE);
        view.getDateEndTextField().setStyle(DEFAULT_BORDER_STYLE);
        view.getMaxStudentsTextField().setStyle(DEFAULT_BORDER_STYLE);
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

    private void highlightField(TextField field) {
        field.setStyle(ERROR_BORDER_STYLE);
    }

    private void clearError() {
        view.getResultLabel().setText("");
    }
}
