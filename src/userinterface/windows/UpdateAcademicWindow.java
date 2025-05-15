package userinterface.windows;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import logic.logicclasses.Academic;
import userinterface.utilities.PasswordToggleField;

public class UpdateAcademicWindow {
    private final VBox view;
    private final TextField nameField;
    private final TextField phoneField;
    private final TextField staffNumberField;
    private final TextField emailField;
    private final PasswordToggleField passwordToggle;
    private final ComboBox<String> typeComboBox;
    private final ComboBox<String> statusComboBox;
    private final Button updateButton;
    private final Button cancelButton;
    private final Label resultLabel;

    public UpdateAcademicWindow() {
        Label titleLabel = new Label("Actualizar académico");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        nameField = new TextField();
        phoneField = new TextField();
        staffNumberField = new TextField();
        emailField = new TextField();

        passwordToggle = new PasswordToggleField();

        typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("Evaluador", "EE");
        typeComboBox.setValue("Evaluador");

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
        grid.add(new Label("Número de personal:"), 0, 2);
        grid.add(staffNumberField, 1, 2);
        grid.add(new Label("Tipo de académico:"), 0, 3);
        grid.add(typeComboBox, 1, 3);
        grid.add(new Label("E-mail:"), 0, 4);
        grid.add(emailField, 1, 4);
        grid.add(new Label("Contraseña:"), 0, 5);
        grid.add(passwordToggle.getContainer(), 1, 5);
        grid.add(new Label("Estado:"), 0, 6);
        grid.add(statusComboBox, 1, 6);

        HBox buttonBox = new HBox(10, updateButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        view = new VBox(15);
        view.setPadding(new Insets(20));
        view.setAlignment(Pos.CENTER);
        view.getChildren().addAll(titleLabel, grid, buttonBox, resultLabel);
    }

    public void loadAcademicData(Academic academic, String email) {
        nameField.setText(academic.getFullName());
        phoneField.setText(academic.getCellPhone());
        staffNumberField.setText(academic.getStaffNumber());
        emailField.setText(email);
        typeComboBox.setValue(academic.getAcademicType().toString());
        statusComboBox.setValue(String.valueOf(academic.getStatus()));
    }

    public VBox getView() { return view; }
    public TextField getNameField() { return nameField; }
    public TextField getPhoneField() { return phoneField; }
    public TextField getStaffNumberField() { return staffNumberField; }
    public TextField getEmailField() { return emailField; }
    public String getPassword() { return passwordToggle.getPassword(); }
    public ComboBox<String> getTypeComboBox() { return typeComboBox; }
    public ComboBox<String> getStatusComboBox() { return statusComboBox; }
    public Button getUpdateButton() { return updateButton; }
    public Button getCancelButton() { return cancelButton; }
    public Label getResultLabel() { return resultLabel; }
    public PasswordToggleField getPasswordToggle() { return passwordToggle; }
}
