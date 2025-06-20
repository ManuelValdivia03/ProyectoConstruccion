package userinterface.controllers;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import logic.enums.ActivityStatus;
import logic.logicclasses.ActivityCronogram;
import userinterface.windows.RegistCronogramActivityWindow;
import logic.daos.ActivityDAO;
import logic.daos.ActivityCronogramDAO;
import logic.logicclasses.Activity;
import java.time.LocalDate;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ControllerRegistCronogramActivityWindow {
    private static final Logger LOGGER = Logger.getLogger(ControllerRegistCronogramActivityWindow.class.getName());

    private final Stage stage;
    private final RegistCronogramActivityWindow view;
    private final ActivityDAO activityDAO;
    private final ActivityCronogramDAO cronogramDAO;
    private Map<LocalDate, Activity> activitiesByDate;
    private int currentCronogramId;

    public ControllerRegistCronogramActivityWindow(Stage stage) {
        this.stage = stage;
        this.view = new RegistCronogramActivityWindow();
        this.activityDAO = new ActivityDAO();
        this.cronogramDAO = new ActivityCronogramDAO();
        this.activitiesByDate = new HashMap<>();
        this.currentCronogramId = getOrCreateMainCronogram();

        loadActivities();
        setupEventHandlers();
    }

    private int getOrCreateMainCronogram() {
        try {
            List<ActivityCronogram> cronograms = cronogramDAO.getAllCronograms();
            if (!cronograms.isEmpty()) {
                LOGGER.log(Level.INFO, "Usando cronograma existente ID: {0}", cronograms.get(0).getIdCronogram());
                return cronograms.get(0).getIdCronogram();
            }

            ActivityCronogram newCronogram = new ActivityCronogram();
            newCronogram.setDateStart(new Timestamp(System.currentTimeMillis()));
            newCronogram.setDateEnd(new Timestamp(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000));

            if (cronogramDAO.addCronogram(newCronogram)) {
                if (cronogramDAO.assignCronogramToAllStudents(newCronogram.getIdCronogram())) {
                    LOGGER.log(Level.INFO, "Nuevo cronograma creado y asignado. ID: {0}", newCronogram.getIdCronogram());
                    return newCronogram.getIdCronogram();
                } else {
                    LOGGER.log(Level.SEVERE, "Error al asignar cronograma a estudiantes");
                    showError("Error al asignar cronograma a estudiantes");
                }
            } else {
                LOGGER.log(Level.SEVERE, "Error al crear nuevo cronograma");
                showError("Error al crear nuevo cronograma");
            }
        } catch (SQLException e) {
            String errorMsg = "Error al inicializar cronograma: " + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMsg, e);
            showError(errorMsg);
        }
        return -1;
    }

    private void loadActivities() {
        try {
            if (currentCronogramId != -1) {
                List<Activity> activities = cronogramDAO.getActivitiesByCronogram(currentCronogramId);
                activitiesByDate.clear();
                for (Activity activity : activities) {
                    LocalDate date = activity.getStartDate().toLocalDateTime().toLocalDate();
                    activitiesByDate.put(date, activity);
                }
                view.updateCalendarWithActivities(activitiesByDate);
                LOGGER.log(Level.INFO, "Actividades cargadas: {0}", activities.size());
            } else {
                LOGGER.log(Level.WARNING, "No se pudieron cargar actividades - ID de cronograma inválido");
            }
        } catch (SQLException e) {
            String errorMsg = "Error al cargar actividades: " + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMsg, e);
            showError(errorMsg);
        }
    }

    private void setupEventHandlers() {
        view.getPreviousMonthButton().setOnAction(e -> {
            view.setCurrentYearMonth(view.getCurrentYearMonth().minusMonths(1));
            view.updateCalendarWithActivities(activitiesByDate);
        });

        view.getNextMonthButton().setOnAction(e -> {
            view.setCurrentYearMonth(view.getCurrentYearMonth().plusMonths(1));
            view.updateCalendarWithActivities(activitiesByDate);
        });

        view.setOnDayClicked(date -> {
            Activity existingActivity = activitiesByDate.get(date);
            if (existingActivity != null) {
                showActivityOptions(date, existingActivity);
            } else {
                showAddActivityDialog(date);
            }
        });
    }

    private void showActivityOptions(LocalDate date, Activity activity) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Gestión de Actividad");
        dialog.setHeaderText("Actividad del " + date);

        ButtonType editButton = new ButtonType("Editar", ButtonBar.ButtonData.OK_DONE);
        ButtonType deleteButton = new ButtonType("Eliminar", ButtonBar.ButtonData.OTHER);
        ButtonType cancelButton = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(editButton, deleteButton, cancelButton);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(new Label(activity.getNameActivity()), 1, 0);
        grid.add(new Label("Descripción:"), 0, 1);
        grid.add(new Label(activity.getDescriptionActivity()), 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == editButton) {
                showEditActivityDialog(date, activity);
            } else if (buttonType == deleteButton) {
                deleteActivity(activity);
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showEditActivityDialog(LocalDate date, Activity existingActivity) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Editar Actividad");

        TextField nameField = new TextField(existingActivity.getNameActivity());
        TextArea descField = new TextArea(existingActivity.getDescriptionActivity());
        descField.setPrefRowCount(3);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));
        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Descripción:"), 0, 1);
        grid.add(descField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        ButtonType saveButton = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButton) {
                updateActivity(existingActivity, nameField.getText(), descField.getText());
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void updateActivity(Activity activity, String newName, String newDesc) {
        try {
            activity.setNameActivity(newName);
            activity.setDescriptionActivity(newDesc);

            if (activityDAO.updateActivity(activity)) {
                loadActivities();
                view.showMessage("Actividad actualizada", false);
                LOGGER.log(Level.INFO, "Actividad actualizada ID: {0}", activity.getIdActivity());
            } else {
                LOGGER.log(Level.WARNING, "No se pudo actualizar actividad ID: {0}", activity.getIdActivity());
                showError("No se pudo actualizar la actividad");
            }
        } catch (SQLException e) {
            String errorMsg = "Error al actualizar: " + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMsg, e);
            showError(errorMsg);
        }
    }

    private void showAddActivityDialog(LocalDate date) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Nueva Actividad");

        TextField nameField = new TextField();
        TextArea descField = new TextArea();
        descField.setPrefRowCount(3);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));
        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Descripción:"), 0, 1);
        grid.add(descField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        ButtonType saveButton = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == saveButton) {
                createNewActivity(date, nameField.getText(), descField.getText());
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void createNewActivity(LocalDate date, String name, String description) {
        try {
            Activity newActivity = new Activity();
            newActivity.setNameActivity(name);
            newActivity.setDescriptionActivity(description);
            newActivity.setStartDate(Timestamp.valueOf(date.atStartOfDay()));
            newActivity.setEndDate(Timestamp.valueOf(date.atTime(23, 59, 59)));
            newActivity.setActivityStatus(ActivityStatus.Pendiente);

            if (activityDAO.addActivity(newActivity)) {
                if (cronogramDAO.addActivityToCronogram(currentCronogramId, newActivity.getIdActivity())) {
                    if (cronogramDAO.initializeActivityForAllStudents(newActivity.getIdActivity())) {
                        loadActivities();
                        view.showMessage("Actividad creada exitosamente", false);
                        LOGGER.log(Level.INFO, "Nueva actividad creada ID: {0}", newActivity.getIdActivity());
                    } else {
                        LOGGER.log(Level.WARNING, "No se pudo inicializar actividad para estudiantes. ID: {0}", newActivity.getIdActivity());
                        showError("Actividad creada pero no asignada a estudiantes");
                    }
                } else {
                    LOGGER.log(Level.WARNING, "No se pudo agregar actividad al cronograma. ID: {0}", newActivity.getIdActivity());
                    showError("Actividad creada pero no vinculada al cronograma");
                }
            } else {
                LOGGER.log(Level.SEVERE, "Error al crear nueva actividad");
                showError("Error al crear la actividad");
            }
        } catch (SQLException e) {
            String errorMsg = "Error al crear: " + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMsg, e);
            showError(errorMsg);
        }
    }

    private void deleteActivity(Activity activity) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar");
        confirm.setHeaderText("Eliminar actividad");
        confirm.setContentText("¿Está seguro de eliminar esta actividad?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    if (cronogramDAO.deleteActivityCompletely(activity.getIdActivity())) {
                        activitiesByDate.remove(activity.getStartDate().toLocalDateTime().toLocalDate());
                        view.updateCalendarWithActivities(activitiesByDate);
                        view.showMessage("Actividad eliminada", false);
                        LOGGER.log(Level.INFO, "Actividad eliminada ID: {0}", activity.getIdActivity());
                    } else {
                        LOGGER.log(Level.WARNING, "No se pudo eliminar actividad ID: {0}", activity.getIdActivity());
                        showError("No se pudo eliminar la actividad");
                    }
                } catch (SQLException e) {
                    String errorMsg = "Error al eliminar: " + e.getMessage();
                    LOGGER.log(Level.SEVERE, errorMsg, e);
                    showError(errorMsg);
                }
            }
        });
    }

    private void showError(String message) {
        view.showMessage(message, true);
    }

    public void show() {
        stage.setScene(new Scene(view.getView(), 600, 500));
        stage.setTitle("Gestión de Cronograma");
        stage.show();
    }
}