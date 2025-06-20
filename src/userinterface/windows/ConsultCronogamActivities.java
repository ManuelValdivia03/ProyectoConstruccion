package userinterface.windows;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import logic.logicclasses.Activity;

public class ConsultCronogamActivities {
    private final BorderPane view;
    private final ListView<Activity> activitiesListView;
    private final Button markCompletedButton;
    private final Button markInProgressButton;
    private final Button refreshButton;
    private final Label statusLabel;
    private final Label cronogramInfoLabel;
    private final TextArea activityDetailsArea;
    private final ProgressBar progressBar;

    public ConsultCronogamActivities() {
        activitiesListView = new ListView<>();
        activitiesListView.setPrefHeight(300);
        activitiesListView.setCellFactory(lv -> new ListCell<Activity>() {
            @Override
            protected void updateItem(Activity item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("[%s] %s - %s",
                            item.getActivityStatus().getDbValue(),
                            item.getNameActivity(),
                            item.getStartDate().toLocalDateTime().toLocalDate()));

                    switch(item.getActivityStatus()) {
                        case Completada:
                            setStyle("-fx-background-color: #dff0d8; -fx-font-weight: bold;");
                            break;
                        case En_progreso:
                            setStyle("-fx-background-color: #fcf8e3;");
                            break;
                        case Pendiente:
                            setStyle("-fx-background-color: #f2dede;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        activityDetailsArea = new TextArea();
        activityDetailsArea.setEditable(false);
        activityDetailsArea.setWrapText(true);
        activityDetailsArea.setPrefHeight(120);

        markCompletedButton = new Button("Marcar como Completada");
        markCompletedButton.setStyle("-fx-background-color: #5cb85c; -fx-text-fill: white;");
        markCompletedButton.setMaxWidth(Double.MAX_VALUE);

        markInProgressButton = new Button("Marcar en Progreso");
        markInProgressButton.setStyle("-fx-background-color: #f0ad4e; -fx-text-fill: white;");
        markInProgressButton.setMaxWidth(Double.MAX_VALUE);

        refreshButton = new Button("Refrescar Lista");
        refreshButton.setStyle("-fx-background-color: #5bc0de; -fx-text-fill: white;");
        refreshButton.setMaxWidth(Double.MAX_VALUE);

        statusLabel = new Label();
        statusLabel.setTextFill(Color.RED);

        cronogramInfoLabel = new Label("Cronograma asignado");
        cronogramInfoLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");

        progressBar = new ProgressBar();
        progressBar.setPrefWidth(200);

        VBox buttonBox = new VBox(10, markCompletedButton, markInProgressButton, refreshButton);
        buttonBox.setPadding(new Insets(10));
        buttonBox.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5;");

        VBox detailsBox = new VBox(10,
                new Label("Detalles de la Actividad:"),
                activityDetailsArea,
                new Label("Progreso:"),
                progressBar);
        detailsBox.setPadding(new Insets(10));

        HBox centerBox = new HBox(20, activitiesListView, buttonBox);
        centerBox.setPadding(new Insets(10));

        VBox mainCenter = new VBox(10, centerBox, detailsBox);
        mainCenter.setPadding(new Insets(10));

        view = new BorderPane();
        view.setTop(cronogramInfoLabel);
        view.setCenter(mainCenter);
        view.setBottom(statusLabel);
        view.setPadding(new Insets(15));
        view.setStyle("-fx-background-color: #f9f9f9;");
    }

    public BorderPane getView() {
        return view;
    }

    public ListView<Activity> getActivitiesListView() {
        return activitiesListView;
    }

    public Button getMarkCompletedButton() {
        return markCompletedButton;
    }

    public Button getMarkInProgressButton() {
        return markInProgressButton;
    }

    public Button getRefreshButton() {
        return refreshButton;
    }

    public Label getStatusLabel() {
        return statusLabel;
    }

    public Label getCronogramInfoLabel() {
        return cronogramInfoLabel;
    }

    public TextArea getActivityDetailsArea() {
        return activityDetailsArea;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public void showMessage(String message, boolean isError) {
        statusLabel.setTextFill(isError ? Color.RED : Color.GREEN);
        statusLabel.setText(message);
    }

    public void updateCronogramInfo(String title, String startDate, String endDate) {
        cronogramInfoLabel.setText(String.format(
                "Cronograma: %s (Del %s al %s)",
                title,
                startDate,
                endDate
        ));
    }

    public void displayActivityDetails(Activity activity) {
        if (activity != null) {
            activityDetailsArea.setText(
                    String.format("Nombre: %s\n\nDescripción:\n%s\n\nFecha: %s\nEstado: %s",
                            activity.getNameActivity(),
                            activity.getDescriptionActivity(),
                            activity.getStartDate().toLocalDateTime().toLocalDate(),
                            activity.getActivityStatus().getDbValue())
            );

            switch(activity.getActivityStatus()) {
                case Completada:
                    progressBar.setProgress(1.0);
                    progressBar.setStyle("-fx-accent: #5cb85c;");
                    break;
                case En_progreso:
                    progressBar.setProgress(0.5);
                    progressBar.setStyle("-fx-accent: #f0ad4e;");
                    break;
                case Pendiente:
                    progressBar.setProgress(0.0);
                    progressBar.setStyle("-fx-accent: #d9534f;");
                    break;
            }
        } else {
            activityDetailsArea.clear();
            progressBar.setProgress(0);
        }
    }
}