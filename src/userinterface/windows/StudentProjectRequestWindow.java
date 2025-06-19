package userinterface.windows;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import javafx.util.Callback;
import logic.logicclasses.Project;
import userinterface.controllers.ControllerStudentProjectRequestWindow;

public class StudentProjectRequestWindow {
    private final VBox view;
    private final TableView<Project> projectsTable;
    private final TextField searchField;
    private final Button searchButton;
    private final Button backButton;
    private final Label resultLabel;

    public StudentProjectRequestWindow() {
        projectsTable = new TableView<>();
        setupTable();

        searchField = new TextField();
        searchField.setPromptText("Buscar proyecto...");
        searchField.setPrefWidth(250);

        searchButton = new Button("Buscar");
        searchButton.setStyle("-fx-background-color: #1A5F4B; -fx-text-fill: white;");

        backButton = new Button("Regresar");
        backButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

        HBox searchBox = new HBox(10, searchField, searchButton);
        searchBox.setAlignment(Pos.CENTER);

        HBox buttonBox = new HBox(10, backButton);
        buttonBox.setAlignment(Pos.CENTER);

        resultLabel = new Label();
        resultLabel.setStyle("-fx-text-fill: red;");

        view = new VBox(15);
        view.setPadding(new Insets(15));
        view.setAlignment(Pos.TOP_CENTER);
        view.getChildren().addAll(
                new Label("Solicitar Participación en Proyectos"),
                searchBox,
                projectsTable,
                buttonBox,
                resultLabel
        );
    }

    private void setupTable() {
        TableColumn<Project, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("idProyect"));
        idColumn.setPrefWidth(60);

        TableColumn<Project, String> titleColumn = new TableColumn<>("Título");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleColumn.setCellFactory(tc -> new TooltipTableCell<>());
        titleColumn.setPrefWidth(150);

        TableColumn<Project, String> descColumn = new TableColumn<>("Descripción");
        descColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descColumn.setCellFactory(tc -> new TooltipTableCell<>());
        descColumn.setPrefWidth(200);

        TableColumn<Project, String> startColumn = new TableColumn<>("Fecha Inicio");
        startColumn.setCellValueFactory(new PropertyValueFactory<>("dateStart"));
        startColumn.setPrefWidth(120);

        TableColumn<Project, String> endColumn = new TableColumn<>("Fecha Fin");
        endColumn.setCellValueFactory(new PropertyValueFactory<>("dateEnd"));
        endColumn.setPrefWidth(120);

        TableColumn<Project, Integer> maxColumn = new TableColumn<>("Cupo");
        maxColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        maxColumn.setPrefWidth(80);

        TableColumn<Project, Integer> currentColumn = new TableColumn<>("Inscritos");
        currentColumn.setCellValueFactory(new PropertyValueFactory<>("currentStudents"));
        currentColumn.setPrefWidth(80);

        TableColumn<Project, String> statusColumn = new TableColumn<>("Estado");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setPrefWidth(80);

        TableColumn<Project, Void> actionColumn = new TableColumn<>("Acción");
        actionColumn.setPrefWidth(100);
        actionColumn.setCellFactory(createRequestButtonCellFactory());

        projectsTable.getColumns().addAll(
                idColumn, titleColumn, descColumn,
                startColumn, endColumn, maxColumn,
                currentColumn, statusColumn, actionColumn
        );

        projectsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        projectsTable.setPlaceholder(new Label("No hay proyectos disponibles"));
        projectsTable.setMinHeight(300);
    }

    private Callback<TableColumn<Project, Void>, TableCell<Project, Void>> createRequestButtonCellFactory() {
        return param -> new TableCell<>() {
            private final Button requestBtn = new Button("Solicitar");

            {
                requestBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                requestBtn.setOnAction(event -> {
                    Project project = getTableView().getItems().get(getIndex());
                    if (getTableView().getProperties().get("controller") != null) {
                        ((ControllerStudentProjectRequestWindow) getTableView().getProperties().get("controller"))
                                .handleRequest(project);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(requestBtn);
                }
            }
        };
    }

    private static class TooltipTableCell<T> extends TableCell<T, String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setTooltip(null);
            } else {
                setText(item);
                setTooltip(new Tooltip(item));
                setWrapText(true);
            }
        }
    }

    public VBox getView() { return view; }
    public TableView<Project> getProjectsTable() { return projectsTable; }
    public TextField getSearchField() { return searchField; }
    public Button getSearchButton() { return searchButton; }
    public Button getBackButton() { return backButton; }
    public Label getResultLabel() { return resultLabel; }

    public void setProjectsList(ObservableList<Project> projects) {
        projectsTable.setItems(projects);
    }

    public void showSuccess(String message) {
        resultLabel.setStyle("-fx-text-fill: green;");
        resultLabel.setText(message);
    }

    public void showError(String message) {
        resultLabel.setStyle("-fx-text-fill: red;");
        resultLabel.setText(message);
    }
}