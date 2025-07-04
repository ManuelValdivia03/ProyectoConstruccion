package userinterface.windows;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
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

        TableColumn<LinkedOrganization, String> nameCol = createStyledColumn("Nombre", "nameLinkedOrganization");
        TableColumn<LinkedOrganization, String> phoneCol = createStyledColumn("Teléfono", "cellPhoneLinkedOrganization");
        TableColumn<LinkedOrganization, String> extCol = createStyledColumn("Ext.", "phoneExtension");
        TableColumn<LinkedOrganization, String> deptCol = createStyledColumn("Departamento", "department");
        TableColumn<LinkedOrganization, String> emailCol = createStyledColumn("Email", "emailLinkedOrganization");
        TableColumn<LinkedOrganization, String> statusCol = createStyledColumn("Estado", "status");

        organizationTable.getColumns().addAll(nameCol, phoneCol, extCol, deptCol, emailCol, statusCol);

        nameCol.setPrefWidth(200);
        phoneCol.setPrefWidth(100);
        extCol.setPrefWidth(60);
        deptCol.setPrefWidth(150);
        emailCol.setPrefWidth(200);
        statusCol.setPrefWidth(80);

        searchField = new TextField();
        searchField.setPromptText("Buscar por nombre...");
        searchField.setStyle("-fx-font-size: 14px; -fx-padding: 5px;");

        searchButton = new Button("Buscar");
        searchButton.setStyle("-fx-background-color: #1A5F4B; -fx-text-fill: white; -fx-font-weight: bold;");

        clearButton = new Button("Limpiar");
        clearButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-weight: bold;");

        HBox searchBox = new HBox(5, searchField, searchButton, clearButton);
        searchBox.setPadding(new Insets(0, 0, 10, 0));

        backButton = new Button("Regresar");
        backButton.setStyle("-fx-background-color: #ff4a4a; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");

        HBox buttonBox = new HBox(15, backButton);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));

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
        manageCol.setPrefWidth(100);
        return manageCol;
    }

    public TableColumn<LinkedOrganization, Void> createDocumentsButtonColumn(EventHandler<ActionEvent> viewDocumentsAction) {
        TableColumn<LinkedOrganization, Void> documentsCol = new TableColumn<>("Documentos");
        documentsCol.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Ver");
            {
                btn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
                btn.setOnAction(event -> {
                    LinkedOrganization org = getTableView().getItems().get(getIndex());
                    if (org != null) {
                        viewDocumentsAction.handle(new ActionEvent(org, btn));
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
        documentsCol.setPrefWidth(100);
        return documentsCol;
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
