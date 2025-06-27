package userinterface.windows;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import logic.daos.AccountDAO;
import logic.logicclasses.Student;
import java.sql.SQLException;

public class ConsultGroupStudentEvaluationsWindow {
    private final VBox view;
    private final TableView<Student> studentTable;
    private final Button backButton;

    public ConsultGroupStudentEvaluationsWindow() {
        studentTable = new TableView<>();
        studentTable.setStyle("-fx-font-size: 14px;");
        studentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

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

        TableColumn<Student, Void> evalCol = createButtonColumn("Ver evaluaciones", this::handleShowEvaluations);
        TableColumn<Student, Void> selfEvalCol = createButtonColumn("Ver autoevaluación", this::handleShowSelfEvaluation);

        studentTable.getColumns().addAll(enrollmentCol, nameCol, phoneCol, emailCol, evalCol, selfEvalCol);

        backButton = new Button("Regresar");
        backButton.setStyle("-fx-background-color: #ff4a4a; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16;");

        HBox buttonBox = new HBox(15, backButton);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));

        view = new VBox(15);
        view.setPadding(new Insets(15));
        view.setStyle("-fx-background-color: #f5f5f5;");
        view.getChildren().addAll(studentTable, buttonBox);
    }

    private TableColumn<Student, String> createStyledColumn(String title, String propertyName) {
        TableColumn<Student, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setStyle("-fx-alignment: CENTER;");
        return column;
    }

    private TableColumn<Student, Void> createButtonColumn(String buttonText, EventHandler<ActionEvent> action) {
        TableColumn<Student, Void> col = new TableColumn<>(buttonText);
        col.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button(buttonText);
            {
                btn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
                btn.setOnAction(event -> {
                    Student student = getTableView().getItems().get(getIndex());
                    if (student != null) {
                        action.handle(new ActionEvent(student, btn));
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
        return col;
    }

    private EventHandler<ActionEvent> onShowEvaluations;
    private EventHandler<ActionEvent> onShowSelfEvaluation;

    private void handleShowEvaluations(ActionEvent event) {
        if (onShowEvaluations != null) onShowEvaluations.handle(event);
    }
    private void handleShowSelfEvaluation(ActionEvent event) {
        if (onShowSelfEvaluation != null) onShowSelfEvaluation.handle(event);
    }

    public void setOnShowEvaluations(EventHandler<ActionEvent> handler) {
        this.onShowEvaluations = handler;
    }
    public void setOnShowSelfEvaluation(EventHandler<ActionEvent> handler) {
        this.onShowSelfEvaluation = handler;
    }

    public VBox getView() {
        return view;
    }

    public TableView<Student> getStudentTable() {
        return studentTable;
    }

    public Button getBackButton() {
        return backButton;
    }

    public void setStudentData(ObservableList<Student> students) {
        studentTable.setItems(students);
    }

    public void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

