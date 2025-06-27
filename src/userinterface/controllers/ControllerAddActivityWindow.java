package userinterface.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import logic.daos.ActivityDAO;
import logic.daos.ActivityCronogramDAO;
import logic.enums.ActivityStatus;
import logic.logicclasses.Activity;
import userinterface.windows.AddActivityWindow;
import logic.services.ExceptionManager;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.function.Consumer;

public class ControllerAddActivityWindow implements EventHandler<ActionEvent> {
    private final AddActivityWindow view;
    private final ActivityDAO activityDAO;
    private final ActivityCronogramDAO cronogramDAO;
    private final Stage stage;
    private final Timestamp defaultDate;
    private final Consumer<Void> refreshCallback;
    private final int cronogramId;

    public ControllerAddActivityWindow(Stage parentStage, Timestamp defaultDate,
                                       int cronogramId, Consumer<Void> refreshCallback) {
        this.view = new AddActivityWindow();
        this.activityDAO = new ActivityDAO();
        this.cronogramDAO = new ActivityCronogramDAO();
        this.defaultDate = defaultDate;
        this.cronogramId = cronogramId;
        this.refreshCallback = refreshCallback;

        this.stage = new Stage();
        stage.initOwner(parentStage);
        stage.setScene(new Scene(view.getView(), 400, 350));
        stage.setTitle("Agregar Nueva Actividad");

        LocalDate date = defaultDate.toLocalDateTime().toLocalDate();
        view.getStartDatePicker().setValue(date);
        view.getEndDatePicker().setValue(date.plusDays(1));
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        view.getSaveButton().setOnAction(this);
        view.getCancelButton().setOnAction(this);
    }

    @Override
    public void handle(ActionEvent event) {
        if (event.getSource() == view.getSaveButton()) {
            handleSaveActivity();
        } else if (event.getSource() == view.getCancelButton()) {
            stage.close();
        }
    }

    private void handleSaveActivity() {
        try {
            if (view.getNameField().getText().isEmpty()) {
                view.showMessage("El nombre es requerido", true);
                return;
            }

            LocalDate startDate = view.getStartDatePicker().getValue();
            LocalDate endDate = view.getEndDatePicker().getValue();

            if (startDate == null || endDate == null) {
                view.showMessage("Las fechas son requeridas", true);
                return;
            }

            if (endDate.isBefore(startDate)) {
                view.showMessage("La fecha de término no puede ser anterior a la de inicio", true);
                return;
            }

            Timestamp startTimestamp = Timestamp.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Timestamp endTimestamp = Timestamp.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            Activity activity = new Activity();
            activity.setNameActivity(view.getNameField().getText());
            activity.setDescriptionActivity(view.getDescriptionArea().getText());
            activity.setStartDate(startTimestamp);
            activity.setEndDate(endTimestamp);
            activity.setActivityStatus(ActivityStatus.Pendiente);

            if (activityDAO.addActivity(activity)) {
                if (cronogramDAO.addActivityToCronogram(cronogramId, activity.getIdActivity())) {
                    if (cronogramDAO.initializeActivityForAllStudents(activity.getIdActivity())) {
                        view.showMessage("Actividad guardada y asignada exitosamente", false);
                        refreshCallback.accept(null);
                        stage.close();
                    } else {
                        view.showMessage("Actividad guardada pero error al asignar a estudiantes", true);
                    }
                } else {
                    view.showMessage("Actividad guardada pero error al vincular al cronograma", true);
                }
            } else {
                view.showMessage("Error al guardar la actividad", true);
            }
        } catch (Exception e) {
            String message = ExceptionManager.handleException(e);
            view.showMessage(message, true);
        }
    }

    public void show() {
        stage.show();
    }
}