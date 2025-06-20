package userinterface.windows;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import userinterface.utilities.PasswordToggleField;

public class CreateAcademicWindow {
    private final VBox view;
    private final TextField nameField;
    private final TextField phoneField;
    private final TextField phoneExtensionField;
    private final TextField staffNumberField;
    private final TextField emailField;
    private final PasswordToggleField passwordToggle;
    private final ComboBox<String> typeComboBox;
    private final Button addButton;
    private final Button cancelButton;
    private final Label resultLabel;

    public CreateAcademicWindow() {
        Label titleLabel = new Label("Registra académico");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        nameField = new TextField();
        phoneField = new TextField();
        phoneExtensionField = new TextField();
        phoneExtensionField.setPromptText("Dejar vacío si no tiene");
        staffNumberField = new TextField();
        emailField = new TextField();

        passwordToggle = new PasswordToggleField();

        typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("Evaluador", "EE");
        typeComboBox.setValue("Evaluador");

        addButton = new Button("Agregar Académico");
        addButton.setStyle("-fx-background-color: #0A1F3F; -fx-text-fill: white;");

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
        grid.add(new Label("Número de personal:"), 0, 2);
        grid.add(staffNumberField, 1, 2);
        grid.add(new Label("Tipo de académico:"), 0, 3);
        grid.add(typeComboBox, 1, 3);
        grid.add(new Label("E-mail:"), 0, 4);
        grid.add(emailField, 1, 4);
        grid.add(new Label("Contraseña:"), 0, 5);
        grid.add(passwordToggle.getContainer(), 1, 5);

        view = new VBox(15);
        view.setPadding(new Insets(20));
        view.setAlignment(Pos.CENTER);
        view.getChildren().addAll(titleLabel, grid, addButton,cancelButton, resultLabel);
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

    public TextField getPhoneExtensionField() {
        return phoneExtensionField;
    }

    public TextField getStaffNumberField() {
        return staffNumberField;
    }

    public TextField getEmailField() {
        return emailField;
    }

    public String getPassword() {
        return passwordToggle.getPassword();
    }

    public ComboBox<String> getTypeComboBox() {
        return typeComboBox;
    }

    public Button getAddButton() {
        return addButton;
    }

    public Label getResultLabel() {
        return resultLabel;
    }

    public PasswordToggleField getPasswordToggle() {
        return passwordToggle;
    }

    public Button getCancelButton() {
        return cancelButton;
    }
}
