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

public class UpdateStudentWindow {
    private final VBox view;
    private final TextField nameField;
    private final TextField phoneField;
    private final TextField phoneExtensionField;
    private final TextField enrollmentField;
    private final TextField emailField;
    private final PasswordToggleField passwordField;
    private final ComboBox<String> statusComboBox;
    private final Button updateButton;
    private final Button cancelButton;
    private final Label resultLabel;

    public UpdateStudentWindow() {
        Label titleLabel = new Label("Actualizar Estudiante");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        nameField = new TextField();
        phoneField = new TextField();
        phoneExtensionField = new TextField();
        phoneExtensionField.setPromptText("Dejar vacío si no tiene");
        enrollmentField = new TextField();
        enrollmentField.setEditable(false);
        emailField = new TextField();
        passwordField = new PasswordToggleField();
        statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("Activo", "Inactivo");
        statusComboBox.setValue("Activo");

        updateButton = new Button("Actualizar");
        updateButton.setStyle("-fx-background-color: #4a7bed; -fx-text-fill: white;");

        cancelButton = new Button("Cancelar");
        cancelButton.setStyle("-fx-background-color: #ff4a4a; -fx-text-fill: white;");

        resultLabel = new Label();

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        grid.add(new Label("Nombre Completo:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Teléfono:"), 0, 1);
        grid.add(phoneField, 1, 1);
        grid.add(new Label("Ext.:"), 2, 1);
        grid.add(phoneExtensionField, 3, 1);
        grid.add(new Label("Matrícula:"), 0, 2);
        grid.add(enrollmentField, 1, 2);
        grid.add(new Label("E-mail:"), 0, 3);
        grid.add(emailField, 1, 3);
        grid.add(new Label("Contraseña:"), 0, 4);
        grid.add(passwordField.getContainer(), 1, 4);
        grid.add(new Label("Estado:"), 0, 5);
        grid.add(statusComboBox, 1, 5);

        HBox buttonBox = new HBox(10, updateButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        view = new VBox(15);
        view.setPadding(new Insets(20));
        view.setAlignment(Pos.CENTER);
        view.getChildren().addAll(titleLabel, grid, buttonBox, resultLabel);
    }

    public void loadStudentData(Student student, String email) {
        nameField.setText(student.getFullName());
        phoneField.setText(student.getCellPhone());
        phoneExtensionField.setText(student.getPhoneExtension());
        enrollmentField.setText(student.getEnrollment());
        emailField.setText(email);
    }

    public VBox getView() { return view; }
    public TextField getNameField() { return nameField; }
    public TextField getPhoneField() { return phoneField; }
    public TextField getPhoneExtensionField() { return phoneExtensionField; }
    public TextField getEnrollmentField() { return enrollmentField; }
    public TextField getEmailField() { return emailField; }
    public String getPassword() { return passwordField.getPassword(); }
    public Button getUpdateButton() { return updateButton; }
    public Button getCancelButton() { return cancelButton; }
    public Label getResultLabel() { return resultLabel; }
    public PasswordToggleField getPasswordField() { return passwordField; }
    public ComboBox<String> getStatusComboBox() { return statusComboBox; }
}
