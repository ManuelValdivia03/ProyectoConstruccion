package userinterface.windows;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import logic.logicclasses.Project;
import logic.logicclasses.Student;

public class ReassignProjectWindow {
    private final VBox view;
    private final TableView<Student> studentsTable;
    private final ComboBox<Project> projectsComboBox;
    private final Label currentProjectLabel;
    private final Button reassignButton;
    private final Button cancelButton;
    private final Label statusLabel;

    public ReassignProjectWindow() {
        studentsTable = new TableView<>();
        setupStudentsTable();

        projectsComboBox = new ComboBox<>();
        projectsComboBox.setCellFactory(lv -> new ListCell<Project>() {
            @Override
            protected void updateItem(Project item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitle());
            }
        });
        projectsComboBox.setButtonCell(new ListCell<Project>() {
            @Override
            protected void updateItem(Project item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitle());
            }
        });
        projectsComboBox.setPromptText("Seleccione un nuevo proyecto");
        projectsComboBox.setMinWidth(250);

        currentProjectLabel = new Label("Proyecto actual: Ninguno");

        reassignButton = new Button("Reasignar");
        reassignButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");

        cancelButton = new Button("Cancelar");
        cancelButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");

        HBox buttonBox = new HBox(10, reassignButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: red;");

        view = new VBox(15);
        view.setPadding(new Insets(15));
        view.getChildren().addAll(
                new Label("Reasignar Proyecto"),
                new Label("Estudiantes:"),
                studentsTable,
                currentProjectLabel,
                new Label("Nuevo Proyecto:"),
                projectsComboBox,
                buttonBox,
                statusLabel
        );
    }

    private void setupStudentsTable() {
        TableColumn<Student, String> idColumn = new TableColumn<>("Matrícula");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("enrollment"));
        idColumn.setPrefWidth(100);

        TableColumn<Student, String> nameColumn = new TableColumn<>("Nombre");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        nameColumn.setPrefWidth(200);

        studentsTable.getColumns().addAll(idColumn, nameColumn);
        studentsTable.setPlaceholder(new Label("No hay estudiantes disponibles"));
        studentsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    public VBox getView() { return view; }
    public TableView<Student> getStudentsTable() { return studentsTable; }
    public ComboBox<Project> getProjectsComboBox() { return projectsComboBox; }
    public Button getReassignButton() { return reassignButton; }
    public Button getCancelButton() { return cancelButton; }
    public Label getStatusLabel() { return statusLabel; }
    public Label getCurrentProjectLabel() { return currentProjectLabel; }

    public void setStudentsList(ObservableList<Student> students) {
        studentsTable.setItems(students);
    }

    public void setProjectsList(ObservableList<Project> projects) {
        projectsComboBox.setItems(projects);
    }

    public void showMessage(String message, boolean isError) {
        statusLabel.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
        statusLabel.setText(message);
    }
}