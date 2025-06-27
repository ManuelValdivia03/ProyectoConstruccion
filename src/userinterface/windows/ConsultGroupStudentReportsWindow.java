package userinterface.windows;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import logic.logicclasses.Student;
import logic.logicclasses.Report;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.List;

public class ConsultGroupStudentReportsWindow {
    private final BorderPane root;
    private final TableView<Student> studentsTable;
    private final TableView<Report> reportsTable;
    private final VBox reportsBox;

    public ConsultGroupStudentReportsWindow() {
        root = new BorderPane();
        studentsTable = new TableView<>();
        reportsTable = new TableView<>();
        reportsBox = new VBox(10);

        setupStudentsTable();
        setupReportsTable();

        reportsBox.setPadding(new Insets(10));
        reportsBox.getChildren().add(new Label("Reportes del estudiante seleccionado:"));
        reportsBox.getChildren().add(reportsTable);

        root.setLeft(studentsTable);
        root.setCenter(reportsBox);
    }

    private void setupStudentsTable() {
        TableColumn<Student, String> matriculaCol = new TableColumn<>("Matrícula");
        matriculaCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEnrollment()));

        TableColumn<Student, String> nombreCol = new TableColumn<>("Nombre");
        nombreCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFullName()));

        TableColumn<Student, Void> verReportesCol = new TableColumn<>("Ver reportes");
        verReportesCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Ver reportes");
            {
                btn.setOnAction(e -> {
                    Student student = getTableView().getItems().get(getIndex());
                    if (onViewReportsListener != null) {
                        onViewReportsListener.onViewReports(student);
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        studentsTable.getColumns().addAll(matriculaCol, nombreCol, verReportesCol);
        studentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupReportsTable() {
        TableColumn<Report, String> fechaCol = new TableColumn<>("Fecha");
        fechaCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getReportDate() != null ?
                        cellData.getValue().getReportDate().toString() : ""
        ));

        TableColumn<Report, String> tipoCol = new TableColumn<>("Tipo");
        tipoCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getReportType().toString()
        ));

        TableColumn<Report, Integer> horasCol = new TableColumn<>("Horas");
        horasCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(
                cellData.getValue().getHoursReport()
        ).asObject());

        TableColumn<Report, String> metodologiaCol = new TableColumn<>("Metodología");
        metodologiaCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getMethodology()
        ));

        TableColumn<Report, String> descripcionCol = new TableColumn<>("Descripción");
        descripcionCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDescription()
        ));

        reportsTable.getColumns().addAll(fechaCol, tipoCol, horasCol, metodologiaCol, descripcionCol);
        reportsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    public void setStudents(List<Student> students) {
        studentsTable.setItems(FXCollections.observableArrayList(students));
    }

    public void setReports(List<Report> reports) {
        reportsTable.setItems(FXCollections.observableArrayList(reports));
    }

    public BorderPane getView() {
        return root;
    }

    public interface OnViewReportsListener {
        void onViewReports(Student student);
    }
    private OnViewReportsListener onViewReportsListener;
    public void setOnViewReportsListener(OnViewReportsListener listener) {
        this.onViewReportsListener = listener;
    }
}