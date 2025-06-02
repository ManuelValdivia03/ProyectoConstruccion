package userinterface.windows;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import logic.logicclasses.Representative;

public class ConsultRepresentativesWindow {
    private final VBox view;
    private final TableView<Representative> representativeTable;
    private final TextField searchField;
    private final Button searchButton;
    private final Button clearButton;
    private final Button backButton;

    public ConsultRepresentativesWindow() {
        representativeTable = new TableView<>();
        representativeTable.setStyle("-fx-font-size: 14px;");
        representativeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Representative, String> nameCol = createStyledColumn("Nombre", "fullName");
        TableColumn<Representative, String> emailCol = createStyledColumn("Correo", "email");
        TableColumn<Representative, String> phoneCol = createStyledColumn("Teléfono", "cellPhone");
        TableColumn<Representative, String> orgCol = createStyledColumn("Organización", "organizationName");

        representativeTable.getColumns().addAll(nameCol, emailCol, phoneCol, orgCol);

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
        view.getChildren().addAll(searchBox, representativeTable, buttonBox);
    }

    private TableColumn<Representative, String> createStyledColumn(String title, String propertyName) {
        TableColumn<Representative, String> column = new TableColumn<>(title);

        if ("organizationName".equals(propertyName)) {
            column.setCellValueFactory(cellData -> {
                Representative rep = cellData.getValue();
                String orgName = rep.getLinkedOrganization() != null ?
                        rep.getLinkedOrganization().getNameLinkedOrganization() :
                        "No asignada";
                return new SimpleStringProperty(orgName);
            });
        } else {
            column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        }

        column.setStyle("-fx-alignment: CENTER;");
        return column;
    }

    public TableColumn<Representative, Void> createAssignButtonColumn(EventHandler<ActionEvent> assignAction) {
        TableColumn<Representative, Void> assignCol = new TableColumn<>("Acción");
        assignCol.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Asignar");
            {
                btn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
                btn.setOnAction(event -> {
                    Representative rep = getTableView().getItems().get(getIndex());
                    if (rep != null) {
                        btn.setUserData(rep);
                        assignAction.handle(new ActionEvent(btn, null));
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                Representative rep = empty ? null : getTableView().getItems().get(getIndex());
                setGraphic((!empty && rep != null && rep.getLinkedOrganization() == null) ? btn : null);
            }
        });
        return assignCol;
    }

    public VBox getView() {
        return view;
    }

    public TableView<Representative> getRepresentativeTable() {
        return representativeTable;
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

    public void setRepresentativeData(ObservableList<Representative> representatives) {
        representativeTable.setItems(representatives);
    }
}