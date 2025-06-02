package userinterface.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import logic.daos.LinkedOrganizationDAO;
import logic.daos.ProyectDAO;
import logic.exceptions.RepeatedProyectException;
import logic.logicclasses.LinkedOrganization;
import logic.logicclasses.Proyect;
import userinterface.utilities.Validators;
import userinterface.windows.ConsultLinkedOrganizationsWindow;
import userinterface.windows.RegistProyectWindow;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

public class ControllerRegistProyectWindow implements EventHandler<ActionEvent> {
    private final RegistProyectWindow view;
    private final ProyectDAO proyectDAO;
    private final LinkedOrganizationDAO organizationDAO;
    private final Validators validators;
    private Stage currentStage;
    private int createdProjectId;

    public ControllerRegistProyectWindow(RegistProyectWindow registProyectWindow, Stage stage) {
        this.view = registProyectWindow;
        this.proyectDAO = new ProyectDAO();
        this.organizationDAO = new LinkedOrganizationDAO();
        this.validators = new Validators();
        this.currentStage = stage;
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

            LocalDate startLocalDate = view.getDateStartPicker().getValue();
            LocalDate endLocalDate = view.getDateEndPicker().getValue();

            Timestamp dateStart = Timestamp.valueOf(startLocalDate.atStartOfDay());
            Timestamp dateEnd = endLocalDate != null ? Timestamp.valueOf(endLocalDate.atStartOfDay()) : null;

            if (!validateDates(dateStart, dateEnd)) {
                return;
            }

            if (proyectDAO.proyectExists(title)) {
                throw new RepeatedProyectException("Ya existe un proyecto con ese título");
            }

            Proyect proyect = new Proyect(0, title, description, dateStart, dateEnd, 'A');

            createdProjectId = proyectDAO.addProyectAndGetId(proyect);
            if (createdProjectId > 0) {
                showSuccessAndOpenOrganizationSelector();
            } else {
                showError("No se pudo registrar el proyecto");
            }

        } catch (RepeatedProyectException e) {
            showError(e.getMessage());
            highlightField(view.getTitleTextField());
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

        if (view.getDateStartPicker().getValue() == null) {
            showError("La fecha de inicio es obligatoria");
            view.getDateStartPicker().setStyle("-fx-border-color: #ff0000; -fx-border-width: 1px;");
            isValid = false;
        }

        return isValid;
    }

    private boolean validateDates(Timestamp start, Timestamp end) {
        if (end != null && start.after(end)) {
            showError("La fecha de inicio debe ser anterior a la fecha de fin");
            view.getDateStartPicker().setStyle("-fx-border-color: #ff0000; -fx-border-width: 1px;");
            view.getDateEndPicker().setStyle("-fx-border-color: #ff0000; -fx-border-width: 1px;");
            return false;
        }
        return true;
    }

    private void showSuccessAndOpenOrganizationSelector() {
        Platform.runLater(() -> {
            showCustomSuccessDialog();
            openOrganizationSelectionWindow();
        });
    }

    private void openOrganizationSelectionWindow() {
        ConsultLinkedOrganizationsWindow orgWindow = new ConsultLinkedOrganizationsWindow();
        Stage orgStage = new Stage();

        // Modify the controller to handle project linking
        new ControllerConsultLinkedOrganizationsWindow(orgWindow, orgStage) {
            public TableColumn<LinkedOrganization, Void> createManageButtonColumn(EventHandler<ActionEvent> manageAction) {
                TableColumn<LinkedOrganization, Void> linkCol = new TableColumn<>("Vincular");
                linkCol.setCellFactory(param -> new TableCell<>() {
                    private final Button btn = new Button("Seleccionar");
                    {
                        btn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
                        btn.setOnAction(event -> {
                            LinkedOrganization org = getTableView().getItems().get(getIndex());
                            if (org != null) {
                                linkProjectToOrganization(org);
                            }
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : btn);
                    }
                });
                return linkCol;
            }
        };

        javafx.scene.Scene scene = new javafx.scene.Scene(orgWindow.getView(), 800, 600);
        orgStage.setScene(scene);
        orgStage.setTitle("Seleccionar Organización para el Proyecto");
        orgStage.show();
    }

    private void linkProjectToOrganization(LinkedOrganization org) {
        try {
            int representativeId = organizationDAO.getRepresentativeId(org.getIdLinkedOrganization());
            if (representativeId > 0) {
                boolean success = proyectDAO.linkProjectToRepresentative(createdProjectId, representativeId, org.getIdLinkedOrganization());
                if (success) {
                    showFinalSuccessDialog();
                    currentStage.close();
                } else {
                    showError("No se pudo vincular el proyecto con la organización");
                }
            } else {
                showError("La organización seleccionada no tiene un representante válido");
            }
        } catch (SQLException e) {
            showError("Error al vincular proyecto: " + e.getMessage());
        }
    }

    private void showCustomSuccessDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Operación exitosa");
        alert.setHeaderText(null);
        alert.setContentText("¡Proyecto registrado correctamente!\nAhora seleccione una organización para vincular.");

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

    private void showFinalSuccessDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Proceso completado");
        alert.setHeaderText(null);
        alert.setContentText("¡Proyecto registrado y vinculado exitosamente!");

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