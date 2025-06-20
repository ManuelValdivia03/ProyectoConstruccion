package userinterface.windows;

import javafx.beans.property.SimpleStringProperty;
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
import logic.daos.AccountDAO;
import logic.logicclasses.Academic;

import java.sql.SQLException;

public class ConsultAcademicsWindow {
    private final VBox view;
    private final TableView<Academic> academicTable;
    private final TextField searchField;
    private final Button searchButton;
    private final Button clearButton;
    private final Button backButton;

    public ConsultAcademicsWindow() {
        academicTable = new TableView<>();
        academicTable.setStyle("-fx-font-size: 14px;");
        academicTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Academic, String> staffNumberCol = createStyledColumn("Núm. Personal", "staffNumber");
        TableColumn<Academic, String> nameCol = createStyledColumn("Nombre", "fullName");
        TableColumn<Academic, String> phoneCol = createStyledColumn("Teléfono", "cellPhone");
        TableColumn<Academic, String> extCol = createStyledColumn("Ext.", "phoneExtension");
        TableColumn<Academic, String> typeCol = createStyledColumn("Tipo", "academicType");

        TableColumn<Academic, String> emailCol = new TableColumn<>("Correo");
        emailCol.setStyle("-fx-alignment: CENTER;");
        emailCol.setCellValueFactory(cellData -> {
            try {
                String email = new AccountDAO().getAccountByUserId(
                        cellData.getValue().getIdUser()).getEmail();
                return new SimpleStringProperty(email != null ? email : "Sin correo");
            } catch (SQLException e) {
                return new SimpleStringProperty("Error");
            }
        });

        academicTable.getColumns().addAll(staffNumberCol, nameCol, phoneCol, extCol, emailCol, typeCol);

        searchField = new TextField();
        searchField.setPromptText("Buscar por número de personal...");
        searchField.setStyle("-fx-font-size: 14px; -fx-padding: 5px;");

        searchButton = new Button("Buscar");
        searchButton.setStyle("-fx-background-color: #1A5F4B; -fx-text-fill: white; -fx-font-weight: bold;");

        clearButton = new Button("Limpiar");
        clearButton.setStyle("-fx-background-color: #ff8900; -fx-text-fill: white; -fx-font-weight: bold;");

        HBox searchBox = new HBox(5, searchField, searchButton, clearButton);
        searchBox.setPadding(new Insets(0, 0, 10, 0));

        backButton = new Button("Regresar");
        backButton.setStyle("-fx-background-color: #ff4a4a; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");

        HBox buttonBox = new HBox(15, backButton);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));

        view = new VBox(15);
        view.setPadding(new Insets(15));
        view.setStyle("-fx-background-color: #f5f5f5;");
        view.getChildren().addAll(searchBox, academicTable, buttonBox);
    }

    private TableColumn<Academic, String> createStyledColumn(String title, String propertyName) {
        TableColumn<Academic, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setStyle("-fx-alignment: CENTER;");
        return column;
    }

    public TableColumn<Academic, Void> createManageButtonColumn(EventHandler<ActionEvent> manageAction) {
        TableColumn<Academic, Void> manageCol = new TableColumn<>("Acciones");
        manageCol.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Gestionar");
            {
                btn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
                btn.setOnAction(event -> {
                    Academic academic = getTableView().getItems().get(getIndex());
                    if (academic != null) {
                        manageAction.handle(new ActionEvent(academic, btn));
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

    public TextField getSearchField() {
        return searchField;
    }

    public Button getSearchButton() {
        return searchButton;
    }

    public Button getClearButton() {
        return clearButton;
    }

    public VBox getView() {
        return view;
    }

    public TableView<Academic> getAcademicTable() {
        return academicTable;
    }

    public Button getBackButton() {
        return backButton;
    }

    public void setAcademicData(ObservableList<Academic> academics) {
        academicTable.setItems(academics);
    }
}
