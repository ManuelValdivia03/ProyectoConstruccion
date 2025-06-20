package userinterface.windows;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ConsultAssignedProjectWindow {
    private final VBox view;
    private final Label titleLabel;
    private final Label descriptionLabel;
    private final Label startDateLabel;
    private final Label endDateLabel;
    private final Label statusLabel;
    private final Button downloadButton;
    private final Button closeButton;

    public ConsultAssignedProjectWindow() {
        titleLabel = new Label();
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        titleLabel.setWrapText(true);

        descriptionLabel = new Label();
        descriptionLabel.setWrapText(true);

        startDateLabel = new Label();
        startDateLabel.setWrapText(true);

        endDateLabel = new Label();
        endDateLabel.setWrapText(true);

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(10);
        infoGrid.setPadding(new Insets(10));

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(120);
        col1.setMaxWidth(120);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);

        infoGrid.getColumnConstraints().addAll(col1, col2);

        Label titleHeaderLabel = new Label("Título:");
        Label descriptionHeaderLabel = new Label("Descripción:");
        Label startDateHeaderLabel = new Label("Fecha de inicio:");
        Label endDateHeaderLabel = new Label("Fecha de término:");

        infoGrid.add(titleHeaderLabel, 0, 0);
        infoGrid.add(titleLabel, 1, 0);
        infoGrid.add(descriptionHeaderLabel, 0, 1);
        infoGrid.add(descriptionLabel, 1, 1);
        infoGrid.add(startDateHeaderLabel, 0, 2);
        infoGrid.add(startDateLabel, 1, 2);
        infoGrid.add(endDateHeaderLabel, 0, 3);
        infoGrid.add(endDateLabel, 1, 3);

        downloadButton = new Button("Descargar Oficio de Asignación");
        downloadButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        closeButton = new Button("Cerrar");
        closeButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

        HBox buttonBox = new HBox(10, downloadButton, closeButton);
        buttonBox.setAlignment(Pos.CENTER);

        statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: red;");

        view = new VBox(20);
        view.setPadding(new Insets(15));
        view.getChildren().addAll(
                new Label("Detalles del Proyecto Asignado"),
                infoGrid,
                buttonBox,
                statusLabel
        );
    }

    public VBox getView() { return view; }
    public Button getDownloadButton() { return downloadButton; }
    public Button getCloseButton() { return closeButton; }
    public Label getStatusLabel() { return statusLabel; }

    public void setProjectData(String title, String description, String startDate, String endDate) {
        titleLabel.setText(title);
        descriptionLabel.setText(description);
        startDateLabel.setText(startDate);
        endDateLabel.setText(endDate);
    }

    public void showMessage(String message, boolean isError) {
        statusLabel.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
        statusLabel.setText(message);
    }
}