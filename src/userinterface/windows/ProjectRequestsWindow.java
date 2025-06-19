package userinterface.windows;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import logic.logicclasses.ProjectRequest;

public class ProjectRequestsWindow {
    private final VBox view;
    private final TableView<ProjectRequest> requestsTable;
    private final Button approveButton;
    private final Button rejectButton;
    private final Label statusLabel;

    public ProjectRequestsWindow() {
        requestsTable = new TableView<>();
        setupTable();

        approveButton = new Button("Aprobar");
        approveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        rejectButton = new Button("Rechazar");
        rejectButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

        HBox buttonBox = new HBox(10, approveButton, rejectButton);
        buttonBox.setAlignment(Pos.CENTER);

        statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: red;");

        view = new VBox(15);
        view.setPadding(new Insets(15));
        view.getChildren().addAll(
                new Label("Solicitudes del Proyecto"),
                requestsTable,
                buttonBox,
                statusLabel
        );
    }

    private void setupTable() {
        TableColumn<ProjectRequest, String> studentColumn = new TableColumn<>("Estudiante");
        studentColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        studentColumn.setPrefWidth(200);

        TableColumn<ProjectRequest, String> dateColumn = new TableColumn<>("Fecha Solicitud");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("requestDate"));
        dateColumn.setPrefWidth(150);

        TableColumn<ProjectRequest, String> statusColumn = new TableColumn<>("Estado");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setPrefWidth(100);

        requestsTable.getColumns().addAll(studentColumn, dateColumn, statusColumn);
        requestsTable.setPlaceholder(new Label("No hay solicitudes para este proyecto"));
    }

    public VBox getView() { return view; }
    public TableView<ProjectRequest> getRequestsTable() { return requestsTable; }
    public Button getApproveButton() { return approveButton; }
    public Button getRejectButton() { return rejectButton; }
    public Label getStatusLabel() { return statusLabel; }

    public void setRequestsList(ObservableList<ProjectRequest> requests) {
        requestsTable.setItems(requests);
    }

    public void showMessage(String message, boolean isError) {
        statusLabel.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
        statusLabel.setText(message);
    }
}