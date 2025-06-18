package userinterface.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import logic.daos.LinkedOrganizationDAO;
import logic.daos.ProyectDAO;
import logic.exceptions.RepeatedProyectException;
import logic.logicclasses.Proyect;
import logic.logicclasses.Representative;
import userinterface.utilities.Validators;
import userinterface.windows.ConsultRepresentativesWindow;
import userinterface.windows.RegistProyectWindow;
import userinterface.windows.AssignRepresentativesToProjectWindow;
import userinterface.controllers.ControllerAssignRepresentativesToProject;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ControllerRegistProyectWindow implements EventHandler<ActionEvent> {
    private static final Logger LOGGER = Logger.getLogger(ControllerRegistProyectWindow.class.getName());
    private static final String ERROR_STYLE = "-fx-border-color: #ff0000; -fx-border-width: 1px;";
    private static final String ERROR_TEXT_STYLE = "-fx-text-fill: #cc0000;";
    private static final String SUCCESS_TITLE = "Operación exitosa";
    private static final String FINAL_SUCCESS_TITLE = "Proceso completado";
    private static final String ICON_PATH = "/images/exito.png";

    private final RegistProyectWindow view;
    private final ProyectDAO proyectDAO;
    private final LinkedOrganizationDAO organizationDAO;
    private final Validators validators;
    private final Stage currentStage;
    private int createdProjectId;

    public ControllerRegistProyectWindow(RegistProyectWindow registProyectWindow, Stage stage) {
        this.view = Objects.requireNonNull(registProyectWindow, "RegistProyectWindow cannot be null");
        this.proyectDAO = new ProyectDAO();
        this.organizationDAO = new LinkedOrganizationDAO();
        this.validators = new Validators();
        this.currentStage = Objects.requireNonNull(stage, "Stage cannot be null");

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

            ProjectRegistrationData data = collectProjectData();

            if (!validateDates(data.startDate(), data.endDate())) {
                return;
            }

            if (proyectDAO.proyectExists(data.title())) {
                throw new RepeatedProyectException("Ya existe un proyecto con ese título");
            }

            Proyect proyect = new Proyect(0, data.title(), data.description(),
                    data.startDate(), data.endDate(), 'A');

            createdProjectId = proyectDAO.addProyectAndGetId(proyect);
            if (createdProjectId > 0) {
                showSuccessAndOpenRepresentativeSelector();
            } else {
                showError("No se pudo registrar el proyecto");
            }

        } catch (RepeatedProyectException e) {
            showFieldError(e.getMessage(), view.getTitleTextField());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during project registration", e);
            showError("Error de base de datos: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during project registration", e);
            showError("Error inesperado: " + e.getMessage());
        }
    }

    private ProjectRegistrationData collectProjectData() {
        String title = view.getTitleTextField().getText().trim();
        String description = view.getDescriptionTextField().getText().trim();
        LocalDate startLocalDate = view.getDateStartPicker().getValue();
        LocalDate endLocalDate = view.getDateEndPicker().getValue();

        return new ProjectRegistrationData(
                title,
                description,
                Timestamp.valueOf(startLocalDate.atStartOfDay()),
                endLocalDate != null ? Timestamp.valueOf(endLocalDate.atStartOfDay()) : null
        );
    }

    private boolean validateAllFields() {
        boolean isValid = true;

        if (view.getTitleTextField().getText().isEmpty()) {
            showFieldError("El título es obligatorio", view.getTitleTextField());
            isValid = false;
        }

        if (view.getDescriptionTextField().getText().isEmpty()) {
            showFieldError("La descripción es obligatoria", view.getDescriptionTextField());
            isValid = false;
        }

        if (view.getDateStartPicker().getValue() == null) {
            showError("La fecha de inicio es obligatoria");
            view.getDateStartPicker().setStyle(ERROR_STYLE);
            isValid = false;
        }

        return isValid;
    }

    private boolean validateDates(Timestamp start, Timestamp end) {
        if (end != null && start.after(end)) {
            showError("La fecha de inicio debe ser anterior a la fecha de fin");
            view.getDateStartPicker().setStyle(ERROR_STYLE);
            view.getDateEndPicker().setStyle(ERROR_STYLE);
            return false;
        }
        return true;
    }

    private void showSuccessAndOpenRepresentativeSelector() {
        Platform.runLater(() -> {
            showSuccessDialog(SUCCESS_TITLE,
                    "¡Proyecto registrado correctamente!\nAhora seleccione un representante para asignar al proyecto.");
            openAssignRepresentativesWindow();
        });
    }

    private void openAssignRepresentativesWindow() {
        AssignRepresentativesToProjectWindow assignWindow = new AssignRepresentativesToProjectWindow();
        Stage assignStage = new Stage();

        new ControllerAssignRepresentativesToProject(assignWindow, assignStage, createdProjectId);

        Scene scene = new Scene(assignWindow.getView(), 800, 600);
        assignStage.setScene(scene);
        assignStage.setTitle("Asignar Representante al Proyecto");
        assignStage.show();
        assignStage.setOnHidden(e -> currentStage.close());
    }

    private void showSuccessDialog(String title, String message) {
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
    }

    private void handleCancel() {
        clearFields();
        currentStage.close();
    }

    private void clearFields() {
        view.getTitleTextField().clear();
        view.getDescriptionTextField().clear();
        view.getDateStartPicker().setValue(null);
        view.getDateEndPicker().setValue(null);
        clearError();
    }

    private void resetFieldStyles() {
        view.getTitleTextField().setStyle("");
        view.getDescriptionTextField().setStyle("");
        view.getDateStartPicker().setStyle("");
        view.getDateEndPicker().setStyle("");
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

    private record ProjectRegistrationData(
            String title,
            String description,
            Timestamp startDate,
            Timestamp endDate
    ) {}
}
