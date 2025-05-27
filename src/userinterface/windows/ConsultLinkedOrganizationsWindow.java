package userinterface.windows;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import logic.logicclasses.LinkedOrganization;

public class ConsultLinkedOrganizationsWindow {
    private final VBox view;
    private final TableView<LinkedOrganization> organizationTable;
    private final TextField searchField;
    private final Button searchButton;
    private final Button clearButton;
    private final Button backButton;

    public ConsultLinkedOrganizationsWindow() {
        organizationTable = new TableView<>();
        organizationTable.setStyle("-fx-font-size: 14px;");
        organizationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Columnas de la tabla
        TableColumn<LinkedOrganization, String> nameCol = createStyledColumn("Nombre", "nameLinkedOrganization");
        TableColumn<LinkedOrganization, String> phoneCol = createStyledColumn("Teléfono", "cellPhoneLinkedOrganization");
        TableColumn<LinkedOrganization, String> emailCol = createStyledColumn("Email", "emailLinkedOrganization");

        organizationTable.getColumns().addAll(nameCol, phoneCol, emailCol);

        // Barra de búsqueda
        searchField = new TextField();
        searchField.setPromptText("Buscar por nombre...");
        searchField.setStyle("-fx-font-size: 14px; -fx-padding: 5px;");

        searchButton = new Button("Buscar");
        searchButton.setStyle("-fx-background-color: #1A5F4B; -fx-text-fill: white; -fx-font-weight: bold;");

        clearButton = new Button("Limpiar");
        clearButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-weight: bold;");

        HBox searchBox = new HBox(5, searchField, searchButton, clearButton);
        searchBox.setPadding(new Insets(0, 0, 10, 0));

        // Botones inferiores
        backButton = new Button("Regresar");
        backButton.setStyle("-fx-background-color: #ff4a4a; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");

        HBox buttonBox = new HBox(15, backButton);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));

        // Contenedor principal
        view = new VBox(15);
        view.setPadding(new Insets(15));
        view.setStyle("-fx-background-color: #f5f5f5;");
        view.getChildren().addAll(searchBox, organizationTable, buttonBox);
    }

    private TableColumn<LinkedOrganization, String> createStyledColumn(String title, String propertyName) {
        TableColumn<LinkedOrganization, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setStyle("-fx-alignment: CENTER;");
        return column;
    }

    public TableColumn<LinkedOrganization, Void> createManageButtonColumn(EventHandler<ActionEvent> manageAction) {
        TableColumn<LinkedOrganization, Void> manageCol = new TableColumn<>("Acciones");
        manageCol.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Gestionar");
            {
                btn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
                btn.setOnAction(event -> {
                    LinkedOrganization org = getTableView().getItems().get(getIndex());
                    if (org != null) {
                        manageAction.handle(new ActionEvent(org, btn));
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
        return manageCol;
    }

    public VBox getView() {
        return view;
    }

    public TableView<LinkedOrganization> getOrganizationTable() {
        return organizationTable;
    }

    public TextField getSearchField() {
        return searchField;
    }

    public Button getSearchButton() {
        return searchButton;
    }

    public Button getClearButton() {
        return clearButton;
    }

    public Button getBackButton() {
        return backButton;
    }

    public void setOrganizationData(ObservableList<LinkedOrganization> organizations) {
        organizationTable.setItems(organizations);
    }
}
