package userinterface.windows;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import logic.logicclasses.Project;
import userinterface.controllers.ControllerCoordinatorProjectsWindow;

public class CoordinatorProjectsWindow {
    private final VBox view;
    private final TableView<Project> projectsTable;
    private final Button viewRequestsButton;
    private final Button refreshButton;
    private final Label statusLabel;

    public CoordinatorProjectsWindow() {
        projectsTable = new TableView<>();
        setupTable();

        viewRequestsButton = new Button("Ver Solicitudes");
        viewRequestsButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");

        refreshButton = new Button("Actualizar");
        refreshButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        HBox buttonBox = new HBox(10, viewRequestsButton, refreshButton);
        buttonBox.setAlignment(Pos.CENTER);

        statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: red;");

        view = new VBox(15);
        view.setPadding(new Insets(15));
        view.setAlignment(Pos.TOP_CENTER);
        view.getChildren().addAll(
                new Label("Gestión de Proyectos"),
                projectsTable,
                buttonBox,
                statusLabel
        );
    }

    private void setupTable() {
        TableColumn<Project, String> titleColumn = new TableColumn<>("Proyecto");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleColumn.setPrefWidth(200);

        TableColumn<Project, Integer> maxColumn = new TableColumn<>("Cupo");
        maxColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        maxColumn.setPrefWidth(80);

        TableColumn<Project, Integer> currentColumn = new TableColumn<>("Inscritos");
        currentColumn.setCellValueFactory(new PropertyValueFactory<>("currentStudents"));
        currentColumn.setPrefWidth(80);

        TableColumn<Project, Void> actionColumn = new TableColumn<>("Acciones");
        actionColumn.setPrefWidth(120);
        actionColumn.setCellFactory(createViewRequestsButtonCellFactory());

        projectsTable.getColumns().addAll(
                titleColumn, maxColumn, currentColumn, actionColumn
        );

        projectsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        projectsTable.setPlaceholder(new Label("No hay proyectos registrados"));
    }

    private Callback<TableColumn<Project, Void>, TableCell<Project, Void>> createViewRequestsButtonCellFactory() {
        return param -> new TableCell<>() {
            private final Button viewBtn = new Button("Ver Solicitudes");

            {
                viewBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: black;");
                viewBtn.setOnAction(event -> {
                    Project project = getTableView().getItems().get(getIndex());
                    if (getTableView().getProperties().get("controller") != null) {
                        ((ControllerCoordinatorProjectsWindow) getTableView().getProperties().get("controller"))
                                .showRequestsForProject(project);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewBtn);
                }
            }
        };
    }

    public VBox getView() { return view; }
    public TableView<Project> getProjectsTable() { return projectsTable; }
    public Button getViewRequestsButton() { return viewRequestsButton; }
    public Button getRefreshButton() { return refreshButton; }
    public Label getStatusLabel() { return statusLabel; }

    public void setProjectsList(ObservableList<Project> projects) {
        projectsTable.setItems(projects);
    }

    public void showMessage(String message, boolean isError) {
        statusLabel.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
        statusLabel.setText(message);
    }
}