package userinterface.windows;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import logic.logicclasses.Student;

public class AssignGradeWindow {
    private final VBox view;
    private final TextField gradeField;
    private final Button confirmButton;
    private final Button cancelButton;
    private final Label messageLabel;

    public AssignGradeWindow(Student student) {
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(15));
        mainLayout.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Asignar Calificación a: " + student.getFullName());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        Label enrollmentLabel = new Label("Matrícula: " + student.getEnrollment());
        Label currentGradeLabel = new Label("Calificación Actual: " +
                (student.getGrade() >= 0 ? student.getGrade() : "No asignada"));

        Label instructionLabel = new Label("Nueva Calificación (0-10):");
        gradeField = new TextField();
        gradeField.setPromptText("Ingrese la calificación");

        messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: #cc0000;");

        confirmButton = new Button("Confirmar");
        confirmButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        cancelButton = new Button("Cancelar");
        cancelButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

        HBox buttonsBox = new HBox(10, confirmButton, cancelButton);
        buttonsBox.setAlignment(Pos.CENTER);

        mainLayout.getChildren().addAll(
                titleLabel, enrollmentLabel, currentGradeLabel,
                instructionLabel, gradeField, messageLabel, buttonsBox
        );

        this.view = mainLayout;
    }

    public VBox getView() {
        return view;
    }

    public TextField getGradeField() {
        return gradeField;
    }

    public Button getConfirmButton() {
        return confirmButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public Label getMessageLabel() {
        return messageLabel;
    }
}