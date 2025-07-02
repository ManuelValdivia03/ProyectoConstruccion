package userinterface.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.stage.Stage;
import logic.daos.ProjectDAO;
import logic.daos.RepresentativeDAO;
import logic.logicclasses.Representative;
import logic.services.ExceptionManager;
import userinterface.windows.AssignRepresentativesToProjectWindow;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class ControllerAssignRepresentativesToProject {
    private final AssignRepresentativesToProjectWindow view;
    private final RepresentativeDAO representativeDAO;
    private final ProjectDAO projectDAO;
    private final Stage currentStage;
    private final int projectId;
    private ObservableList<Representative> allRepresentatives;

    public ControllerAssignRepresentativesToProject(AssignRepresentativesToProjectWindow view, Stage stage, int projectId) {
        this.view = Objects.requireNonNull(view, "La vista no puede ser nula");
        this.representativeDAO = new RepresentativeDAO();
        this.projectDAO = new ProjectDAO();
        this.currentStage = Objects.requireNonNull(stage, "El stage no puede ser nulo");
        this.projectId = projectId;
        this.allRepresentatives = FXCollections.observableArrayList();

        TableColumn<Representative, Void> assignCol = view.createAssignButtonColumn(this::handleAssignToProject);
        view.getRepresentativeTable().getColumns().add(assignCol);

        setupEventHandlers();
        loadRepresentativeData();
    }

    private void setupEventHandlers() {
        view.getBackButton().setOnAction(e -> currentStage.close());
    }

    private void loadRepresentativeData() {
        try {
            List<Representative> reps = representativeDAO.getAllRepresentatives();
            allRepresentatives.setAll(reps);
            view.setRepresentativeData(allRepresentatives);
        } catch (SQLException e) {
            String message = ExceptionManager.handleException(e);
            showAlert(Alert.AlertType.ERROR, "Error",
                    "No se pudieron cargar los representantes: " + message);
        }
    }

    private void handleAssignToProject(ActionEvent event) {
        boolean shouldContinue = true;
        Object source = event.getSource();
        if (!(source instanceof Button)) {
            shouldContinue = false;
        }

        Button button = null;
        Representative rep = null;
        if (shouldContinue) {
            button = (Button) source;
            rep = (Representative) button.getUserData();
            if (rep == null) {
                shouldContinue = false;
            }
        }

        if (shouldContinue) {
            try {
                boolean success = projectDAO.linkProjectToRepresentative(projectId, rep.getIdRepresentative());
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Éxito",
                            "Representante " + rep.getFullName() + " asignado al proyecto exitosamente.");
                    currentStage.close();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "No se pudo asignar el representante al proyecto.");
                }
            } catch (SQLException e) {
                String message = ExceptionManager.handleException(e);
                showAlert(Alert.AlertType.ERROR, "Error",
                        "Error al asignar representante: " + message);
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
