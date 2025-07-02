package userinterface.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import logic.daos.ReportDAO;
import logic.daos.StudentDAO;
import logic.enums.ReportType;
import logic.logicclasses.Report;
import logic.logicclasses.Student;
import logic.services.ExceptionManager;
import userinterface.utilities.Validators;
import userinterface.windows.RegistReportWindow;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ControllerRegistReportWindow implements EventHandler<ActionEvent> {
    private static final Logger LOGGER = Logger.getLogger(ControllerRegistReportWindow.class.getName());
    private static final String ERROR_STYLE = "-fx-border-color: #ff0000; -fx-border-width: 1px;";
    private static final String ERROR_TEXT_STYLE = "-fx-text-fill: #cc0000;";
    private static final String SUCCESS_TITLE = "Reporte registrado";
    private static final String ICON_PATH = "/images/exito.png";

    private final RegistReportWindow view;
    private final ReportDAO reportDAO;
    private final StudentDAO studentDAO;
    private final Validators validators;
    private final Stage currentStage;
    private final Student currentStudent;

    public ControllerRegistReportWindow(RegistReportWindow view, Stage stage, Student student) {
        this.view = Objects.requireNonNull(view, "ReportRegistrationWindow cannot be null");
        this.reportDAO = new ReportDAO();
        this.studentDAO = new StudentDAO();
        this.validators = new Validators();
        this.currentStage = Objects.requireNonNull(stage, "Stage cannot be null");
        this.currentStudent = Objects.requireNonNull(student, "Student cannot be null");

        setupEventHandlers();
    }

    private void setupEventHandlers() {
        view.getRegisterButton().setOnAction(this);
        view.getCancelButton().setOnAction(event -> handleCancel());
    }

    @Override
    public void handle(ActionEvent event) {
        if (event.getSource() == view.getRegisterButton()) {
            handleRegisterReport();
        }
    }

    private void handleRegisterReport() {
        try {
            clearError();
            resetFieldStyles();

            if (!validateAllFields()) {
                return;
            }

            Report report = createReportFromInput();
            boolean success = reportDAO.addReport(report);

            if (success) {
                showSuccess(SUCCESS_TITLE, "El reporte se ha registrado correctamente");
                clearFields();
            } else {
                showError("No se pudo registrar el reporte");
            }

        } catch (SQLException e) {
            String message = ExceptionManager.handleException(e);
            showError(message);
        } catch (Exception e) {
            String message = ExceptionManager.handleException(e);
            showError(message);
        }
    }

    private Report createReportFromInput() {
        ReportType type = view.getTypeComboBox().getValue();
        int hours = Integer.parseInt(view.getHoursTextField().getText().trim());
        LocalDate localDate = view.getDatePicker().getValue();
        Timestamp date = Timestamp.valueOf(localDate.atStartOfDay());
        String methodology = view.getMethodologyTextArea().getText().trim();
        String description = view.getDescriptionTextArea().getText().trim();

        return new Report(0, date, hours, type, methodology, description, currentStudent);
    }

    private boolean validateAllFields() {
        boolean isValid = true;

        if (view.getTypeComboBox().getValue() == null) {
            showFieldError("El tipo de reporte es obligatorio", view.getTypeComboBox());
            isValid = false;
        }

        if (view.getHoursTextField().getText().isEmpty()) {
            showFieldError("Las horas son obligatorias", view.getHoursTextField());
            isValid = false;
        } else {
            try {
                int hours = Integer.parseInt(view.getHoursTextField().getText());
                if (hours <= 0) {
                    showFieldError("Las horas deben ser mayores a 0", view.getHoursTextField());
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                showFieldError("Las horas deben ser un número válido", view.getHoursTextField());
                isValid = false;
            }
        }

        if (view.getDatePicker().getValue() == null) {
            showFieldError("La fecha es obligatoria", view.getDatePicker());
            isValid = false;
        }

        String methodology = view.getMethodologyTextArea().getText();
        if (methodology.isEmpty()) {
            showFieldError("La metodología es obligatoria", view.getMethodologyTextArea());
            isValid = false;
        } else if (!validators.validateReportTextField(methodology)) {
            showFieldError("La metodología debe contener al menos una letra", view.getMethodologyTextArea());
            isValid = false;
        }

        String description = view.getDescriptionTextArea().getText();
        if (description.isEmpty()) {
            showFieldError("La descripción es obligatoria", view.getDescriptionTextArea());
            isValid = false;
        } else if (!validators.validateReportTextField(description)) {
            showFieldError("La descripción debe contener al menos una letra", view.getDescriptionTextArea());
            isValid = false;
        }

        return isValid;
    }

    private void showSuccess(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);

            try {
                ImageView icon = new ImageView(new Image(Objects.requireNonNull(
                        getClass().getResourceAsStream(ICON_PATH))));
                icon.setFitHeight(50);
                icon.setFitWidth(50);
                alert.setGraphic(icon);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Could not load success icon", e);
            }

            alert.showAndWait();
        });
    }

    private void handleCancel() {
        clearFields();
        currentStage.close();
    }

    private void clearFields() {
        view.getTypeComboBox().setValue(null);
        view.getHoursTextField().clear();
        view.getDatePicker().setValue(null);
        view.getMethodologyTextArea().clear();
        view.getDescriptionTextArea().clear();
        clearError();
    }

    private void resetFieldStyles() {
        view.getTypeComboBox().setStyle("");
        view.getHoursTextField().setStyle("");
        view.getDatePicker().setStyle("");
        view.getMethodologyTextArea().setStyle("");
        view.getDescriptionTextArea().setStyle("");
    }

    private void showFieldError(String message, Control field) {
        showError(message);
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