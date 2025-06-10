package userinterface.windows;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import logic.logicclasses.Student;
import userinterface.utilities.PasswordToggleField;

public class EditProfileStudentWindow {
    private final VBox view;
    private final Label nameLabel;
    private final Label phoneLabel;
    private final Label enrollmentLabel;
    private final TextField emailField;
    private final PasswordToggleField passwordField;
    private final PasswordToggleField confirmPasswordField;
    private final ComboBox<String> statusComboBox;
    private final Button updateButton;
    private final Button cancelButton;
    private final Label resultLabel;

    public EditProfileStudentWindow() {
        Label titleLabel = new Label("Actualizar Cuenta");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        nameLabel = new Label();

        phoneLabel = new Label();

        enrollmentLabel = new Label();

        emailField = new TextField();

        passwordField = new PasswordToggleField();
        confirmPasswordField = new PasswordToggleField();

        statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("Activo", "Inactivo");
        statusComboBox.setDisable(true);

        updateButton = new Button("Actualizar");
        updateButton.setStyle("-fx-background-color: #0A1F3F; -fx-text-fill: white;");

        cancelButton = new Button("Cancelar");
        cancelButton.setStyle("-fx-background-color: #ff4a4a; -fx-text-fill: white;");

        resultLabel = new Label();

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        grid.add(new Label("Nombre Completo:"), 0, 0);
        grid.add(nameLabel, 1, 0);
        grid.add(new Label("Teléfono:"), 0, 1);
        grid.add(phoneLabel, 1, 1);
        grid.add(new Label("Matrícula:"), 0, 2);
        grid.add(enrollmentLabel, 1, 2);
        grid.add(new Label("E-mail:"), 0, 3);
        grid.add(emailField, 1, 3);
        grid.add(new Label("Nueva Contraseña:"), 0, 4);
        grid.add(passwordField.getContainer(), 1, 4);
        grid.add(new Label("Confirmar Contraseña:"), 0, 5);
        grid.add(confirmPasswordField.getContainer(), 1, 5);
        grid.add(new Label("Estado:"), 0, 6);
        grid.add(statusComboBox, 1, 6);

        HBox buttonBox = new HBox(10, updateButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        view = new VBox(15);
        view.setPadding(new Insets(20));
        view.setAlignment(Pos.CENTER);
        view.getChildren().addAll(titleLabel, grid, buttonBox, resultLabel);
    }

    public void loadStudentData(Student student, String email, String status) {
        nameLabel.setText(student.getFullName());
        phoneLabel.setText(student.getCellPhone());
        enrollmentLabel.setText(student.getEnrollment());
        emailField.setText(email);
        statusComboBox.setValue(status);
    }

    public VBox getView() { return view; }
    public Label getNameField() { return nameLabel; }
    public Label getPhoneField() { return phoneLabel; }
    public Label getEnrollmentField() { return enrollmentLabel; }
    public TextField getEmailField() { return emailField; }
    public String getPassword() { return passwordField.getPassword(); }
    public String getConfirmPassword() { return confirmPasswordField.getPassword(); }
    public Button getUpdateButton() { return updateButton; }
    public Button getCancelButton() { return cancelButton; }
    public Label getResultLabel() { return resultLabel; }
    public ComboBox<String> getStatusComboBox() { return statusComboBox; }
}