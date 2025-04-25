package userinterface.windows;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import userinterface.utilities.PasswordToggleField;

public class CreateStudentWindow {
    private final VBox view;
    private final TextField nameField;
    private final TextField phoneField;
    private final TextField enrollmentField;
    private final TextField emailField;
    private final PasswordToggleField passwordField;
    private final Button addButton;
    private final Button cancelButton;
    private final Label resultLabel;

    public CreateStudentWindow() {
        Label titleLabel = new Label("Registrar Estudiante");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        nameField = new TextField();
        phoneField = new TextField();
        enrollmentField = new TextField();
        emailField = new TextField();
        passwordField = new PasswordToggleField();

        addButton = new Button("Agregar Estudiante");
        addButton.setStyle("-fx-background-color: #4a7bed; -fx-text-fill: white;");
        cancelButton = new Button("Cancelar");
        cancelButton.setStyle("-fx-background-color: #ff4a4a; -fx-text-fill: white;");

        resultLabel = new Label();
        resultLabel.setStyle("-fx-text-fill: red;");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        grid.add(new Label("Nombre Completo:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Teléfono:"), 0, 1);
        grid.add(phoneField, 1, 1);
        grid.add(new Label("Matrícula:"), 0, 2);
        grid.add(enrollmentField, 1, 2);
        grid.add(new Label("E-mail:"), 0, 3);
        grid.add(emailField, 1, 3);
        grid.add(new Label("Contraseña:"), 0, 4);
        grid.add(passwordField.getContainer(), 1, 4);

        view = new VBox(15);
        view.setPadding(new Insets(20));
        view.setAlignment(Pos.CENTER);
        view.getChildren().addAll(titleLabel, grid, addButton, cancelButton, resultLabel);
    }

    public VBox getView() {
        return view;
    }

    public TextField getNameField() {
        return nameField;
    }

    public TextField getPhoneField() {
        return phoneField;
    }

    public TextField getEnrollmentField() {
        return enrollmentField;
    }

    public TextField getEmailField() {
        return emailField;
    }

    public String getPassword(){
        return passwordField.getPassword();
    }

    public Button getAddButton() {
        return addButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public Label getResultLabel() {
        return resultLabel;
    }

    public PasswordToggleField getPasswordField() {
        return passwordField;
    }
}
