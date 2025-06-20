package userinterface.windows;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Control;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import logic.logicclasses.Project;
import userinterface.controllers.ControllerConsultProyectsWindow;

public class ConsultProyectsWindow {
    private final VBox view;
    private final TableView<Project> projectsTable;
    private final TextField searchField;
    private final Button searchButton;
    private final Button refreshButton;
    private final Button backButton;
    private final Label resultLabel;

    public ConsultProyectsWindow() {
        projectsTable = new TableView<>();
        setupTable();

        searchField = new TextField();
        searchField.setPromptText("Buscar por título...");
        searchField.setPrefWidth(250);

        searchButton = new Button("Buscar");
        searchButton.setStyle("-fx-background-color: #1A5F4B; -fx-text-fill: white;");

        refreshButton = new Button("Limpiar");
        refreshButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white;");

        HBox searchBox = new HBox(10, searchField, searchButton, refreshButton);
        searchBox.setAlignment(Pos.CENTER);

        backButton = new Button("Regresar");
        backButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

        resultLabel = new Label();
        resultLabel.setStyle("-fx-text-fill: red;");

        view = new VBox(15);
        view.setPadding(new Insets(15));
        view.setAlignment(Pos.TOP_CENTER);
        view.getChildren().addAll(
                new Label("Consultar Proyectos"),
                searchBox,
                projectsTable,
                backButton,
                resultLabel
        );
    }

    private void setupTable() {
        TableColumn<Project, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("idProyect"));
        idColumn.setPrefWidth(60);

        TableColumn<Project, String> titleColumn = new TableColumn<>("Título");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleColumn.setCellFactory(tc -> new TableCell<>() {
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
                    setMaxWidth(Double.MAX_VALUE);
                    setPrefHeight(Control.USE_COMPUTED_SIZE);
                }
            }
        });
        titleColumn.setPrefWidth(150);
        titleColumn.setMinWidth(150);

        TableColumn<Project, String> descColumn = new TableColumn<>("Descripción");
        descColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descColumn.setPrefWidth(200);

        TableColumn<Project, String> startColumn = new TableColumn<>("Inicio");
        startColumn.setCellValueFactory(new PropertyValueFactory<>("dateStart"));
        startColumn.setPrefWidth(100);

        TableColumn<Project, String> endColumn = new TableColumn<>("Fin");
        endColumn.setCellValueFactory(new PropertyValueFactory<>("dateEnd"));
        endColumn.setPrefWidth(100);

        TableColumn<Project, Integer> maxStudentsColumn = new TableColumn<>("Cupo");
        maxStudentsColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        maxStudentsColumn.setPrefWidth(100);

        TableColumn<Project, Integer> currentStudentsColumn = new TableColumn<>("Estudiantes Actuales");
        currentStudentsColumn.setCellValueFactory(new PropertyValueFactory<>("currentStudents"));
        currentStudentsColumn.setPrefWidth(120);

        TableColumn<Project, String> statusColumn = new TableColumn<>("Estado");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setPrefWidth(80);

        TableColumn<Project, Void> actionsColumn = new TableColumn<>("Acciones");
        actionsColumn.setPrefWidth(100);
        actionsColumn.setCellFactory(createEditButtonCellFactory());

        projectsTable.getColumns().addAll(
                idColumn, titleColumn, descColumn,
                startColumn, endColumn, maxStudentsColumn,
                currentStudentsColumn, statusColumn, actionsColumn
        );

        projectsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        projectsTable.setPlaceholder(new Label("No hay proyectos registrados"));
        projectsTable.setMinHeight(300);
    }

    private Callback<TableColumn<Project, Void>, TableCell<Project, Void>> createEditButtonCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<Project, Void> call(final TableColumn<Project, Void> param) {
                return new TableCell<>() {
                    private final Button editBtn = new Button("Editar");

                    {
                        editBtn.setStyle("-fx-background-color: #FFC107; -fx-text-fill: black;");
                        editBtn.setOnAction(event -> {
                            Project project = getTableView().getItems().get(getIndex());
                            if (getTableView().getProperties().get("controller") != null) {
                                ControllerConsultProyectsWindow controller =
                                        (ControllerConsultProyectsWindow) getTableView().getProperties().get("controller");
                                controller.handleEdit(project);
                            }
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(editBtn);
                        }
                    }
                };
            }
        };
    }

    public VBox getView() {
        return view;
    }

    public TableView<Project> getProjectsTable() {
        return projectsTable;
    }

    public TextField getSearchField() {
        return searchField;
    }

    public Button getSearchButton() {
        return searchButton;
    }

    public Button getRefreshButton() {
        return refreshButton;
    }

    public Button getBackButton() {
        return backButton;
    }

    public Label getResultLabel() {
        return resultLabel;
    }

    public void setProjectsList(ObservableList<Project> projects) {
        projectsTable.setItems(projects);
    }
}
