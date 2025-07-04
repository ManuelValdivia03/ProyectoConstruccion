package userinterface.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import logic.services.ExceptionManager;

public class ControllerConsultGroupsWindow {
    private final ConsultGroupsWindow view;
    private final GroupDAO groupDAO;
    private final Stage currentStage;
    private ObservableList<Group> allGroups;

    private static final Group EMPTY_GROUP = new Group(-1, "", Collections.emptyList(), null);
    private static final Academic EMPTY_ACADEMIC = new Academic(-1, "", "", "",'I', "", AcademicType.NONE);

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
            String message = ExceptionManager.handleException(e);
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudieron cargar los grupos: " + message);
        }
    }

    private void handleAssignAcademic(ActionEvent event) {
        Group group = extractGroupFromEvent(event);
        if (group == EMPTY_GROUP) {
            return;
        }
        ConsultAcademicsWindow academicsWindow = new ConsultAcademicsWindow();
        TableColumn<Academic, Void> assignCol = createAssignColumn(group, academicsWindow);

        academicsWindow.getAcademicTable().getColumns().add(assignCol);

        loadAcademicsData(academicsWindow);

        showAcademicsWindow(academicsWindow);
    }

    private Group extractGroupFromEvent(ActionEvent event) {
        Object source = event.getSource();
        if (!(source instanceof Button)) {
            return EMPTY_GROUP;
        }
        Button button = (Button) source;
        Object userData = button.getUserData();
        if (userData instanceof Group) {
            return (Group) userData;
        }
        return EMPTY_GROUP;
    }

    private TableColumn<Academic, Void> createAssignColumn(Group group, ConsultAcademicsWindow academicsWindow) {
        return academicsWindow.createManageButtonColumn(assignEvent -> {
            Academic academic = extractAcademicFromEvent(assignEvent);
            if (academic == EMPTY_ACADEMIC) {
                return;
            }
            assignAcademicToGroup(group, academic, academicsWindow);
        });
    }

    private Academic extractAcademicFromEvent(ActionEvent assignEvent) {
        Object assignSource = assignEvent.getSource();
        if (assignSource instanceof Academic) {
            return (Academic) assignSource;
        }
        return EMPTY_ACADEMIC;
    }

    private void assignAcademicToGroup(Group group, Academic academic, ConsultAcademicsWindow academicsWindow) {
        try {
            boolean assigned = groupDAO.assignEeAcademic(group.getNrc(), academic.getIdUser());
            if (assigned) {
                group.setAcademic(academic);
                view.getGroupTable().refresh();
                showAlert(Alert.AlertType.INFORMATION, "Éxito",
                    "Se asignó el académico " + academic.getFullName() + " al grupo " + group.getGroupName());
                closeWindow(academicsWindow);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "No se pudo asignar el académico al grupo.");
            }
        } catch (SQLException e) {
            String message = ExceptionManager.handleException(e);
            showAlert(Alert.AlertType.ERROR, "Error", "Error al asignar el académico: " + message);
        }
    }

    private void closeWindow(ConsultAcademicsWindow academicsWindow) {
        Stage stage = (Stage) academicsWindow.getView().getScene().getWindow();
        stage.close();
    }

    private void loadAcademicsData(ConsultAcademicsWindow academicsWindow) {
        AcademicDAO academicDAO = new AcademicDAO();
        try {
            List<Academic> academics = academicDAO.getAllAcademicsByType(AcademicType.EE);
            academicsWindow.setAcademicData(FXCollections.observableArrayList(academics));
        } catch (SQLException e) {
            String message = ExceptionManager.handleException(e);
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudieron cargar los académicos EE: " + message);
        }
    }

    private void showAcademicsWindow(ConsultAcademicsWindow academicsWindow) {
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
