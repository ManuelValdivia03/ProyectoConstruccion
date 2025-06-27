package userinterface.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import logic.daos.ActivityCronogramDAO;
import logic.daos.ActivityTrackingDAO;
import logic.enums.ActivityStatus;
import logic.logicclasses.Activity;
import logic.logicclasses.ActivityCronogram;
import logic.services.ExceptionManager;
import userinterface.windows.ConsultCronogamActivities;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ControllerConsultCronogamActivities implements EventHandler<ActionEvent> {
    private final ConsultCronogamActivities view;
    private final ActivityCronogramDAO cronogramDAO;
    private final ActivityTrackingDAO trackingDAO;
    private final int studentId;
    private final Stage stage;
    private ActivityCronogram currentCronogram;

    public ControllerConsultCronogamActivities(Stage parentStage, int studentId) {
        this.view = new ConsultCronogamActivities();
        this.cronogramDAO = new ActivityCronogramDAO();
        this.trackingDAO = new ActivityTrackingDAO();
        this.studentId = studentId;

        this.stage = new Stage();
        stage.initOwner(parentStage);
        stage.setScene(new Scene(view.getView(), 800, 600));
        stage.setTitle("Mis Actividades");

        setupEventHandlers();
        loadStudentCronogram();
    }

    private void setupEventHandlers() {
        view.getMarkCompletedButton().setOnAction(this);
        view.getMarkInProgressButton().setOnAction(this);
        view.getRefreshButton().setOnAction(this);

        view.getActivitiesListView().getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> view.displayActivityDetails(newVal)
        );
    }

    private void loadStudentCronogram() {
        try {
            List<Integer> cronogramIds = trackingDAO.getStudentCronograms(studentId);
            if (cronogramIds.isEmpty()) {
                view.showMessage("No tienes actividades asignadas", true);
                return;
            }

            currentCronogram = cronogramDAO.getCronogramById(cronogramIds.get(0));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String startDate = currentCronogram.getDateStart().toLocalDateTime().format(formatter);
            String endDate = currentCronogram.getDateEnd().toLocalDateTime().format(formatter);
            view.updateCronogramInfo("Mis Actividades", startDate, endDate);

            refreshActivitiesList();

        } catch (SQLException e) {
            String message = ExceptionManager.handleException(e);
            view.showMessage("Error al cargar actividades: " + message, true);
        }
    }

    private void refreshActivitiesList() {
        try {
            List<Activity> activities = trackingDAO.getStudentActivitiesWithStatus(studentId);
            view.getActivitiesListView().getItems().setAll(activities);

            if (activities.isEmpty()) {
                view.showMessage("No hay actividades asignadas", false);
            } else {
                view.showMessage(activities.size() + " actividades cargadas", false);
            }

        } catch (SQLException e) {
            String message = ExceptionManager.handleException(e);
            view.showMessage("Error al cargar actividades: " + message, true);
        }
    }

    @Override
    public void handle(ActionEvent event) {
        Activity selected = view.getActivitiesListView().getSelectionModel().getSelectedItem();

        if (selected == null) {
            view.showMessage("Selecciona una actividad primero", true);
            return;
        }

        try {
            if (event.getSource() == view.getMarkCompletedButton()) {
                updateActivityStatus(selected, ActivityStatus.Completada);
            } else if (event.getSource() == view.getMarkInProgressButton()) {
                updateActivityStatus(selected, ActivityStatus.En_progreso);
            } else if (event.getSource() == view.getRefreshButton()) {
                refreshActivitiesList();
            }
        } catch (Exception e) {
            String message = ExceptionManager.handleException(e);
            view.showMessage("Error: " + message, true);
        }
    }

    private void updateActivityStatus(Activity activity, ActivityStatus newStatus) throws SQLException {
        if (trackingDAO.updateActivityStatus(studentId, activity.getIdActivity(), newStatus)) {
            activity.setActivityStatus(newStatus);
            view.displayActivityDetails(activity);
            refreshActivitiesList();
            view.showMessage("Estado actualizado a: " + newStatus.getDbValue(), false);
        } else {
            view.showMessage("No se pudo actualizar el estado", true);
        }
    }

    public void show() {
        stage.show();
    }
}