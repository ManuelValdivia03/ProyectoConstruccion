package userinterface.windows;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import logic.logicclasses.Evaluation;

public class ConsultStudentEvaluationsWindow {
    private final TableView<Evaluation> evaluationsTable = new TableView<>();
    private final VBox root = new VBox(15);

    public ConsultStudentEvaluationsWindow() {
        root.setPadding(new Insets(20));
        root.getChildren().add(new Label("Tus evaluaciones:"));

        TableColumn<Evaluation, String> academicCol = new TableColumn<>("Académico Evaluador");
        academicCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getAcademic() != null ? data.getValue().getAcademic().getFullName() : ""
        ));

        TableColumn<Evaluation, String> projectCol = new TableColumn<>("Proyecto");
        projectCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getPresentation().getStudent().getCellPhone()
        ));

        TableColumn<Evaluation, String> typeCol = new TableColumn<>("Tipo Presentación");
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getPresentation().getPresentationType().name()));

        TableColumn<Evaluation, Number> gradeCol = new TableColumn<>("Calificación");
        gradeCol.setCellValueFactory(data -> new SimpleIntegerProperty(
                data.getValue().getCalification()));

        TableColumn<Evaluation, String> dateCol = new TableColumn<>("Fecha");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getEvaluationDate().toString()));

        TableColumn<Evaluation, String> commentsCol = new TableColumn<>("Comentarios");
        commentsCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDescription()));

        evaluationsTable.getColumns().addAll(academicCol, projectCol, typeCol, gradeCol, dateCol, commentsCol);
        evaluationsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        root.getChildren().add(evaluationsTable);
    }

    public Parent getView() {
        return root;
    }

    public void setEvaluations(ObservableList<Evaluation> evaluations) {
        evaluationsTable.setItems(evaluations);
    }

    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

