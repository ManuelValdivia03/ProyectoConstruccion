package userinterface.windows;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import logic.logicclasses.Representative;

public class AssignRepresentativesToProjectWindow {
    private final VBox view;
    private final TableView<Representative> representativeTable;
    private final Button backButton;

    public AssignRepresentativesToProjectWindow() {
        representativeTable = new TableView<>();
        representativeTable.setStyle("-fx-font-size: 14px;");
        representativeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Representative, String> nameCol = createStyledColumn("Nombre", "fullName");
        TableColumn<Representative, String> emailCol = createStyledColumn("Correo", "email");
        TableColumn<Representative, String> phoneCol = createStyledColumn("Teléfono", "cellPhone");

        representativeTable.getColumns().addAll(nameCol, emailCol, phoneCol);

        backButton = new Button("Regresar");
        backButton.setStyle("-fx-background-color: #ff4a4a; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");

        HBox buttonBox = new HBox(15, backButton);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));

        view = new VBox(15);
        view.setPadding(new Insets(15));
        view.setStyle("-fx-background-color: #f5f5f5;");
        view.getChildren().addAll(representativeTable, buttonBox);
    }

    private TableColumn<Representative, String> createStyledColumn(String title, String propertyName) {
        TableColumn<Representative, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setStyle("-fx-alignment: CENTER;");
        return column;
    }

    public TableColumn<Representative, Void> createAssignButtonColumn(EventHandler<ActionEvent> assignAction) {
        TableColumn<Representative, Void> assignCol = new TableColumn<>("Asignar");
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
                setGraphic(empty ? null : btn);
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

    public Button getBackButton() {
        return backButton;
    }

    public void setRepresentativeData(ObservableList<Representative> representatives) {
        representativeTable.setItems(representatives);
    }
}