package userinterface.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import logic.daos.LinkedOrganizationDAO;
import logic.daos.ProyectDAO;
import logic.exceptions.RepeatedProyectException;
import logic.logicclasses.LinkedOrganization;
import logic.logicclasses.Proyect;
import logic.logicclasses.Representative;
import userinterface.utilities.Validators;
import userinterface.windows.ConsultRepresentativesWindow;
import userinterface.windows.RegistProyectWindow;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;

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
                showSuccessAndOpenReoresentativenSelector();
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

    private void showSuccessAndOpenReoresentativenSelector() {
        Platform.runLater(() -> {
            showCustomSuccessDialog();
            openRepresentativesSelectionWindow();
        });
    }

    private void openRepresentativesSelectionWindow() {
        ConsultRepresentativesWindow repsWindow = new ConsultRepresentativesWindow();
        Stage repsStage = new Stage();

        new ControllerConsultRepresentativesWindow(repsWindow, repsStage) {
            public TableColumn<Representative, Void> createAssignButtonColumn(EventHandler<ActionEvent> assignAction) {
                TableColumn<Representative, Void> assignCol = new TableColumn<>("Asignar");
                assignCol.setCellFactory(param -> new TableCell<>() {
                    private final Button btn = new Button("Asignar");
                    {
                        btn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
                        btn.setOnAction(event -> {
                            Representative rep = getTableView().getItems().get(getIndex());
                            if (rep != null) {
                                assignProjectToRepresentative(rep);
                            }
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : btn);
                    }
                });
                return assignCol;
            }
        };

        javafx.scene.Scene scene = new javafx.scene.Scene(repsWindow.getView(), 800, 600);
        repsStage.setScene(scene);
        repsStage.setTitle("Asignar Representante al Proyecto");
        repsStage.show();
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

    private void assignProjectToRepresentative(Representative rep) {
        try {
            boolean success = proyectDAO.linkProjectToRepresentative(
                    createdProjectId,
                    rep.getIdRepresentative(),
                    rep.getLinkedOrganization() != null ? rep.getLinkedOrganization().getIdLinkedOrganization() : null
            );

            if (success) {
                Platform.runLater(() -> {
                    showFinalSuccessDialog();
                    currentStage.close();
                });
            } else {
                showError("No se pudo asignar el representante al proyecto");
            }
        } catch (SQLException e) {
            showError("Error al asignar representante: " + e.getMessage());
        }
    }


    private void showCustomSuccessDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Operación exitosa");
        alert.setHeaderText(null);
        alert.setContentText("¡Proyecto registrado correctamente!\nAhora seleccione un representante para asignar al proyecto.");

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
        alert.setContentText("¡Proyecto registrado y representante asignado exitosamente!");

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