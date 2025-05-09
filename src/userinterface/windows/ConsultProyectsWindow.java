package userinterface.windows;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import logic.logicclasses.Proyect;
import userinterface.controllers.ControllerConsultProyectsWindow;

public class ConsultProyectsWindow {
    private final VBox view;
    private final TableView<Proyect> projectsTable;
    private final TextField searchField;
    private final Button searchButton;
    private final Button refreshButton;
    private final Button backButton;
    private final Label resultLabel;

    public ConsultProyectsWindow() {
        // Crear tabla
        projectsTable = new TableView<>();
        setupTable();

        // Crear controles de búsqueda
        searchField = new TextField();
        searchField.setPromptText("Buscar por título...");
        searchField.setPrefWidth(250);

        searchButton = new Button("Buscar");
        searchButton.setStyle("-fx-background-color: #4a7bed; -fx-text-fill: white;");

        refreshButton = new Button("Refrescar");
        refreshButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        HBox searchBox = new HBox(10, searchField, searchButton, refreshButton);
        searchBox.setAlignment(Pos.CENTER);

        // Botón de regresar
        backButton = new Button("Regresar");
        backButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

        // Label para mensajes
        resultLabel = new Label();
        resultLabel.setStyle("-fx-text-fill: red;");

        // Configurar vista principal
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
        TableColumn<Proyect, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("idProyect"));
        idColumn.setPrefWidth(60);

        TableColumn<Proyect, String> titleColumn = new TableColumn<>("Título");
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

        TableColumn<Proyect, String> descColumn = new TableColumn<>("Descripción");
        descColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descColumn.setPrefWidth(200);

        TableColumn<Proyect, String> startColumn = new TableColumn<>("Inicio");
        startColumn.setCellValueFactory(new PropertyValueFactory<>("dateStart"));
        startColumn.setPrefWidth(100);

        TableColumn<Proyect, String> endColumn = new TableColumn<>("Fin");
        endColumn.setCellValueFactory(new PropertyValueFactory<>("dateEnd"));
        endColumn.setPrefWidth(100);

        TableColumn<Proyect, String> statusColumn = new TableColumn<>("Estado");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setPrefWidth(80);

        TableColumn<Proyect, Void> actionsColumn = new TableColumn<>("Acciones");
        actionsColumn.setPrefWidth(100);
        actionsColumn.setCellFactory(createEditButtonCellFactory());

        projectsTable.getColumns().addAll(
                idColumn, titleColumn, descColumn,
                startColumn, endColumn, statusColumn, actionsColumn
        );

        projectsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        projectsTable.setPlaceholder(new Label("No hay proyectos registrados"));
        projectsTable.setMinHeight(300);
    }

    private Callback<TableColumn<Proyect, Void>, TableCell<Proyect, Void>> createEditButtonCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<Proyect, Void> call(final TableColumn<Proyect, Void> param) {
                return new TableCell<>() {
                    private final Button editBtn = new Button("Editar");

                    {
                        editBtn.setStyle("-fx-background-color: #FFC107; -fx-text-fill: black;");
                        editBtn.setOnAction(event -> {
                            Proyect proyect = getTableView().getItems().get(getIndex());
                            // Llamar al controlador principal para manejar la edición
                            if (getTableView().getProperties().get("controller") != null) {
                                ControllerConsultProyectsWindow controller =
                                        (ControllerConsultProyectsWindow) getTableView().getProperties().get("controller");
                                controller.handleEdit(proyect);
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

    public TableView<Proyect> getProjectsTable() {
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

    public void setProjectsList(ObservableList<Proyect> projects) {
        projectsTable.setItems(projects);
    }
}