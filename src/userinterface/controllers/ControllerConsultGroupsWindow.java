package userinterface.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.stage.Stage;
import logic.daos.AcademicDAO;
import logic.daos.GroupDAO;
import logic.logicclasses.Academic;
import logic.logicclasses.Group;
import logic.enums.AcademicType;
import userinterface.windows.ConsultGroupsWindow;
import userinterface.windows.ConsultAcademicsWindow;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class ControllerConsultGroupsWindow {
    private final ConsultGroupsWindow view;
    private final GroupDAO groupDAO;
    private final Stage currentStage;
    private ObservableList<Group> allGroups;

    public ControllerConsultGroupsWindow(ConsultGroupsWindow view, Stage stage) {
        this.view = Objects.requireNonNull(view);
        this.groupDAO = new GroupDAO();
        this.currentStage = Objects.requireNonNull(stage);
        this.allGroups = FXCollections.observableArrayList();

        view.setAssignAcademicHandler(this::handleAssignAcademic);

        setupEventHandlers();
        loadGroupData();
    }

    private void setupEventHandlers() {
        view.getBackButton().setOnAction(e -> currentStage.close());
    }

    private void loadGroupData() {
        try {
            List<Group> groups = groupDAO.getAllGroups();
            allGroups.setAll(groups);
            view.setGroupData(allGroups);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudieron cargar los grupos: " + e.getMessage());
        }
    }

    private void handleAssignAcademic(ActionEvent event) {
        Object source = event.getSource();
        if (!(source instanceof javafx.scene.control.Button)) {
            return;
        }
        javafx.scene.control.Button button = (javafx.scene.control.Button) source;
        Group group = (Group) button.getUserData();
        if (group == null) {
            return;
        }

        ConsultAcademicsWindow academicsWindow = new ConsultAcademicsWindow();
        TableColumn<Academic, Void> assignCol = academicsWindow.createManageButtonColumn(assignEvent -> {
            Object assignSource = assignEvent.getSource();
            if (!(assignSource instanceof Academic)) {
                return;
            }
            Academic academic = (Academic) assignSource;
            try {
                boolean assigned = groupDAO.assignEeAcademic(group.getNrc(), academic.getIdUser());
                if (assigned) {
                    group.setAcademic(academic);
                    view.getGroupTable().refresh();
                    showAlert(Alert.AlertType.INFORMATION, "Éxito", 
                        "Se asignó el académico " + academic.getFullName() + " al grupo " + group.getGroupName());
                    Stage stage = (Stage) academicsWindow.getView().getScene().getWindow();
                    stage.close();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "No se pudo asignar el académico al grupo.");
                }
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Error al asignar el académico: " + e.getMessage());
            }
        });

        assignCol.setText("Asignar");
        academicsWindow.getAcademicTable().getColumns().add(assignCol);

        AcademicDAO academicDAO = new AcademicDAO();
        try {
            List<Academic> academics = academicDAO.getAllAcademicsByType(AcademicType.EE);
            academicsWindow.setAcademicData(FXCollections.observableArrayList(academics));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudieron cargar los académicos EE: " + e.getMessage());
            return;
        }

        Stage academicsStage = new Stage();
        academicsWindow.getBackButton().setOnAction(e -> academicsStage.close());
        academicsStage.setScene(new Scene(academicsWindow.getView(), 800, 600));
        academicsStage.setTitle("Asignar Académico EE al Grupo");
        academicsStage.show();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
