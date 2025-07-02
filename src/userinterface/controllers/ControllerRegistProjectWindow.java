package userinterface.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import logic.daos.LinkedOrganizationDAO;
import logic.daos.ProjectDAO;
import logic.exceptions.RepeatedProyectException;
import logic.logicclasses.Project;
import logic.services.ExceptionManager;
import userinterface.utilities.Validators;
import userinterface.windows.RegistProjectWindow;
import userinterface.windows.AssignRepresentativesToProjectWindow;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ControllerRegistProjectWindow implements EventHandler<ActionEvent> {
    private static final Logger LOGGER = Logger.getLogger(ControllerRegistProjectWindow.class.getName());
    private static final String ERROR_STYLE = "-fx-border-color: #ff0000; -fx-border-width: 1px;";
    private static final String ERROR_TEXT_STYLE = "-fx-text-fill: #cc0000;";
    private static final String SUCCESS_TITLE = "Operación exitosa";
    private static final String FINAL_SUCCESS_TITLE = "Proceso completado";
    private static final String ICON_PATH = "/images/exito.png";

    private final RegistProjectWindow view;
    private final ProjectDAO projectDAO;
    private final LinkedOrganizationDAO organizationDAO;
    private final Validators validators;
    private final Stage currentStage;
    private int createdProjectId;

    public ControllerRegistProjectWindow(RegistProjectWindow registProjectWindow, Stage stage) {
        this.view = Objects.requireNonNull(registProjectWindow, "RegistProyectWindow cannot be null");
        this.projectDAO = new ProjectDAO();
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
            handleRegisterProject();
        }
    }

    private void handleRegisterProject() {
        boolean canContinue = true;
        try {
            clearError();
            resetFieldStyles();

            if (!validateAllFields()) {
                canContinue = false;
            }

            ProjectRegistrationData data = null;
            if (canContinue) {
                data = collectProjectData();
                if (!validateDates(data.startDate(), data.endDate())) {
                    canContinue = false;
                }
            }

            if (canContinue && projectDAO.proyectExists(data.title())) {
                throw new RepeatedProyectException("Ya existe un proyecto con ese título");
            }

            if (canContinue) {
                Project project = new Project(0, data.title(), data.description(),
                        data.startDate(), data.endDate(), 'A', data.maxStudents(), 0);

                createdProjectId = projectDAO.addProyectAndGetId(project);
                if (createdProjectId > 0) {
                    showSuccessAndOpenRepresentativeSelector();
                } else {
                    showError("No se pudo registrar el proyecto");
                }
            }

        } catch (RepeatedProyectException e) {
            showFieldError(e.getMessage(), view.getTitleTextField());
        } catch (SQLException e) {
            String message = ExceptionManager.handleException(e);
            showError(message);
        } catch (Exception e) {
            String message = ExceptionManager.handleException(e);
            showError(message);
        }
    }

    private ProjectRegistrationData collectProjectData() {
        String title = view.getTitleTextField().getText().trim();
        String description = view.getDescriptionTextField().getText().trim();
        int maxStudents = Integer.parseInt(view.getMaxStudentsTextField().getText().trim());
        LocalDate startLocalDate = view.getDateStartPicker().getValue();
        LocalDate endLocalDate = view.getDateEndPicker().getValue();

        Timestamp startTimestamp = (startLocalDate != null && !startLocalDate.equals(LocalDate.MIN))
                ? Timestamp.valueOf(startLocalDate.atStartOfDay())
                : null;
        Timestamp endTimestamp = (endLocalDate != null && !endLocalDate.equals(LocalDate.MIN))
                ? Timestamp.valueOf(endLocalDate.atStartOfDay())
                : null;

        return new ProjectRegistrationData(
                title,
                description,
                maxStudents,
                startTimestamp,
                endTimestamp
        );
    }

    private boolean validateAllFields() {
        boolean isValid = true;

        String title = view.getTitleTextField().getText();
        if (title.isEmpty()) {
            showFieldError("El título es obligatorio", view.getTitleTextField());
            isValid = false;
        } else if (!validators.validateProjectName(title)) {
            showFieldError("El título debe contener letras válidas", view.getTitleTextField());
            isValid = false;
        }

        String description = view.getDescriptionTextField().getText();
        if (description.isEmpty()) {
            showFieldError("La descripción es obligatoria", view.getDescriptionTextField());
            isValid = false;
        } else if (!validators.validateProjectDescription(description)) {
            showFieldError("La descripción debe contener letras válidas", view.getDescriptionTextField());
            isValid = false;
        }

        if (view.getMaxStudentsTextField().getText().isEmpty()) {
            showFieldError("El cupo máximo es obligatorio", view.getMaxStudentsTextField());
            isValid = false;
        } else {
            try {
                int maxStudents = Integer.parseInt(view.getMaxStudentsTextField().getText());
                if (maxStudents <= 0) {
                    showFieldError("El cupo máximo debe ser mayor a 0", view.getMaxStudentsTextField());
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                showFieldError("El cupo máximo debe ser un número válido", view.getMaxStudentsTextField());
                isValid = false;
            }
        }

        LocalDate startDate = view.getDateStartPicker().getValue();
        if (startDate == null || startDate.equals(LocalDate.MIN)) {
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
        view.getMaxStudentsTextField().clear();
        view.getDateStartPicker().setValue(LocalDate.MIN);
        view.getDateEndPicker().setValue(LocalDate.MIN);
        clearError();
    }

    private void resetFieldStyles() {
        view.getTitleTextField().setStyle("");
        view.getDescriptionTextField().setStyle("");
        view.getMaxStudentsTextField().setStyle("");
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
            int maxStudents,
            Timestamp startDate,
            Timestamp endDate
    ) {}
}
