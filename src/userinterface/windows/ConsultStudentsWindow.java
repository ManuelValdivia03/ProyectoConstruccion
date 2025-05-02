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
import logic.logicclasses.Student;

import java.sql.SQLException;

public class ConsultStudentsWindow {
    private final VBox view;
    private final TableView<Student> studentTable;
    private final TextField searchField;
    private final Button searchButton;
    private final Button clearButton;
    private final Button refreshButton;
    private final Button backButton;

    public ConsultStudentsWindow() {
        studentTable = new TableView<>();
        studentTable.setStyle("-fx-font-size: 14px;");
        studentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Columnas de la tabla
        TableColumn<Student, String> enrollmentCol = createStyledColumn("Matrícula", "enrollment");
        TableColumn<Student, String> nameCol = createStyledColumn("Nombre", "fullName");
        TableColumn<Student, String> phoneCol = createStyledColumn("Teléfono", "cellPhone");

        TableColumn<Student, String> emailCol = new TableColumn<>("Correo");
        emailCol.setStyle("-fx-alignment: CENTER;");
        emailCol.setCellValueFactory(cellData -> {
            try {
                String email = new AccountDAO().getAccountByUserId(
                        cellData.getValue().getIdUser()).getEmail();
                return new SimpleStringProperty(email);
            } catch (SQLException e) {
                return new SimpleStringProperty("Error");
            }
        });

        studentTable.getColumns().addAll(enrollmentCol, nameCol, phoneCol, emailCol);

        searchField = new TextField();
        searchField.setPromptText("Ingrese matrícula (S seguida de 8 dígitos)");
        searchField.setStyle("-fx-font-size: 14px; -fx-padding: 5px;");
        searchField.setPrefWidth(300);

        searchButton = new Button("Buscar");
        searchButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

        clearButton = new Button("Limpiar");
        clearButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-weight: bold;");

        HBox searchBox = new HBox(5, searchField, searchButton, clearButton);
        searchBox.setPadding(new Insets(0, 0, 10, 0));

        refreshButton = new Button("Actualizar Lista");
        refreshButton.setStyle("-fx-background-color: #4a7bed; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");

        backButton = new Button("Regresar");
        backButton.setStyle("-fx-background-color: #ff4a4a; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");

        HBox buttonBox = new HBox(15, refreshButton, backButton);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));

        view = new VBox(15);
        view.setPadding(new Insets(15));
        view.setStyle("-fx-background-color: #f5f5f5;");
        view.getChildren().addAll(searchBox, studentTable, buttonBox);
    }

    private TableColumn<Student, String> createStyledColumn(String title, String propertyName) {
        TableColumn<Student, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setStyle("-fx-alignment: CENTER;");
        return column;
    }

    public TableColumn<Student, Void> createManageButtonColumn(EventHandler<ActionEvent> manageAction) {
        TableColumn<Student, Void> manageCol = new TableColumn<>("Gestionar");
        manageCol.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Gestionar");
            {
                btn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
                btn.setOnAction(event -> {
                    Student student = getTableView().getItems().get(getIndex());
                    if (student != null) {
                        manageAction.handle(new ActionEvent(student, btn));
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

    public TableColumn<Student, Void> createAssignGradeButtonColumn(EventHandler<ActionEvent> assignGradeAction) {
        TableColumn<Student, Void> gradeCol = new TableColumn<>("Calificación");
        gradeCol.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Asignar");
            {
                btn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
                btn.setOnAction(event -> {
                    Student student = getTableView().getItems().get(getIndex());
                    if (student != null) {
                        assignGradeAction.handle(new ActionEvent(student, btn));
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
        return gradeCol;
    }

    public VBox getView() {
        return view;
    }

    public TableView<Student> getStudentTable() {
        return studentTable;
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

    public Button getRefreshButton() {
        return refreshButton;
    }

    public Button getBackButton() {
        return backButton;
    }

    public void setStudentData(ObservableList<Student> students) {
        studentTable.setItems(students);
    }
}