package userinterface.windows;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
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

        // Configurar columnas
        TableColumn<Academic, String> staffNumberCol = createColumn("Núm. Personal", "staffNumber");
        TableColumn<Academic, String> nameCol = createColumn("Nombre", "fullName");
        TableColumn<Academic, String> phoneCol = createColumn("Teléfono", "cellPhone");
        TableColumn<Academic, String> typeCol = createColumn("Tipo", "academicType");

        // Columna para el email
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

        // Columna de Gestionar
        TableColumn<Academic, Void> manageCol = new TableColumn<>("Gestionar");
        manageCol.setCellFactory(param -> new TableCell<>() {
            private final Button manageButton = new Button("Gestionar");

            {
                manageButton.setOnAction(event -> {
                    Academic academic = getTableView().getItems().get(getIndex());
                    System.out.println("Gestionar académico: " + academic.getStaffNumber());
                    // Aquí irá la lógica de gestión
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

        academicTable.getColumns().addAll(staffNumberCol, nameCol, phoneCol, emailCol, typeCol, manageCol);

        // Botones
        refreshButton = new Button("Actualizar Lista");
        refreshButton.setStyle("-fx-background-color: #4a7bed; -fx-text-fill: white;");

        backButton = new Button("Regresar");
        backButton.setStyle("-fx-background-color: #ff4a4a; -fx-text-fill: white;");

        // Contenedor de botones
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