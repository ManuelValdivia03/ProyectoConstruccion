package userinterface.windows;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class CreateLinkedOrganizationWindow {
    private final VBox view;
    private final TextField nameField;
    private final TextField phoneField;
    private final TextField emailField;
    private final TextField phoneExtensionField;
    private final TextField departmentField;
    private final Button addButton;
    private final Button cancelButton;
    private final Label resultLabel;

    public CreateLinkedOrganizationWindow() {
        Label titleLabel = new Label("Registrar Organización Vinculada");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        nameField = new TextField();
        phoneField = new TextField();
        emailField = new TextField();
        phoneExtensionField = new TextField();
        phoneExtensionField.setPromptText("Dejar vacío si no tiene");
        departmentField = new TextField();
        departmentField.setPromptText("Departamento o área");

        addButton = new Button("Agregar Organización");
        addButton.setStyle("-fx-background-color: #0A1F3F; -fx-text-fill: white;");
        cancelButton = new Button("Regresar");
        cancelButton.setStyle("-fx-background-color: #ff4a4a; -fx-text-fill: white;");

        resultLabel = new Label();
        resultLabel.setStyle("-fx-text-fill: red;");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        grid.add(new Label("Nombre de la Organización:"), 0, 0);
        grid.add(nameField, 1, 0, 3, 1);
        grid.add(new Label("Teléfono:"), 0, 1);
        grid.add(phoneField, 1, 1);
        grid.add(new Label("Ext.:"), 2, 1);
        grid.add(phoneExtensionField, 3, 1);
        grid.add(new Label("Departamento:"), 0, 2);
        grid.add(departmentField, 1, 2, 3, 1);
        grid.add(new Label("E-mail:"), 0, 3);
        grid.add(emailField, 1, 3, 3, 1);

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

    public TextField getEmailField() {
        return emailField;
    }

    public TextField getPhoneExtensionField() {
        return phoneExtensionField;
    }

    public TextField getDepartmentField() {
        return departmentField;
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
}
