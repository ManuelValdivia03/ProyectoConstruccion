package userinterface.windows;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import logic.logicclasses.Evaluation;

public class ConsultRegisteredEvaluationsWindow {
    private final TableView<Evaluation> evaluationsTable = new TableView<>();
    private final VBox root = new VBox(15);

    public ConsultRegisteredEvaluationsWindow() {
        root.setPadding(new Insets(20));
        root.getChildren().add(new Label("Evaluaciones registradas por usted:"));

        TableColumn<Evaluation, String> studentCol = new TableColumn<>("Estudiante");
        studentCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getPresentation().getStudent().getFullName()));

        TableColumn<Evaluation, String> projectCol = new TableColumn<>("Proyecto");
        projectCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getPresentation().getStudent().getCellPhone() // Aquí se muestra el título del proyecto
        ));

        TableColumn<Evaluation, String> typeCol = new TableColumn<>("Tipo Presentación");
        typeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getPresentation().getPresentationType().name()));

        TableColumn<Evaluation, Number> gradeCol = new TableColumn<>("Calificación");
        gradeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(
                data.getValue().getCalification()));

        TableColumn<Evaluation, String> dateCol = new TableColumn<>("Fecha");
        dateCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getEvaluationDate().toString()));

        TableColumn<Evaluation, String> commentsCol = new TableColumn<>("Comentarios");
        commentsCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getDescription()));

        evaluationsTable.getColumns().addAll(studentCol, projectCol, typeCol, gradeCol, dateCol, commentsCol);
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