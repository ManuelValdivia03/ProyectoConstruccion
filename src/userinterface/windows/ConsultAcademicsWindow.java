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
import logic.daos.AccountDAO;
import logic.logicclasses.Academic;

import java.sql.SQLException;

public class ConsultAcademicsWindow {
    private final VBox view;
    private final TableView<Academic> academicTable;
    private final Button refreshButton;
    private final Button backButton;

    public ConsultAcademicsWindow() {
        academicTable = new TableView<>();

        TableColumn<Academic, String> staffNumberCol = createColumn("Núm. Personal", "staffNumber");
        TableColumn<Academic, String> nameCol = createColumn("Nombre", "fullName");
        TableColumn<Academic, String> phoneCol = createColumn("Teléfono", "cellPhone");
        TableColumn<Academic, String> typeCol = createColumn("Tipo", "academicType");

        TableColumn<Academic, String> emailCol = new TableColumn<>("Correo");
        emailCol.setCellValueFactory(cellData -> {
            try {
                String email = new AccountDAO().getAccountByUserId(
                        cellData.getValue().getIdUser()).getEmail();
                return new SimpleStringProperty(email);
            } catch (SQLException e) {
                return new SimpleStringProperty("Error");
            }
        });

        academicTable.getColumns().addAll(staffNumberCol, nameCol, phoneCol, emailCol, typeCol);

        // Botones
        refreshButton = new Button("Actualizar Lista");
        refreshButton.setStyle("-fx-background-color: #4a7bed; -fx-text-fill: white;");

        backButton = new Button("Regresar");
        backButton.setStyle("-fx-background-color: #ff4a4a; -fx-text-fill: white;");

        HBox buttonBox = new HBox(10, refreshButton, backButton);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        view = new VBox(10);
        view.setPadding(new Insets(10));
        view.getChildren().addAll(academicTable, buttonBox);
    }

    private TableColumn<Academic, String> createColumn(String title, String propertyName) {
        TableColumn<Academic, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        return column;
    }

    public TableColumn<Academic, Void> createManageButtonColumn(EventHandler<ActionEvent> manageAction) {
        TableColumn<Academic, Void> manageCol = new TableColumn<>("Gestionar");
        manageCol.setCellFactory(param -> new TableCell<>() {
            private final Button manageButton = new Button("Gestionar");

            {
                manageButton.setOnAction(event -> {
                    Academic academic = getTableView().getItems().get(getIndex());
                    if (academic != null) {
                        manageAction.handle(new ActionEvent(academic, manageButton));
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(manageButton);
                }
            }
        });
        return manageCol;
    }

    public VBox getView() {
        return view;
    }

    public TableView<Academic> getAcademicTable() {
        return academicTable;
    }

    public Button getRefreshButton() {
        return refreshButton;
    }

    public Button getBackButton() {
        return backButton;
    }

    public void setAcademicData(ObservableList<Academic> academics) {
        academicTable.setItems(academics);
    }
}